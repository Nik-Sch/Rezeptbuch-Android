package net.ddns.raspi_server.rezeptbuch.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import net.ddns.raspi_server.rezeptbuch.R;
import net.ddns.raspi_server.rezeptbuch.ui.header.HeaderView;
import net.ddns.raspi_server.rezeptbuch.ui.images.ImageProcessing;
import net.ddns.raspi_server.rezeptbuch.util.DataStructures;

import java.io.Serializable;

public class RecipeActivity extends AppCompatActivity {

  public static final String ARG_RECIPE = "mRecipe";

  private HeaderView mToolbarHeaderView;
  private HeaderView mFloatHeaderView;
  private boolean isToolbarViewHidden = false;

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
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    ((TextView) findViewById(R.id.ingredients)).setText(mRecipe.ingredients);
    ((TextView) findViewById(R.id.description)).setText(mRecipe.description);

    ImageProcessing.loadRecipeImage(this, mRecipe, (ImageView) findViewById(R
        .id.app_bar_image));
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        onBackPressed();
    }
    return super.onOptionsItemSelected(item);
  }
}
