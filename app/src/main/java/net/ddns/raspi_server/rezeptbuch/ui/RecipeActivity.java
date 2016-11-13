package net.ddns.raspi_server.rezeptbuch.ui;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import net.ddns.raspi_server.rezeptbuch.R;
import net.ddns.raspi_server.rezeptbuch.ui.header.HeaderView;
import net.ddns.raspi_server.rezeptbuch.ui.images.ImageProcessing;
import net.ddns.raspi_server.rezeptbuch.util.DataStructures;
import net.ddns.raspi_server.rezeptbuch.util.db.RecipeDatabase;

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
    getSupportActionBar().setDisplayShowTitleEnabled(false);


    ((AppBarLayout) findViewById(R.id.app_bar)).
        addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
          @Override
          public void onOffsetChanged(AppBarLayout appBarLayout, int offset) {
            int maxScroll = appBarLayout.getTotalScrollRange();
            float percentage = (float) Math.abs(offset) / (float) maxScroll;

            if (percentage == 1f && isToolbarViewHidden) {
              mToolbarHeaderView.setVisibility(View.VISIBLE);
              isToolbarViewHidden = !isToolbarViewHidden;
            } else if (percentage < 1f && !isToolbarViewHidden) {
              mToolbarHeaderView.setVisibility(View.GONE);
              isToolbarViewHidden = !isToolbarViewHidden;
            }
          }
        });
    String title = mRecipe.title;
    String subtitle = new RecipeDatabase(this).getCategoryById(mRecipe
        .category).name;
    mFloatHeaderView = ((HeaderView) findViewById(R.id.float_header_view));
    mFloatHeaderView.bindTo(title, subtitle);
    mToolbarHeaderView = ((HeaderView) findViewById(R.id.toolbar_header_view));
    mToolbarHeaderView.bindTo(title, subtitle);

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
