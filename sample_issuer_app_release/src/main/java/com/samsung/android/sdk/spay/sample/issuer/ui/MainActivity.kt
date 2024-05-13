package com.samsung.android.sdk.spay.sample.issuer.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceActivity
import android.preference.PreferenceManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.RadioGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.samsung.android.sdk.spay.sample.issuer.ui.helper.ProgressDialogHelper
import com.samsung.android.sdk.samsungpay.v2.payment.PaymentManager
import com.samsung.android.sdk.samsungpay.v2.card.CardManager
import androidx.databinding.DataBindingUtil
import com.samsung.android.sdk.samsungpay.v2.*
import com.samsung.android.sdk.samsungpay.v2.card.Card
import com.samsung.android.sdk.spay.sample.issuer.util.PartnerInfoHolder
import com.samsung.android.sdk.spay.sample.issuer.util.SpayConstant
import com.samsung.android.sdk.spay.sample.issuer.ui.provision.AddCardActivity
import com.samsung.android.sdk.spay.sample.issuer.ui.extra.UpdateAdditionalServiceActivity
import com.samsung.android.sdk.spay.sample.issuer.ui.mst.StartMstActivity
import com.samsung.android.sdk.samsungpay.v2.card.IdvVerifyInfo
import com.samsung.android.sdk.spay.sample.issuer.ui.helper.ErrorResultUiHelper
import com.samsung.android.sdk.spay.sample.issuer.ui.extra.UpdateCardActivity
import com.samsung.android.sdk.spay.sample.issuer.ui.extra.StartSimplePayRefundActivity
import com.samsung.android.sdk.samsungpay.v2.card.GetCardListener
import com.samsung.android.sdk.samsungpay.v2.payment.CardInfo
import com.samsung.android.sdk.spay.sample.issuer.BuildConfig
import com.samsung.android.sdk.spay.sample.issuer.R
import com.samsung.android.sdk.spay.sample.issuer.ui.helper.GetAllCardsSelectorActivity
import com.samsung.android.sdk.spay.sample.issuer.ui.status.SamsungPayStatusActivity
import com.samsung.android.sdk.spay.sample.issuer.databinding.SampleIssuerMainActivityBinding
import com.samsung.android.sdk.spay.sample.issuer.ui.extra.CardIdvDialogFragment
import com.samsung.android.sdk.spay.sample.issuer.ui.status.MyPreferenceActivity
import com.samsung.android.sdk.spay.sample.issuer.util.ErrorCode
import java.util.ArrayList

class MainActivity : Activity(), View.OnClickListener {
    // Only if getSamsungPayStatus returns SPAY_READY, APIs can be tested
    private var isSpayReady = false

    // In case issuer wants to add a card to Samsung Watch. Can be changed from MORE/Settings menu
    private var isWatchMode = false

    private lateinit var progressUtil: ProgressDialogHelper

    // For getSamsungPayStatus, activateSamsungPay, goToUpdatePage, getSamsungPayTransitStatus
    private lateinit var samsungPay: SamsungPay

    // For Samsung Watch: getSamsungPayStatus, getAllCards, activateSamsungPay, goToUpdatePage
    private lateinit var watchManager: WatchManager

    // (KR issuers only)
    // For startSimplePay
    private lateinit var paymentManager: PaymentManager

    // For getAllCards, verifyCardIDv
    private lateinit var cardManager: CardManager

    private lateinit var binding: SampleIssuerMainActivityBinding

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.sample_issuer_main_activity)

        /* APIs available for each region will be updated upon country selection*/
        binding.countrySelectGroup.setOnCheckedChangeListener { _: RadioGroup?, checkedId: Int ->
            updateLayoutForCountrySelection(
                checkedId
            )
        }

        actionBar?.title = getString(R.string.app_name) + " " + BuildConfig.VERSION_NAME

        //user guide: getSamsungPayStatus MUST be the first step before using any API

        // API buttons
        val activity = this
        binding.apply {
            getSamsungPayStatusButton.setOnClickListener(activity)
            activateSamsungPayButton.setOnClickListener(activity)
            goToUpdatePageButton.setOnClickListener(activity)
            getAllCardsButton.setOnClickListener(activity)
            addCardButton.setOnClickListener(activity)
            updateCardButton.setOnClickListener(activity)
            verifyCardIdvButton.setOnClickListener(activity)
            startSimplePayButton.setOnClickListener(activity)
            startSimplePayForRefundButton.setOnClickListener(activity)
            startDeepLink.setOnClickListener(activity)
            updateAdditionalServiceButton.setOnClickListener(activity)
            startMstButton.setOnClickListener(activity)
            transitCardStatusButton.setOnClickListener(activity)
        }

        val partnerInfo = PartnerInfoHolder.getInstance(this).partnerInfo
        samsungPay = SamsungPay(this, partnerInfo)
        paymentManager = PaymentManager(this, partnerInfo)
        cardManager = CardManager(this, partnerInfo)
        watchManager = WatchManager(this, partnerInfo)
        progressUtil = ProgressDialogHelper(this)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                SpayConstant.MY_PERMISSIONS_REQUEST_READ_STORAGE
            )
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val itemId = item.itemId
        if (itemId == R.id.action_launch_preference) {
            val intent = Intent(this, MyPreferenceActivity::class.java)
            intent.putExtra(PreferenceActivity.EXTRA_NO_HEADERS, false)
            startActivityForResult(intent, SpayConstant.REQUEST_PREFERENCE_ACTIVITY)
        }
        return true
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.get_samsung_pay_status_button -> requestGetSamsungPayStatus()
            R.id.activate_samsung_pay_button -> if (isWatchMode) {
                watchManager.activateSamsungPay() // For Samsung Watch
            } else {
                samsungPay.activateSamsungPay()
            }
            R.id.go_to_update_page_button -> if (isWatchMode) {
                watchManager.goToUpdatePage() // For Samsung Watch
            } else {
                samsungPay.goToUpdatePage()
            }
            R.id.get_all_cards_button -> requestGetAllCards(SpayConstant.REQUEST_GET_ALL_CARDS_FOR_API_VERIFICATION)
            R.id.add_card_button -> {
                val addCardIntent = Intent(this, AddCardActivity::class.java)
                startActivity(addCardIntent)
            }
            R.id.update_card_button -> requestGetAllCards(SpayConstant.REQUEST_GET_ALL_CARDS_FOR_UPDATE_CARD)
            R.id.verify_card_idv_button -> requestGetAllCards(SpayConstant.REQUEST_GET_ALL_CARDS_FOR_VERIFY_CARD_IDV)
            R.id.start_simple_pay_button -> requestGetAllCards(SpayConstant.REQUEST_GET_ALL_CARDS_FOR_START_SIMPLE_PAY)
            R.id.start_deep_link -> requestGetAllCards(SpayConstant.REQUEST_GET_ALL_CARD_FOR_START_DEEP_LINK)
            R.id.start_simple_pay_for_refund_button -> requestGetAllCards(SpayConstant.REQUEST_GET_ALL_CARDS_FOR_START_SIMPLE_PAY_REFUND)
            R.id.update_additional_service_button -> {
                val updateAdditionalServiceIntent =
                    Intent(this, UpdateAdditionalServiceActivity::class.java)
                startActivity(updateAdditionalServiceIntent)
            }
            R.id.start_mst_button -> {
                val mstIntent = Intent(this, StartMstActivity::class.java)
                startActivity(mstIntent)
            }
            R.id.transit_card_status_button -> requestGetTransitCardStatus()

        }
    }

    override fun onResume() {
        super.onResume()
        if (binding.countrySelectGroup.checkedRadioButtonId == -1) {
            binding.countryGlobalButton.isChecked = true
        }
    }

    fun executeVerifyCardIdv(cardId: String, code: String?, idvType: String?) {
        Log.d(TAG, "IdvVerifyInfo cardId : $cardId")
        val cardInfoData = Bundle()
        val idvVerifyInfo = IdvVerifyInfo(cardId, code, cardInfoData)
        idvVerifyInfo.iDnVType = idvType

        cardManager.verifyCardIdv(idvVerifyInfo, object : StatusListener {
            override fun onSuccess(status: Int, data: Bundle) {
                Log.d(TAG, "VerifyCardIdv onSuccess callback is called")
                Toast.makeText(
                    applicationContext,
                    getString(R.string.on_success),
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onFail(errorCode: Int, errorData: Bundle) {
                Log.e(TAG, "VerifyCardIdv onFail callback is called, errorCode:$errorCode")
                ErrorResultUiHelper.showErrorResultPage(
                    applicationContext, errorCode,
                    ErrorCode.getErrorCodeName(errorCode), errorData
                )
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            var selectedId: String? = null

            data?.let {
                selectedId = it.getStringExtra(SpayConstant.SELECTED_CARD_ID)
            }

            when (requestCode) {
                SpayConstant.REQUEST_PREFERENCE_ACTIVITY -> {
                    isSpayReady = false
                    updateLayout()
                }
                SpayConstant.REQUEST_GET_ALL_CARDS_FOR_START_SIMPLE_PAY -> verifyCardIdSelectedAndDoNextAction(
                    selectedId, getString(
                        R.string.select_card_to_start_simple_pay
                    ), requestCode
                )
                SpayConstant.REQUEST_GET_SAMSUNG_PAY_STATUS_RESULT -> updateLayout()
                SpayConstant.REQUEST_GET_ALL_CARDS_FOR_UPDATE_CARD -> verifyCardIdSelectedAndDoNextAction(
                    selectedId, getString(
                        R.string.select_card_to_start_update_card
                    ), requestCode
                )
                SpayConstant.REQUEST_GET_ALL_CARDS_FOR_VERIFY_CARD_IDV -> verifyCardIdSelectedAndDoNextAction(
                    selectedId, getString(
                        R.string.select_card_to_verify_card_idv
                    ), requestCode
                )
                SpayConstant.REQUEST_GET_ALL_CARDS_FOR_START_SIMPLE_PAY_REFUND -> verifyCardIdSelectedAndDoNextAction(
                    selectedId, getString(
                        R.string.select_card_to_start_simple_pay_for_refund
                    ), requestCode
                )
                SpayConstant.REQUEST_GET_ALL_CARD_FOR_START_DEEP_LINK -> verifyCardIdSelectedAndDoNextAction(
                    selectedId, getString(
                        R.string.select_card_to_start_deep_link
                    ), requestCode
                )
            }
        }
    }

    private fun verifyCardIdSelectedAndDoNextAction(selectedId: String?, msg: String, action: Int) {
        if (selectedId.isNullOrEmpty()) {
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
        } else {
            // Trigger next action as per requested action
            when (action) {
                SpayConstant.REQUEST_GET_ALL_CARDS_FOR_START_SIMPLE_PAY -> requestStartSimplePay(
                    selectedId
                )
                SpayConstant.REQUEST_GET_ALL_CARDS_FOR_UPDATE_CARD -> {
                    val updateCardIntent = Intent(this, UpdateCardActivity::class.java)
                    updateCardIntent.putExtra(SpayConstant.SELECTED_CARD_ID, selectedId)
                    startActivity(updateCardIntent)
                }
                SpayConstant.REQUEST_GET_ALL_CARDS_FOR_VERIFY_CARD_IDV -> requestVerifyCardIdv(
                    selectedId
                )
                SpayConstant.REQUEST_GET_ALL_CARDS_FOR_START_SIMPLE_PAY_REFUND -> {
                    val refundIntent = Intent(this, StartSimplePayRefundActivity::class.java)
                    refundIntent.putExtra(SpayConstant.SELECTED_CARD_ID, selectedId)
                    startActivity(refundIntent)
                }
                SpayConstant.REQUEST_GET_ALL_CARD_FOR_START_DEEP_LINK -> {
                    val base = "samsungpay://launch?action=selectcard&tokenId="
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(base + selectedId))
                    val cn = intent.resolveActivity(packageManager)
                    if (cn == null) {
                        Toast.makeText(this, "no activity for deeplink", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this, "activity found for deeplink", Toast.LENGTH_LONG)
                            .show()
                        Log.d(TAG, "deep link = $intent")
                        startActivity(intent)
                    }
                }

            }
        }
    }

    private fun updateLayout() {
        val mPrefs = PreferenceManager.getDefaultSharedPreferences(this)
        isWatchMode = mPrefs.getBoolean(getString(R.string.pref_watchMode), false)

        // If SPAY_READY is not the case, block all the UI component except basic APIs such as getSamsungPayStatus, activateSamsungPay, goToUpdatePage
        if (isSpayReady) {
            binding.callGetSamsungPayFirstTextView.visibility = View.GONE
            binding.dynamicButtonsLayout.visibility = View.VISIBLE
        } else {
            binding.callGetSamsungPayFirstTextView.visibility = View.VISIBLE
            binding.dynamicButtonsLayout.visibility = View.GONE
        }
        if (isWatchMode) {
            updateLayoutForWatch()
        } else {
            updateLayoutForCountrySelection(binding.countrySelectGroup.checkedRadioButtonId)
        }
    }

    override fun startActivityForResult(intent: Intent, requestCode: Int) {
        intent.putExtra(SpayConstant.REQUEST_CODE, requestCode)
        super.startActivityForResult(intent, requestCode)
    }

    private fun requestGetAllCards(requestCode: Int) {
        progressUtil.showProgressDialog()
        val getCardListener: GetCardListener = object : GetCardListener {
            override fun onSuccess(cards: List<Card>) {
                Log.d(TAG, "onSuccess callback is called, list.size= ${cards.size}")
                progressUtil.dismissProgressDialog()
                sendToCardSelectorActivity(cards, requestCode)
            }

            override fun onFail(errorCode: Int, errorData: Bundle) {
                Log.d(TAG, "onFail callback is called, errorCode:$errorCode")
                progressUtil.dismissProgressDialog()
                ErrorResultUiHelper.showErrorResultPage(
                    applicationContext, errorCode,
                    ErrorCode.getErrorCodeName(errorCode), errorData
                )
            }
        }
        if (isWatchMode) {
            watchManager.getAllCards(null, getCardListener)
        } else {
            cardManager.getAllCards(null, getCardListener)
        }
    }

    private fun sendToCardSelectorActivity(cards: List<Card>, requestCode: Int) {
        val intent = Intent(this, GetAllCardsSelectorActivity::class.java)
        val arrayList = ArrayList(cards)
        intent.putParcelableArrayListExtra(SpayConstant.CARD_INFO, arrayList)
        startActivityForResult(intent, requestCode)
    }

    private fun sendToStatusActivity(status: Int, bundle: Bundle) {
        val intent = Intent(applicationContext, SamsungPayStatusActivity::class.java)
        intent.putExtra(SpayConstant.SAMSUNG_PAY_STATUS_CODE_KEY, status)
        var statusName = ErrorCode.getErrorCodeName(status)
        if (statusName.isNullOrEmpty()) {
            statusName = applicationContext.getString(R.string.unknown)
        }
        intent.putExtra(SpayConstant.SAMSUNG_PAY_STATUS_NAME_KEY, statusName)
        intent.putExtra(SpayConstant.SAMSUNG_PAY_STATUS_BUNDLE, bundle)
        startActivityForResult(intent, SpayConstant.REQUEST_GET_SAMSUNG_PAY_STATUS_RESULT)
    }

    private fun requestGetSamsungPayStatus() {
        progressUtil.showProgressDialog()
        val statusListener: StatusListener = object : StatusListener {
            override fun onSuccess(status: Int, bundle: Bundle) {
                Log.d(TAG, "onSuccess callback is called, status=$status,bundle:$bundle")
                progressUtil.dismissProgressDialog()
                isSpayReady = status == SpaySdk.SPAY_READY // update ready status

                // Send status & bundle so that it can display dialog in case of SPAY_NOT_READY
                // In case of ERROR_SPAY_SETUP_NOT_COMPLETED, map OK button to SamsungPay.activateSamsungPay
                // In case of ERROR_SPAY_APP_NEED_TO_UPDATE, map OK button to SamsungPay.goToUpdatePage
                sendToStatusActivity(status, bundle)
            }

            override fun onFail(errorCode: Int, bundle: Bundle) {
                Log.d(TAG, "onFail callback is called, i=$errorCode,bundle:$bundle")
                progressUtil.dismissProgressDialog()
                isSpayReady = false
                ErrorResultUiHelper.showErrorResultPage(
                    applicationContext, errorCode,
                    ErrorCode.getErrorCodeName(errorCode), bundle
                )
            }
        }
        if (isWatchMode) {
            watchManager.getSamsungPayStatus(statusListener)
        } else {
            samsungPay.getSamsungPayStatus(statusListener)
        }
    }

    private fun requestStartSimplePay(cardId: String?) {
        progressUtil.showProgressDialog()
        val metaData = Bundle()
        metaData.putString(
            PaymentManager.EXTRA_PAY_OPERATION_TYPE,
            PaymentManager.PAY_OPERATION_TYPE_PAYMENT
        )
        metaData.putInt(
            PaymentManager.EXTRA_TRANSACTION_TYPE,
            PaymentManager.TRANSACTION_TYPE_MST or PaymentManager.TRANSACTION_TYPE_NFC
        )
        val cardInfo = CardInfo.Builder().setCardId(cardId).setCardMetaData(metaData).build()
        paymentManager.startSimplePay(cardInfo, object : StatusListener {
            override fun onSuccess(status: Int, data: Bundle) {
                progressUtil.dismissProgressDialog()
                Log.d(TAG, "onSuccess status : $status  data : $data content : " +
                        "${data.getString(PaymentManager.EXTRA_ISSUER_NAME)}")
            }

            override fun onFail(errorCode: Int, data: Bundle) {
                progressUtil.dismissProgressDialog()
                Log.d(TAG, "onFail errorCode : $errorCode, data : $data")
                ErrorResultUiHelper.showErrorResultPage(
                    applicationContext, errorCode,
                    ErrorCode.getErrorCodeName(errorCode), data
                )
            }
        })
    }

    private fun requestVerifyCardIdv(selectedCardId: String?) {
        val fm = fragmentManager
        val mCardIdvDialogFragment = CardIdvDialogFragment()
        val bundle = Bundle()
        bundle.putString(SpayConstant.SELECTED_CARD_ID, selectedCardId)
        mCardIdvDialogFragment.arguments = bundle
        mCardIdvDialogFragment.show(fm, "CardIdvDialogFragment")
    }

    private fun requestGetTransitCardStatus() {
        val statusListener: StatusListener = object : StatusListener {
            override fun onSuccess(status: Int, bundle: Bundle) {
                val result: String = if (status == SamsungPay.SPAY_HAS_TRANSIT_CARD) {
                    getString(R.string.has_transit_card)
                } else {
                    getString(R.string.no_transit_card)
                }
                Toast.makeText(applicationContext, result, Toast.LENGTH_LONG).show()
            }

            override fun onFail(errorCode: Int, bundle: Bundle) {
                ErrorResultUiHelper.showErrorResultPage(
                    applicationContext, errorCode,
                    ErrorCode.getErrorCodeName(errorCode), bundle
                )
            }
        }
        samsungPay.getSamsungPayTransitStatus(statusListener)
    }

    private fun updateLayoutForWatch() {
        // For Samsung Watch, there is no difference between countries. So, no need for country selection mode
        binding.apply {
            countrySelectGroup.visibility = View.GONE
            globalUsButtonsLayout.visibility = View.GONE
            koreaButtonsLayout.visibility = View.GONE
            startSimplePayButton.visibility = View.GONE
            startDeepLink.visibility = View.GONE
        }

    }

    private fun updateLayoutForCountrySelection(checkedId: Int) {
        binding.apply {
            countrySelectGroup.visibility = View.VISIBLE
            startSimplePayButton.visibility = View.VISIBLE
            startDeepLink.visibility = View.VISIBLE
            when (checkedId) {
                R.id.country_global_button -> {
                    globalUsButtonsLayout.visibility = View.VISIBLE
                    updateCardButton.visibility = View.GONE
                    updateAdditionalServiceButton.visibility = View.VISIBLE
                    koreaButtonsLayout.visibility = View.GONE
                }
                R.id.country_us_button -> {
                    globalUsButtonsLayout.visibility = View.VISIBLE
                    updateCardButton.visibility = View.VISIBLE
                    updateAdditionalServiceButton.visibility = View.GONE
                    koreaButtonsLayout.visibility = View.GONE
                }
                R.id.country_kr_button -> {
                    koreaButtonsLayout.visibility = View.VISIBLE
                    globalUsButtonsLayout.visibility = View.GONE
                }
            }
        }
    }

    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }
}