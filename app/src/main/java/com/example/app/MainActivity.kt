package com.dev.assista.app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.huawei.hms.hihealth.HuaweiHiHealth
import com.huawei.hms.hihealth.SettingController
import com.huawei.hms.hihealth.data.Scopes
import com.huawei.hms.hihealth.result.HealthKitAuthResult


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        initService()
        // Step 2 Authorization process, which is called each time the process is started.
        requestAuth()
    }

    /**
     * Initialize SettingController.
     */
    private fun initService() {
        mSettingController = HuaweiHiHealth.getSettingController(this)
    }

    // SettingController object.
    private var mSettingController: SettingController? = null

    /**
     * Declare the scope to be applied for and obtain the intent to start the authorization process. This method must be used in the activity.
     */
    private fun requestAuth() {

        // Add scopes to apply for. The following only shows an example. You need to add scopes according to your specific needs.
        val scopes = arrayOf( // View and store the step count in Health Kit.
            Scopes.HEALTHKIT_STEP_READ,
            Scopes.HEALTHKIT_STEP_WRITE,  // View and store the height and weight in Health Kit.
            Scopes.HEALTHKIT_HEIGHTWEIGHT_READ,
            Scopes.HEALTHKIT_HEIGHTWEIGHT_WRITE,  // View and store the heart rate data in Health Kit.
            Scopes.HEALTHKIT_HEARTRATE_READ,
            Scopes.HEALTHKIT_HEARTRATE_WRITE,
            Scopes.HEALTHKIT_SLEEP_READ
        )

        // Obtain the intent of the authorization process. The value true indicates that the authorization process of the Health app is enabled, and false indicates that the authorization process is disabled.
        val intent: Intent = mSettingController!!.requestAuthorizationIntent(scopes, true)

        // Open the authorization process screen.
        Log.i(TAG, "start authorization activity")
        startActivityForResult(intent, REQUEST_AUTH)
    }

    // Request code for displaying the authorization screen using the startActivityForResult method. The value can be customized.
    private val REQUEST_AUTH = 1002
    private val TAG = "HealthKitAuthActivity"

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Process only the response result of the authorization process.
        if (requestCode == REQUEST_AUTH) {
            // Obtain the authorization response result from the intent.
            val result: HealthKitAuthResult =
                mSettingController!!.parseHealthKitAuthResultFromIntent(data)
            if (result == null) {
                Log.w(TAG, "authorization fail")
                return
            }
            if (result.isSuccess) {
                Log.i(TAG, "authorization success")
            } else {
                Log.w(TAG, "authorization fail, errorCode:" + result.errorCode)
            }
        }
    }
}