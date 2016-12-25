package net.ddns.raspi_server.rezeptbuch.ui;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import com.bumptech.glide.Glide;

import net.ddns.raspi_server.rezeptbuch.R;
import net.ddns.raspi_server.rezeptbuch.ui.images.ImageProcessing;
import net.ddns.raspi_server.rezeptbuch.util.DataStructures;
import net.ddns.raspi_server.rezeptbuch.util.WebClient;
import net.ddns.raspi_server.rezeptbuch.util.db.RecipeDatabase;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;

public class CreateRecipeActivity extends AppCompatActivity implements WebClient.RecipeUploadCallback {

  public static final String ARG_RECIPE = "mRecipe";
  private static final int PERMISSION_REQUEST_READ_EXTERNAL = 1;
  private static final int PICK_IMAGE = 2;


  private Menu mMenu;
  private List<DataStructures.Category> mCategories;
  private Spinner mSpinner;
  private EditText mTitleEdit;
  private EditText mIngredientsEdit;
  private EditText mDescriptionEdit;
  private ImageView mImageView;

  private String mImagePath;
  private DataStructures.Recipe mRecipe;

  private ProgressDialog uploadProgressDialog;

  private void browseForImage() {
    Intent intent = new Intent(Intent.ACTION_PICK,
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
    startActivityForResult(intent, PICK_IMAGE);
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[]
      permissions, @NonNull int[] grantResults) {
    switch (requestCode) {
      case PERMISSION_REQUEST_READ_EXTERNAL:
        if (grantResults.length > 0 && grantResults[0] == PackageManager
            .PERMISSION_GRANTED)
          browseForImage();
    }
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    switch (requestCode) {
      case PICK_IMAGE:
        if (resultCode == RESULT_OK && data != null) {
          Uri imageUri = data.getData();

          String[] filePathColumn = {MediaStore.Images.Media.DATA};
          Cursor cursor = getContentResolver().query(imageUri,
              filePathColumn, null, null, null);
          cursor.moveToFirst();
          int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
          mImagePath = cursor.getString(columnIndex);
          cursor.close();

          Glide.with(this)
              .load(mImagePath)
              .placeholder(R.drawable.default_recipe_image_low)
              .crossFade()
              .into(mImageView);
        }
    }
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_create_recipe);

    setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    uploadProgressDialog = new ProgressDialog(this);

    mTitleEdit = (EditText) findViewById(R.id.title);
    mIngredientsEdit = (EditText) findViewById(R.id.ingredients);
    mDescriptionEdit = (EditText) findViewById(R.id.description);
    mSpinner = (Spinner) findViewById(R.id.category);
    mImageView = (ImageView) findViewById(R.id.add_image);
    mImageView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        if (ContextCompat.checkSelfPermission(CreateRecipeActivity.this, android.Manifest
            .permission.READ_EXTERNAL_STORAGE) != PackageManager
            .PERMISSION_GRANTED) {
          if (ActivityCompat.shouldShowRequestPermissionRationale
              (CreateRecipeActivity.this, android.Manifest.permission
                  .READ_EXTERNAL_STORAGE)) {
            new AlertDialog.Builder(CreateRecipeActivity.this)
                .setTitle(R.string.permission_needed)
                .setMessage(R.string.permission_read_storage_image)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                  @Override
                  public void onClick(DialogInterface dialogInterface, int i) {
                    ActivityCompat.requestPermissions(CreateRecipeActivity.this, new
                            String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                        PERMISSION_REQUEST_READ_EXTERNAL);
                  }
                })
                .show();
          } else {
            ActivityCompat.requestPermissions(CreateRecipeActivity.this, new
                    String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                PERMISSION_REQUEST_READ_EXTERNAL);
          }
        } else {
          browseForImage();
        }
      }
    });

    ChangeListener changeListener = new ChangeListener();
    mTitleEdit.addTextChangedListener(changeListener);
    mIngredientsEdit.addTextChangedListener(changeListener);
    mDescriptionEdit.addTextChangedListener(changeListener);
    mSpinner.setOnItemSelectedListener(changeListener);

    // fill the spinner
    ArrayAdapter<DataStructures.Category> adapter = new CategorySpinnerAdapter(this, android.R
        .layout.simple_spinner_item, android.R.id.text1);
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    mSpinner.setAdapter(adapter);
    RecipeDatabase database = new RecipeDatabase(this);
    mCategories = database.getCategories();
    mCategories.add(0, new DataStructures.Category(-2, getResources()
        .getString(R.string.spinner_choose_category)));
    mCategories.add(1, new DataStructures.Category(-1, getResources()
        .getString(R.string.spinner_add_category)));

    for (DataStructures.Category category : mCategories)
      adapter.add(category);

    adapter.notifyDataSetChanged();
    Bundle bundle = getIntent().getExtras();
    if (bundle != null && bundle.containsKey(ARG_RECIPE)) {
      Serializable s = bundle.getSerializable(ARG_RECIPE);
      if (s instanceof DataStructures.Recipe) {
        mRecipe = (DataStructures.Recipe) s;
        mTitleEdit.setText(mRecipe.mTitle);
        mIngredientsEdit.setText(mRecipe.mIngredients);
        mDescriptionEdit.setText(mRecipe.mDescription);
        ImageProcessing.loadRecipeImage(this, mRecipe, (ImageView)
            findViewById(R.id.add_image));
        int i = 0;
        // using for each loop because it might be faster?
        for (DataStructures.Category category : mCategories) {
          if (category._ID == mRecipe.mCategory) {
            mSpinner.setSelection(i);
            break;
          }
          i++;
        }
      }
    }
    if (mSpinner.getSelectedItemPosition() == 0)
      mSpinner.setSelection(-1);
  }

  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    menu.getItem(0).setEnabled(false);
    return super.onPrepareOptionsMenu(menu);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    mMenu = menu;
    getMenuInflater().inflate(R.menu.create, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        onBackPressed();
        return true;
      case R.id.action_save:
        // the progress dialog to show when waiting for a network response
        saveRecipe();
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

  private void saveRecipe() {
    uploadProgressDialog.setIndeterminate(true);
    uploadProgressDialog.setCancelable(false);
    WebClient webClient = new WebClient(this);
    DataStructures.Recipe recipe = new DataStructures.Recipe(
        (mRecipe == null) ? -1 : mRecipe._ID,
        mTitleEdit.getText().toString(),
        mSpinner.getSelectedItemPosition() - 1,
        mIngredientsEdit.getText().toString(),
        mDescriptionEdit.getText().toString(),
        mImagePath,
        null /*date*/);
    if (mRecipe == null) {
      uploadProgressDialog.setTitle(CreateRecipeActivity.this.getResources().getString(R
          .string.create_recipe_dialog_title));
      uploadProgressDialog.show();
      webClient.uploadRecipe(recipe, this);
    } else {
      uploadProgressDialog.setTitle(CreateRecipeActivity.this.getResources().getString(R
          .string.update_recipe_dialog_title));
      uploadProgressDialog.show();
      webClient.updateRecipe(recipe, this);
    }
  }

  private void updateSaveItem() {
    if (mMenu != null)
      mMenu.getItem(0).setEnabled(
          mSpinner.getSelectedItemPosition() > 1 &&
              !mTitleEdit.getText().toString().isEmpty() &&
              !mIngredientsEdit.getText().toString().isEmpty() &&
              !mDescriptionEdit.getText().toString().isEmpty());
  }

  @Override
  public void finished(DataStructures.Recipe recipe) {
    // always dismiss the progress dialog
    uploadProgressDialog.dismiss();
    // if an error occurred, show a message and probably store the
    // recipe locally somehow #TODO
    if (recipe == null) {

      AlertDialog.Builder builder1 = new AlertDialog
          .Builder(CreateRecipeActivity.this);
      builder1.setTitle(R.string.error_title_internet);
      builder1.setMessage(R.string.error_description_internet);
      builder1.setPositiveButton(R.string.ok, null);
      builder1.show();
    } else {
      // add the recipe to the local db
      RecipeDatabase recipeDatabase = new RecipeDatabase(this);
      if (mRecipe != null) {
        File imageFile = new File(getFilesDir(), mRecipe.mImageName);
        imageFile.delete();
        recipeDatabase.deleteRecipe(mRecipe);
      }

      Bitmap bitmap = BitmapFactory.decodeFile(mImagePath);
      File file = new File(getFilesDir(), recipe.mImageName);
      try {
        file.createNewFile();
        FileOutputStream out = new FileOutputStream(file);
        if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out))
          throw new RuntimeException("couldn't compress image.");
      } catch (IOException e) {
        e.printStackTrace();
      }
      recipeDatabase.putRecipe(recipe);
      finish();
      // show the recipe
      Intent intent = new Intent(CreateRecipeActivity.this, RecipeActivity
          .class);
      intent.putExtra(RecipeActivity.ARG_RECIPE, recipe);
      CreateRecipeActivity.this.startActivity(intent);
    }
  }

  @Override
  public void onProgress(int progress) {

  }

  private class ChangeListener implements TextWatcher, AdapterView
      .OnItemSelectedListener {

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void afterTextChanged(Editable editable) {
      CreateRecipeActivity.this.updateSaveItem();
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
      CreateRecipeActivity.this.updateSaveItem();
      // when choosing create category
      if (i == 1) {
        // the progress dialog to show when waiting for a network response
        final ProgressDialog progressDialog = new ProgressDialog(CreateRecipeActivity
            .this);
        progressDialog.setTitle(CreateRecipeActivity.this.getResources().getString(R
            .string.create_category_dialog_title));
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);

        // the dialog to enter a new category with the EditText
        final EditText input = new EditText(CreateRecipeActivity.this);
        final AlertDialog dialog = new AlertDialog.Builder(CreateRecipeActivity
            .this)
            .setTitle(R.string.create_category_title)
            .setMessage(R.string.create_category_message)
            .setView(input)
            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialogInterface, int i) {
                // when accepting show the uploadProgressDialog and request the server
                progressDialog.show();
                WebClient webClient = new WebClient(CreateRecipeActivity.this);
                webClient.uploadCategory(input.getText().toString(), new
                    WebClient.CategoryUploadCallback() {
                      @Override
                      public void finished(DataStructures.Category category) {
                        // always dismiss the dialog when the server responded
                        progressDialog.dismiss();
                        // if an error occurred, show a message and set the
                        // selection back to "Choose a category"
                        if (category == null) {
                          AlertDialog.Builder builder1 = new AlertDialog
                              .Builder(CreateRecipeActivity.this);
                          builder1.setTitle(R.string.error_title_internet);
                          builder1.setMessage(R.string.error_description_internet);
                          builder1.setPositiveButton(R.string.ok, null);
                          builder1.show();
                          CreateRecipeActivity.this.mSpinner.setSelection(0);
                        } else {
                          // otherwise, store the recipe in the database and
                          // select it in the spinner
                          new RecipeDatabase(CreateRecipeActivity.this).putCategory(category);
                          CreateRecipeActivity.this.mCategories.add(category);
                          CreateRecipeActivity.this.mSpinner.invalidate();
                          CreateRecipeActivity.this.mSpinner.setSelection(CreateRecipeActivity.this
                              .mSpinner.getCount() - 1);
                        }
                      }
                    });
              }
            })
            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialogInterface, int i) {
                // cancelling will result in "Choose a category" to be selected
                CreateRecipeActivity.this.mSpinner.setSelection(0);
              }
            }).create();

        // make sure that a value is entered before clicking ok
        input.addTextChangedListener(new TextWatcher() {
          @Override
          public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

          }

          @Override
          public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

          }

          @Override
          public void afterTextChanged(Editable editable) {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(editable
                .length() > 0);
          }
        });
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
      }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
  }
}
