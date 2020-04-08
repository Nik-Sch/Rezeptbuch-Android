package de.niklas_schelten.rezeptbuch.ui.header

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView

import de.niklas_schelten.rezeptbuch.R


class HeaderView : LinearLayout {
  internal var title: TextView? = null
  internal var subtitle: TextView? = null

  constructor(context: Context) : super(context)

  constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

  constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

  override fun onFinishInflate() {
    super.onFinishInflate()
    title = findViewById<View>(R.id.title) as TextView?
    subtitle = findViewById<View>(R.id.subtitle) as TextView?
  }

  fun bindTo(title: String, subtitle: String) {
    this.title?.text = title
    this.subtitle?.text = subtitle
  }

  fun setTextSize(size: Float) {
    title?.setTextSize(TypedValue.COMPLEX_UNIT_PX, size)
  }
}
