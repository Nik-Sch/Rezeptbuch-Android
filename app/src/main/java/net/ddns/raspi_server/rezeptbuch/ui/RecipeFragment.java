package net.ddns.raspi_server.rezeptbuch.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import net.ddns.raspi_server.rezeptbuch.R;
import net.ddns.raspi_server.rezeptbuch.ui.images.ImageProcessing;
import net.ddns.raspi_server.rezeptbuch.util.DataStructures;
import net.ddns.raspi_server.rezeptbuch.util.db.RecipeDatabase;

import java.io.Serializable;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link RecipeFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link RecipeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RecipeFragment extends Fragment{
  private static final String ARG_RECIPE = "mRecipe";

  private DataStructures.Recipe mRecipe;

  private OnFragmentInteractionListener mListener;

  public RecipeFragment(){
    // Required empty public constructor
  }

  /**
   * Use this factory method to create a new instance of
   * this fragment using the provided parameters.
   *
   * @param recipe The mRecipe to show
   * @return A new instance of fragment RecipeFragment.
   */
  public static RecipeFragment newInstance(DataStructures.Recipe recipe){
    RecipeFragment fragment = new RecipeFragment();
    Bundle args = new Bundle();
    args.putSerializable(ARG_RECIPE, recipe);
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState){
    super.onCreate(savedInstanceState);
    if (getArguments() != null){
      Serializable s = getArguments().getSerializable(ARG_RECIPE);
      if (s instanceof DataStructures.Recipe)
        mRecipe = (DataStructures.Recipe) s;
      else
        throw new RuntimeException("Serializable instance of wrong class");
    }
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState){
    // Inflate the layout for this fragment
    View view = inflater.inflate(R.layout.content_recipe, container, false);
    ((TextView) view.findViewById(R.id.title)).setText(mRecipe.title);
    ((TextView) view.findViewById(R.id.category)).setText(new RecipeDatabase(getContext())
            .getCategoryById(mRecipe.category).name);
    ((TextView) view.findViewById(R.id.ingredients)).setText(mRecipe.ingredients);
    ((TextView) view.findViewById(R.id.description)).setText(mRecipe.description);

    ImageProcessing.loadRecipeImage(getContext(), mRecipe, (ImageView) view
            .findViewById(R.id.image));
    return view;
  }

  @Override
  public void onAttach(Context context){
    super.onAttach(context);
//    if (context instanceof OnFragmentInteractionListener){
//      mListener = (OnFragmentInteractionListener) context;
//    }else{
//      throw new RuntimeException(context.toString()
//              + " must implement OnFragmentInteractionListener");
//    }
  }

  @Override
  public void onDetach(){
    super.onDetach();
    mListener = null;
  }

  /**
   * This interface must be implemented by activities that contain this
   * fragment to allow an interaction in this fragment to be communicated
   * to the activity and potentially other fragments contained in that
   * activity.
   * <p>
   * See the Android Training lesson <a href=
   * "http://developer.android.com/training/basics/fragments/communicating.html"
   * >Communicating with Other Fragments</a> for more information.
   */
  public interface OnFragmentInteractionListener{
//    void onFragmentInteraction(Uri uri);
  }
}
