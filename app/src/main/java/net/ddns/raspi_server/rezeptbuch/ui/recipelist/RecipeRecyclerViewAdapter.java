package net.ddns.raspi_server.rezeptbuch.ui.recipelist;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.futuremind.recyclerviewfastscroll.SectionTitleProvider;

import net.ddns.raspi_server.rezeptbuch.R;
import net.ddns.raspi_server.rezeptbuch.ui.images.ImageProcessing;
import net.ddns.raspi_server.rezeptbuch.ui.recipelist.RecipeListFragment.OnRecipeClickListener;
import net.ddns.raspi_server.rezeptbuch.util.DataStructures.Recipe;
import net.ddns.raspi_server.rezeptbuch.util.db.RecipeDatabase;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Recipe} and makes a
 * call to the specified {@link OnRecipeClickListener}.
 */
public class RecipeRecyclerViewAdapter extends RecyclerView
    .Adapter<RecipeRecyclerViewAdapter.ViewHolder> implements SectionTitleProvider {

  private final List<Recipe> mValues;
  private final OnRecipeClickListener mListener;
  private final Context mContext;
  private final RecipeDatabase database;

  public RecipeRecyclerViewAdapter(final List<Recipe> items,
                                   RecipeListFragment.OnRecipeClickListener
                                       listener, Context context) {
    mValues = items;
    mListener = listener;
    this.mContext = context;
    database = new RecipeDatabase(context);
  }

  @Override
  public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext())
        .inflate(R.layout.fragment_recipe_list_item, parent, false);
    return new ViewHolder(view);
  }

  @Override
  public void onBindViewHolder(final ViewHolder holder, int position) {
    holder.mItem = mValues.get(position);
    holder.mCategory.setText(database.getCategoryById(holder.mItem.getMCategory()).getMName());
    holder.mTitle.setText(holder.mItem.getMTitle());
    // for making the marquee (auto scroll) work
    holder.mTitle.setSelected(true);
    holder.mDescription.setText(holder.mItem.getMDescription());
    ImageProcessing.INSTANCE.loadRecipeImage(mContext, holder.mItem, holder.mImage);

    holder.mView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (null != mListener) {
          // Notify the active callbacks interface (the activity, if the
          // fragment is attached to one) that an item has been selected.
          mListener.onRecipeClicked(holder.mItem);
        }
      }
    });
  }

  @Override
  public int getItemCount() {
    return mValues.size();
  }

  @Override
  public String getSectionTitle(int position) {
    return new SimpleDateFormat("MMM yy").format(mValues.get(position).getMDate());
  }

  class ViewHolder extends RecyclerView.ViewHolder {
    final View mView;
    Recipe mItem;
    final ImageView mImage;
    final TextView mTitle;
    final TextView mCategory;
    final TextView mDescription;

    ViewHolder(View view) {
      super(view);
      mView = view;
      mImage = (ImageView) view.findViewById(R.id.image);
      mTitle = (TextView) view.findViewById(R.id.title);
      mCategory = (TextView) view.findViewById(R.id.category);
      mDescription = (TextView) view.findViewById(R.id.description);
    }

  }
}
