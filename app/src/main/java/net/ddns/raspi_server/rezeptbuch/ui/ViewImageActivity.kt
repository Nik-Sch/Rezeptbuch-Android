package net.ddns.raspi_server.rezeptbuch.ui

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.bumptech.glide.signature.ObjectKey
import net.ddns.raspi_server.rezeptbuch.R
import net.ddns.raspi_server.rezeptbuch.GlideApp
import net.ddns.raspi_server.rezeptbuch.util.DataStructures
import net.ddns.raspi_server.rezeptbuch.util.WebClient

class ViewImageActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.activity_view_image)

    val recipe = intent.extras?.getSerializable(RecipeActivity.ARG_RECIPE) as? DataStructures.Recipe
            ?: throw RuntimeException("The recipe activity has to be called with a recipe argument")

    window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_FULLSCREEN or
            View.SYSTEM_UI_FLAG_IMMERSIVE

    GlideApp.with(this)
            .load(WebClient.getImageUrl(recipe))
            .error(
                    GlideApp.with(this)
                            .load(R.drawable.default_recipe_image_high)
                            .fitCenter()
            )
            .thumbnail(
                    GlideApp.with(this)
                            .load(R.drawable.default_recipe_image_low)
                            .fitCenter()
            )
            .fitCenter()
            .signature(ObjectKey(recipe.mDate))
            .into(findViewById(R.id.image))
  }

  override fun onWindowFocusChanged(hasFocus: Boolean) {
    super.onWindowFocusChanged(hasFocus)
    if (hasFocus)
      window.decorView.systemUiVisibility =
              View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
              View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
              View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
              View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
              View.SYSTEM_UI_FLAG_FULLSCREEN or
              View.SYSTEM_UI_FLAG_IMMERSIVE
  }
}
