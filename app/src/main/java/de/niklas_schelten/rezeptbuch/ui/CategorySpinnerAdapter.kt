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
package de.niklas_schelten.rezeptbuch.ui

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

import de.niklas_schelten.rezeptbuch.util.DataStructures.Category

/**
 * Is basically an ArrayAdapter but hides the first item from the drop down.
 */
class CategorySpinnerAdapter : ArrayAdapter<Category> {


  constructor(context: Context, resource: Int) : super(context, resource)

  constructor(context: Context, resource: Int, textViewResourceId: Int) : super(context, resource, textViewResourceId)

  constructor(context: Context, resource: Int, objects: Array<Category>) : super(context, resource, objects)

  constructor(context: Context, resource: Int, textViewResourceId: Int, objects: Array<Category>) : super(context, resource, textViewResourceId, objects)

  constructor(context: Context, resource: Int, objects: List<Category>) : super(context, resource, objects)

  constructor(context: Context, resource: Int, textViewResourceId: Int, objects: List<Category>) : super(context, resource, textViewResourceId, objects)

  override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
    return if (position == 0) {
      val v = TextView(context)
      v.visibility = View.GONE
      v
    } else
      super.getDropDownView(position, null, parent)
  }
}
