package com.samsung.android.sdk.spay.sample.issuer.ui.status

import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceActivity
import android.preference.PreferenceFragment
import android.preference.PreferenceManager
import android.util.Log
import com.samsung.android.sdk.spay.sample.issuer.R

class MyPreferenceActivity : PreferenceActivity() {
    private lateinit var prefs: SharedPreferences
    private var isWatchValue = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        super.setTitle("Settings")
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false)
        Log.d(TAG, "onCreate")
        Companion.packageName = packageName
        prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val mPreferenceFragment = MyPreferenceFragment()
        fragmentManager.beginTransaction().replace(android.R.id.content, mPreferenceFragment)
            .commit()
        isWatchValue = prefs.getBoolean(getString(R.string.pref_watchMode), false)
    }

    class MyPreferenceFragment : PreferenceFragment() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            val resourceID = this.resources.getIdentifier(PREFERENCE_FILE_NAME, "xml", packageName)
            addPreferencesFromResource(resourceID)
        }
    }

    override fun onBackPressed() {
        if (prefs.getBoolean(getString(R.string.pref_watchMode), false) != isWatchValue) {
            setResult(RESULT_OK)
        }
        super.onBackPressed()
    }

    companion object {
        const val TAG = "MyPreferenceActivity"
        const val PREFERENCE_FILE_NAME = "preferences"
        private var packageName: String? = null
    }
}