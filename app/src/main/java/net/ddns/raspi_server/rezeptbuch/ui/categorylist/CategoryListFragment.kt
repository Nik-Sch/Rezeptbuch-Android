package net.ddns.raspi_server.rezeptbuch.ui.categorylist

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup

import net.ddns.raspi_server.rezeptbuch.R
import net.ddns.raspi_server.rezeptbuch.util.DataStructures.Category

/**
 * A fragment representing a list of Items.
 *
 *
 * Activities containing this fragment MUST implement the
 * [OnCategoryClickListener] interface.
 */
/**
 * Mandatory empty constructor for the fragment manager to instantiate the
 * fragment (e.g. upon screen orientation changes).
 */
class CategoryListFragment : Fragment() {

  private lateinit var mListener: OnCategoryClickListener

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    val view = inflater.inflate(R.layout.fragment_category_list, container, false)

    // Set the adapter
    if (view is RecyclerView) {
      view.layoutManager = LinearLayoutManager(view.getContext())
      view.adapter = CategoryRecyclerViewAdapter(context!!, mListener)
    }
    return view
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setHasOptionsMenu(true)
  }

  override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
    super.onCreateOptionsMenu(menu, inflater)
    menu?.findItem(R.id.action_search)?.isVisible = false
    menu?.findItem(R.id.action_refresh)?.isVisible = false
  }

  override fun onAttach(context: Context?) {
    super.onAttach(context)
    if (context is OnCategoryClickListener) {
      mListener = context
    } else {
      throw RuntimeException(context.toString() + " must implement OnRecipeClickListener")
    }
  }

  interface OnCategoryClickListener {
    fun onCategoryClicked(category: Category)
  }

  companion object {

    fun newInstance(): CategoryListFragment {
      val fragment = CategoryListFragment()
      val args = Bundle()
      fragment.arguments = args
      return fragment
    }
  }
}
