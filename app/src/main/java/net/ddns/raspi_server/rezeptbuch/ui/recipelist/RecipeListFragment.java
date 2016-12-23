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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.ddns.raspi_server.rezeptbuch.R;
import net.ddns.raspi_server.rezeptbuch.util.DataStructures.Recipe;
import net.ddns.raspi_server.rezeptbuch.util.WebClient;
import net.ddns.raspi_server.rezeptbuch.util.db.RecipeDatabase;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the
 * {@link OnListFragmentInteractionListener}
 * interface.
 */
public class RecipeListFragment extends Fragment {

  private static final String ARG_SEARCH = "ARG_SEARCH";
  private OnListFragmentInteractionListener mListener;

  private List<Recipe> mRecipeList;
  private RecyclerView.Adapter mAdapter;
  private TextView mMessageDownloadingView;
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
      } else if (!mRecipeList.isEmpty()){
        mNotificationView.setText(R.string.notification_failed_update);
        mNotificationView.setBackgroundColor(getResources().getColor(android.R
            .color.holo_red_dark));
      } else {
        showNotification = false;
        mMessageDownloadingView.setText(R.string.notification_failed_download);
        mMessageDownloadingView.setTextColor(getResources().getColor(android
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

  /**
   * Mandatory empty constructor for the fragment manager to instantiate the
   * fragment (e.g. upon screen orientation changes).
   */
  public RecipeListFragment() {
  }

  public static RecipeListFragment newInstance() {
    RecipeListFragment fragment = new RecipeListFragment();
    Bundle args = new Bundle();
    fragment.setArguments(args);
    return fragment;
  }

  public static RecipeListFragment newInstance(String search) {
    RecipeListFragment fragment = new RecipeListFragment();
    Bundle args = new Bundle();
    args.putString(ARG_SEARCH, search);
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setHasOptionsMenu(true);

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
    mMessageDownloadingView = (TextView) rootView.findViewById(R.id
        .message_downloading);
    mNotificationView = (TextView) rootView.findViewById(R.id.notification);
    // Set the adapter
    if (listView instanceof RecyclerView) {
      Context context = listView.getContext();
      RecyclerView recyclerView = (RecyclerView) listView;
      recyclerView.setLayoutManager(new LinearLayoutManager(context));
      RecipeDatabase database = new RecipeDatabase(context);
      mRecipeList = getArguments().containsKey(ARG_SEARCH)
          ? database.getRecipesBySearch(getArguments().getString(ARG_SEARCH))
          : database.getRecipes();
      if (mRecipeList.isEmpty()) {
        mMessageDownloadingView.setVisibility(View.VISIBLE);
      }
      mAdapter = new RecipeRecyclerViewAdapter(mRecipeList, mListener, context);
      recyclerView.setAdapter(mAdapter);
    }
    return rootView;
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
    List<Recipe> list = getArguments().containsKey(ARG_SEARCH)
        ? new RecipeDatabase(getContext()).getRecipesBySearch
        (getArguments().getString(ARG_SEARCH))
        : new RecipeDatabase(getContext()).getRecipes();

    mMessageDownloadingView.setVisibility(list.isEmpty()
        ? View.VISIBLE : View.GONE);

    mRecipeList.clear();
    for (Recipe recipe : list)
      mRecipeList.add(recipe);
    mAdapter.notifyDataSetChanged();
  }

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
    if (context instanceof OnListFragmentInteractionListener) {
      mListener = (OnListFragmentInteractionListener) context;
    } else {
      throw new RuntimeException(context.toString()
          + " must implement OnListFragmentInteractionListener");
    }
  }

  @Override
  public void onDetach() {
    super.onDetach();
    mListener = null;
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
  public interface OnListFragmentInteractionListener {
    void onRecipeClicked(Recipe item);
  }
}