package net.ddns.raspi_server.rezeptbuch.ui.recipelist

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.signature.ObjectKey

import com.futuremind.recyclerviewfastscroll.SectionTitleProvider
import net.ddns.raspi_server.rezeptbuch.GlideApp

import net.ddns.raspi_server.rezeptbuch.R
import net.ddns.raspi_server.rezeptbuch.ui.recipelist.RecipeListFragment.OnRecipeClickListener
import net.ddns.raspi_server.rezeptbuch.util.DataStructures.Recipe
import net.ddns.raspi_server.rezeptbuch.util.WebClient
import net.ddns.raspi_server.rezeptbuch.util.db.RecipeDatabase

import java.text.SimpleDateFormat
import java.util.*

/**
 * [RecyclerView.Adapter] that can display a [Recipe] and makes a
 * call to the specified [OnRecipeClickListener].
 */
class RecipeRecyclerViewAdapter(private val mValues: List<Recipe>,
                                private val mListener: RecipeListFragment.OnRecipeClickListener,
                                private val mContext: Context)
  : RecyclerView.Adapter<RecipeRecyclerViewAdapter.ViewHolder>(), SectionTitleProvider {

  private val database: RecipeDatabase = RecipeDatabase(mContext)

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_recipe_list_item, parent, false)
    return ViewHolder(view)
  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    holder.mItem = mValues[position]
    holder.mCategory.text = database.getCategoryById(holder.mItem?.mCategory).mName
    holder.mTitle.text = holder.mItem?.mTitle
    // for making the marquee (auto scroll) work
    holder.mTitle.isSelected = true
    holder.mDescription.text = holder.mItem?.mDescription + "\n\n"

    GlideApp.with(mContext)
            .load(WebClient.getImageUrl(holder.mItem))
            .error(R.drawable.default_recipe_image_high)
            .placeholder(R.drawable.default_recipe_image_low)
            .centerCrop()
            .signature(ObjectKey(holder.mItem?.mDate ?: Date()))
            .into(holder.mImage)

    holder.mView.setOnClickListener {
      mListener.onRecipeClicked(holder.mItem!!)
    }
  }

  override fun getItemCount(): Int {
    return mValues.size
  }

  override fun getSectionTitle(position: Int): String {
    return SimpleDateFormat("MMM yy", Locale.US).format(mValues[position].mDate)
  }

  inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
    var mItem: Recipe? = null
    val mImage: ImageView = mView.findViewById<View>(R.id.image) as ImageView
    val mTitle: TextView = mView.findViewById<View>(R.id.title) as TextView
    val mCategory: TextView = mView.findViewById<View>(R.id.category) as TextView
    val mDescription: TextView = mView.findViewById<View>(R.id.description) as TextView

  }
}
