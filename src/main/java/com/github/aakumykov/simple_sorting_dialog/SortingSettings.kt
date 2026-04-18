package com.github.aakumykov.simple_sorting_dialog

import com.google.gson.Gson
import java.io.Serializable

data class SortingSettings(
    val sortingMode: SimpleSortingDialog.SortingMode,
    val reverseOrder: Boolean,
    val foldersFirst: Boolean
): Serializable {

    fun toJSON(gson: Gson): String {
        return gson.toJson(this)
    }

    fun toHumanString(): String {
        return toString()
            .replace("("," (\n")
            .replace(")","\n)")
            .replace(", ",",\n")
    }

    companion object {
        fun fromJSON(json: String, gson: Gson): SortingSettings {
            return gson.fromJson(json, SortingSettings::class.java)
        }
    }
}