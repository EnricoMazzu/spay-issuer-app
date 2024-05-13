package com.samsung.android.sdk.spay.sample.issuer.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.samsung.android.sdk.samsungpay.v2.card.CardManager
import com.samsung.android.sdk.samsungpay.v2.card.AddCardInfo
import com.samsung.android.sdk.samsungpay.v2.card.Card
import com.samsung.android.sdk.spay.sample.issuer.databinding.GetAllCardsListBinding
import java.util.*

class GetAllCardsListAdapter(
        private val mContext: Context,
        values: List<Card>
) : ArrayAdapter<Card>(mContext, -1, values) {
    private val cardList = ArrayList<Card>()

    init {
        cardList.addAll(values)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val cardListView = convertView ?: run {
            val binding = GetAllCardsListBinding.inflate(LayoutInflater.from(mContext), parent, false)
            val view = binding.root
            val holder = ViewHolder()
            holder.cardLast4DPAN = binding.cardLast4Dpan
            holder.cardLast4FPAN = binding.cardLast4Fpan
            holder.cardId = binding.valueCardId
            holder.cardBrand = binding.valueCardBrand
            holder.cardIssuer = binding.valueCardIssuer
            holder.cardType = binding.valueCardType
            holder.cardStatus = binding.valueStatus
            holder.isSamsungPayCard = binding.valueIsSamsungPayCard
            view.tag = holder
            view
        }
        val viewHolder = cardListView.tag as ViewHolder
        setCardView(viewHolder, position)
        return cardListView

    }

    fun getSelectedCardId(position: Int): String {
        return cardList[position].cardId
    }

    private fun setCardView(viewHolder: ViewHolder, position: Int) {
        val cardInfo = cardList[position]
        val bundle = cardInfo.cardInfo
        if (bundle != null) {
            viewHolder.cardLast4DPAN?.text = bundle.getString(CardManager.EXTRA_LAST4_DPAN)
            viewHolder.cardLast4FPAN?.text = bundle.getString(CardManager.EXTRA_LAST4_FPAN)
            viewHolder.cardIssuer?.text = bundle.getString(CardManager.EXTRA_ISSUER_NAME)
            viewHolder.cardType?.text = bundle.getString(CardManager.EXTRA_CARD_TYPE)
            viewHolder.isSamsungPayCard?.text =
                    if (bundle.getBoolean(
                                    AddCardInfo.EXTRA_SAMSUNG_PAY_CARD,
                                    false
                            )
                    ) "TRUE" else "FALSE"
        }
        viewHolder.cardId?.text = cardInfo.cardId
        val brand = cardInfo.cardBrand
        if (brand != null) {
            viewHolder.cardBrand?.text = brand.toString()
        }
        viewHolder.cardStatus?.text = cardInfo.cardStatus
    }

    internal class ViewHolder {
        var cardLast4DPAN: TextView? = null
        var cardLast4FPAN: TextView? = null
        var cardId: TextView? = null
        var cardBrand: TextView? = null
        var cardIssuer: TextView? = null
        var cardType: TextView? = null
        var cardStatus: TextView? = null
        var isSamsungPayCard: TextView? = null
    }

    companion object {
        const val TAG = "GetAllCardsListAdapter"
    }
}