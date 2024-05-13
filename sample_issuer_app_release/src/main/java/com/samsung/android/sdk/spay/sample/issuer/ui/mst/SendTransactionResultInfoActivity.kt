package com.samsung.android.sdk.spay.sample.issuer.ui.mst

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.samsung.android.sdk.spay.sample.issuer.ui.helper.ErrorResultUiHelper.showErrorResultPage
import androidx.databinding.DataBindingUtil
import com.samsung.android.sdk.spay.sample.issuer.util.PartnerInfoHolder
import com.samsung.android.sdk.samsungpay.v2.payment.MstManager
import com.samsung.android.sdk.samsungpay.v2.payment.TransactionResultInfo
import com.samsung.android.sdk.samsungpay.v2.payment.TransactionResultListener
import com.samsung.android.sdk.spay.sample.issuer.R
import com.samsung.android.sdk.spay.sample.issuer.databinding.SendTransactionResultInfoActivityBinding
import com.samsung.android.sdk.spay.sample.issuer.util.ErrorCode

class SendTransactionResultInfoActivity : Activity(), View.OnClickListener {
    private lateinit var binding: SendTransactionResultInfoActivityBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.send_transaction_result_info_activity)
    }

    override fun onClick(v: View) {
        if (v.id == R.id.send_button) {
            doSendTransactionResultInfo()
        }
    }

    private fun doSendTransactionResultInfo() {
        if (binding.amount.text.toString().isEmpty()) {
            binding.amount.setText("0")
        }
        val partnerInfo = PartnerInfoHolder.getInstance(this).partnerInfo
        val mstManager = MstManager(this, partnerInfo)
        val extraBundle = Bundle()
        extraBundle.putString(binding.extraDataKey.text.toString(), binding.extraDataValue.text.toString())
        val mstTransactionResultInfoBuilder = TransactionResultInfo.Builder()
        val transactionResultInfo = mstTransactionResultInfoBuilder
                .setTransactionAmount(binding.amount.text.toString().toDouble())
                .setTransactionCurrency(binding.currency.text.toString())
                .setTransactionDateTime(binding.dateTime.text.toString())
                .setTransactionIssuer(binding.issuer.text.toString())
                .setTransactionApprovalType(TransactionResultInfo.ApprovalType.valueOf(binding.approvalType.selectedItem.toString()))
                .setTransactionMerchantName(binding.merchantName.text.toString())
                .setUserProfileId(binding.userProfileId.text.toString())
                .setExtraData(extraBundle)
                .build()
        mstManager.sendTransactionResultInfo(transactionResultInfo, object : TransactionResultListener {
            override fun onSuccess(data: Bundle) {
                Log.d(TAG, "onSuccess callback is called")
                Toast.makeText(this@SendTransactionResultInfoActivity, getString(R.string.on_success),
                        Toast.LENGTH_SHORT).show()
            }

            override fun onFail(errorCode: Int, errorData: Bundle) {
                Log.e(TAG, "onFail callback is called, code:$errorCode")
                showErrorResultPage(applicationContext, errorCode, ErrorCode.getErrorCodeName(errorCode), errorData)
            }
        })
    }

    companion object {
        private val TAG = SendTransactionResultInfoActivity::class.java.simpleName
    }
}