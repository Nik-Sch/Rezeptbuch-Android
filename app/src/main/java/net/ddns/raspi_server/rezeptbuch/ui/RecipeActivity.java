package net.ddns.raspi_server.rezeptbuch.ui;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import net.ddns.raspi_server.rezeptbuch.R;
import net.ddns.raspi_server.rezeptbuch.ui.images.ImageProcessing;
import net.ddns.raspi_server.rezeptbuch.util.DataStructures;
import net.ddns.raspi_server.rezeptbuch.util.Favorite;
import net.ddns.raspi_server.rezeptbuch.util.WebClient;
import net.ddns.raspi_server.rezeptbuch.util.db.RecipeDatabase;

import java.io.File;
import java.io.Serializable;


public class RecipeActivity extends AppCompatActivity {

  public static final String ARG_RECIPE = "mRecipe";

  private DataStructures.Recipe mRecipe;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Bundle bundle = getIntent().getExtras();
    if (bundle == null || !bundle.containsKey(ARG_RECIPE))
      throw new RuntimeException("The recipe activity has to be called with a" +
          " recipe argument");
    Serializable s = bundle.getSerializable(ARG_RECIPE);
    if (!(s instanceof DataStructures.Recipe))
      throw new RuntimeException("The recipe activity has to be called with a" +
          " recipe argument");
    mRecipe = (DataStructures.Recipe) s;

    setContentView(R.layout.activity_recipe);

    setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
    ActionBar actionBar = getSupportActionBar();
    actionBar.setDisplayHomeAsUpEnabled(true);

    String category = new RecipeDatabase(this).getCategoryById(mRecipe
        .mCategory).mName;

    actionBar.setTitle(mRecipe.mTitle);

    ((TextView) findViewById(R.id.title)).setText(mRecipe.mTitle);
    ((TextView) findViewById(R.id.category)).setText(category);
    ((TextView) findViewById(R.id.ingredients)).setText(mRecipe.mIngredients);
    ((TextView) findViewById(R.id.description)).setText(mRecipe.mDescription);

    ImageProcessing.loadRecipeImage(this, mRecipe, (ImageView) findViewById(R
        .id.app_bar_image), true);

    // make the title only appear if the toolbar is collapsed
    final CollapsingToolbarLayout collapsingToolbarLayout =
        (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);

    collapsingToolbarLayout.setTitle(" ");
    ((AppBarLayout) findViewById(R.id.app_bar)).addOnOffsetChangedListener
        (new AppBarLayout.OnOffsetChangedListener() {
          boolean isShow = false;
          int scrollRange = -1;

          @Override
          public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
            if (scrollRange == -1) {
              scrollRange = appBarLayout.getTotalScrollRange();
            }
            if (scrollRange + verticalOffset == 0) {
              collapsingToolbarLayout.setTitle(mRecipe.mTitle);
              isShow = true;
            } else if (isShow) {
              collapsingToolbarLayout.setTitle(" ");
              isShow = false;
            }
          }
        });
  }

  @Override
  protected void onResume() {
    super.onResume();
    if (new RecipeDatabase(this).getRecipeById(mRecipe._ID) == null)
      finish();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.recipe, menu);
    menu.findItem(R.id.action_fav).setIcon(Favorite.getInstance()
        .contains(mRecipe)
        ? R.drawable.ic_favorite_white_24dp
        : R.drawable.ic_favorite_border_white_24dp);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        onBackPressed();
        return true;
      case R.id.action_edit:
        Intent intent = new Intent(this, CreateRecipeActivity.class);
        intent.putExtra(CreateRecipeActivity.ARG_RECIPE, mRecipe);
        startActivity(intent);
        return true;
      case R.id.action_delete:
        new AlertDialog.Builder(this)
            .setTitle(R.string.title_delete_recipe)
            .setMessage(R.string.description_delete_recipe)
            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                deleteRecipe();
              }
            })
            .setNegativeButton(R.string.no, null)
            .show();
        return true;
      case R.id.action_fav:
        item.setIcon(Favorite.getInstance().toggleRecipe(mRecipe)
            ? R.drawable.ic_favorite_white_24dp
            : R.drawable.ic_favorite_border_white_24dp);
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

  private void deleteRecipe() {
    final ProgressDialog progressDialog = new ProgressDialog(this);
    progressDialog.setTitle(getResources().getString(R
        .string.delete_recipe_dialog_title));
    progressDialog.setIndeterminate(true);
    progressDialog.setCancelable(false);
    progressDialog.show();
    new WebClient(this).deleteRecipe(mRecipe, new WebClient.DeleteRecipeCallback() {
      @Override
      public void finished(boolean success) {
        progressDialog.dismiss();
        if (success) {
          File imageFile = new File(RecipeActivity.this.getFilesDir(), mRecipe
              .mImageName);
          imageFile.delete();
          new RecipeDatabase(RecipeActivity.this).deleteRecipe(mRecipe);
          finish();
        } else
          new AlertDialog.Builder(RecipeActivity.this)
              .setTitle(R.string.error_title_internet)
              .setMessage(R.string.error_description_delete_recipe)
              .setPositiveButton(R.string.ok, null)
              .show();
      }
    });
  }
}
