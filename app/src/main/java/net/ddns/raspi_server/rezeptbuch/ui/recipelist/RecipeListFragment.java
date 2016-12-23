package net.ddns.raspi_server.rezeptbuch.ui.recipelist;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import net.ddns.raspi_server.rezeptbuch.R;
import net.ddns.raspi_server.rezeptbuch.util.DataStructures.Recipe;
import net.ddns.raspi_server.rezeptbuch.util.db.RecipeDatabase;

import java.util.List;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class RecipeListFragment extends Fragment {

  private static final String ARG_SEARCH = "ARG_SEARCH";
  private OnListFragmentInteractionListener mListener;

  private List<Recipe> mRecipeList;
  private RecyclerView.Adapter mAdapter;

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
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_recipe_list, container, false);

    // Set the adapter
    if (view instanceof RecyclerView) {
      Context context = view.getContext();
      RecyclerView recyclerView = (RecyclerView) view;
      recyclerView.setLayoutManager(new LinearLayoutManager(context));
      RecipeDatabase database = new RecipeDatabase(context);
      mRecipeList = getArguments().containsKey(ARG_SEARCH)
          ? database.getRecipesBySearch(getArguments().getString(ARG_SEARCH))
          : database.getRecipes();
      mAdapter = new RecipeRecyclerViewAdapter(mRecipeList, mListener, context);
      recyclerView.setAdapter(mAdapter);
    }
    return view;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();
    switch (id) {
      case R.id.action_refresh:
        // TODO: isn't quite working, though
        List<Recipe> list = new RecipeDatabase(getContext()).getRecipes();
        mRecipeList.clear();
        for (Recipe recipe : list)
          mRecipeList.add(recipe);
        mAdapter.notifyDataSetChanged();
        return true;
    }
    return false;
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