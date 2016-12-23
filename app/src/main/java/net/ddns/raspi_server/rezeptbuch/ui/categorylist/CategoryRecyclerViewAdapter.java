package net.ddns.raspi_server.rezeptbuch.ui.categorylist;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.ddns.raspi_server.rezeptbuch.R;
import net.ddns.raspi_server.rezeptbuch.util.DataStructures.Recipe;
import net.ddns.raspi_server.rezeptbuch.util.DataStructures.Category;
import net.ddns.raspi_server.rezeptbuch.ui.categorylist.CategoryListFragment.OnCategoryClickListener;
import net.ddns.raspi_server.rezeptbuch.util.db.RecipeDatabase;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Category} and makes
 * a call to the specified {@link OnCategoryClickListener}.
 */
public class CategoryRecyclerViewAdapter extends RecyclerView
    .Adapter<CategoryRecyclerViewAdapter.ViewHolder> {

  private static final String TAG = "CategoryViewAdapter";
  private final List<Category> mValues;
  private final OnCategoryClickListener mListener;
  //  private final Context mContext;
  private final RecipeDatabase mDatabase;

  public CategoryRecyclerViewAdapter(Context context,
                                     OnCategoryClickListener
                                         listener) {
    mListener = listener;
//    mContext = context;
    mDatabase = new RecipeDatabase(context);
    mValues = mDatabase.getCategories();
  }

  @Override
  public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext())
        .inflate(R.layout.fragment_category_list_item, parent, false);
    return new ViewHolder(view);
  }

  @Override
  public void onBindViewHolder(final ViewHolder holder, int position) {
    holder.mCategory = mValues.get(position);
    holder.mName.setText(holder.mCategory.mName);
    List<Recipe> recipes = mDatabase.getRecipesByCategory(holder.mCategory._ID);
    String text = "";
    if (recipes != null)
      for (int i = 0; i < 3; i++)
        if (recipes.size() <= i || recipes.get(i) == null)
          break;
        else
          text += recipes.get(i) + "\n";
    holder.mExample1.setText(text.isEmpty()
        ? ""
        : text.substring(0, text.length() - 2));

    holder.mView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (null != mListener) {
          // Notify the active callbacks interface (the activity, if the
          // fragment is attached to one) that an item has been selected.
          mListener.onCategoryClicked(holder.mCategory);
        }
      }
    });
  }

  @Override
  public int getItemCount() {
    return mValues.size();
  }

  public class ViewHolder extends RecyclerView.ViewHolder {
    final View mView;
    final TextView mName;
    final TextView mExample1;
    Category mCategory;

    public ViewHolder(View view) {
      super(view);
      mView = view;
      mName = (TextView) view.findViewById(R.id.name);
      mExample1 = (TextView) view.findViewById(R.id.example_recipe_1);
    }

    @Override
    public String toString() {
      return super.toString() + " '" + mExample1.getText() + "'";
    }
  }
}
