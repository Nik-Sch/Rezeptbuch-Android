package de.niklas_schelten.rezeptbuch.ui

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.app.FragmentManager
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import de.niklas_schelten.rezeptbuch.R
import de.niklas_schelten.rezeptbuch.ui.categorylist.CategoryListFragment
import de.niklas_schelten.rezeptbuch.ui.recipelist.RecipeListFragment
import de.niklas_schelten.rezeptbuch.util.DataStructures
import de.niklas_schelten.rezeptbuch.util.History
import de.niklas_schelten.rezeptbuch.util.UpdateChecker
import de.niklas_schelten.rezeptbuch.util.WebClient

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener,
        RecipeListFragment.OnRecipeClickListener, CategoryListFragment.OnCategoryClickListener,
        FragmentManager.OnBackStackChangedListener {

  private lateinit var mToggle: ActionBarDrawerToggle
  private lateinit var mNavigationView: NavigationView
  private var mRootSelection = 0

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    val toolbar = findViewById<Toolbar>(R.id.toolbar)
    setSupportActionBar(toolbar)

    val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
    mToggle = ActionBarDrawerToggle(
            this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)

    mNavigationView = findViewById(R.id.nav_view)
    mNavigationView.setNavigationItemSelectedListener(this)

    // retrieve new recipes if there are any
    WebClient(applicationContext).downloadRecipes()

    // check for updates
    UpdateChecker(applicationContext).checkRelease()

    if (savedInstanceState == null) {
      mNavigationView.menu?.performIdentifierAction(R.id.nav_home, 0)
      mNavigationView.menu?.getItem(0)?.isChecked = true
    }
    supportFragmentManager.addOnBackStackChangedListener(this)
  }


  override fun onPostCreate(savedInstanceState: Bundle?) {
    super.onPostCreate(savedInstanceState)
    mToggle.syncState()
  }

  override fun onBackPressed() {
    val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
    if (drawer.isDrawerOpen(GravityCompat.START)) {
      drawer.closeDrawer(GravityCompat.START)
    } else {
      super.onBackPressed()
    }
  }


  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    // Inflate the menu; this adds items to the action bar if it is present.
    menuInflater.inflate(R.menu.main, menu)
    return true
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    val id = item.itemId

    val intent: Intent
    when (id) {
      R.id.action_settings -> return true
      R.id.action_create_recipe -> {
        intent = Intent(this, CreateRecipeActivity::class.java)
        startActivity(intent)
        return true
      }
//      R.id.action_database_debug -> {
//        intent = Intent(this, AndroidDatabaseManager::class.java)
//        startActivity(intent)
//        return true
//      }
    }

    return super.onOptionsItemSelected(item)
  }

  override fun onNavigationItemSelected(item: MenuItem): Boolean {
    supportFragmentManager.popBackStackImmediate()
    // Handle navigation view item clicks here.
    val id = item.itemId
    val recipeListFragment: RecipeListFragment
    when (id) {
      R.id.nav_home -> {
        mRootSelection = 0
        recipeListFragment = RecipeListFragment.newInstance(RecipeListFragment.Type.ALL)
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.content_main, recipeListFragment)
                .commit()
      }
      R.id.nav_favorites -> {
        mRootSelection = 1
        recipeListFragment = RecipeListFragment.newInstance(RecipeListFragment.Type.FAVORITE)
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.content_main, recipeListFragment)
                .addToBackStack(RecipeListFragment::class.java.name)
                .commit()
      }
      R.id.nav_category -> {
        mRootSelection = 2
        val categoryListFragment = CategoryListFragment.newInstance()
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.content_main, categoryListFragment)
                .addToBackStack(RecipeListFragment::class.java.name)
                .commit()
      }
      R.id.nav_history -> {
        mRootSelection = 3
        recipeListFragment = RecipeListFragment.newInstance(RecipeListFragment.Type.HISTORY)
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.content_main, recipeListFragment)
                .addToBackStack(RecipeListFragment::class.java.name)
                .commit()
      }
      R.id.nav_settings -> {
        startActivity(Intent(this, SettingsActivity::class.java))
      }
    }

    val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
    drawer.closeDrawer(GravityCompat.START)
    return true
  }

  override fun onRecipeClicked(item: DataStructures.Recipe) {
    History.putRecipe(item)
    val intent = Intent(this, RecipeActivity::class.java)
    intent.putExtra(RecipeActivity.ARG_RECIPE, item)
    startActivity(intent)
  }

  override fun onCategoryClicked(category: DataStructures.Category) {
    val recipeListFragment = RecipeListFragment.newInstance(category._ID)
    supportFragmentManager
            .beginTransaction()
            .replace(R.id.content_main, recipeListFragment)
            .addToBackStack(RecipeListFragment::class.java.name)
            .commit()
    val size = mNavigationView.menu?.size() ?: 0
    for (i in 0 until size)
      mNavigationView.menu?.getItem(i)?.isChecked = false
    supportActionBar?.title = category.toString()
  }

  override fun onBackStackChanged() {
    when (supportFragmentManager.backStackEntryCount) {
      0 -> {
        mRootSelection = 0
        supportActionBar?.setTitle(R.string.app_name)
      }
      1 -> {
        when (mRootSelection) {
          0 -> supportActionBar?.setTitle(R.string.app_name)
          1 -> supportActionBar?.setTitle(R.string.nav_favorites)
          2 -> supportActionBar?.setTitle(R.string.nav_category)
          3 -> supportActionBar?.setTitle(R.string.nav_history)
        }
      }
    }
    mNavigationView.menu?.getItem(mRootSelection)?.isChecked = true
  }

  companion object {
    const val ARG_ERROR = "error"
  }
}
