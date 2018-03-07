package net.ddns.raspi_server.rezeptbuch.util

import java.io.Serializable
import java.util.Date

class DataStructures {

  class Recipe : Serializable {

    var _ID: Int = 0
    var mTitle = "INVALID"
    var mCategory = -1
    var mIngredients = "INVALID"
    var mDescription = "INVALID"
    var mImageName = "INVALID"
    var mDate = Date(0)

    override fun equals(other: Any?): Boolean {
      return other is Recipe && other._ID == _ID
    }

    constructor()

    constructor(_ID: Int, mTitle: String, mCategory: Int, mIngredients: String, mDescription:
    String, mImageName: String, mDate: Date) {
      this._ID = _ID
      this.mTitle = mTitle
      this.mCategory = mCategory
      this.mIngredients = mIngredients
      this.mDescription = mDescription
      this.mImageName = mImageName
      this.mDate = mDate
    }

    override fun toString(): String {
      return mTitle
    }

    /**
     * auto-generated
     */
    override fun hashCode(): Int {
      var result = _ID
      result = 31 * result + mTitle.hashCode()
      result = 31 * result + mCategory
      result = 31 * result + mIngredients.hashCode()
      result = 31 * result + mDescription.hashCode()
      result = 31 * result + mImageName.hashCode()
      result = 31 * result + mDate.hashCode()
      return result
    }
  }

  class Category : Serializable {
    var _ID = -1
    var mName = "INVALID"

    override fun equals(other: Any?): Boolean {
      return other is Category && other._ID == _ID
    }

    constructor()

    constructor(_ID: Int, mName: String) {
      this._ID = _ID
      this.mName = mName
    }

    override fun toString(): String {
      return mName
    }

    /**
     * auto-generated
     */
    override fun hashCode(): Int {
      var result = _ID
      result = 31 * result + mName.hashCode()
      return result
    }
  }
}
