package net.ddns.raspi_server.rezeptbuch.util;

import java.io.Serializable;
import java.util.Date;

public final class DataStructures {

  public static class Recipe implements Serializable {
    public int _ID;

    @Override
    public boolean equals(Object obj) {
      return ((obj instanceof Recipe) && ((Recipe) obj)._ID == _ID);
    }

    public String mTitle;
    public int mCategory;
    public String mIngredients;
    public String mDescription;
    public String mImageName;
    public Date mDate;

    public Recipe() {
    }

    public Recipe(int _ID, String mTitle, int mCategory, String mIngredients, String
        mDescription, String mImageName, Date mDate) {
      this._ID = _ID;
      this.mTitle = mTitle;
      this.mCategory = mCategory;
      this.mIngredients = mIngredients;
      this.mDescription = mDescription;
      this.mImageName = mImageName;
      this.mDate = mDate;
    }

    @Override
    public String toString() {
      return mTitle;
    }
  }

  public static class Category implements Serializable {
    public int _ID;
    public String mName;

    @Override
    public boolean equals(Object obj) {
      return ((obj instanceof Category) && ((Category) obj)._ID == _ID);
    }

    public Category() {
    }

    public Category(int _ID, String mName) {
      this._ID = _ID;
      this.mName = mName;
    }

    @Override
    public String toString() {
      return mName;
    }
  }
}
