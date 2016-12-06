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
package net.ddns.raspi_server.rezeptbuch.ui;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import net.ddns.raspi_server.rezeptbuch.util.DataStructures.Category;

import java.util.List;

/**
 * Is basically an ArrayAdapter but hides the first item from the drop down.
 */
public class CategorySpinnerAdapter extends ArrayAdapter<Category> {


  public CategorySpinnerAdapter(Context context, int resource) {
    super(context, resource);
  }

  public CategorySpinnerAdapter(Context context, int resource, int textViewResourceId) {
    super(context, resource, textViewResourceId);
  }

  public CategorySpinnerAdapter(Context context, int resource, Category[] objects) {
    super(context, resource, objects);
  }

  public CategorySpinnerAdapter(Context context, int resource, int textViewResourceId, Category[] objects) {
    super(context, resource, textViewResourceId, objects);
  }

  public CategorySpinnerAdapter(Context context, int resource, List<Category> objects) {
    super(context, resource, objects);
  }

  public CategorySpinnerAdapter(Context context, int resource, int textViewResourceId, List<Category> objects) {
    super(context, resource, textViewResourceId, objects);
  }

  @Override
  public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
    if (position == 0) {
      View v = new TextView(getContext());
      v.setVisibility(View.GONE);
      return v;
    } else
      return super.getDropDownView(position, null, parent);
  }
}
