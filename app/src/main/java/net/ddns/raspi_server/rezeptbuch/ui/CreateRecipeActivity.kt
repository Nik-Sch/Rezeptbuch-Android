package net.ddns.raspi_server.rezeptbuch.ui

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import com.bumptech.glide.signature.ObjectKey
import net.ddns.raspi_server.rezeptbuch.GlideApp

import net.ddns.raspi_server.rezeptbuch.R
import net.ddns.raspi_server.rezeptbuch.util.DataStructures
import net.ddns.raspi_server.rezeptbuch.util.WebClient
import net.ddns.raspi_server.rezeptbuch.util.db.RecipeDatabase

import java.io.File
import java.util.*

class CreateRecipeActivity : AppCompatActivity(), WebClient.RecipeUploadCallback {


  private var mMenu: Menu? = null
  private lateinit var mCategories: MutableList<DataStructures.Category>
  private lateinit var mSpinner: Spinner
  private lateinit var mTitleEdit: EditText
  private lateinit var mIngredientsEdit: EditText
  private lateinit var mDescriptionEdit: EditText
  private lateinit var mImageView: ImageView

  private var mLocalImagePath: String? = null
  private var mRecipe: DataStructures.Recipe? = null

  private lateinit var uploadProgressDialog: ProgressDialog

  private fun browseForImage() {
    val intent = Intent(Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
    startActivityForResult(intent, PICK_IMAGE)
  }

  override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
    when (requestCode) {
      PERMISSION_REQUEST_READ_EXTERNAL -> {
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
          browseForImage()
      }
    }
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    when (requestCode) {
      PICK_IMAGE -> if (resultCode == Activity.RESULT_OK) {
        val imageUri = data?.data

        val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = contentResolver.query(imageUri,
                filePathColumn, null, null, null)
        cursor.moveToFirst()
        val columnIndex = cursor.getColumnIndex(filePathColumn[0])
        mLocalImagePath = cursor?.getString(columnIndex)
        cursor?.close()

        GlideApp.with(this)
                .load(mLocalImagePath)
                .error(R.drawable.default_recipe_image_high)
                .placeholder(R.drawable.default_recipe_image_low)
                .centerCrop()
                .into(mImageView)
      }
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_create_recipe)

    setSupportActionBar(findViewById(R.id.toolbar))
    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    uploadProgressDialog = ProgressDialog(this)

    mTitleEdit = findViewById(R.id.title)
    mIngredientsEdit = findViewById(R.id.ingredients)
    mDescriptionEdit = findViewById(R.id.description)
    mSpinner = findViewById(R.id.category)
    mImageView = findViewById(R.id.add_image)
    mImageView.setOnClickListener {
      if (ContextCompat.checkSelfPermission(this@CreateRecipeActivity,
                      android.Manifest.permission.READ_EXTERNAL_STORAGE)
              != PackageManager.PERMISSION_GRANTED) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this@CreateRecipeActivity,
                        android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
          AlertDialog.Builder(this@CreateRecipeActivity)
                  .setTitle(R.string.permission_needed)
                  .setMessage(R.string.permission_read_storage_image)
                  .setPositiveButton(R.string.ok) { _, _ ->
                    ActivityCompat.requestPermissions(this@CreateRecipeActivity,
                            arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                            PERMISSION_REQUEST_READ_EXTERNAL)
                  }
                  .show()
        } else {
          ActivityCompat.requestPermissions(this@CreateRecipeActivity,
                  arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                  PERMISSION_REQUEST_READ_EXTERNAL)
        }
      } else {
        browseForImage()
      }
    }

    val changeListener = ChangeListener()
    mTitleEdit.addTextChangedListener(changeListener)
    mIngredientsEdit.addTextChangedListener(changeListener)
    mDescriptionEdit.addTextChangedListener(changeListener)
    mSpinner.onItemSelectedListener = changeListener

    // fill the spinner
    val adapter = CategorySpinnerAdapter(this, android.R
            .layout.simple_spinner_item, android.R.id.text1)
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
    mSpinner.adapter = adapter
    val database = RecipeDatabase(this)
    mCategories = database.categories
    mCategories.add(0, DataStructures.Category(-2, resources
            .getString(R.string.spinner_choose_category)))
    mCategories.add(1, DataStructures.Category(-1, resources
            .getString(R.string.spinner_add_category)))

    for (category in mCategories)
      adapter.add(category)

    adapter.notifyDataSetChanged()
    val bundle = intent.extras
    if (bundle != null && bundle.containsKey(ARG_RECIPE)) {
      val s = bundle.getSerializable(ARG_RECIPE)
      if (s is DataStructures.Recipe) {
        mRecipe = s
        mTitleEdit.setText(mRecipe?.mTitle)
        mIngredientsEdit.setText(mRecipe?.mIngredients)
        mDescriptionEdit.setText(mRecipe?.mDescription)

        GlideApp.with(this)
                .load(WebClient.getImageUrl(mRecipe))
                .error(R.drawable.default_recipe_image_high)
                .placeholder(R.drawable.default_recipe_image_low)
                .centerCrop()
                .signature(ObjectKey(mRecipe?.mDate ?: Date()))
                .into(findViewById(R.id.add_image))

        var i = 0
        for (category in mCategories) {
          if (category._ID == mRecipe?.mCategory) {
            mSpinner.setSelection(i)
            break
          }
          i++
        }
      }
    }
    if (mSpinner.selectedItemPosition == 0)
      mSpinner.setSelection(-1)
  }

  override fun onPrepareOptionsMenu(menu: Menu): Boolean {
    menu.getItem(0).isEnabled = false
    return super.onPrepareOptionsMenu(menu)
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    mMenu = menu
    menuInflater.inflate(R.menu.create, menu)
    return true
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    when (item.itemId) {
      android.R.id.home -> {
        onBackPressed()
        return true
      }
      R.id.action_save -> {
        // the progress dialog to show when waiting for a network response
        saveRecipe()
        return true
      }
    }
    return super.onOptionsItemSelected(item)
  }

  private fun saveRecipe() {
    uploadProgressDialog.isIndeterminate = true
    uploadProgressDialog.setCancelable(false)
    val webClient = WebClient(this)
    val recipe = DataStructures.Recipe(mRecipe?._ID ?: -1,
            mTitleEdit.text.toString(),
            mSpinner.selectedItemPosition - 1,
            mIngredientsEdit.text.toString(),
            mDescriptionEdit.text.toString(),
            mLocalImagePath ?: "",
            Date())
    if (mRecipe == null) {
      uploadProgressDialog.setTitle(this@CreateRecipeActivity.resources.getString(R
              .string.create_recipe_dialog_title))
      uploadProgressDialog.show()
      webClient.uploadRecipe(recipe, this)
    } else {
      uploadProgressDialog.setTitle(this@CreateRecipeActivity.resources.getString(R
              .string.update_recipe_dialog_title))
      uploadProgressDialog.show()
      webClient.updateRecipe(recipe, this)
    }
  }

  private fun updateSaveItem() {
    mMenu?.getItem(0)?.isEnabled =
            mSpinner.selectedItemPosition > 1
            && !mTitleEdit.text.toString().isEmpty()
            && !mIngredientsEdit.text.toString().isEmpty()
            && !mDescriptionEdit.text.toString().isEmpty()
  }

  // overrides WebClient.RecipeUploadCallback
  override fun finished(recipe: DataStructures.Recipe?) {
    // always dismiss the progress dialog
    uploadProgressDialog.dismiss()
    // if an error occurred, show a message and probably store the
    // recipe locally somehow #TODO

    if (recipe == null) {
      val builder = AlertDialog.Builder(this)
      builder.setTitle(R.string.error_title_internet)
      builder.setMessage(R.string.error_description_internet)
      builder.setPositiveButton(R.string.ok, null)
      builder.show()
    } else {
      // add the recipe to the local db
      val recipeDatabase = RecipeDatabase(this)

      recipeDatabase.putRecipe(recipe)
      finish()
      // show the recipe
      val intent = Intent(this, RecipeActivity::class.java)
      intent.putExtra(RecipeActivity.ARG_RECIPE, recipe)
      startActivity(intent)

    }
  }

  override fun onProgress(progress: Int) {}

  private inner class ChangeListener : TextWatcher, AdapterView.OnItemSelectedListener {

    override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

    override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

    override fun afterTextChanged(editable: Editable) {
      this@CreateRecipeActivity.updateSaveItem()
    }

    override fun onItemSelected(adapterView: AdapterView<*>, view: View, i: Int, l: Long) {
      this@CreateRecipeActivity.updateSaveItem()
      // when choosing create category
      if (i == 1) {
        // the progress dialog to show when waiting for a network response
        val progressDialog = ProgressDialog(this@CreateRecipeActivity)
        progressDialog.setTitle(this@CreateRecipeActivity.resources.getString(R
                .string.create_category_dialog_title))
        progressDialog.isIndeterminate = true
        progressDialog.setCancelable(false)

        // the dialog to enter a new category with the EditText
        val input = EditText(this@CreateRecipeActivity)
        val dialog = AlertDialog.Builder(this@CreateRecipeActivity)
                .setTitle(R.string.create_category_title)
                .setMessage(R.string.create_category_message)
                .setView(input)
                .setPositiveButton(R.string.ok) { _, _ ->
                  // when accepting show the uploadProgressDialog and request the server
                  progressDialog.show()
                  val webClient = WebClient(this@CreateRecipeActivity)
                  webClient.uploadCategory(input.text.toString()) { category ->
                    // always dismiss the dialog when the server responded
                    progressDialog.dismiss()
                    // if an error occurred, show a message and set the
                    // selection back to "Choose a category"
                    if (category == null) {
                      val builder1 = AlertDialog.Builder(this@CreateRecipeActivity)
                      builder1.setTitle(R.string.error_title_internet)
                      builder1.setMessage(R.string.error_description_internet)
                      builder1.setPositiveButton(R.string.ok, null)
                      builder1.show()
                      this@CreateRecipeActivity.mSpinner.setSelection(0)
                    } else {
                      // otherwise, store the recipe in the database and
                      // select it in the spinner
                      RecipeDatabase(this@CreateRecipeActivity).putCategory(category)
                      this@CreateRecipeActivity.mCategories.add(category)
                      this@CreateRecipeActivity.mSpinner.invalidate()
                      this@CreateRecipeActivity.mSpinner.setSelection(this@CreateRecipeActivity
                              .mSpinner.count - 1)
                    }
                  }
                }
                .setNegativeButton(R.string.cancel) { _, _ ->
                  // cancelling will result in "Choose a category" to be selected
                  this@CreateRecipeActivity.mSpinner.setSelection(0)
                }.create()

        // make sure that a value is entered before clicking ok
        input.addTextChangedListener(object : TextWatcher {
          override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

          override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

          override fun afterTextChanged(editable: Editable) {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = editable.isNotEmpty()
          }
        })
        dialog.show()
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
      }
    }

    override fun onNothingSelected(adapterView: AdapterView<*>) {}
  }

  companion object {
    const val ARG_RECIPE = "mRecipe"
    private const val PERMISSION_REQUEST_READ_EXTERNAL = 1
    private const val PICK_IMAGE = 2
  }
}
