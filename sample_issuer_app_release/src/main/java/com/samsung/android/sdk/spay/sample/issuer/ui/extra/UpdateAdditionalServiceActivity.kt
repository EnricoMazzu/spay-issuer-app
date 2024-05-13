package com.samsung.android.sdk.spay.sample.issuer.ui.extra

import android.app.Activity
import android.os.Bundle
import android.os.Environment
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.samsung.android.sdk.spay.sample.issuer.ui.helper.ProgressDialogHelper
import androidx.databinding.DataBindingUtil
import com.samsung.android.sdk.spay.sample.issuer.util.PartnerInfoHolder
import com.samsung.android.sdk.spay.sample.issuer.ui.helper.ErrorResultUiHelper
import com.samsung.android.sdk.samsungpay.v2.SamsungPay
import com.samsung.android.sdk.samsungpay.v2.card.*
import com.samsung.android.sdk.spay.sample.issuer.R
import com.samsung.android.sdk.spay.sample.issuer.databinding.UpdateAdditionalServiceActivityBinding
import com.samsung.android.sdk.spay.sample.issuer.util.ErrorCode
import java.io.File
import java.util.ArrayList

class UpdateAdditionalServiceActivity : Activity(), View.OnClickListener,
        AdapterView.OnItemSelectedListener {
    private lateinit var cardManager: CardManager
    private val cards: MutableList<Card> = ArrayList()
    private val cardArrayList: MutableList<String> = ArrayList()
    private lateinit var progressUtil: ProgressDialogHelper
    private lateinit var binding: UpdateAdditionalServiceActivityBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.update_additional_service_activity)
        binding.transactionId.addTextChangedListener(textWatcher)
        binding.additionalServiceData.addTextChangedListener(textWatcher)
        binding.additionalServiceDataDescription.addTextChangedListener(textWatcher)
        binding.guiCheckingButton.setOnClickListener(this)
        setGUICheckingButtonIfNeed()
        cardManager = CardManager(this, PartnerInfoHolder.getInstance(this).partnerInfo)
        progressUtil = ProgressDialogHelper(this)
    }

    override fun onClick(v: View) {
        if (v.id == R.id.get_all_cards_for_list_button) {
            clearAllView()
            getAllCards()
        } else if (v.id == R.id.update_additional_service_button) {
            doUpdateAdditionalService(false)
        } else if (v.id == R.id.gui_checking_button) {
            doUpdateAdditionalService(true)
        }
    }

    private fun clearAllView() {
        binding.cardListTableLayout.visibility = View.INVISIBLE
        binding.cardListSizeTextView.text = ""
        binding.cardStatus.text = ""
        binding.cardId.text = ""
        binding.cardBalance.text = ""
        binding.transactionId.setText("")
        binding.additionalServiceData.setText("")
        binding.additionalServiceDataDescription.setText("")
        cards.clear()
        cardArrayList.clear()
    }

    private fun getAllCards() {

        progressUtil.showProgressDialog()
        cardManager.getAllCards(Bundle(), object : GetCardListener {
            override fun onSuccess(cards: List<Card>) {
                Log.d(TAG, "onSuccess callback is called, list.size= ${cards.size}")
                showCardList(cards)
                progressUtil.dismissProgressDialog()
            }

            override fun onFail(errorCode: Int, errorData: Bundle) {
                Log.d(TAG, "onFail callback is called, code:$errorCode")
                binding.cardListTableLayout.visibility = View.INVISIBLE
                progressUtil.dismissProgressDialog()
                ErrorResultUiHelper.showErrorResultPage(
                        applicationContext, errorCode,
                        ErrorCode.getErrorCodeName(errorCode), errorData
                )
            }
        })
    }

    fun showCardList(cardList: List<Card>) {
        for (card in cardList.indices) {
            var issuerName: String? = ""
            if (cardList[card].cardInfo.getString(SamsungPay.EXTRA_ISSUER_NAME) != null) {
                issuerName = cardList[card].cardInfo.getString(SamsungPay.EXTRA_ISSUER_NAME)
            }
            val last4DPAN = cardList[card].cardInfo.getString(SamsungPay.EXTRA_LAST4_DPAN)
            val last4FPAN = cardList[card].cardInfo.getString(SamsungPay.EXTRA_LAST4_FPAN)
            cards.add(cardList[card])
            cardArrayList.add("$issuerName LAST4 DPAN:$last4DPAN FPAN:$last4FPAN")
        }
        if (cards.isEmpty()) {
            val cardListSize = getString(R.string.card_list_size) + "0"
            binding.cardListSizeTextView.text = cardListSize
            binding.cardListTableLayout.visibility = View.INVISIBLE
            return
        } else {
            binding.cardListSizeTextView.text = getString(R.string.card_list_size) + this.cards.size
            binding.cardListTableLayout.visibility = View.VISIBLE
        }
        val adapter = ArrayAdapter(this, R.layout.drop_down_item, cardArrayList)
        adapter.setDropDownViewResource(R.layout.drop_down_item)
        binding.cardListSpinner.adapter = adapter
        binding.cardListSpinner.onItemSelectedListener = this
    }

    private fun doUpdateAdditionalService(needDummyData: Boolean) {
        // Use-Case : Update additional service for Transit Card.
        val transitCard = TransitCard()
        val referenceId = if (needDummyData) "dummy" else binding.cardId.text.toString()
        val trId = if (needDummyData) "dummy" else binding.transactionId.text.toString()
        val serviceType =
                if (needDummyData) TransitCard.ADDITIONAL_SERVICE_CHARGE else binding.operationTypeSpinner.selectedItem.toString()
        val serviceData =
                if (needDummyData) null else binding.additionalServiceData.text.toString()
        val serviceDescription =
                if (needDummyData) resources.getString(R.string.gui_test_description) else binding.additionalServiceDataDescription.text.toString()

        transitCard.apply {
            // #1 : Set Card Reference ID
            // Partner can get this ID through getAllCards() API.
            cardReferenceId = referenceId

            // #2 : Set Transaction ID
            // Partner can get this ID through their server.
            transactionId = trId

            // #3 : Set Service Operation Type
            additionalServiceType = serviceType

            // #4 : Set Service Data (If needed)
            // Partner can set additional data like amount or balance, if partner wants.
            additionalServiceData = serviceData

            // #5 : Set Service Description (This message will be shown in authentication view)
            // Partner can set partner's own description, if partner wants.
            additionalServiceDescription = serviceDescription
        }
        cardManager.updateAdditionalService(transitCard, object : CardListener {
            override fun onSuccess(status: Int, data: Bundle) {
                Log.d(TAG, "onSuccess callback is called")
                Toast.makeText(
                        this@UpdateAdditionalServiceActivity,
                        getString(R.string.on_success),
                        Toast.LENGTH_SHORT
                ).show()
            }

            override fun onFail(errorCode: Int, errorData: Bundle) {
                Log.e(TAG, "onFail callback is called")
                ErrorResultUiHelper.showErrorResultPage(
                        applicationContext, errorCode,
                        ErrorCode.getErrorCodeName(errorCode), errorData
                )
            }
        })
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
        binding.apply {
            cardStatus.text = cards[position].cardStatus
            cardId.text = cards[position].cardId
            val balance = cards[position].cardInfo.getString(CardManager.EXTRA_CARD_BALANCE)
            cardBalance.text = balance
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        // no need
    }

    private var textWatcher: TextWatcher = object : TextWatcher {
        override fun afterTextChanged(edit: Editable) {
            // no need
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            // no need
        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            binding.apply{
                if (transactionId.text.toString().isEmpty()) {
                    updateAdditionalServiceButton.isClickable = false
                    updateAdditionalServiceButton.alpha = 0.4f
                } else {
                    updateAdditionalServiceButton.isClickable = true
                    updateAdditionalServiceButton.alpha = 1.0f
                }
            }

        }
    }

    private fun setGUICheckingButtonIfNeed() {
        val storagePath = Environment.getExternalStorageDirectory().path
        val path = "$storagePath/octopus.txt"
        val octopusFile = File(path)
        if (octopusFile.exists()) {
            binding.guiCheckingButton.visibility = View.VISIBLE
        } else {
            binding.guiCheckingButton.visibility = View.GONE
        }
    }

    companion object {
        private val TAG = UpdateAdditionalServiceActivity::class.java.simpleName
    }
}