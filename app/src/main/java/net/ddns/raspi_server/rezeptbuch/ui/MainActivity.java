package net.ddns.raspi_server.rezeptbuch.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import net.ddns.raspi_server.rezeptbuch.R;
import net.ddns.raspi_server.rezeptbuch.ui.recipelist.RecipeListFragment;
import net.ddns.raspi_server.rezeptbuch.util.DataStructures;
import net.ddns.raspi_server.rezeptbuch.util.WebClient;
import net.ddns.raspi_server.rezeptbuch.util.db.AndroidDatabaseManager;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, RecipeListFragment.OnListFragmentInteractionListener{

  ActionBarDrawerToggle mToggle;
  NavigationView mNavigationView;

  @Override
  protected void onCreate(Bundle savedInstanceState){
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

    if (savedInstanceState == null)
      mNavigationView.getMenu().performIdentifierAction(R.id.nav_home, 0);
  }

  @Override
  protected void onPostCreate(@Nullable Bundle savedInstanceState){
    super.onPostCreate(savedInstanceState);
    mToggle.syncState();
  }

  @Override
  public void onBackPressed(){
    DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
    if (drawer.isDrawerOpen(GravityCompat.START)){
      drawer.closeDrawer(GravityCompat.START);
    }else{
      super.onBackPressed();
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu){
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item){
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    //noinspection SimplifiableIfStatement
    Intent intent;
    switch (id){
      case R.id.action_settings:
        return true;
      case R.id.action_create_recipe:
        intent = new Intent(this, CreateRecipe.class);
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
  public boolean onNavigationItemSelected(@NonNull MenuItem item){
    // Handle navigation view item clicks here.
    int id = item.getItemId();
    switch (id){
      case R.id.nav_home:
        RecipeListFragment recipeListFragment = RecipeListFragment.newInstance();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content_main, recipeListFragment)
                .commit();
    }

    DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
    drawer.closeDrawer(GravityCompat.START);
    return true;
  }

  @Override
  public void onRecipeClicked(DataStructures.Recipe recipe){
    Intent intent = new Intent(this, RecipeActivity.class);
    intent.putExtra(RecipeActivity.ARG_RECIPE, recipe);
    startActivity(intent);
  }
}
