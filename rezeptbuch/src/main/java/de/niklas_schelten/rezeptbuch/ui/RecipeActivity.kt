package de.niklas_schelten.rezeptbuch.ui

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.AppBarLayout
import android.support.design.widget.CollapsingToolbarLayout
import android.support.v4.content.FileProvider
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.signature.ObjectKey
import de.niklas_schelten.rezeptbuch.GlideApp

import de.niklas_schelten.rezeptbuch.R
import de.niklas_schelten.rezeptbuch.util.DataStructures
import de.niklas_schelten.rezeptbuch.util.Favorite
import de.niklas_schelten.rezeptbuch.util.WebClient
import de.niklas_schelten.rezeptbuch.util.db.RecipeDatabase

import java.io.File
import java.io.FileOutputStream
import java.util.regex.Pattern


class RecipeActivity : AppCompatActivity() {

  private lateinit var mRecipe: DataStructures.Recipe


  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    try {
      if (intent.action == Intent.ACTION_VIEW) {
        Log.d(TAG, "actionView")
        val matcher = Pattern.compile(".*recipe_id=(\\d+)", Pattern.COMMENTS).matcher(intent.dataString)
        if (matcher.matches()) {
          Log.d(TAG, "matches")
          val recipeId = Integer.valueOf(matcher.group(1))
          mRecipe = RecipeDatabase(this).getRecipeById(recipeId) ?: throw RuntimeException("No recipe with such an id")
          Log.d(TAG, "found recipe")
        }
      } else {
        mRecipe = intent.extras?.getSerializable(ARG_RECIPE) as? DataStructures.Recipe
                ?: throw RuntimeException("The recipe activity has to be called with a recipe argument")
      }
    } catch (e: RuntimeException) {
      Log.d(TAG, "exception: $e")
      val parentIntent = Intent(this, MainActivity::class.java)
      startActivity(parentIntent)
      Toast.makeText(this, R.string.no_recipe, Toast.LENGTH_LONG).show()
      finish()
      return
    }

    setContentView(R.layout.activity_recipe)

    setSupportActionBar(findViewById(R.id.toolbar))
    val actionBar = supportActionBar
    actionBar?.setDisplayHomeAsUpEnabled(true)

    val category = RecipeDatabase(this).getCategoryById(mRecipe.mCategory).mName

    actionBar?.title = mRecipe.mTitle

    (findViewById<View>(R.id.title) as TextView).text = mRecipe.mTitle
    (findViewById<View>(R.id.category) as TextView).text = category
    (findViewById<View>(R.id.ingredients) as TextView).text = mRecipe.mIngredients
    (findViewById<View>(R.id.description) as TextView).text = mRecipe.mDescription

    val imageView = findViewById<ImageView>(R.id.app_bar_image)

    GlideApp.with(this)
            .load(WebClient.getImageUrl(mRecipe, this))
            .error(
                    GlideApp.with(this)
                            .load(R.drawable.default_recipe_image_high_dark)
                            .centerCrop()
            )
            .thumbnail(
                    GlideApp.with(this)
                            .load(R.drawable.default_recipe_image_low_dark)
                            .centerCrop()
            )
            .centerCrop()
            .signature(ObjectKey(mRecipe.mDate))
            .into(imageView)
    imageView.setOnClickListener {
      // show the image
      val intent = Intent(this@RecipeActivity, ViewImageActivity::class.java)
      intent.putExtra(ARG_RECIPE, mRecipe)
      this.startActivity(intent)
    }

    // make the title only appear if the toolbar is collapsed
    val collapsingToolbarLayout = findViewById<CollapsingToolbarLayout>(R.id.toolbar_layout)

    collapsingToolbarLayout.title = " "
    findViewById<AppBarLayout>(R.id.app_bar).addOnOffsetChangedListener(
            object : AppBarLayout.OnOffsetChangedListener {
              private var isShow = false
              private var scrollRange = -1

              override fun onOffsetChanged(appBarLayout: AppBarLayout, verticalOffset: Int) {
                if (scrollRange == -1) {
                  scrollRange = appBarLayout.totalScrollRange
                }
                if (scrollRange + verticalOffset == 0) {
                  collapsingToolbarLayout.title = mRecipe.mTitle
                  isShow = true
                } else if (isShow) {
                  collapsingToolbarLayout.title = " "
                  isShow = false
                }
              }
            })
  }

  override fun onResume() {
    super.onResume()
    if (RecipeDatabase(this).getRecipeById(mRecipe._ID) == null)
      finish()
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    menuInflater.inflate(R.menu.recipe, menu)
    menu.findItem(R.id.action_fav).setIcon(if (Favorite.contains(mRecipe))
      R.drawable.ic_favorite_white_24dp
    else
      R.drawable.ic_favorite_border_white_24dp)
    return true
  }

  override fun onBackPressed() {
    if (isTaskRoot) {
      val parentIntent = Intent(this, MainActivity::class.java)
      startActivity(parentIntent)
      finish()
    } else {
      super.onBackPressed()
    }
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    when (item.itemId) {
      android.R.id.home -> {
        onBackPressed()
        return true
      }
      R.id.action_edit -> {
        val intent = Intent(this, CreateRecipeActivity::class.java)
        intent.putExtra(CreateRecipeActivity.ARG_RECIPE, mRecipe)
        startActivity(intent)
        return true
      }
      R.id.action_delete -> {
        AlertDialog.Builder(this)
                .setTitle(R.string.title_delete_recipe)
                .setMessage(R.string.description_delete_recipe)
                .setPositiveButton(R.string.yes) { _, _ -> deleteRecipe() }
                .setNegativeButton(R.string.no, null)
                .show()
        return true
      }
      R.id.action_fav -> {
        item.setIcon(if (Favorite.toggleRecipe(mRecipe))
          R.drawable.ic_favorite_white_24dp
        else
          R.drawable.ic_favorite_border_white_24dp)
        return true
      }
      R.id.action_share -> {
        shareRecipe()
        return true
      }
      R.id.action_share_image -> {
        shareRecipeImage()
        return true
      }
    }
    return super.onOptionsItemSelected(item)
  }

  private fun shareRecipe() {
    val url = PreferenceManager.getDefaultSharedPreferences(this).getString("url",
            getString(R.string.pref_default_url))
    val sendIntent: Intent = Intent().apply {
      action = Intent.ACTION_SEND
      putExtra(Intent.EXTRA_TEXT, "$url/show_recipe.php?recipe_id=${mRecipe._ID}".replace("//show", "/show"))
      type = "text/plain"
    }

    val shareIntent = Intent.createChooser(sendIntent, getString(R.string.share))
    startActivity(shareIntent)

  }

  private fun shareRecipeImage() {
    val view = findViewById<View>(R.id.recipe)
    val bitmap = createBitmapFromView(view)

    // save image to internal storage
    val path = File(applicationContext.cacheDir, "images")
    path.mkdirs()
    val fos = FileOutputStream("$path/image.jpeg")
    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
    fos.close()

    // share the image
    val newFile = File(path, "image.jpeg")
    val uri = FileProvider.getUriForFile(applicationContext, "de.niklas_schelten.rezeptbuch",
            newFile)
    if (uri != null) {
      val intent = Intent()
      intent.action = Intent.ACTION_SEND
      intent.putExtra(Intent.EXTRA_STREAM, uri)
      intent.type = "image/jpeg"
      startActivity(Intent.createChooser(intent, getString(R.string.share_image)))
    }
  }

  private fun deleteRecipe() {
    val progressDialog = ProgressDialog(this)
    progressDialog.setTitle(resources.getString(R
            .string.delete_recipe_dialog_title))
    progressDialog.isIndeterminate = true
    progressDialog.setCancelable(false)
    progressDialog.show()
    WebClient(this).deleteRecipe(mRecipe._ID) { success ->
      progressDialog.dismiss()
      Log.i(TAG, "delete returned $success")
      when (success) {
        200 -> {
          RecipeDatabase(this@RecipeActivity).deleteRecipe(mRecipe)
          finish()
        }
        405 -> {
          AlertDialog.Builder(this@RecipeActivity)
                  .setTitle(R.string.error_title_not_allowed)
                  .setMessage(R.string.error_description_not_allowed)
                  .setPositiveButton(R.string.ok, null)
                  .show()
        }
        403 -> {
          AlertDialog.Builder(this@RecipeActivity)
                  .setTitle(R.string.error_title_wrong_password)
                  .setMessage(R.string.error_description_wrong_password)
                  .setPositiveButton(R.string.ok, null)
                  .show()
        }
        else -> {
          AlertDialog.Builder(this@RecipeActivity)
                  .setTitle(R.string.error_title_internet)
                  .setMessage(R.string.error_description_internet)
                  .setPositiveButton(R.string.ok, null)
                  .show()

        }
      }
    }
  }

  companion object {
    const val ARG_RECIPE = "mRecipe"
    private const val TAG = "RecipeActivity"

    private fun createBitmapFromView(view: View): Bitmap {
      val ret = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
      val canvas = Canvas(ret)
      val bgDrawable = view.background
      if (bgDrawable != null)
        bgDrawable.draw(canvas)
      else
        canvas.drawColor(Color.WHITE)
      view.draw(canvas)
      return ret
    }
  }
}
