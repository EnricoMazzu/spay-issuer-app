package com.samsung.android.sdk.spay.sample.issuer.ui.helper

import android.app.Activity
import android.app.ProgressDialog
import com.samsung.android.sdk.spay.sample.issuer.R

class ProgressDialogHelper(private val activity: Activity?) {
    private var progressDialog: ProgressDialog? = null
    fun showProgressDialog() {
        if (activity == null) {
            progressDialog?.let {
                if (it.isShowing) {
                    it.dismiss()
                    progressDialog = null
                }
            }
            return
        }

        progressDialog?.let {
            if (!it.isShowing) it.show()
        } ?: run { progressDialog = ProgressDialog.show(activity, null, activity?.getString(R.string.processing), true) }

    }

    fun dismissProgressDialog() {
        if (activity != null && (activity.isFinishing || activity.isDestroyed)) {
            return
        }
        progressDialog?.dismiss()
        progressDialog = null
    }
}