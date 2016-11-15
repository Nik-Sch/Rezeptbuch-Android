package net.ddns.raspi_server.rezeptbuch.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import net.ddns.raspi_server.rezeptbuch.R;
import net.ddns.raspi_server.rezeptbuch.ui.images.ImageProcessing;
import net.ddns.raspi_server.rezeptbuch.util.DataStructures;
import net.ddns.raspi_server.rezeptbuch.util.db.RecipeDatabase;

import java.io.Serializable;
import java.util.List;

public class CreateRecipe extends AppCompatActivity{

  public static final String ARG_RECIPE = "mRecipe";

  private DataStructures.Recipe mRecipe;
  private List<DataStructures.Category> mCategories;
  private Spinner mSpinner;

  @Override
  protected void onCreate(Bundle savedInstanceState){
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_create_recipe);

    ArrayAdapter<DataStructures.Category> adapter = new CategorySpinnerAdapter(this, android.R
            .layout.simple_spinner_item, android.R.id.text1);
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    mSpinner = ((Spinner) findViewById(R.id.category));
    mSpinner.setAdapter(adapter);
    RecipeDatabase database = new RecipeDatabase(this);
    mCategories = database.getCategories();
    mCategories.add(0, new DataStructures.Category(-2, getResources()
            .getString(R.string.spinner_choose_category)));
    mCategories.add(1, new DataStructures.Category(-1, getResources()
            .getString(R.string.spinner_add_category)));

    for (DataStructures.Category category : mCategories)
      adapter.add(category);

    adapter.notifyDataSetChanged();
    Bundle bundle = getIntent().getExtras();
    if (bundle != null && bundle.containsKey(ARG_RECIPE)){
      Serializable s = bundle.getSerializable(ARG_RECIPE);
      if (s instanceof DataStructures.Recipe){
        mRecipe = (DataStructures.Recipe) s;
        ((EditText) findViewById(R.id.title)).setText(mRecipe.title);
        ((EditText) findViewById(R.id.ingredients)).setText(mRecipe.ingredients);
        ((EditText) findViewById(R.id.description)).setText(mRecipe.description);
        ImageProcessing.loadRecipeImage(this, mRecipe, (ImageView)
                findViewById(R.id.add_image));
        int i = 0;
        // using for each loop because it might be faster?
        for (DataStructures.Category category : mCategories){
          if (category.id == mRecipe.category){
            mSpinner.setSelection(i);
            break;
          }
          i++;
        }
      }
    }
    if (mSpinner.getSelectedItemPosition() == 0)
      mSpinner.setSelection(-1);
  }
}
