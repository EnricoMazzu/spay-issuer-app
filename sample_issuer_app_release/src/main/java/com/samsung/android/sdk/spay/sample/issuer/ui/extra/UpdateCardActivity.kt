package com.samsung.android.sdk.spay.sample.issuer.ui.extra

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.CompoundButton
import android.widget.Toast
import com.samsung.android.sdk.spay.sample.issuer.ui.helper.ErrorResultUiHelper.showErrorResultPage
import androidx.databinding.DataBindingUtil
import com.samsung.android.sdk.samsungpay.v2.card.Card
import com.samsung.android.sdk.spay.sample.issuer.util.PartnerInfoHolder
import com.samsung.android.sdk.samsungpay.v2.card.CardManager
import com.samsung.android.sdk.samsungpay.v2.card.CardListener
import com.samsung.android.sdk.spay.sample.issuer.R
import com.samsung.android.sdk.spay.sample.issuer.databinding.UpdateCardActivityBinding
import com.samsung.android.sdk.spay.sample.issuer.util.ErrorCode
import com.samsung.android.sdk.spay.sample.issuer.util.SpayConstant.SELECTED_CARD_ID

class UpdateCardActivity : Activity(), View.OnClickListener {
    private lateinit var binding: UpdateCardActivityBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.update_card_activity)
        val extras = intent.extras
        if (extras != null) {
            val cardId = extras.getString(SELECTED_CARD_ID)
            if (!cardId.isNullOrEmpty()) {
                binding.cardId.text = cardId
            }
        }
        binding.cardLinkSwitch.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean -> binding.cardLinkSwitch.isChecked = isChecked }
        binding.updateCardButton.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        if (v.id == R.id.update_card_button) {
            doUpdateCard()
        }
    }

    private fun doUpdateCard() {
        val partnerInfo = PartnerInfoHolder.getInstance(this).partnerInfo
        val cardManager = CardManager(this, partnerInfo)
        val cardInfoBundle = Bundle()
        cardInfoBundle.putBoolean(CardManager.EXTRA_ISSUER_APP_CARD_LINKED, binding.cardLinkSwitch.isChecked)
        val updateCard = Card(binding.cardId.text.toString(), cardInfoBundle)
        cardManager.updateCard(updateCard, object : CardListener {
            override fun onSuccess(status: Int, data: Bundle) {
                Log.d(TAG, "onSuccess callback is called")
                Toast.makeText(this@UpdateCardActivity, getString(R.string.on_success), Toast.LENGTH_LONG).show()
            }

            override fun onFail(errorCode: Int, errorData: Bundle) {
                showErrorResultPage(applicationContext, errorCode,
                        ErrorCode.getErrorCodeName(errorCode), errorData)
            }
        })
    }

    companion object {
        private const val TAG = "UpdateCardActivity"
    }
}