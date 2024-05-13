package com.samsung.android.sdk.spay.sample.issuer.ui.helper

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.samsung.android.sdk.spay.sample.issuer.R
import com.samsung.android.sdk.spay.sample.issuer.ui.status.ErrorResultActivity

object ErrorResultUiHelper {
    fun showErrorResultPage(
            context: Context,
            errorCode: Int,
            errorCodeName: String?,
            bundle: Bundle?
    ) {
        val intent = Intent(context, ErrorResultActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.putExtra(ErrorResultActivity.ERROR_CODE_KEY, errorCode)
        intent.putExtra(
                ErrorResultActivity.ERROR_CODE_NAME_KEY,
                if (errorCodeName.isNullOrEmpty()) context.getString(R.string.unknown) else errorCodeName
        )
        val msg = context.getString(R.string.on_fail)
        intent.putExtra(ErrorResultActivity.ERROR_MSG_KEY, msg)
        intent.putExtra(ErrorResultActivity.ERROR_BUNDLE_KEY, bundle)
        context.startActivity(intent)
    }
}