package com.samsung.android.sdk.spay.sample.issuer.ui.extra

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import com.samsung.android.sdk.samsungpay.v2.payment.PaymentManager
import com.samsung.android.sdk.spay.sample.issuer.ui.helper.ProgressDialogHelper
import androidx.databinding.DataBindingUtil
import com.samsung.android.sdk.samsungpay.v2.StatusListener
import com.samsung.android.sdk.samsungpay.v2.payment.CardInfo
import com.samsung.android.sdk.spay.sample.issuer.R
import com.samsung.android.sdk.spay.sample.issuer.databinding.StartSimplePayRefundActivityBinding
import com.samsung.android.sdk.spay.sample.issuer.util.PartnerInfoHolder
import com.samsung.android.sdk.spay.sample.issuer.util.SpayConstant.SELECTED_CARD_ID
import com.samsung.android.sdk.spay.sample.issuer.util.SpayConstant.TRANSACTION_DATE_REQUIRED_LENGTH

/*This is only for Korean issuers*/
class StartSimplePayRefundActivity : Activity(), View.OnClickListener, TextWatcher {
    private lateinit var context: Context
    private lateinit var progressUtil: ProgressDialogHelper
    private lateinit var binding: StartSimplePayRefundActivityBinding
    private var cardId: String? = null
    private var transactionType =
            PaymentManager.TRANSACTION_TYPE_MST or PaymentManager.TRANSACTION_TYPE_NFC

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context = this
        binding = DataBindingUtil.setContentView(this, R.layout.start_simple_pay_refund_activity)
        val intent = intent
        if (intent != null) {
            cardId = intent.getStringExtra(SELECTED_CARD_ID)
        }
        if (!cardId.isNullOrEmpty()) {
            binding.cardId.text = cardId
        } else {
            finish()
        }
        binding.mstNfcRadioBtn.setOnClickListener(this)
        binding.mstRadioBtn.setOnClickListener(this)
        binding.nfcRadioBtn.setOnClickListener(this)
        binding.startSimplePayForRefundButton.setOnClickListener(this)
        binding.startSimplePayForRefundButton.isEnabled = false
        binding.transactionYyyyMMDdHHMm.addTextChangedListener(this)
        binding.transactionNumberEditText.addTextChangedListener(this)
        progressUtil = ProgressDialogHelper(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.start_simple_pay_for_refund_button -> startSimplePay()
            R.id.mst_nfc_radio_btn -> transactionType =
                    PaymentManager.TRANSACTION_TYPE_MST or PaymentManager.TRANSACTION_TYPE_NFC
            R.id.mst_radio_btn -> transactionType = PaymentManager.TRANSACTION_TYPE_MST
            R.id.nfc_radio_btn -> transactionType = PaymentManager.TRANSACTION_TYPE_NFC
            else -> {
            }
        }
    }

    private fun dismissProgressDialog() {
        progressUtil.dismissProgressDialog()
    }

    private fun startSimplePay() {
        val pInfo = PartnerInfoHolder.getInstance(this).partnerInfo
        val paymentManager = PaymentManager(context, pInfo)

        val transactionDate = binding.transactionYyyyMMDdHHMm.text.toString()
        val transactionNumber = binding.transactionNumberEditText.text.toString()
        val metaData = Bundle().apply {
            putString(PaymentManager.EXTRA_TRANSACTION_DATE, transactionDate)
            putString(PaymentManager.EXTRA_TRANSACTION_NUMBER, transactionNumber)
            putString(
                    PaymentManager.EXTRA_PAY_OPERATION_TYPE,
                    PaymentManager.PAY_OPERATION_TYPE_REFUND
            )
            putInt(PaymentManager.EXTRA_TRANSACTION_TYPE, transactionType)
        }
        val cardInfo = CardInfo.Builder().setCardId(cardId).setCardMetaData(metaData).build()
        progressUtil.showProgressDialog()
        paymentManager.startSimplePay(cardInfo, object : StatusListener {
            override fun onSuccess(status: Int, data: Bundle) {
                dismissProgressDialog()
            }

            override fun onFail(errorCode: Int, data: Bundle) {
                dismissProgressDialog()
            }
        })
    }

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
        // no need
    }

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        // no need
    }

    override fun afterTextChanged(s: Editable) {
        val transactionDateLength = binding.transactionYyyyMMDdHHMm.text.toString().length
        binding.startSimplePayForRefundButton.isEnabled =
                (transactionDateLength == TRANSACTION_DATE_REQUIRED_LENGTH
                        && !binding.transactionNumberEditText.text.toString().isEmpty())
    }
}