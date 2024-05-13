package com.samsung.android.sdk.spay.sample.issuer.ui.provision

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.os.Environment
import android.preference.PreferenceManager
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.View
import android.widget.*
import com.samsung.android.sdk.samsungpay.v2.card.CardManager
import com.samsung.android.sdk.spay.sample.issuer.ui.helper.ProgressDialogHelper
import androidx.databinding.DataBindingUtil
import com.samsung.android.sdk.samsungpay.v2.*
import com.samsung.android.sdk.samsungpay.v2.card.AddCardInfo
import com.samsung.android.sdk.samsungpay.v2.card.AddCardInfo.*
import com.samsung.android.sdk.spay.sample.issuer.util.SpayConstant
import com.samsung.android.sdk.spay.sample.issuer.util.PartnerInfoHolder
import com.samsung.android.sdk.samsungpay.v2.card.AddCardListener
import com.samsung.android.sdk.samsungpay.v2.card.Card
import com.samsung.android.sdk.spay.payloadLoader.SampleDataLoader
import com.samsung.android.sdk.spay.payloadLoader.VisaDecryptedPayloadTest
import com.samsung.android.sdk.spay.sample.issuer.R
import com.samsung.android.sdk.spay.sample.issuer.databinding.AddCardActivityBinding
import com.samsung.android.sdk.spay.sample.issuer.ui.helper.ErrorResultUiHelper
import com.samsung.android.sdk.spay.sample.issuer.util.ErrorCode
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.IOException
import java.lang.StringBuilder
import java.util.ArrayList

class AddCardActivity : Activity(), View.OnClickListener {
    private var networkProvider = PROVIDER_VISA
    private lateinit var binding: AddCardActivityBinding

    // In case issuer wants to add a card to Samsung Watch. Can be changed from MORE/Settings menu
    private var isWatchMode = false

    // To get wallet information from Samsung Pay
    private lateinit var samsungPay: SamsungPay

    // To add a card to Samsung Pay mobile application
    private lateinit var cardManager: CardManager

    // To add a card to Samsung Gear watch application
    private lateinit var watchManager: WatchManager
    private lateinit var progressUtil: ProgressDialogHelper

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        actionBar?.title = getString(R.string.add_card_title)

        binding = DataBindingUtil.setContentView(this, R.layout.add_card_activity)

        binding.tokenizationProviderSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    adapterView: AdapterView<*>?,
                    view: View,
                    i: Int,
                    l: Long
                ) {
                    binding.encryptedPayloadDescription.visibility = View.VISIBLE
                    binding.payloadEncryptedEditText.visibility = View.VISIBLE
                    binding.payloadEncryptedNotice.visibility = View.VISIBLE

                    networkProvider = (view as TextView).text.toString()

                    when (networkProvider) {
                        PROVIDER_VISA -> {
                            binding.getWalletInfoRequiredLayout.visibility =
                                View.VISIBLE // VISA requires wallet info
                            binding.encryptedPayloadDescription.text =
                                getString(R.string.payload_encrypted_guide_title)

                            val payload = loadSamplePayload(PROVIDER_VISA)

                            if (payload.isNotEmpty()) {
                                val spannable: Spannable = SpannableString(payload)
                                makeSpannableStringWithKeyword(
                                    spannable,
                                    SpayConstant.KEY_CLIENT_DEVICE_ID,
                                    Color.RED
                                )
                                makeSpannableStringWithKeyword(
                                    spannable,
                                    SpayConstant.KEY_CLIENT_WALLET_ACCOUNT_ID,
                                    Color.RED
                                )
                                binding.payloadNonEncryptedText.setText(spannable)
                            } else {
                                binding.payloadNonEncryptedText.setText("")
                            }
                        }
                        PROVIDER_MASTERCARD -> {
                            binding.apply {
                                // MC does not require wallet info
                                getWalletInfoRequiredLayout.visibility = View.GONE
                                encryptedPayloadDescription.text =
                                    getString(R.string.payload_encrypted_base64_mastercard)
                                payloadNonEncryptedText.setText(
                                    loadSamplePayload(
                                        PROVIDER_MASTERCARD
                                    )
                                )
                                nextButton.isEnabled = true
                            }
                        }
                        PROVIDER_MIR, PROVIDER_PAGOBANCOMAT -> {
                            binding.apply {
                                // MIR does not require wallet info
                                getWalletInfoRequiredLayout.visibility = View.GONE
                                encryptedPayloadDescription.text =
                                    getString(R.string.payload_encrypted_guide_title)
                                payloadNonEncryptedText.text = null
                                nextButton.isEnabled = true
                            }
                        }

                        PROVIDER_VACCINE_PASS -> {
                            binding.apply {
                                getWalletInfoRequiredLayout.visibility = View.GONE
                                encryptedPayloadDescription.text =
                                    getString(R.string.payload_encrypted_guide_title)
                                payloadNonEncryptedText.setText(
                                    loadSamplePayload(PROVIDER_VACCINE_PASS)
                                )
                                encryptedPayloadDescription.visibility = View.GONE
                                payloadEncryptedEditText.visibility = View.GONE
                                payloadEncryptedNotice.visibility = View.GONE
                                nextButton.isEnabled = true
                            }
                        }
                    }
                }

                override fun onNothingSelected(adapterView: AdapterView<*>?) {
                    // no need
                }
            }
        binding.getWalletInfoButton.setOnClickListener(this)
        binding.cancelButton.setOnClickListener(this)
        binding.nextButton.setOnClickListener(this)
        val partnerInfo = PartnerInfoHolder.getInstance(this).partnerInfo
        samsungPay = SamsungPay(this, partnerInfo)
        cardManager = CardManager(this, partnerInfo)
        watchManager = WatchManager(this, partnerInfo)

        // In case issuer wants to add a card to Samsung Watch. Can be changed from MORE/Settings menu
        val mPrefs = PreferenceManager.getDefaultSharedPreferences(this)
        isWatchMode = mPrefs.getBoolean(getString(R.string.pref_watchMode), false)
        progressUtil = ProgressDialogHelper(this)
        val samsungPayCard = findViewById<CheckBox>(R.id.samsung_pay_card)

        if (isWatchMode) {
            samsungPayCard.isEnabled = false
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.get_wallet_info_button -> requestGetWalletInfo()
            R.id.cancel_button -> finish()
            R.id.next_button -> requestAddCard(
                if (PROVIDER_VACCINE_PASS == networkProvider)
                    binding.payloadNonEncryptedText.text.toString() else binding.payloadEncryptedEditText.text.toString(),
                networkProvider
            )
        }
    }

    private fun highlightKeyword(payload: String, color: Int): Spannable {
        val example: Spannable = SpannableString(payload)
        makeSpannableStringWithKeyword(example, SpayConstant.KEY_CLIENT_DEVICE_ID, color)
        makeSpannableStringWithKeyword(example, SpayConstant.KEY_CLIENT_WALLET_ACCOUNT_ID, color)
        return example
    }

    private fun makeSpannableStringWithKeyword(
        baseSpannable: Spannable,
        keyword: String,
        color: Int
    ) {
        val plainText = baseSpannable.toString()
        val startIndex = plainText.indexOf(keyword)
        val keywordLength = keyword.length
        if (startIndex != -1) {
            baseSpannable.setSpan(
                ForegroundColorSpan(color),
                startIndex,
                startIndex + keywordLength,
                0
            )
            baseSpannable.setSpan(
                StyleSpan(Typeface.BOLD),
                startIndex,
                startIndex + keywordLength,
                0
            )
        }
    }

    private fun requestAddCard(payload: String, tokenizationProvider: String) {
        Log.d(TAG, "addCard payload : $payload, tokenizationProvider : $tokenizationProvider")
        if (payload.isEmpty() || tokenizationProvider.isEmpty()) {
            Toast.makeText(
                this,
                getString(R.string.payload_or_provider_is_empty),
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        progressUtil.showProgressDialog()
        val cardDetail = Bundle()
        cardDetail.putString(EXTRA_PROVISION_PAYLOAD, payload) // encrypted payload
        if ((findViewById<View>(R.id.samsung_pay_card) as CheckBox).isChecked) {
            cardDetail.putBoolean(EXTRA_SAMSUNG_PAY_CARD, true)
        }
        var cardType = Card.CARD_TYPE_CREDIT_DEBIT
        if (PROVIDER_VACCINE_PASS == tokenizationProvider) {
            cardType = Card.CARD_TYPE_VACCINE_PASS
        }
        val addCardInfo = AddCardInfo(cardType, tokenizationProvider, cardDetail)
        val addCardListener: AddCardListener = object : AddCardListener {
            override fun onSuccess(status: Int, card: Card) {
                Log.d(TAG, "doAddCard onSuccess callback is called")
                progressUtil.dismissProgressDialog()
            }

            override fun onFail(errorCode: Int, errorData: Bundle) {
                Log.d(TAG, "doAddCard onFail callback is called, errorCode:$errorCode")
                if (errorData.containsKey(SpaySdk.EXTRA_ERROR_REASON_MESSAGE)) {
                    Log.e(
                        TAG,
                        "doAddCard onFail extra reason message: ${errorData.getString(SpaySdk.EXTRA_ERROR_REASON_MESSAGE)}"
                    )
                }
                ErrorResultUiHelper.showErrorResultPage(
                    applicationContext,
                    errorCode,
                    ErrorCode.getErrorCodeName(errorCode),
                    errorData
                )
                progressUtil.dismissProgressDialog()
            }

            override fun onProgress(currentCount: Int, totalCount: Int, bundleData: Bundle) {
                Log.d(TAG, "doAddCard onProgress : $currentCount / $totalCount")
                progressUtil.dismissProgressDialog()
            }
        }
        if (isWatchMode) { // In case of issuer wants to add a card to Samsung Watch
            watchManager.addCard(addCardInfo, addCardListener)
        } else {
            cardManager.addCard(addCardInfo, addCardListener)
        }
    }

    /* In case of VISA, enrollment payload MUST contain some information retrieved from Samsung Pay*/
    private fun requestGetWalletInfo() {
        progressUtil.showProgressDialog()
        val keys = ArrayList<String>()
        keys.add(SamsungPay.WALLET_DM_ID)
        keys.add(SamsungPay.DEVICE_ID)
        keys.add(SamsungPay.WALLET_USER_ID)
        if (isWatchMode) {
            // In case of Samsung Watch, device serial number should be used.
            keys.add(WatchManager.DEVICE_SERIAL_NUM)
        }
        val statusListener: StatusListener = object : StatusListener {
            override fun onSuccess(status: Int, walletData: Bundle) {
                progressUtil.dismissProgressDialog()

                // VISA requires DEVICE_ID for deviceID, WALLET_USER_ID for walletAccountId. Please refer VISA spec document.
                val clientDeviceId: String? = walletData.getString(SamsungPay.DEVICE_ID)
                val clientWalletAccountId: String? = walletData.getString(SamsungPay.WALLET_USER_ID)

                // Set clientDeviceId & clientWalletAccountId with values from Samsung Pay
                var currentPayload = binding.payloadNonEncryptedText.text.toString()
                if (currentPayload.isNotEmpty() && !clientDeviceId.isNullOrEmpty() && !clientWalletAccountId.isNullOrEmpty())
                {
                    currentPayload =
                        currentPayload.replace(
                            SpayConstant.KEY_WORD_DEVICE_ID,
                            clientDeviceId,
                            false
                        )
                            .replace(
                                SpayConstant.KEY_WORD_WALLET_ACCOUNT_ID,
                                clientWalletAccountId, false
                            )
                    val spannable = highlightKeyword(currentPayload, Color.BLUE)
                    val newPayload = VisaDecryptedPayloadTest.createJwe(
                        spannable.toString(),
                        VisaDecryptedPayloadTest.KID, VisaDecryptedPayloadTest.SHARED_SECRET
                    )
                    binding.payloadEncryptedEditText.setText(newPayload)
                    binding.payloadNonEncryptedText.setText(spannable)
                    binding.nextButton.isEnabled = true
                    // Now, it is ready to add a card to Samsung Pay for VISA
                }
            }

            override fun onFail(errorCode: Int, errorData: Bundle) {
                progressUtil.dismissProgressDialog()
                Log.d(TAG, "doGetWalletInfo onFail callback is called, errorCode:$errorCode")
                ErrorResultUiHelper.showErrorResultPage(
                    applicationContext,
                    errorCode,
                    ErrorCode.getErrorCodeName(errorCode),
                    errorData
                )
            }
        }
        if (isWatchMode) {
            watchManager.getWalletInfo(keys, statusListener)
        } else {
            samsungPay.getWalletInfo(keys, statusListener)
        }
    }

    private fun loadSamplePayload(provider: String): String {
        //first, check external storage
        val storagePath = Environment.getExternalStorageDirectory().path
        val path = "$storagePath/enrollPayload.txt"
        val payloadFile: File
        val sb = StringBuilder()
        var reader: BufferedReader? = null
        try {
            payloadFile = File(path)
            reader = BufferedReader(FileReader(payloadFile))
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                sb.append(line)
            }
            return sb.toString()
        } catch (e: IOException) {
            Log.e(TAG, "read payload from storage failed : " + e.message)

            //second, try to read from asset
            return when (provider) {
                PROVIDER_VISA -> SampleDataLoader.getDefaultVisaPayload(
                    applicationContext
                )
                PROVIDER_AMEX -> SampleDataLoader.getDefaultAMEXData(applicationContext)
                PROVIDER_MASTERCARD -> SampleDataLoader.getDefaultMasterCardData(
                    applicationContext
                )
                PROVIDER_VACCINE_PASS -> SampleDataLoader.getDefaultVaccinePassData(
                    applicationContext
                )
                else -> ""
            }
        } finally {
            if (reader != null) {
                try {
                    reader.close()
                } catch (e: IOException) {
                    Log.e(TAG, "loadSamplePayload :  ${e.message}")
                }
            }
        }

    }

    companion object {
        private val TAG = AddCardActivity::class.java.simpleName
    }
}