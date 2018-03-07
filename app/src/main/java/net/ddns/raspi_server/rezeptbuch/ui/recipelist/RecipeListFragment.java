package net.ddns.raspi_server.rezeptbuch.ui.recipelist;

import android.animation.Animator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.futuremind.recyclerviewfastscroll.FastScroller;

import net.ddns.raspi_server.rezeptbuch.R;
import net.ddns.raspi_server.rezeptbuch.util.DataStructures.Recipe;
import net.ddns.raspi_server.rezeptbuch.util.Favorite;
import net.ddns.raspi_server.rezeptbuch.util.History;
import net.ddns.raspi_server.rezeptbuch.util.Util;
import net.ddns.raspi_server.rezeptbuch.util.WebClient;
import net.ddns.raspi_server.rezeptbuch.util.db.RecipeDatabase;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the
 * {@link OnRecipeClickListener}
 * interface.
 */
public class RecipeListFragment extends Fragment implements SearchView
    .OnQueryTextListener {

  public enum Typ {
    ALL, HISTORY, INVALID, FAVORITE
  }

  private static final String ARG_CATEGORY = "ARG_CATEGORY";
  private static final String ARG_TYP = "ARG_TYP";
  private OnRecipeClickListener mListener;

  private List<Recipe> mRecipeList;
  private RecyclerView.Adapter mAdapter;
  private TextView mInfoTextView;
  private TextView mNotificationView;
  private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      boolean success = intent.getBooleanExtra(WebClient
          .ARG_BROADCAST_DOWNLOAD_FINISHED_SUCCESS, false);
      boolean showNotification = true;
      DateFormat dateFormat = android.text.format.DateFormat.getTimeFormat
          (getContext());
      if (success) {
        mNotificationView.setText(String.format(
            context.getResources().getString(R.string.notification_updated),
            dateFormat.format(new Date())));
        mNotificationView.setBackgroundColor(getResources().getColor(android.R
            .color.darker_gray));
        refresh();
      } else if (!mRecipeList.isEmpty()) {
        mNotificationView.setText(R.string.notification_failed_update);
        mNotificationView.setBackgroundColor(getResources().getColor(android.R
            .color.holo_red_dark));
      } else {
        showNotification = false;
        mInfoTextView.setText(R.string.notification_failed_download);
        mInfoTextView.setTextColor(getResources().getColor(android
            .R.color.holo_red_dark));
      }

      if (showNotification) {
        mNotificationView.setTranslationY(-mNotificationView.getHeight());
        mNotificationView.setVisibility(View.VISIBLE);
        mNotificationView.animate()
            .translationY(0)
            .setListener(new Animator.AnimatorListener() {
              @Override
              public void onAnimationStart(Animator animation) {

              }

              @Override
              public void onAnimationEnd(Animator animation) {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                  @Override
                  public void run() {
                    mNotificationView.animate()
                        .translationY(-mNotificationView.getHeight())
                        .setListener(new Animator.AnimatorListener() {
                          @Override
                          public void onAnimationStart(Animator animation) {

                          }

                          @Override
                          public void onAnimationEnd(Animator animation) {
                            mNotificationView.setVisibility(View.GONE);
                          }

                          @Override
                          public void onAnimationCancel(Animator animation) {

                          }

                          @Override
                          public void onAnimationRepeat(Animator animation) {

                          }
                        });
                  }
                }, 3000);
              }

              @Override
              public void onAnimationCancel(Animator animation) {

              }

              @Override
              public void onAnimationRepeat(Animator animation) {

              }
            });
      }
    }
  };
  private String mCurrentSearch = "";
  private Typ mTyp;

  /**
   * Mandatory empty constructor for the fragment manager to instantiate the
   * fragment (e.g. upon screen orientation changes).
   */
  public RecipeListFragment() {
  }

  public static RecipeListFragment newInstance(Typ typ) {
    RecipeListFragment fragment = new RecipeListFragment();
    Bundle args = new Bundle();
    args.putSerializable(ARG_TYP, typ);
    fragment.setArguments(args);
    return fragment;
  }

  public static RecipeListFragment newInstance(int categoryID) {
    RecipeListFragment fragment = new RecipeListFragment();
    Bundle args = new Bundle();
    args.putInt(ARG_CATEGORY, categoryID);
    args.putSerializable(ARG_TYP, Typ.INVALID);
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setHasOptionsMenu(true);

    mTyp = (Typ) getArguments().getSerializable(ARG_TYP);

    LocalBroadcastManager.getInstance(getContext()).registerReceiver
        (mBroadcastReceiver, new IntentFilter(WebClient
            .EVENT_BROADCAST_DOWNLOAD_FINISHED));
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    LocalBroadcastManager.getInstance(getContext()).unregisterReceiver
        (mBroadcastReceiver);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.fragment_recipe_list,
        container, false);
    View listView = rootView.findViewById(R.id.list);
    mInfoTextView = (TextView) rootView.findViewById(R.id
        .info_text);
    mNotificationView = (TextView) rootView.findViewById(R.id.notification);
    // Set the adapter
    if (listView instanceof RecyclerView) {
      Context context = listView.getContext();
      RecyclerView recyclerView = (RecyclerView) listView;
      recyclerView.setLayoutManager(new LinearLayoutManager(context));
      mRecipeList = getRecipes();
      if (mRecipeList.isEmpty()) {
        mInfoTextView.setVisibility(View.VISIBLE);
      }
      mAdapter = new RecipeRecyclerViewAdapter(mRecipeList, mListener, context);
      recyclerView.setAdapter(mAdapter);

      // FastScroller
      ((FastScroller) rootView.findViewById(R.id.fastscroll)).setRecyclerView(recyclerView);
    }
    return rootView;
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);
    menu.findItem(R.id.action_search).setVisible(true);
    menu.findItem(R.id.action_refresh).setVisible(true);
    SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu
        .findItem(R.id.action_search));
    searchView.setOnQueryTextListener(this);
  }

  @Override
  public void onResume() {
    super.onResume();
    refresh();
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();
    switch (id) {
      case R.id.action_refresh:
        new WebClient(getContext()).downloadRecipes();
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

  private void refresh() {
    List<Recipe> list = getRecipes();

    if (list == null || list.isEmpty()) {
      mInfoTextView.setText(mTyp == Typ.HISTORY

          ? R.string.empty_history
          : mTyp == Typ.FAVORITE

          ? R.string.no_favorite
          :(mCurrentSearch == null || mCurrentSearch.isEmpty())

          ? R.string.downloading_recipes
          : R.string.no_search_results);
      mInfoTextView.setVisibility(View.VISIBLE);
    } else
      mInfoTextView.setVisibility(View.GONE);

    if (!Util.INSTANCE.listEqualsNoOrder(list, mRecipeList)) {
      mRecipeList.clear();
      if (list != null)
        for (Recipe recipe : list)
          mRecipeList.add(recipe);

      mAdapter.notifyDataSetChanged();
    }
  }

  private List<Recipe> getRecipes() {
    /* get either:
     * - all recipes
     * - recipes by search string
     * - recipes by category
     * - recipes by favorite
     */
    RecipeDatabase database = new RecipeDatabase(getContext());
    return (mCurrentSearch != null && !mCurrentSearch.isEmpty())
        ? database.getRecipesBySearch(mCurrentSearch)
        : getArguments().containsKey(ARG_CATEGORY)

        ? database.getRecipesByCategory(getArguments().getInt(ARG_CATEGORY))
        : mTyp == Typ.HISTORY

        ? History.INSTANCE.getRecipes()
        : mTyp == Typ.FAVORITE

        ? Favorite.INSTANCE.getRecipes()
        : database.getRecipes();
  }

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
    if (context instanceof OnRecipeClickListener) {
      mListener = (OnRecipeClickListener) context;
    } else {
      throw new RuntimeException(context.toString()
          + " must implement OnRecipeClickListener");
    }
  }

  @Override
  public void onDetach() {
    super.onDetach();
    mListener = null;
  }

  @Override
  public boolean onQueryTextSubmit(String query) {
    return false;
  }

  @Override
  public boolean onQueryTextChange(String newText) {
    mCurrentSearch = newText;
    refresh();
    return false;
  }

  /**
   * This interface must be implemented by activities that contain this
   * fragment to allow an interaction in this fragment to be communicated
   * to the activity and potentially other fragments contained in that
   * activity.
   * <p/>
   * See the Android Training lesson <a href=
   * "http://developer.android.com/training/basics/fragments/communicating.html"
   * >Communicating with Other Fragments</a> for more information.
   */
  public interface OnRecipeClickListener {
    void onRecipeClicked(Recipe item);
  }
}
