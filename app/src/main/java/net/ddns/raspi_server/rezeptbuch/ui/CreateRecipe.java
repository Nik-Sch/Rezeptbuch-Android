package net.ddns.raspi_server.rezeptbuch.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
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

public class CreateRecipe extends AppCompatActivity {

  public static final String ARG_RECIPE = "mRecipe";

  private Menu mMenu;
  private DataStructures.Recipe mRecipe;
  private List<DataStructures.Category> mCategories;
  private Spinner mSpinner;
  private EditText mTitleEdit;
  private EditText mIngredientsEdit;
  private EditText mDescriptionEdit;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_create_recipe);

    setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    mTitleEdit = (EditText) findViewById(R.id.title);
    mIngredientsEdit = (EditText) findViewById(R.id.ingredients);
    mDescriptionEdit = (EditText) findViewById(R.id.description);
    mSpinner = ((Spinner) findViewById(R.id.category));

    ChangeListener changeListener = new ChangeListener();
    mTitleEdit.addTextChangedListener(changeListener);
    mIngredientsEdit.addTextChangedListener(changeListener);
    mDescriptionEdit.addTextChangedListener(changeListener);
    mSpinner.setOnItemSelectedListener(changeListener);

    // fill the spinner
    ArrayAdapter<DataStructures.Category> adapter = new CategorySpinnerAdapter(this, android.R
        .layout.simple_spinner_item, android.R.id.text1);
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
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
    if (bundle != null && bundle.containsKey(ARG_RECIPE)) {
      Serializable s = bundle.getSerializable(ARG_RECIPE);
      if (s instanceof DataStructures.Recipe) {
        mRecipe = (DataStructures.Recipe) s;
        mTitleEdit.setText(mRecipe.title);
        mIngredientsEdit.setText(mRecipe.ingredients);
        mDescriptionEdit.setText(mRecipe.description);
        ImageProcessing.loadRecipeImage(this, mRecipe, (ImageView)
            findViewById(R.id.add_image));
        int i = 0;
        // using for each loop because it might be faster?
        for (DataStructures.Category category : mCategories) {
          if (category.id == mRecipe.category) {
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

  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    menu.getItem(0).setEnabled(false);
    return super.onPrepareOptionsMenu(menu);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    mMenu = menu;
    getMenuInflater().inflate(R.menu.create, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        onBackPressed();
        break;
      case R.id.action_save:
    }
    return super.onOptionsItemSelected(item);
  }

  private void updateSaveItem() {
    mMenu.getItem(0).setEnabled(
        mSpinner.getSelectedItemPosition() > 1 &&
            !mTitleEdit.getText().toString().isEmpty() &&
            !mIngredientsEdit.getText().toString().isEmpty() &&
            !mDescriptionEdit.getText().toString().isEmpty());
  }

  private class ChangeListener implements TextWatcher, AdapterView
  .OnItemSelectedListener {

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void afterTextChanged(Editable editable) {
      CreateRecipe.this.updateSaveItem();
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
      CreateRecipe.this.updateSaveItem();
      // TODO: Also do something when selecting 'create new recipe'
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
  }
}
