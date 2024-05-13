package com.samsung.android.sdk.spay.sample.issuer.ui.helper

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.AdapterView
import com.samsung.android.sdk.spay.sample.issuer.adapter.GetAllCardsListAdapter
import androidx.databinding.DataBindingUtil
import com.samsung.android.sdk.samsungpay.v2.card.Card
import com.samsung.android.sdk.spay.sample.issuer.R
import com.samsung.android.sdk.spay.sample.issuer.databinding.GetAllCardsActivityBinding
import com.samsung.android.sdk.spay.sample.issuer.util.SpayConstant
import com.samsung.android.sdk.spay.sample.issuer.util.SpayConstant.REQUEST_CODE
import com.samsung.android.sdk.spay.sample.issuer.util.SpayConstant.REQUEST_GET_ALL_CARDS_FOR_API_VERIFICATION
import com.samsung.android.sdk.spay.sample.issuer.util.SpayConstant.REQUEST_GET_ALL_CARDS_FOR_START_SIMPLE_PAY
import com.samsung.android.sdk.spay.sample.issuer.util.SpayConstant.REQUEST_GET_ALL_CARDS_FOR_START_SIMPLE_PAY_REFUND
import com.samsung.android.sdk.spay.sample.issuer.util.SpayConstant.REQUEST_GET_ALL_CARDS_FOR_UPDATE_CARD
import com.samsung.android.sdk.spay.sample.issuer.util.SpayConstant.REQUEST_GET_ALL_CARDS_FOR_VERIFY_CARD_IDV
import com.samsung.android.sdk.spay.sample.issuer.util.SpayConstant.REQUEST_GET_ALL_CARD_FOR_START_DEEP_LINK
import com.samsung.android.sdk.spay.sample.issuer.util.SpayConstant.SELECTED_CARD_ID
import java.util.ArrayList

class GetAllCardsSelectorActivity : Activity() {
    private var selectedId: String? = null
    private var requestForIDnV = false
    private val newList = ArrayList<Card>()
    private lateinit var cardListAdapter: GetAllCardsListAdapter
    private lateinit var binding: GetAllCardsActivityBinding
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.get_all_cards_activity)
        val intent = intent
        updateTitle(intent)

        val cardList: ArrayList<Card>? = intent.getParcelableArrayListExtra(SpayConstant.CARD_INFO)

        updateNewCardList(cardList)
        cardListAdapter = GetAllCardsListAdapter(this, newList)
        cardListAdapter.notifyDataSetChanged()
        binding.listView.adapter = cardListAdapter



        binding.listView.onItemClickListener = AdapterView.OnItemClickListener { adapterView: AdapterView<*>, _: View?, i: Int, _: Long ->
            selectedId = (adapterView.adapter as GetAllCardsListAdapter).getSelectedCardId(i)
        }
        val finalCardListSize = newList.size
        binding.done.setOnClickListener {
            if (finalCardListSize != 0) {
                val resultIntent = Intent()
                resultIntent.putExtra(SELECTED_CARD_ID, selectedId)
                setResult(RESULT_OK, resultIntent)
            }
            finish()
        }
    }

    private fun updateNewCardList(list: ArrayList<Card>?) {
        newList.clear()
        if (list != null && list.isNotEmpty()) {
            if (requestForIDnV) {
                for (card in list) {
                    if (TextUtils.equals(card.cardStatus, Card.PENDING_PROVISION)) {
                        newList.add(card)
                    }
                }
            } else {
                newList.addAll(list)
            }
        }
        if (newList.isNotEmpty()) {
            binding.totalCountValue.text = newList.size.toString()
        } else {
            binding.userGuideDescription.setText(R.string.no_card_for_the_request)
        }
    }

    private fun updateTitle(intent: Intent) {
        val extras = intent.extras
        if (extras != null) {
            when (extras.getInt(REQUEST_CODE, -1)) {
                REQUEST_GET_ALL_CARDS_FOR_VERIFY_CARD_IDV -> {
                    setTitle(R.string.verify_card_idv)
                    requestForIDnV = true
                }
                REQUEST_GET_ALL_CARDS_FOR_START_SIMPLE_PAY -> setTitle(R.string.start_simple_pay)
                REQUEST_GET_ALL_CARDS_FOR_UPDATE_CARD -> setTitle(R.string.update_card)
                REQUEST_GET_ALL_CARDS_FOR_START_SIMPLE_PAY_REFUND -> setTitle(R.string.start_simple_pay_for_refund)
                REQUEST_GET_ALL_CARD_FOR_START_DEEP_LINK -> title = getString(R.string.start_deep_link)
                REQUEST_GET_ALL_CARDS_FOR_API_VERIFICATION -> setDefaultTitleAndNoGuide()
                else -> setDefaultTitleAndNoGuide()
            }
        } else {
            setDefaultTitleAndNoGuide()
        }
    }

    private fun setDefaultTitleAndNoGuide() {
        setTitle(R.string.get_all_cards)
        binding.userGuideDescription.visibility = View.GONE
    }
}