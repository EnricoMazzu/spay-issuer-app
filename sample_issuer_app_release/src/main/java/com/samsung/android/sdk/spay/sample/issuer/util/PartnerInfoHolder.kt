package com.samsung.android.sdk.spay.sample.issuer.util

import android.content.Context
import android.os.Bundle
import com.samsung.android.sdk.samsungpay.v2.PartnerInfo

import com.samsung.android.sdk.samsungpay.v2.SamsungPay
import com.samsung.android.sdk.samsungpay.v2.SpaySdk
import com.samsung.android.sdk.spay.sample.issuer.R

class PartnerInfoHolder private constructor(context: Context) {
    val partnerInfo: PartnerInfo

    init {
        val serviceId = context.resources.getString(R.string.gradle_product_id)
        val bundle = Bundle()
        bundle.putString(SamsungPay.PARTNER_SERVICE_TYPE, SpaySdk.ServiceType.APP2APP.toString())
        partnerInfo = PartnerInfo(serviceId, bundle)
    }

    companion object {
        @Volatile
        private var INSTANCE: PartnerInfoHolder? = null

        fun getInstance(context: Context): PartnerInfoHolder =
                INSTANCE ?: synchronized(this) {
                    INSTANCE ?: PartnerInfoHolder(context).also { INSTANCE = it }
                }
    }


}