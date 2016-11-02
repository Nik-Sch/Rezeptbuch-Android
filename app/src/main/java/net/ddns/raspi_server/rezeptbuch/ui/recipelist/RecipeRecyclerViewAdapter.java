package net.ddns.raspi_server.rezeptbuch.ui.recipelist;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import net.ddns.raspi_server.rezeptbuch.R;
import net.ddns.raspi_server.rezeptbuch.ui.images.ImageProcessing;
import net.ddns.raspi_server.rezeptbuch.ui.recipelist.RecipeListFragment.OnListFragmentInteractionListener;
import net.ddns.raspi_server.rezeptbuch.util.DataStructures.Recipe;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Recipe} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 */
public class RecipeRecyclerViewAdapter extends RecyclerView.Adapter<RecipeRecyclerViewAdapter.ViewHolder> {

  private final List<Recipe> mValues;
  private final OnListFragmentInteractionListener mListener;
  private final Context context;

  public RecipeRecyclerViewAdapter(List<Recipe> items, OnListFragmentInteractionListener
          listener, Context context) {
    mValues = items;
    mListener = listener;
    this.context = context;
  }

  @Override
  public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.fragment_recipe, parent, false);
    return new ViewHolder(view);
  }

  @Override
  public void onBindViewHolder(final ViewHolder holder, int position) {
    holder.mItem = mValues.get(position);
    holder.mCategory.setText(holder.mItem.category);
    holder.mTitle.setText(holder.mItem.title);
    new ImageProcessing(context).loadRecipeImage(holder.mItem, holder.mImage);

    holder.mView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (null != mListener) {
          // Notify the active callbacks interface (the activity, if the
          // fragment is attached to one) that an item has been selected.
          mListener.onListFragmentInteraction(holder.mItem);
        }
      }
    });
  }

  @Override
  public int getItemCount() {
    return mValues.size();
  }

  class ViewHolder extends RecyclerView.ViewHolder {
    View mView;
    Recipe mItem;
    ImageView mImage;
    TextView mTitle;
    TextView mCategory;

    ViewHolder(View view) {
      super(view);
      mView = view;
      mImage = (ImageView) view.findViewById(R.id.image);
      mTitle = (TextView) view.findViewById(R.id.title);
      mCategory = (TextView) view.findViewById(R.id.category);
    }

    @Override
    public String toString() {
      return super.toString();
    }
  }
}
