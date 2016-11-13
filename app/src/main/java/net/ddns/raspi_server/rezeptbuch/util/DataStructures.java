package net.ddns.raspi_server.rezeptbuch.util;

import java.io.Serializable;
import java.util.Date;

public final class DataStructures {

  public static class Recipe implements Serializable {
    public int id;
    public String title;
    public int category;
    public String ingredients;
    public String description;
    public String imageName;
    public Date date;

    public Recipe() {
    }

    public Recipe(int id, String title, int category, String ingredients, String
        description, String imageName, Date date) {
      this.id = id;
      this.title = title;
      this.category = category;
      this.ingredients = ingredients;
      this.description = description;
      this.imageName = imageName;
      this.date = date;
    }
  }

  public static class Category implements Serializable {
    public int id;
    public String name;

    public Category() {
    }

    public Category(int id, String name) {
      this.id = id;
      this.name = name;
    }
  }
}
