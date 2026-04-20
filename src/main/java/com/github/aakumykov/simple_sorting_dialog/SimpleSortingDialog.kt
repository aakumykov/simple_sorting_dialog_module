package com.github.aakumykov.simple_sorting_dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.github.aakumykov.simple_sorting_dialog.databinding.DialogSortingBinding
import com.github.aakumykov.simple_sorting_dialog.extensions.getBooleanFromPreferences
import com.github.aakumykov.simple_sorting_dialog.extensions.getStringFromPreferences
import com.github.aakumykov.simple_sorting_dialog.extensions.storeBooleanInPreferences
import com.github.aakumykov.simple_sorting_dialog.extensions.storeStringInPreferences

class SimpleSortingDialog : DialogFragment() {

    private var _binding: DialogSortingBinding? = null
    private val binding: DialogSortingBinding get() = _binding!!

    private var _callbacks: Callbacks? = null


    private val isFirstRun: Boolean
        get() = getBooleanFromPreferences(FIRST_RUN, true)


    private val initialSortingMode: SortingMode get() {
        val s = arguments?.getString(INITIAL_SORTING_MODE)
        return try { SortingMode.valueOf(s!!) }
        catch (_: Exception) { defaultSortingMode }
    }

    private val initialReverseOrder: Boolean
        get() = arguments?.getBoolean(INITIAL_REVERSE_ORDER) ?: defaultReverseOrder

    private val initialFoldersFirst: Boolean
        get() = arguments?.getBoolean(INITIAL_FOLDERS_FIRST) ?: defaultFoldersFirst


    private val sortingSettingsFromGUI: SortingSettings
        get() = SortingSettings(
            sortingMode = viewId2sortingMode(binding.sortingModeSelector.checkedRadioButtonId),
            reverseOrder = binding.reverseOrderCheckbox.isChecked,
            foldersFirst = binding.foldersFirstCheckbox.isChecked
        )


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        _binding = DialogSortingBinding.inflate(layoutInflater)

        binding.applyButton.setOnClickListener { onApplyClicked() }

        if (isFirstRun) {
            applyInitialSettings()
            storeBooleanInPreferences(FIRST_RUN, false)
        }
        else {
            restoreSettings()
        }

        return AlertDialog.Builder(requireContext())
            .setTitle(R.string.sorting_dialog_title)
            .setView(binding.root)
            .create()
    }


    private fun applyInitialSettings() {
        binding.apply {
            sortingModeSelector.check(sortingMode2viewId(initialSortingMode))
            reverseOrderCheckbox.isChecked = initialReverseOrder
            foldersFirstCheckbox.isChecked = initialFoldersFirst
        }
    }

    private fun onApplyClicked() {
        _callbacks?.onSortingApplied(sortingSettingsFromGUI)
        dismiss()
    }

    override fun onCancel(dialog: DialogInterface) {
        _callbacks?.onSortingCancelled()
        super.onCancel(dialog)
    }

    override fun onDestroyView() {
        storeSettings()
        _callbacks = null
        _binding = null
        super.onDestroyView()
    }

    private fun storeSettings() {
        storeStringInPreferences(SORTING_MODE, sortingSettingsFromGUI.sortingMode.name)
        storeBooleanInPreferences(REVERSE_ORDER, sortingSettingsFromGUI.reverseOrder)
        storeBooleanInPreferences(FOLDERS_FIRST, sortingSettingsFromGUI.foldersFirst)
    }

    private fun restoreSettings() {
        binding.apply {
            sortingModeSelector.check(getViewIdForStoredSortingMode())
            reverseOrderCheckbox.isChecked = getBooleanFromPreferences(REVERSE_ORDER, defaultReverseOrder)
            foldersFirstCheckbox.isChecked = getBooleanFromPreferences(FOLDERS_FIRST, defaultFoldersFirst)
        }
    }

    private fun getViewIdForStoredSortingMode(): Int {
        return try {
            getStringFromPreferences(SORTING_MODE).let { SortingMode.valueOf(it!!) }
        } catch (_: Exception) {
            defaultSortingMode
        }.let { savedSortingMode ->
            sortingMode2viewId(savedSortingMode)
        }
    }

    private fun sortingMode2viewId(sortingMode: SortingMode): Int {
        return when(sortingMode) {
            SortingMode.NAME -> R.id.sortingModeByName
            SortingMode.SIZE -> R.id.sortingModeBySize
            SortingMode.M_TIME -> R.id.sortingModeByMTime
        }
    }

    private fun viewId2sortingMode(viewId: Int): SortingMode {
        return when(viewId) {
            R.id.sortingModeBySize -> SortingMode.SIZE
            R.id.sortingModeByMTime -> SortingMode.M_TIME
            else -> SortingMode.NAME
        }
    }

    fun display(fragmentManager: FragmentManager): SimpleSortingDialog {
        show(fragmentManager, TAG)
        return this
    }

    interface Callbacks {
        fun onSortingApplied(sortingSettings: SortingSettings)
        fun onSortingCancelled(){}
    }


    fun setCallbacks(callbacks: Callbacks): SimpleSortingDialog {
        _callbacks = callbacks
        return this
    }

    companion object {
        val TAG: String = SimpleSortingDialog::class.java.simpleName

        private val defaultSortingMode: SortingMode = SortingMode.NAME
        const val defaultReverseOrder: Boolean = false
        const val defaultFoldersFirst: Boolean = true

        private const val INITIAL_SORTING_MODE = "INITIAL_FOLDERS_FIRST"
        private const val INITIAL_REVERSE_ORDER = "INITIAL_REVERSE_ORDER"
        private const val INITIAL_FOLDERS_FIRST = "INITIAL_FOLDERS_FIRST"

        const val FIRST_RUN = "FIRST_RUN"

        const val SORTING_MODE = "SORTING_MODE"
        const val REVERSE_ORDER = "REVERSE_ORDER"
        const val FOLDERS_FIRST = "FOLDERS_FIRST"

        fun createAndShow(
            fragmentManager: FragmentManager,
            initialSortingMode: SortingMode = defaultSortingMode,
            initialReverseOrder: Boolean = defaultReverseOrder,
            initialFoldersFirst: Boolean = defaultFoldersFirst
        ): SimpleSortingDialog
        {
            return SimpleSortingDialog()
                .apply {
                    arguments = bundleOf(
                        INITIAL_SORTING_MODE to initialSortingMode,
                        INITIAL_REVERSE_ORDER to initialReverseOrder,
                        INITIAL_FOLDERS_FIRST to initialFoldersFirst
                    )
                }
                .display(fragmentManager)
        }

        fun  find(fragmentManager: FragmentManager): SimpleSortingDialog? {
            return fragmentManager.findFragmentByTag(TAG)?.let {
                it as? SimpleSortingDialog
            }
        }
    }

    enum class SortingMode {
        NAME, SIZE, M_TIME
    }
}
