package com.samsung.android.sdk.spay.sample.issuer.ui.status

import android.app.Activity
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import androidx.databinding.DataBindingUtil
import com.samsung.android.sdk.samsungpay.v2.SpaySdk
import com.samsung.android.sdk.spay.sample.issuer.R
import com.samsung.android.sdk.spay.sample.issuer.databinding.KeyValueLayoutBinding
import com.samsung.android.sdk.spay.sample.issuer.databinding.ShowErrorResultActivityBinding
import com.samsung.android.sdk.spay.sample.issuer.util.ErrorCode

class ErrorResultActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = intent
        val errorCode = intent.getIntExtra(ERROR_CODE_KEY, 0)
        val errorCodeName = intent.getStringExtra(ERROR_CODE_NAME_KEY)
        val errorMsg = intent.getStringExtra(ERROR_MSG_KEY)
        val bundle = intent.getBundleExtra(ERROR_BUNDLE_KEY)
        val inflater = this.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val binding: ShowErrorResultActivityBinding =
            DataBindingUtil.setContentView(this, R.layout.show_error_result_activity)
        var keyValueBinding: KeyValueLayoutBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.key_value_layout,
            binding.errorRowLayout,
            false
        )
        keyValueBinding.key.text = getString(R.string.error_code)
        keyValueBinding.value.text = errorCode.toString()
        binding.errorRowLayout.addView(keyValueBinding.keyValueContainer)

        if (errorCodeName != null) {
            keyValueBinding = DataBindingUtil.inflate(
                inflater,
                R.layout.key_value_layout,
                binding.errorRowLayout,
                false
            )
            keyValueBinding.key.text = ""
            keyValueBinding.value.text = errorCodeName
            binding.errorRowLayout.addView(keyValueBinding.keyValueContainer)
        }
        if (!errorMsg.isNullOrEmpty()) {
            keyValueBinding = DataBindingUtil.inflate(
                inflater,
                R.layout.key_value_layout,
                binding.errorRowLayout,
                false
            )
            keyValueBinding.key.text = ""
            keyValueBinding.value.text = errorMsg
            binding.errorRowLayout.addView(keyValueBinding.keyValueContainer)
        }
        if (bundle != null) {
            keyValueBinding = DataBindingUtil.inflate(
                inflater,
                R.layout.key_value_layout,
                binding.errorRowLayout,
                false
            )
            keyValueBinding.key.text = getString(R.string.extra)
            keyValueBinding.key.setTypeface(null, Typeface.BOLD)
            keyValueBinding.value.text = ""
            binding.errorRowLayout.addView(keyValueBinding.keyValueContainer)
            for (key in bundle.keySet()) {
                keyValueBinding = DataBindingUtil.inflate(
                    inflater,
                    R.layout.key_value_layout,
                    binding.errorRowLayout,
                    false
                )
                keyValueBinding.key.text = key
                val bundleObject = bundle[key]

                if (bundleObject != null) {
                    when {
                        key.equals(SpaySdk.EXTRA_ERROR_REASON, ignoreCase = true) -> {
                            val errorNum = bundleObject as Int
                            val errorReasonMsg = "$bundleObject : ${ ErrorCode.getErrorCodeName(errorNum)}"
                            keyValueBinding.value.text = errorReasonMsg
                        }
                        key.equals(SpaySdk.EXTRA_ERROR_REASON_MESSAGE, ignoreCase = true) -> {
                            keyValueBinding.value.text = bundleObject as String?
                        }
                        else -> keyValueBinding.value.text = getString(R.string.null_string)
                    }
                    binding.errorRowLayout.addView(keyValueBinding.keyValueContainer)
                }
            }
        }
    }

    companion object {
        const val ERROR_CODE_KEY = "error_code"
        const val ERROR_CODE_NAME_KEY = "error_code_name"
        const val ERROR_MSG_KEY = "error_msg"
        const val ERROR_BUNDLE_KEY = "error_bundle"
    }
}