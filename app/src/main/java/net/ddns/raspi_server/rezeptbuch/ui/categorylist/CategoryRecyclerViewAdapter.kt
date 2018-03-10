package net.ddns.raspi_server.rezeptbuch.ui.categorylist

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import net.ddns.raspi_server.rezeptbuch.R
import net.ddns.raspi_server.rezeptbuch.util.DataStructures.Recipe
import net.ddns.raspi_server.rezeptbuch.util.DataStructures.Category
import net.ddns.raspi_server.rezeptbuch.ui.categorylist.CategoryListFragment.OnCategoryClickListener
import net.ddns.raspi_server.rezeptbuch.util.db.RecipeDatabase

/**
 * [RecyclerView.Adapter] that can display a [Category] and makes
 * a call to the specified [OnCategoryClickListener].
 */
class CategoryRecyclerViewAdapter(context: Context, private val mListener: OnCategoryClickListener?)
  : RecyclerView.Adapter<CategoryRecyclerViewAdapter.ViewHolder>() {

  private val mDatabase: RecipeDatabase = RecipeDatabase(context)
  private val mValues: List<Category> = mDatabase.categories

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_category_list_item, parent, false)
    return ViewHolder(view)
  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    holder.mCategory = mValues[position]
    holder.mName.text = holder.mCategory?.mName
    val recipes = mDatabase.getRecipesByCategory(holder.mCategory?._ID)
    var text = ""
    if (recipes != null)
      for (i in 0..2)
        if (recipes.size <= i || recipes[i] == null)
          break
        else
          text += recipes[i].toString() + "\n"
    holder.mExample1.text =
            if (text.isEmpty())
              ""
            else
              text.substring(0, text.length - 2)

    holder.mView.setOnClickListener {
      mListener?.onCategoryClicked(holder.mCategory!!)
    }
  }

  override fun getItemCount(): Int {
    return mValues.size
  }

  inner class ViewHolder(internal val mView: View) : RecyclerView.ViewHolder(mView) {
    internal var mCategory: Category? = null
    internal val mName: TextView = mView.findViewById<View>(R.id.name) as TextView
    internal val mExample1: TextView = mView.findViewById<View>(R.id.example_recipe_1) as TextView

    override fun toString(): String {
      return super.toString() + " '" + mExample1.text + "'"
    }
  }
}
