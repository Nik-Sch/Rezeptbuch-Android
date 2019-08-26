package net.ddns.raspi_server.rezeptbuch.ui.recipelist

import android.animation.Animator
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v4.content.LocalBroadcastManager
import android.support.v4.view.MenuItemCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import com.futuremind.recyclerviewfastscroll.FastScroller

import net.ddns.raspi_server.rezeptbuch.R
import net.ddns.raspi_server.rezeptbuch.util.DataStructures.Recipe
import net.ddns.raspi_server.rezeptbuch.util.Favorite
import net.ddns.raspi_server.rezeptbuch.util.History
import net.ddns.raspi_server.rezeptbuch.util.Util
import net.ddns.raspi_server.rezeptbuch.util.WebClient
import net.ddns.raspi_server.rezeptbuch.util.db.RecipeDatabase

import java.util.Date

/**
 * A fragment representing a list of Items.
 *
 *
 * Activities containing this fragment MUST implement the
 * [OnRecipeClickListener]
 * interface.
 */
/**
 * Mandatory empty constructor for the fragment manager to instantiate the
 * fragment (e.g. upon screen orientation changes).
 */
class RecipeListFragment : Fragment(), SearchView.OnQueryTextListener {
  enum class Type {
    ALL, HISTORY, INVALID, FAVORITE
  }

  private var mCurrentSearch: String = ""

  private lateinit var mListener: OnRecipeClickListener
  private lateinit var mType: Type
  private lateinit var mRecipeList: MutableList<Recipe>
  private lateinit var mAdapter: RecyclerView.Adapter<*>
  private lateinit var mInfoTextView: TextView
  private lateinit var mNotificationView: TextView

  private val mBroadcastReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
      val success = intent.getIntExtra(WebClient
              .ARG_BROADCAST_DOWNLOAD_FINISHED_SUCCESS, 500)
      var showNotification = true
      val dateFormat = android.text.format.DateFormat.getTimeFormat(getContext())
      when {
        success / 100 == 2 -> {
          mNotificationView.text = String.format(
                  context.resources.getString(R.string.notification_updated),
                  dateFormat.format(Date()))
          mNotificationView.setBackgroundColor(resources.getColor(android.R
                  .color.darker_gray))
          refresh()
        }
        mRecipeList.isNotEmpty() -> {
          mNotificationView.text = context.getString(R.string.notification_failed_update,
                  Util.httpStatusString(success))
          mNotificationView.setBackgroundColor(resources.getColor(android.R
                  .color.holo_red_dark))
        }
        else -> {
          showNotification = false
          mInfoTextView.text = context.getString(R.string.notification_failed_download, Util
                  .httpStatusString(success))
          mInfoTextView.setTextColor(resources.getColor(android
                  .R.color.holo_red_dark))
        }
      }

      if (showNotification) {
        mNotificationView.translationY = (-mNotificationView.height).toFloat()
        mNotificationView.visibility = View.VISIBLE
        mNotificationView.animate()
                .translationY(0f)
                .setListener(object : Animator.AnimatorListener {
                  override fun onAnimationStart(animation: Animator) {}

                  override fun onAnimationEnd(animation: Animator) {
                    val handler = Handler()
                    handler.postDelayed({
                      mNotificationView.animate()
                              .translationY((-mNotificationView.height).toFloat())
                              .setListener(object : Animator.AnimatorListener {
                                override fun onAnimationStart(animation: Animator) {}

                                override fun onAnimationEnd(animation: Animator) {
                                  mNotificationView.visibility = View.GONE
                                }

                                override fun onAnimationCancel(animation: Animator) {}

                                override fun onAnimationRepeat(animation: Animator) {}
                              })
                    }, 3000)
                  }

                  override fun onAnimationCancel(animation: Animator) {}

                  override fun onAnimationRepeat(animation: Animator) {}
                })
      }
    }
  }


  private fun getRecipes(): MutableList<Recipe> {
    /* get either:
     * - all recipes
     * - recipes by search string
     * - recipes by category
     * - recipes by favorite
     */
    context?.let { ctx ->
      val database = RecipeDatabase(ctx)
      val args = arguments
      return if (mCurrentSearch.isNotEmpty())
        database.getRecipesBySearch(mCurrentSearch)
      else if (args != null && args.containsKey(ARG_CATEGORY))
        database.getRecipesByCategory(args.getInt(ARG_CATEGORY))
      else if (mType == Type.HISTORY)
        History.getRecipes()
      else if (mType == Type.FAVORITE)
        Favorite.getRecipes()
      else
        database.recipes
    }
    return mutableListOf()
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setHasOptionsMenu(true)

    val args = arguments
    mType = args?.getSerializable(ARG_TYP) as Type

    context?.let { ctx ->
      LocalBroadcastManager.getInstance(ctx).registerReceiver(mBroadcastReceiver,
              IntentFilter(WebClient.EVENT_BROADCAST_DOWNLOAD_FINISHED))
    }
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    val rootView = inflater.inflate(R.layout.fragment_recipe_list,
            container, false)
    val listView = rootView.findViewById<View>(R.id.list)
    mInfoTextView = rootView.findViewById(R.id.info_text)
    mNotificationView = rootView.findViewById(R.id.notification)
    // Set the adapter
    if (listView is RecyclerView) {
      val context = listView.getContext()
      listView.layoutManager = LinearLayoutManager(context)
      mRecipeList = getRecipes()
      if (mRecipeList.isEmpty()) {
        mInfoTextView.visibility = View.VISIBLE
      }
      mAdapter = RecipeRecyclerViewAdapter(mRecipeList, mListener, context)
      listView.adapter = mAdapter

      // FastScroller
      (rootView.findViewById<View>(R.id.fastscroll) as FastScroller).setRecyclerView(listView)
    }
    return rootView
  }

  override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
    super.onCreateOptionsMenu(menu, inflater)
    menu?.findItem(R.id.action_search)?.isVisible = true
    menu?.findItem(R.id.action_refresh)?.isVisible = true
    val searchView = MenuItemCompat.getActionView(menu?.findItem(R.id.action_search)) as SearchView
    searchView.setOnQueryTextListener(this)
  }

  override fun onResume() {
    super.onResume()
    refresh()
  }

  override fun onDestroy() {
    super.onDestroy()
    context?.let { ctx ->
      LocalBroadcastManager.getInstance(ctx).unregisterReceiver(mBroadcastReceiver)
    }
  }

  override fun onOptionsItemSelected(item: MenuItem?): Boolean {
    val id = item?.itemId
    when (id) {
      R.id.action_refresh -> {
        context?.let { ctx ->
          WebClient(ctx).downloadRecipes()
          return true
        }
      }
    }
    return super.onOptionsItemSelected(item)
  }

  private fun refresh() {
    val list = getRecipes()

    if (list.isEmpty()) {
      mInfoTextView.setText(when {
        mType == Type.HISTORY -> R.string.empty_history
        mType == Type.FAVORITE -> R.string.no_favorite
        mCurrentSearch.isEmpty() -> R.string.downloading_recipes
        else -> R.string.no_search_results
      })
      mInfoTextView.visibility = View.VISIBLE
    } else
      mInfoTextView.visibility = View.GONE

    if (!Util.listEqualsNoOrder(list, mRecipeList)) {
      mRecipeList.clear()
      for (recipe in list)
        mRecipeList.add(recipe)

      mAdapter.notifyDataSetChanged()
    }
  }

  override fun onAttach(context: Context?) {
    super.onAttach(context)
    if (context is OnRecipeClickListener) {
      mListener = context
    } else {
      throw RuntimeException(context.toString() + " must implement OnRecipeClickListener")
    }
  }

  override fun onQueryTextSubmit(query: String): Boolean {
    return false
  }

  override fun onQueryTextChange(newText: String): Boolean {
    mCurrentSearch = newText
    refresh()
    return false
  }

  interface OnRecipeClickListener {
    fun onRecipeClicked(item: Recipe)
  }

  companion object {

    private const val ARG_CATEGORY = "ARG_CATEGORY"
    private const val ARG_TYP = "ARG_TYP"

    fun newInstance(type: Type): RecipeListFragment {
      val fragment = RecipeListFragment()
      val args = Bundle()
      args.putSerializable(ARG_TYP, type)
      fragment.arguments = args
      return fragment
    }

    fun newInstance(categoryID: Int): RecipeListFragment {
      val fragment = RecipeListFragment()
      val args = Bundle()
      args.putInt(ARG_CATEGORY, categoryID)
      args.putSerializable(ARG_TYP, Type.INVALID)
      fragment.arguments = args
      return fragment
    }
  }
}
