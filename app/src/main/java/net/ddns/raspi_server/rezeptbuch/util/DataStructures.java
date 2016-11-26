package net.ddns.raspi_server.rezeptbuch.util;

import java.io.Serializable;
import java.util.Date;

public final class DataStructures {

  public static class Recipe implements Serializable {
    public int mId;
    public String mTitle;
    public int mCategory;
    public String mIngredients;
    public String mDescription;
    public String mImageName;
    public Date mDate;

    public Recipe() {
    }

    public Recipe(int mId, String mTitle, int mCategory, String mIngredients, String
        mDescription, String mImageName, Date mDate) {
      this.mId = mId;
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
    public int mId;
    public String mName;

    public Category() {
    }

    public Category(int mId, String mName) {
      this.mId = mId;
      this.mName = mName;
    }

    @Override
    public String toString() {
      return mName;
    }
  }
}
