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
package net.ddns.raspi_server.rezeptbuch.ui.images;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

public class RecipeImageView extends ImageView{
  public RecipeImageView(Context context){
    super(context);
  }

  public RecipeImageView(Context context, AttributeSet attrs){
    super(context, attrs);
  }

  public RecipeImageView(Context context, AttributeSet attrs, int defStyleAttr){
    super(context, attrs, defStyleAttr);
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    int width = getMeasuredWidth();
    int height = width * 9 / 16;
    setMeasuredDimension(width, height);
  }
}
