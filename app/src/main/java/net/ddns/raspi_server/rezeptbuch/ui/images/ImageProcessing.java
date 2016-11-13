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

import com.bumptech.glide.Glide;

import net.ddns.raspi_server.rezeptbuch.R;
import net.ddns.raspi_server.rezeptbuch.util.DataStructures.Recipe;
import net.ddns.raspi_server.rezeptbuch.util.WebClient;

import java.io.File;
import java.lang.ref.WeakReference;

public class ImageProcessing {

  public static void loadRecipeImage(final Context context, final Recipe recipe,
                              final ImageView imageView) {
    if (recipe == null || recipe.imageName == null)
      return;

    final File imageFile = new File(context.getFilesDir(), recipe.imageName);

    // if there is a current file but it is deprecated or if there is no
    // file, try to download the new image
    if ((!recipe.imageName.equals("") && !imageFile.exists())
        || (!recipe.imageName.equals("") && imageFile.exists()
        && imageFile.lastModified() < recipe.date.getTime())) {
      new WebClient(context).downloadImage(recipe.imageName, new WebClient.DownloadCallback() {
        @Override
        public void finished(final boolean success) {
          imageView.post(new Runnable() {
            @Override
            public void run() {
              Glide
                  .with(context)
                  // if successful, load the downloaded image, otherwise
                  // load the resource
                  .load(success ? imageFile : R.drawable.default_recipe_image_high)
                  .placeholder(R.drawable.default_recipe_image_low)
                  .crossFade()
                  .into(imageView);
            }
          });
        }
      });
    }
    Glide
        .with(context)
        // if the file exists load it (no matter if it is being downloaded
        // atm), otherwise, show the resource
        .load((!recipe.imageName.equals("") && imageFile.exists())
            ? imageFile
            : R.drawable.default_recipe_image_high)
        .placeholder(R.drawable.default_recipe_image_low)
        .crossFade()
        .into(imageView);
  }
}
