package com.samsung.android.sdk.spay.sample.issuer.ui.extra

import android.app.DialogFragment
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.samsung.android.sdk.spay.sample.issuer.R
import com.samsung.android.sdk.spay.sample.issuer.databinding.VerifyCardIdvDialogBinding
import com.samsung.android.sdk.spay.sample.issuer.util.SpayConstant
import com.samsung.android.sdk.spay.sample.issuer.ui.MainActivity

class CardIdvDialogFragment : DialogFragment(), View.OnClickListener {
    private lateinit var mContext: Context
    private lateinit var binding: VerifyCardIdvDialogBinding
    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        val dialog = dialog
        dialog.setTitle(R.string.verify_card_idv)
        binding =
                DataBindingUtil.inflate(inflater, R.layout.verify_card_idv_dialog, container, false)
        val extras = arguments
        if (extras != null) {
            val selectedCardId = extras.getString(SpayConstant.SELECTED_CARD_ID)
            if (!selectedCardId.isNullOrEmpty()) {
                binding.cardId.text = selectedCardId
            }
        }
        binding.verifyCardIdvOkButton.setOnClickListener(this)
        binding.verifyCardIdvCancelButton.setOnClickListener(this)
        return binding.root
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.verify_card_idv_ok_button -> {
                val cardId = binding.cardId.text.toString()
                val code = binding.idvCodeEditText.text.toString()
                val idvType = binding.idvType.selectedItem.toString()
                (mContext as MainActivity).executeVerifyCardIdv(cardId, code, idvType)
                dismiss()
            }
            R.id.verify_card_idv_cancel_button -> dismiss()
        }
    }
}