package com.github.aakumykov.simple_sorting_dialog

import java.io.Serializable

data class SortingSettings(
    val sortingMode: SimpleSortingDialog.SortingMode,
    val reverseOrder: Boolean,
    val foldersFirst: Boolean
): Serializable {

    fun toHumanString(): String {
        return toString()
            .replace("("," (\n")
            .replace(")","\n)")
            .replace(", ",",\n")
    }
}