/*
 * Copyright 2016 Niklas Schelten
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.ddns.raspi_server.rezeptbuch.ui.header;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.ddns.raspi_server.rezeptbuch.R;
import net.ddns.raspi_server.rezeptbuch.ui.recipelist.RecipeListFragment;
import net.ddns.raspi_server.rezeptbuch.util.DataStructures;

import java.util.List;

@SuppressWarnings("ALL")
public class SimpleAdapter extends RecyclerView.Adapter<SimpleAdapter.ViewHolder>{

  private final List<DataStructures.Recipe> mValues;
  private final RecipeListFragment.OnListFragmentInteractionListener mListener;

  public SimpleAdapter(List<DataStructures.Recipe> items, RecipeListFragment.OnListFragmentInteractionListener
          listener, Context context) {
    mValues = items;
    mListener = listener;
  }

  @Override
  public SimpleAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.content_recipe, parent, false);
    return new SimpleAdapter.ViewHolder(view);
  }

  @Override
  public void onBindViewHolder(final SimpleAdapter.ViewHolder holder, int position) {
    holder.mItem = mValues.get(position);
    holder.mDescription.setText(holder.mItem.mDescription);
    holder.mIngredients.setText(holder.mItem.mIngredients);

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

  class ViewHolder extends RecyclerView.ViewHolder {
    final View mView;
    DataStructures.Recipe mItem;
    final TextView mIngredients;
    final TextView mDescription;

    ViewHolder(View view) {
      super(view);
      mView = view;
      mIngredients = (TextView) view.findViewById(R.id.ingredients);
      mDescription = (TextView) view.findViewById(R.id.description);
    }

  }
}
