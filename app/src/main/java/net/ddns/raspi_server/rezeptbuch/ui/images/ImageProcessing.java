package net.ddns.raspi_server.rezeptbuch.ui.images;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.LruCache;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import net.ddns.raspi_server.rezeptbuch.R;
import net.ddns.raspi_server.rezeptbuch.util.DataStructures.Recipe;
import net.ddns.raspi_server.rezeptbuch.util.WebClient;

import java.io.File;
import java.lang.ref.WeakReference;

public class ImageProcessing {
  private final Context context;
  private final Bitmap mPlaceHolderBitmap;

  private LruCache<String, Bitmap> mMemoryCache;


  public ImageProcessing(Context context) {
    this.context = context;
    mPlaceHolderBitmap = BitmapFactory.decodeResource(context.getResources(), R
            .drawable.default_recipe_image_low);

    // create the LruCache
    final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
    final int cacheSize = maxMemory / 8;
    mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
      @Override
      protected int sizeOf(String key, Bitmap value) {
        // measure cacheSize in kilobytes instead of items
        return value.getByteCount() / 1024;
      }
    };
  }

  public void loadRecipeImage(final Recipe recipe, final ImageView imageView) {
    if (recipe == null || recipe.imagePath == null)
      return;

    final File imageFile = new File(context.getFilesDir(), recipe.imagePath);

    // if there is a current file but it is deprecated or if there is no file, try to download the
    // new image
    if ((imageFile.isFile() && !imageFile.exists())
            || (imageFile.isFile() && imageFile.exists()
            && imageFile.lastModified() < recipe.date.getTime())) {
      new WebClient(context).downloadImage(recipe.imagePath, recipe.imagePath, new WebClient.DownloadCallback() {
        @Override
        public void finished(boolean success) {
          if (success)
            loadImage(imageFile, imageView);
          else
            loadRecipeResourceImage(imageView);
        }
      });
    }
    // if the file exists load it (no matter if it is being updated or not), otherwise, show the
    // resource
    // also make sure that the height and width can be measured when calling the functions
    ViewTreeObserver vto = imageView.getViewTreeObserver();
    if (imageFile.isFile() && imageFile.exists()) {
      vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
        @Override
        public boolean onPreDraw() {
          imageView.getViewTreeObserver().removeOnPreDrawListener(this);
          loadImage(imageFile, imageView);
          return true;
        }
      });
    }else{
      vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
        @Override
        public boolean onPreDraw() {
          imageView.getViewTreeObserver().removeOnPreDrawListener(this);
          loadRecipeResourceImage(imageView);
          return true;
        }
      });
    }
  }

  private void loadRecipeResourceImage(final ImageView imageView) {
    final int resId = R.drawable.default_recipe_image_high;
    final String imageKey = String.valueOf(resId);
    final Bitmap cachedBitmap = getBitmapFromMemoryCache(imageKey);
    if (cachedBitmap != null) {
      imageView.setImageBitmap(cachedBitmap);
    } else {
      // make sure nothing different is loading in the imageView
      if (cancelPotentialResourceWork(resId, imageView)) {
        BitmapResourceWorkerTask task = new BitmapResourceWorkerTask(context, imageView, imageView.getWidth(),
                imageView.getHeight());
        // draw the placeHolder
        final AsyncResourceDrawable asyncResourceDrawable = new AsyncResourceDrawable(context
                .getResources(), mPlaceHolderBitmap, task);
        imageView.setImageDrawable(asyncResourceDrawable);
        // execute the background thread to load
        task.execute(resId);
      }
    }
  }

  private void loadImage(final File image, final ImageView imageView) {
    final String file = image.getAbsolutePath();

    final Bitmap cachedBitmap = getBitmapFromMemoryCache(file);
    if (cachedBitmap != null) {
      imageView.setImageBitmap(cachedBitmap);
    } else {
      if (cancelPotentialFileWork(file, imageView)) {
        BitmapFileWorkerTask task = new BitmapFileWorkerTask(imageView, imageView.getWidth
                (), imageView.getHeight());
        final AsyncFileDrawable asyncFileDrawable = new AsyncFileDrawable(context.getResources(),
                mPlaceHolderBitmap, task);
        imageView.setImageDrawable(asyncFileDrawable);
        task.execute(file);
      }
    }
  }


  private void addBitmapToMemoryCache(String key, Bitmap bitmap) {
    if (getBitmapFromMemoryCache(key) == null) {
      mMemoryCache.put(key, bitmap);
    }
  }

  private Bitmap getBitmapFromMemoryCache(String key) {
    return mMemoryCache.get(key);
  }

  /*
  ##################################################################################################
  ##################################   IMAGE UTIL FUNCTIONS   ######################################
  ##################################################################################################
   */

  private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int
          reqHeight){
    final int height = options.outHeight;
    final int width = options.outWidth;
    int inSampleSize = 1;
    if (height > reqHeight || width > reqWidth) {
      final int halfHeight = height / 2;
      final int halfWidth = width / 2;

      while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth)
        inSampleSize *= 2;
    }
    return inSampleSize;
  }

  /*
  FROM RESOURCE
   */

  private Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
                                                 int width, int height) {
    BitmapFactory.Options options = new BitmapFactory.Options();
    options.inJustDecodeBounds = true;
    BitmapFactory.decodeResource(res, resId, options);

    options.inSampleSize = calculateInSampleSize(options, width, height);

    options.inJustDecodeBounds = false;
    return BitmapFactory.decodeResource(res, resId, options);
  }

  private boolean cancelPotentialResourceWork(int data, ImageView imageView) {
    final BitmapResourceWorkerTask task = getBitmapResourceWorkerTask(imageView);

    if (task != null) {
      final int bitmapData = task.data;
      if (bitmapData == 0 || bitmapData != data) {
        task.cancel(true);
      } else {
        return false;
      }
    }
    // not task was cancelled
    return true;
  }

  private BitmapResourceWorkerTask getBitmapResourceWorkerTask(ImageView imageView) {
    if (imageView != null) {
      final Drawable drawable = imageView.getDrawable();
      if (drawable instanceof AsyncResourceDrawable) {
        return ((AsyncResourceDrawable) drawable).getTask();
      }
    }
    return null;
  }

  private Bitmap decodeSampledBitmapFromFile(String path, int width, int height) {
    BitmapFactory.Options options = new BitmapFactory.Options();
    options.inJustDecodeBounds = true;
    BitmapFactory.decodeFile(path, options);

    options.inSampleSize = calculateInSampleSize(options, width, height);

    options.inJustDecodeBounds = false;
    return BitmapFactory.decodeFile(path, options);
  }

  private boolean cancelPotentialFileWork(String data, ImageView imageView) {
    final BitmapFileWorkerTask task = getBitmapFileWorkerTask(imageView);

    if (task != null) {
      final String bitmapData = task.data;
      if (bitmapData == null || bitmapData.equals(data)) {
        task.cancel(true);
      } else {
        return false;
      }
    }
    // no task was cancelled
    return true;
  }

  /*
  FROM FILE
   */

  private BitmapFileWorkerTask getBitmapFileWorkerTask(ImageView imageView) {
    if (imageView != null) {
      final Drawable drawable = imageView.getDrawable();
      if (drawable instanceof AsyncFileDrawable) {
        return ((AsyncFileDrawable) drawable).getTask();
      }
    }
    return null;
  }

  private class BitmapResourceWorkerTask extends AsyncTask<Integer, Void, Bitmap> {

    private final WeakReference<ImageView> imageViewWeakReference;
    private final Context context;
    private final int width;
    private final int height;
    private int data = 0;

    private BitmapResourceWorkerTask(Context context, ImageView imageView, int width, int height) {
      imageViewWeakReference = new WeakReference<ImageView>(imageView);
      this.context = context;
      this.width = width;
      this.height = height;
    }

    @Override
    protected Bitmap doInBackground(Integer... params) {
      data = params[0];
      Bitmap bitmap = decodeSampledBitmapFromResource(context.getResources(), data, width, height);
      addBitmapToMemoryCache(String.valueOf(data), bitmap);
      return bitmap;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
      if (isCancelled()) {
        bitmap = null;
      }

      if (bitmap != null) {
        final ImageView imageView = imageViewWeakReference.get();
        final BitmapResourceWorkerTask workerTask = getBitmapResourceWorkerTask(imageView);
        // should also check if imageView is not null but if it would getBitmapResourceWorkerTask
        // would return null (contract) and this is never null, therefore, I am doing this check
        if (this == workerTask) {
          imageView.setImageBitmap(bitmap);
        }
      }
    }
  }

  private class AsyncResourceDrawable extends BitmapDrawable {
    private final WeakReference<BitmapResourceWorkerTask> taskWeakReference;

    public AsyncResourceDrawable(Resources res, Bitmap bitmap, BitmapResourceWorkerTask task) {
      super(res, bitmap);
      taskWeakReference = new WeakReference<BitmapResourceWorkerTask>(task);
    }

    public BitmapResourceWorkerTask getTask() {
      return taskWeakReference.get();
    }
  }

  private class BitmapFileWorkerTask extends AsyncTask<String, Void, Bitmap> {

    private final WeakReference<ImageView> imageViewWeakReference;
    private final int width;
    private final int height;
    private String data = null;

    private BitmapFileWorkerTask(ImageView imageView, int width, int height) {
      imageViewWeakReference = new WeakReference<ImageView>(imageView);
      this.width = width;
      this.height = height;
    }

    @Override
    protected Bitmap doInBackground(String... params) {
      data = params[0];
      Bitmap bitmap = decodeSampledBitmapFromFile(data, width, height);
      addBitmapToMemoryCache(data, bitmap);
      return bitmap;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
      if (bitmap != null) {
        final ImageView imageView = imageViewWeakReference.get();
        if (imageView != null) {
          imageView.setImageBitmap(bitmap);
        }
      }
    }
  }

  private class AsyncFileDrawable extends BitmapDrawable {
    private final WeakReference<BitmapFileWorkerTask> taskWeakReference;

    public AsyncFileDrawable(Resources res, Bitmap bitmap, BitmapFileWorkerTask task){
      super(res, bitmap);
      taskWeakReference = new WeakReference<BitmapFileWorkerTask>(task);
    }

    public BitmapFileWorkerTask getTask() {
      return taskWeakReference.get();
    }
  }
}
