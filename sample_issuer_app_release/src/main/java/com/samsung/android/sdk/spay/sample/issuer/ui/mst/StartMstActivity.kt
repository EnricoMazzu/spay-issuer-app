package com.samsung.android.sdk.spay.sample.issuer.ui.mst

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import com.samsung.android.sdk.spay.sample.issuer.ui.helper.ErrorResultUiHelper.showErrorResultPage
import com.samsung.android.sdk.samsungpay.v2.payment.MstManager
import androidx.databinding.DataBindingUtil
import com.samsung.android.sdk.spay.sample.issuer.util.PartnerInfoHolder
import com.samsung.android.sdk.samsungpay.v2.payment.MstPaymentListener
import com.samsung.android.sdk.samsungpay.v2.payment.SpayResponseInfo
import com.samsung.android.sdk.samsungpay.v2.payment.MstPaymentInfo
import com.samsung.android.sdk.samsungpay.v2.payment.EncryptionKeyListener
import com.samsung.android.sdk.samsungpay.v2.payment.SpayPublicKey
import com.samsung.android.sdk.spay.payloadLoader.SampleDataLoader
import com.samsung.android.sdk.spay.sample.issuer.R
import com.samsung.android.sdk.spay.sample.issuer.databinding.StartMstActivityBinding
import com.samsung.android.sdk.spay.sample.issuer.util.ErrorCode
import org.json.JSONException
import org.json.JSONObject
import java.lang.Exception
import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher

class StartMstActivity : Activity(), View.OnClickListener {
    private lateinit var mstManager: MstManager// SDK class to handle MST request
    private var receivedPublicKey: PublicKey? = null// Encryption token key from Samsung pay

    private lateinit var binding: StartMstActivityBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.start_mst_activity)
        binding.apply {
            plainTokenEditText.addTextChangedListener(textWatcher)
            encryptedTokenEditText.addTextChangedListener(textWatcher)
            signedTokenEditText.addTextChangedListener(textWatcher)
            intermediateCaEditText.addTextChangedListener(textWatcher)
            endEntityCaEditText.addTextChangedListener(textWatcher)
            durationTimeEditText.addTextChangedListener(textWatcher)
            getTokenEncryptionKeyButton.setOnClickListener(this@StartMstActivity)
            signTokenButton.setOnClickListener(this@StartMstActivity)
            startMstButton.setOnClickListener(this@StartMstActivity)
            stopMstButton.setOnClickListener(this@StartMstActivity)
            sendTransactionResultInfoButton.setOnClickListener(this@StartMstActivity)

            enableButton(getTokenEncryptionKeyButton, false)
            enableButton(signTokenButton, false)
            enableButton(startMstButton, false)
            enableButton(stopMstButton, false)
        }

        mstManager = MstManager(this, PartnerInfoHolder.getInstance(this).partnerInfo)

        val startMstDefaultData = SampleDataLoader.getDefaultMstData(applicationContext)
        val jsonObject: JSONObject
        try {
            jsonObject = JSONObject(startMstDefaultData)
            val signedOTTString = jsonObject.getString("signedOTTString")
            val signedOTTByteArray = hexStringToByteArray(signedOTTString)
            val signedOTTBase64 = String(Base64.encode(signedOTTByteArray, Base64.NO_WRAP))
            val intermediateCA = jsonObject.getString("intermediateCA")
            val endEntityCA = jsonObject.getString("endEntityCA")

            binding.apply {
                plainTokenEditText.setText(jsonObject.getString("plainToken"))
                signedTokenEditText.setText(signedOTTBase64)
                intermediateCaEditText.setText(intermediateCA)
                endEntityCaEditText.setText(endEntityCA)
                durationTimeEditText.setText(jsonObject.getString("durationTime"))
            }


        } catch (e: JSONException) {
            Log.e(TAG, "${e.message}")
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.get_token_encryption_key_button -> doGetTokenEncryptionKey()
            R.id.sign_token_button -> doSignTokenAndUpdateEditField()
            R.id.start_mst_button -> doStartMST()
            R.id.stop_mst_button -> doStopMST()
            R.id.send_transaction_result_info_button -> {
                val intent = Intent(this, SendTransactionResultInfoActivity::class.java)
                startActivity(intent)
            }
        }
    }

    private fun doStopMST() {
        mstManager.stopMST(object : MstPaymentListener {
            override fun onSuccess(response: SpayResponseInfo) {
                runOnUiThread {
                    Toast.makeText(this@StartMstActivity, getString(R.string.stop_mst_success), Toast.LENGTH_SHORT).show()
                    enableButton(binding.stopMstButton, false)
                    enableButton(binding.startMstButton, true)
                }
            }

            override fun onFail(errorCode: Int, errorData: Bundle) {
                runOnUiThread {
                    Toast.makeText(this@StartMstActivity, getString(R.string.stop_mst_failure), Toast.LENGTH_SHORT).show()
                    enableButton(binding.stopMstButton, false)
                }
            }
        })
    }

    private fun doStartMST() {
        if (binding.durationTimeEditText.text.toString().isEmpty()) {
            binding.durationTimeEditText.setText("0")
        }
        val mstPaymentInfoBuilder = MstPaymentInfo.Builder()
        val mstPaymentInfo = mstPaymentInfoBuilder
                .setEncryptedToken(binding.encryptedTokenEditText.text.toString())
                .setSignedToken(binding.signedTokenEditText.text.toString())
                .setIntermediateCA(binding.intermediateCaEditText.text.toString())
                .setEndEntityCA(binding.endEntityCaEditText.text.toString())
                .setDurationTime(binding.durationTimeEditText.text.toString().toInt())
                .setIsEncrypted(true)
                .setIsRetry(binding.isRetryCheckbox.isChecked)
                .build()
        mstManager.startMST(this, mstPaymentInfo, object : MstPaymentListener {
            override fun onSuccess(response: SpayResponseInfo) {
                Log.d(TAG, "onSuccess callback is called PayTransactionId: ${response.payTransactionId}"
                        + " PayModeCycleTime: ${response.payModeCycleTime} PayModeDelayTime: ${response.payModeDelayTime}")
                Toast.makeText(this@StartMstActivity, getString(R.string.start_mst_success), Toast.LENGTH_SHORT).show()
                enableButton(binding.startMstButton, false)
                enableButton(binding.stopMstButton, true)
            }

            override fun onFail(errorCode: Int, errorData: Bundle) {
                Log.e(TAG, "onFail callback is called, code:$errorCode")
                showErrorResultPage(applicationContext, errorCode, ErrorCode.getErrorCodeName(errorCode), errorData)
            }
        })
    }

    private fun doGetTokenEncryptionKey() {
        mstManager.getTokenEncryptionKey(object : EncryptionKeyListener {
            override fun onSuccess(pubKey: SpayPublicKey) {

                Log.d(TAG, "onSuccess SpayPublicKey : $pubKey")
                Toast.makeText(applicationContext,
                        "getTokenEncryptionKey onSuccess", Toast.LENGTH_SHORT).show()
                try {
                    val keyFactory = KeyFactory.getInstance(pubKey.algorithm)
                    val publicKeySpec = X509EncodedKeySpec(pubKey.encoded)
                    receivedPublicKey = keyFactory.generatePublic(publicKeySpec)
                    val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
                    cipher.init(Cipher.ENCRYPT_MODE, receivedPublicKey)
                    val partnerOutput = cipher.doFinal(binding.plainTokenEditText.text.toString().toByteArray())
                    binding.encryptedTokenEditText.setText(String(Base64.encode(partnerOutput, Base64.NO_WRAP)))
                } catch (e: Exception) {
                    Log.e(TAG, "doGetTokenEncryptionKey - e :  ${e.message}")
                }
            }

            override fun onFail(errorCode: Int, errorData: Bundle) {
                Log.e(TAG, "onFail error : $errorCode, data : $errorData")
                showErrorResultPage(applicationContext, errorCode, ErrorCode.getErrorCodeName(errorCode), errorData)
            }
        })
    }

    private fun doSign(_plainToken: String): String {
        //TODo: Sign plainToken using End-Entity CA : below is dummy string for example
        return "1234567891230123454914561312156489741320"
    }

    private fun doSignTokenAndUpdateEditField() {
        val plainToken = binding.plainTokenEditText.text.toString()
        val signedOTTString = doSign(plainToken)
        val signedOTTByteArray = hexStringToByteArray(signedOTTString)
        val signedOTTBase64 = String(Base64.encode(signedOTTByteArray, Base64.NO_WRAP))
        binding.signedTokenEditText.setText(signedOTTBase64)
    }

    private fun enableButton(button: Button, isEnabled: Boolean) {
        button.isClickable = isEnabled
        button.alpha = if (isEnabled) 1.0f else 0.4f
    }

    private var textWatcher: TextWatcher = object : TextWatcher {
        override fun afterTextChanged(edit: Editable) {
            // no need
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            // no need
        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            if (binding.plainTokenEditText.text.isEmpty()) {
                enableButton(binding.getTokenEncryptionKeyButton, false)
                enableButton(binding.signTokenButton, false)
            } else {
                enableButton(binding.getTokenEncryptionKeyButton, true)
                enableButton(binding.signTokenButton, true)
            }
            if (binding.encryptedTokenEditText.text.isEmpty() || binding.signedTokenEditText.text.isEmpty()
                    || binding.intermediateCaEditText.text.isEmpty() || binding.endEntityCaEditText.text.isEmpty()) {
                enableButton(binding.startMstButton, false)
                enableButton(binding.stopMstButton, false)
            } else {
                enableButton(binding.startMstButton, true)
                enableButton(binding.stopMstButton, false)
            }
        }
    }

    private fun hexStringToByteArray(s: String): ByteArray {
        try {
            val len = s.length
            val data = ByteArray(len / 2)
            var i = 0
            while (i < len) {
                data[i / 2] = ((Character.digit(s[i], 16) shl 4)
                        + Character.digit(s[i + 1], 16)).toByte()
                i += 2
            }
            return data
        } catch (e: Exception) {
            Log.e(TAG, "hexStringToByteArray : e =  ${e.message}")
        }
        return ByteArray(0)
    }

    companion object {
        const val TAG = "StartMstActivity"
    }
}