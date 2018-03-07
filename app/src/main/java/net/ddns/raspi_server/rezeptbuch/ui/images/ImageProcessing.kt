package net.ddns.raspi_server.rezeptbuch.ui.images

import android.content.Context
import android.widget.ImageView

import com.bumptech.glide.DrawableRequestBuilder
import com.bumptech.glide.Glide

import net.ddns.raspi_server.rezeptbuch.R
import net.ddns.raspi_server.rezeptbuch.util.DataStructures.Recipe
import net.ddns.raspi_server.rezeptbuch.util.WebClient

import java.io.File

object ImageProcessing {

  @JvmOverloads
  fun loadRecipeImage(context: Context, recipe: Recipe?,
                      imageView: ImageView, dark: Boolean = false) {
    if (recipe?.mImageName == null) {
      Glide.with(context)
              .load(if (dark)
                R.drawable.default_recipe_image_high_dark
              else
                R.drawable.default_recipe_image_high)
              .placeholder(if (dark)
                R.drawable.default_recipe_image_low_dark
              else
                R.drawable.default_recipe_image_low)
              .into(imageView)
      return
    }

    val imageFile = File(context.filesDir, recipe.mImageName)

    // if there is a current file but it is deprecated or if there is no
    // file, try to download the new image
    if (recipe.mImageName != "" && !imageFile.exists() || (recipe.mImageName != "" && imageFile.exists()
                    && imageFile.lastModified() < recipe.mDate.time)) {
      WebClient(context).downloadImage(recipe.mImageName) { success ->
        imageView.post {
          val builder = Glide
                  .with(context)
                  // if successful, load the downloaded image, otherwise
                  // load the resource
                  .load(when {
                    success -> imageFile
                    dark -> R.drawable.default_recipe_image_high_dark
                    else -> R.drawable.default_recipe_image_high
                  })
                  .placeholder(if (dark)
                    R.drawable.default_recipe_image_low_dark
                  else
                    R.drawable.default_recipe_image_low)
          if (success)
            builder.crossFade()
          builder.into(imageView)
        }
      }
    }
    val image = recipe.mImageName != "" && imageFile.exists()
    val builder = Glide
            .with(context)
            // if the file exists load it (no matter if it is being downloaded
            // atm), otherwise, show the resource
            .load(when {
              image -> imageFile
              dark -> R.drawable.default_recipe_image_high_dark
              else -> R.drawable.default_recipe_image_high
            })
            .placeholder(if (dark)
              R.drawable.default_recipe_image_low_dark
            else
              R.drawable.default_recipe_image_low)
    if (image)
      builder.crossFade()
    builder.into(imageView)
  }
}
