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

class SimpleSortingDialog<ExternalSortingMode> : DialogFragment() {

    private var _binding: DialogSortingBinding? = null
    private val binding: DialogSortingBinding get() = _binding!!

    private var _callbacks: Callbacks? = null
    private var _translator: Translator<ExternalSortingMode>? = null

    private val initialSettings: SortingSettings? get() {
        return arguments?.getString(INITIAL_SETTINGS)?.let {
            Gson().fromJson(it, SortingSettings::class.java)
        }
    }

    private val currentSortingSettings: SortingSettings<ExternalSortingMode>
        get() {
            val simpleSortingMode = viewId2sortingMode(binding.sortingModeSelector.checkedRadioButtonId)
            val externalSortingMode = _translator?.simpleSortingMode2externalMode(simpleSortingMode)
            return SortingSettings<ExternalSortingMode>(
                sortingMode = externalSortingMode,
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
        _translator = null
        _callbacks = null
        _binding = null
        super.onDestroyView()
    }

    private fun sortingMode2viewId(sortingMode: SimpleSortingMode): Int {
        return when(sortingMode) {
            SimpleSortingMode.NAME -> R.id.sortingModeByName
            SimpleSortingMode.SIZE -> R.id.sortingModeBySize
            SimpleSortingMode.M_TIME -> R.id.sortingModeByMTime
        }
    }

    private fun viewId2sortingMode(viewId: Int): SimpleSortingMode {
        return when(viewId) {
            R.id.sortingModeBySize -> SimpleSortingMode.SIZE
            R.id.sortingModeByMTime -> SimpleSortingMode.M_TIME
            else -> SimpleSortingMode.NAME
        }
    }

    fun display(fragmentManager: FragmentManager): SimpleSortingDialog<ExternalSortingMode> {
        show(fragmentManager, TAG)
        return this
    }

    interface Callbacks {
        fun onSortingApplied(sortingSettings: SortingSettings)
        fun onSortingCancelled(){}
    }

    interface Translator<ExternalSortingMode> {
        fun externalMode2simpleSortingMode(externalMode: ExternalSortingMode): SimpleSortingMode
        fun simpleSortingMode2externalMode(simpleSortingMode: SimpleSortingMode): ExternalSortingMode
    }

    fun setCallbacks(callbacks: Callbacks): SimpleSortingDialog<ExternalSortingMode> {
        _callbacks = callbacks
        return this
    }

    fun setTranslator(translator: Translator<ExternalSortingMode>) {
        _translator = translator
    }

    companion object {
        val TAG: String = SimpleSortingDialog::class.java.simpleName
        const val INITIAL_SETTINGS = "INITIAL_SETTINGS"

        fun <ExternalSortingMode> createAndShow(
            fragmentManager: FragmentManager,
            initialSettings: SortingSettings? = null
        ): SimpleSortingDialog<ExternalSortingMode> {
            return SimpleSortingDialog<ExternalSortingMode>()
                .apply {
                    arguments = bundleOf(
                        INITIAL_SETTINGS to Gson().toJson(initialSettings),
                    )
                }
                .display(fragmentManager)
        }

        fun <ExternalSortingMode> find(fragmentManager: FragmentManager): SimpleSortingDialog<ExternalSortingMode>? {
            return fragmentManager.findFragmentByTag(TAG)?.let {
                it as? SimpleSortingDialog<ExternalSortingMode>
            }
        }
    }
}
