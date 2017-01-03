package net.ddns.raspi_server.rezeptbuch.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import net.ddns.raspi_server.rezeptbuch.R;
import net.ddns.raspi_server.rezeptbuch.ui.categorylist.CategoryListFragment;
import net.ddns.raspi_server.rezeptbuch.ui.recipelist.RecipeListFragment;
import net.ddns.raspi_server.rezeptbuch.util.DataStructures;
import net.ddns.raspi_server.rezeptbuch.util.History;
import net.ddns.raspi_server.rezeptbuch.util.WebClient;
import net.ddns.raspi_server.rezeptbuch.util.db.AndroidDatabaseManager;

public class MainActivity extends AppCompatActivity
    implements NavigationView.OnNavigationItemSelectedListener,
    RecipeListFragment.OnRecipeClickListener, CategoryListFragment
        .OnCategoryClickListener, FragmentManager.OnBackStackChangedListener {

  private static final String TAG = "MainActivity";
  private ActionBarDrawerToggle mToggle;
  private NavigationView mNavigationView;
  private int mRootSelection = 0;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
    mToggle = new ActionBarDrawerToggle(
        this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

    mNavigationView = (NavigationView) findViewById(R.id.nav_view);
    mNavigationView.setNavigationItemSelectedListener(this);

    // retrieve new recipes if there are any
    new WebClient(getApplicationContext()).downloadRecipes();

    if (savedInstanceState == null) {
      mNavigationView.getMenu().performIdentifierAction(R.id.nav_home, 0);
      mNavigationView.getMenu().getItem(0).setChecked(true);
    }
    getSupportFragmentManager().addOnBackStackChangedListener(this);
  }


  @Override
  protected void onPostCreate(@Nullable Bundle savedInstanceState) {
    super.onPostCreate(savedInstanceState);
    mToggle.syncState();
  }

  @Override
  public void onBackPressed() {
    DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
    if (drawer.isDrawerOpen(GravityCompat.START)) {
      drawer.closeDrawer(GravityCompat.START);
    } else {
      super.onBackPressed();
    }
  }


  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    //noinspection SimplifiableIfStatement
    Intent intent;
    switch (id) {
      case R.id.action_settings:
        return true;
      case R.id.action_create_recipe:
        intent = new Intent(this, CreateRecipeActivity.class);
        startActivity(intent);
        return true;
      case R.id.action_database_debug:
        intent = new Intent(this, AndroidDatabaseManager.class);
        startActivity(intent);
        return true;
    }

    return super.onOptionsItemSelected(item);
  }

  @Override
  public boolean onNavigationItemSelected(@NonNull MenuItem item) {
    getSupportFragmentManager().popBackStackImmediate();
    // Handle navigation view item clicks here.
    int id = item.getItemId();
    RecipeListFragment recipeListFragment;
    switch (id) {
      case R.id.nav_home:
        mRootSelection = 0;
         recipeListFragment = RecipeListFragment.newInstance
             (RecipeListFragment.Typ.ALL);
        getSupportFragmentManager()
            .beginTransaction()
            .replace(R.id.content_main, recipeListFragment)
            .commit();
        break;
      case R.id.nav_favorites:
        mRootSelection = 1;
        recipeListFragment = RecipeListFragment.newInstance
            (RecipeListFragment.Typ.FAVORITE);
        getSupportFragmentManager()
            .beginTransaction()
            .replace(R.id.content_main, recipeListFragment)
            .commit();
        break;
      case R.id.nav_category:
      mRootSelection = 2;
      CategoryListFragment categoryListFragment = CategoryListFragment
            .newInstance();
      getSupportFragmentManager()
            .beginTransaction()
            .replace(R.id.content_main, categoryListFragment)
            .commit();
      break;
      case R.id.nav_history:
        mRootSelection = 3;
        recipeListFragment = RecipeListFragment.newInstance
            (RecipeListFragment.Typ.HISTORY);
        getSupportFragmentManager()
            .beginTransaction()
            .replace(R.id.content_main, recipeListFragment)
            .commit();
        break;
    }

    DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
    drawer.closeDrawer(GravityCompat.START);
    return true;
  }

  @Override
  public void onRecipeClicked(DataStructures.Recipe recipe) {
    History.getInstance().putRecipe(recipe);
    Intent intent = new Intent(this, RecipeActivity.class);
    intent.putExtra(RecipeActivity.ARG_RECIPE, recipe);
    startActivity(intent);
  }

  @Override
  public void onCategoryClicked(DataStructures.Category category) {
    RecipeListFragment recipeListFragment = RecipeListFragment.newInstance
        (category._ID);
    getSupportFragmentManager()
        .beginTransaction()
        .replace(R.id.content_main, recipeListFragment)
        .addToBackStack(RecipeListFragment.class.getName())
        .commit();
    int size = mNavigationView.getMenu().size();
    for (int i = 0; i < size; i++)
      mNavigationView.getMenu().getItem(i).setChecked(false);
    getSupportActionBar().setTitle(category.toString());
  }

  @Override
  public void onBackStackChanged() {
    if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
      mNavigationView.getMenu().getItem(mRootSelection).setChecked(true);
      getSupportActionBar().setTitle(R.string.app_name);
    }
  }
}
