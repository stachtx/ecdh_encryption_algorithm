package com.example.ecca_encryption

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.observe
import androidx.savedstate.SavedStateRegistryOwner
import com.example.ecca_encryption.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val viewModel: MainActivityViewModel by viewModels {
        MainActivityViewModelFactory(this, baseContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        binding.insertsButton.setOnClickListener { _ ->
            viewModel.onInsertsClicked()
        }
        binding.selectsIndexedButton.setOnClickListener { _ ->
            viewModel.onSelectIndexedClicked()
        }
        binding.selectsNoindexButton.setOnClickListener { _ ->
            viewModel.onSelectNoIndexClicked()
        }

        binding.cancelButton.setOnClickListener {
            viewModel.onCancelClicked()
        }

        binding.transactionCount.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit

            override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit

            override fun onProgressChanged(
                seekBar: SeekBar?,
                progress: Int,
                fromUser: Boolean
            ) {
                if (fromUser) {
                    val length = when (progress) {
                        1 -> 10000
                        2 -> 50000
                        3 -> 100000
                        else -> 1000
                    }
                    viewModel.updateQuerySize(length)
                }
            }
        })

        binding.securityRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.no_encryption -> {
                    viewModel.encrypted = false
                    viewModel.encryptedWithMemorySecurity = false
                    viewModel.runAll = false
                }
                R.id.encrypted -> {
                    viewModel.encrypted = true
                    viewModel.encryptedWithMemorySecurity = false
                    viewModel.runAll = false
                }
                R.id.encrypted_with_memory_security -> {
                    viewModel.encrypted = true
                    viewModel.encryptedWithMemorySecurity = true
                    viewModel.runAll = false
                }
                R.id.run_all -> {
                    viewModel.runAll = true
                }
            }
        }

        binding.results.setOnLongClickListener {
            try {
                val clipboard: ClipboardManager =
                    getSystemService(AppCompatActivity.CLIPBOARD_SERVICE) as ClipboardManager
                val clip =
                    ClipData.newPlainText("sqlperformanceresults", binding.results.text.toString())
                clipboard.setPrimaryClip(clip)

                Toast.makeText(baseContext, "Results copied", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            true
        }

        // LiveData
        viewModel.enableUI.observe(this) {
            setUIEnabled(it)
        }

        viewModel.results.observe(this) {
            binding.results.text = it

            // Scroll down
            binding.scrollView.post {
                binding.scrollView.fullScroll(View.FOCUS_DOWN)
            }
        }

        viewModel.querySize.observe(this) {
            binding.count = it
        }
    }

    private fun setUIEnabled(enabled: Boolean) {
        binding.cancelButton.isEnabled = !enabled

        binding.controllers.referencedIds.forEach {
            binding.root.findViewById<View>(it).isEnabled = enabled
        }
    }

}

@Suppress("UNCHECKED_CAST")
class MainActivityViewModelFactory(
    owner: SavedStateRegistryOwner,
    private val context: Context,
    defaultArgs: Bundle? = null
) : AbstractSavedStateViewModelFactory(owner, defaultArgs) {
    override fun <T : ViewModel> create(
        key: kotlin.String,
        modelClass: Class<T>,
        handle: SavedStateHandle
    ): T {
        return MainActivityViewModel(context) as T
    }
}