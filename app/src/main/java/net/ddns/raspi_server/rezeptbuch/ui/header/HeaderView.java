package net.ddns.raspi_server.rezeptbuch.ui.header;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.ddns.raspi_server.rezeptbuch.R;


public class HeaderView extends LinearLayout {
  TextView title;
  TextView subtitle;

  public HeaderView(Context context) {
    super(context);
  }

  public HeaderView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public HeaderView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  public HeaderView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
  }

  @Override
  protected void onFinishInflate() {
    super.onFinishInflate();
    title = (TextView) findViewById(R.id.title);
    subtitle = (TextView) findViewById(R.id.subtitle);
  }

  public void bindTo(String title, String subtitle) {
    this.title.setText(title);
    this.subtitle.setText(subtitle);
  }

  public void setTextSize(float size) {
    title.setTextSize(TypedValue.COMPLEX_UNIT_PX, size);
  }
}
