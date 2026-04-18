package com.github.aakumykov.simple_sorting_dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.github.aakumykov.simple_sorting_dialog.databinding.DialogSortingBinding
import com.google.gson.Gson
import kotlin.jvm.java

class SimpleSortingDialog : DialogFragment() {

    private var _binding: DialogSortingBinding? = null
    private val binding: DialogSortingBinding get() = _binding!!

    private var _callbacks: Callbacks? = null

    private val initialSettings: SortingSettings? get() {
        return arguments?.getString(INITIAL_SETTINGS)?.let {
            Gson().fromJson(it, SortingSettings::class.java)
        }
    }

    private val currentSortingSettings: SortingSettings
        get() {
        return SortingSettings(
            sortingMode = viewId2sortingMode(binding.sortingModeSelector.checkedRadioButtonId),
            reverseOrder = binding.reverseOrderCheckbox.isChecked,
            foldersFirst = binding.foldersFirstCheckbox.isChecked
        )
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        _binding = DialogSortingBinding.inflate(layoutInflater)

        binding.applyButton.setOnClickListener { onApplyClicked() }

        applyInitialSettings()

        return AlertDialog.Builder(requireContext())
            .setTitle(R.string.sorting_dialog_title)
            .setView(binding.root)
            .create()
    }

    private fun applyInitialSettings() {
        initialSettings?.also {
            binding.apply {
                sortingModeSelector.check(sortingMode2viewId(it.sortingMode))
                reverseOrderCheckbox.isChecked = it.reverseOrder
                foldersFirstCheckbox.isChecked = it.foldersFirst
            }
        }
    }

    private fun onApplyClicked() {
        _callbacks?.onSortingApplied(currentSortingSettings)
        dismiss()
    }

    override fun onCancel(dialog: DialogInterface) {
        _callbacks?.onSortingCancelled()
        super.onCancel(dialog)
    }

    override fun onDestroyView() {
        _callbacks = null
        _binding = null
        super.onDestroyView()
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
        const val INITIAL_SETTINGS = "INITIAL_SETTINGS"

        fun createAndShow(
            fragmentManager: FragmentManager,
            initialSettings: SortingSettings? = null
        ): SimpleSortingDialog {
            return SimpleSortingDialog()
                .apply {
                    arguments = bundleOf(
                        INITIAL_SETTINGS to Gson().toJson(initialSettings),
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
