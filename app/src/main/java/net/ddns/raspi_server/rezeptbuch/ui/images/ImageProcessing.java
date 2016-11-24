package net.ddns.raspi_server.rezeptbuch.ui.images;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.DrawableRequestBuilder;
import com.bumptech.glide.Glide;

import net.ddns.raspi_server.rezeptbuch.R;
import net.ddns.raspi_server.rezeptbuch.util.DataStructures.Recipe;
import net.ddns.raspi_server.rezeptbuch.util.WebClient;

import java.io.File;

public class ImageProcessing {

  public static void loadRecipeImage(final Context context, final Recipe
      recipe, final ImageView imageView) {
    loadRecipeImage(context, recipe, imageView, false);
  }

  public static void loadRecipeImage(final Context context, final Recipe recipe,
                                     final ImageView imageView, final boolean dark) {
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
              DrawableRequestBuilder builder = Glide
                  .with(context)
                  // if successful, load the downloaded image, otherwise
                  // load the resource
                  .load(success ?
                      imageFile :
                      dark ?
                          R.drawable.default_recipe_image_high_dark :
                          R.drawable.default_recipe_image_high)
                  .placeholder(dark ?
                      R.drawable.default_recipe_image_low_dark :
                      R.drawable.default_recipe_image_low);
              if (success)
                builder.crossFade();
              builder.into(imageView);
            }
          });
        }
      });
    }
    boolean image = !recipe.imageName.equals("") && imageFile.exists();
    DrawableRequestBuilder builder = Glide
        .with(context)
        // if the file exists load it (no matter if it is being downloaded
        // atm), otherwise, show the resource
        .load(image ?
            imageFile :
            dark ?
                R.drawable.default_recipe_image_high_dark :
                R.drawable.default_recipe_image_high)
        .placeholder(dark ?
            R.drawable.default_recipe_image_low_dark :
            R.drawable.default_recipe_image_low);
    if (image)
      builder.crossFade();
    builder.into(imageView);
  }
}
