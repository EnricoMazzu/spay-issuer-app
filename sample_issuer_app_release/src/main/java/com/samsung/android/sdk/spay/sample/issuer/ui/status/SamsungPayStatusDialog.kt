package com.samsung.android.sdk.spay.sample.issuer.ui.status

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.preference.PreferenceManager
import android.util.Log
import android.widget.TextView
import com.samsung.android.sdk.samsungpay.v2.SamsungPay
import com.samsung.android.sdk.samsungpay.v2.WatchManager
import com.samsung.android.sdk.spay.sample.issuer.R
import com.samsung.android.sdk.spay.sample.issuer.util.ErrorCode
import com.samsung.android.sdk.spay.sample.issuer.util.PartnerInfoHolder
import kotlin.jvm.Synchronized

class SamsungPayStatusDialog private constructor() {
    private var alertDialog: AlertDialog? = null

    fun showSamsungPayStatusErrorDialog(activity: Activity, error: Int) {

        alertDialog?.let{
            if (it.isShowing) {
                it.dismiss()
                alertDialog = null
            }
        }

        val dialogListener =
            DialogInterface.OnClickListener { dialog: DialogInterface, which: Int ->
                Log.d(TAG, ErrorCode.getErrorCodeName(error))
                when (which) {
                    AlertDialog.BUTTON_POSITIVE -> {
                        val pInfo = PartnerInfoHolder.getInstance(activity).partnerInfo
                        if (SamsungPay.ERROR_SPAY_APP_NEED_TO_UPDATE == error) {
                            if (isWatchMode(activity)) {
                                val watchManager = WatchManager(activity, pInfo)
                                watchManager.goToUpdatePage()
                            } else {
                                val spay = SamsungPay(activity, pInfo)
                                spay.goToUpdatePage()
                            }
                        } else if (SamsungPay.ERROR_SPAY_SETUP_NOT_COMPLETED == error) {
                            if (isWatchMode(activity)) {
                                val watchManager = WatchManager(activity, pInfo)
                                watchManager.activateSamsungPay()
                            } else {
                                val spay = SamsungPay(activity, pInfo)
                                spay.activateSamsungPay()
                            }
                        }
                        dialog.dismiss()
                    }
                    AlertDialog.BUTTON_NEGATIVE -> dialog.cancel()
                    else -> dialog.dismiss()
                }
            }
        if (!activity.isFinishing) {
            val builder = AlertDialog.Builder(activity)
            alertDialog = builder.setTitle(getErrorDialogTitle(activity, error))
                .setMessage(getErrorDialogMessage(activity, error))
                .setPositiveButton(activity.getString(R.string.ok), dialogListener)
                .setNegativeButton(activity.getString(R.string.cancel), dialogListener)
                .setCancelable(false)
                .show()
            val textView = alertDialog?.findViewById<TextView>(android.R.id.message)
            textView?.textSize = 12f
        }
    }

    private fun getErrorDialogTitle(mContext: Context, error: Int): String {
        return when (error) {
            SamsungPay.ERROR_SPAY_APP_NEED_TO_UPDATE -> mContext.getString(R.string.go_to_samsung_pay_update_popup_title)
            SamsungPay.ERROR_SPAY_SETUP_NOT_COMPLETED -> mContext.getString(R.string.go_to_samsung_pay_start_popup_title)
            else -> mContext.getString(R.string.error)
        }
    }

    private fun getErrorDialogMessage(mContext: Context, error: Int): String {
        val errorMessage = ErrorCode.getErrorCodeName(error) + " : " + error
        return if (error == SamsungPay.ERROR_PARTNER_SERVICE_TYPE) {
            """
     $errorMessage
     ${mContext.getString(R.string.partner_did_not_set_service_type)}
     """.trimIndent()
        } else {
            errorMessage
        }
    }

    private fun isWatchMode(context: Context?): Boolean {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getBoolean(context!!.getString(R.string.pref_watchMode), false)
    }

    companion object {
        private var mSamsungPayStatusDialog: SamsungPayStatusDialog? = null
        private const val TAG = "SamsungPayStatusDialog"

        @get:Synchronized
        val instance: SamsungPayStatusDialog
            get() {
                mSamsungPayStatusDialog?.let{
                    return it
                }?: return SamsungPayStatusDialog()
            }
    }
}