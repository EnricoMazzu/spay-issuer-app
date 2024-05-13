package com.samsung.android.sdk.spay.sample.issuer.ui.status

import android.app.Activity
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.samsung.android.sdk.samsungpay.v2.SpaySdk
import com.samsung.android.sdk.samsungpay.v2.SamsungPay
import com.samsung.android.sdk.spay.sample.issuer.R
import com.samsung.android.sdk.spay.sample.issuer.databinding.GetSamsungPayStatusActivityBinding
import com.samsung.android.sdk.spay.sample.issuer.databinding.KeyValueLayoutBinding
import com.samsung.android.sdk.spay.sample.issuer.util.ErrorCode
import com.samsung.android.sdk.spay.sample.issuer.util.SpayConstant

class SamsungPayStatusActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = intent
        val statusCode = intent.getIntExtra(SpayConstant.SAMSUNG_PAY_STATUS_CODE_KEY, 0)
        val statusName = intent.getStringExtra(SpayConstant.SAMSUNG_PAY_STATUS_NAME_KEY)
        val bundle = intent.getBundleExtra(SpayConstant.SAMSUNG_PAY_STATUS_BUNDLE)
        val inflater = this.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val binding: GetSamsungPayStatusActivityBinding =
                DataBindingUtil.setContentView(this, R.layout.get_samsung_pay_status_activity)
        var keyValueBinding: KeyValueLayoutBinding = DataBindingUtil.inflate(
                inflater,
                R.layout.key_value_layout,
                binding.statusRowLayout,
                false
        )
        keyValueBinding.key.text = getString(R.string.status)
        keyValueBinding.value.text = statusCode.toString()
        binding.statusRowLayout.addView(keyValueBinding.keyValueContainer)
        if (statusName != null) {
            keyValueBinding = DataBindingUtil.inflate(
                    inflater,
                    R.layout.key_value_layout,
                    binding.statusRowLayout,
                    false
            )
            keyValueBinding.key.text = ""
            keyValueBinding.value.text = statusName
            binding.statusRowLayout.addView(keyValueBinding.keyValueContainer)
        }
        if (bundle != null) {
            keyValueBinding = DataBindingUtil.inflate(
                    inflater,
                    R.layout.key_value_layout,
                    binding.statusRowLayout,
                    false
            )
            keyValueBinding.key.text = getString(R.string.extra)
            keyValueBinding.key.setTypeface(null, Typeface.BOLD)
            keyValueBinding.value.text = ""
            binding.statusRowLayout.addView(keyValueBinding.keyValueContainer)
            for (key in bundle.keySet()) {
                keyValueBinding = DataBindingUtil.inflate(
                        inflater,
                        R.layout.key_value_layout,
                        binding.statusRowLayout,
                        false
                )
                keyValueBinding.key.text = key
                val bundleObject = bundle[key]
                if (bundleObject != null) {
                    if (key.equals(SpaySdk.EXTRA_ERROR_REASON, ignoreCase = true)) {
                        val errorNum = bundleObject as Int
                        val errorReasonMsg =
                                "$bundleObject : ${ErrorCode.getErrorCodeName(errorNum)}"
                        keyValueBinding.value.text = errorReasonMsg
                    } else {
                        keyValueBinding.value.text = bundleObject.toString()
                    }
                } else {
                    keyValueBinding.value.text = getString(R.string.null_string)
                }
                binding.statusRowLayout.addView(keyValueBinding.keyValueContainer)
            }
        }
        when (statusCode) {
            SamsungPay.SPAY_NOT_SUPPORTED -> Toast.makeText(
                    this,
                    getString(R.string.samsung_pay_is_not_supported),
                    Toast.LENGTH_LONG
            ).show()
            SamsungPay.SPAY_NOT_READY -> {
                Toast.makeText(
                        this,
                        getString(R.string.samsung_pay_is_not_ready),
                        Toast.LENGTH_LONG
                ).show()
                val extraError: Int
                if (bundle != null) {
                    extraError = bundle.getInt(SamsungPay.EXTRA_ERROR_REASON)
                    SamsungPayStatusDialog.instance.showSamsungPayStatusErrorDialog(
                            this,
                            extraError
                    )
                }
            }
            SamsungPay.SPAY_READY -> {
                Toast.makeText(this, getString(R.string.samsung_pay_is_ready), Toast.LENGTH_LONG)
                        .show()
                Log.d(TAG, "Samsung Pay is ready.")
            }
            SamsungPay.SPAY_NOT_ALLOWED_TEMPORALLY -> {
                Toast.makeText(
                        this,
                        getString(R.string.samsung_pay_is_not_allowed_temporally),
                        Toast.LENGTH_LONG
                ).show()
                Log.d(TAG, "Samsung Pay is not allowed temporally!")
            }
        }
    }

    override fun onBackPressed() {
        setResult(RESULT_OK)
        super.onBackPressed()
    }

    companion object {
        private val TAG = SamsungPayStatusActivity::class.java.simpleName
    }
}