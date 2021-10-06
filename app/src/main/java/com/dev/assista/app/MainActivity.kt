package com.dev.assista.app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.huawei.hms.hihealth.HuaweiHiHealth
import com.huawei.hms.hihealth.SettingController
import com.huawei.hms.hihealth.data.*
import com.huawei.hms.hihealth.options.HealthRecordReadOptions
import com.huawei.hms.hihealth.result.HealthKitAuthResult
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        initService()
        // Step 2 Authorization process, which is called each time the process is started.
        requestAuth()
        requestSleepdata()
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

    private  fun requestSleepdata(){
        val healthRecordController = HuaweiHiHealth.getHealthRecordController(applicationContext)

// Set the start time and end time of the request body.

// Set the start time and end time of the request body.
        val cal: Calendar = Calendar.getInstance()
        val now = Date()
        cal.setTime(now)
        val endTime: Long = cal.getTimeInMillis()
        cal.add(Calendar.DAY_OF_YEAR, -1)
        val startTime: Long = cal.getTimeInMillis()

// Build the HealthRecordReadOptions parameter for reading health records.

// Build the HealthRecordReadOptions parameter for reading health records.
        val subDataTypeList: MutableList<DataType> = ArrayList()
        subDataTypeList.add(DataType.DT_CONTINUOUS_SLEEP)
        val healthRecordReadOptions = HealthRecordReadOptions.Builder().setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS)
                .readHealthRecordsFromAllApps()
                .readByDataType(HealthDataTypes.DT_HEALTH_RECORD_SLEEP)
                .setSubDataTypeList(subDataTypeList)
                .build()

// Pass the HealthRecordReadOptions parameter to query the health record.

// Pass the HealthRecordReadOptions parameter to query the health record.
        val task = healthRecordController.getHealthRecord(healthRecordReadOptions)
        task.addOnSuccessListener { readResponse ->
            logger("Get HealthRecord was successful!")
            // Print the health records obtained by calling the API.
            val recordList = readResponse.healthRecords
            for (record in recordList) {
                if (record == null) {
                    continue
                }
                dumpHealthRecord(record)
                logger("Print detailed data points associated with health records")
                for (dataSet in record.subDataDetails) {
                    dumpDataSet(dataSet)
                }
            }
        }
        task.addOnFailureListener { e -> logger(e.toString()) }
    }
    private val TAG = "HealthRecordController"

    /**
     * Also send operation result logs to the logcat.
     *
     * @param string Log string.
     */
    private fun logger(string: String) {
        Log.i(TAG, string)
    }

    /**
     * Print the information in the HealthRecord object.
     *
     * @param healthRecord Health record object.
     */
    private fun dumpHealthRecord(healthRecord: HealthRecord?) {
        logger("Print health record summary information!")
        val dateFormat: DateFormat = DateFormat.getDateInstance()
        val timeFormat: DateFormat = DateFormat.getTimeInstance()
        if (healthRecord != null) {
            logger("""	HealthRecordIdentifier: ${healthRecord.healthRecordId}
	packageName: ${healthRecord.dataCollector.packageName}
	StartTime: ${dateFormat.format(healthRecord.getStartTime(TimeUnit.MILLISECONDS))} ${timeFormat.format(healthRecord.getStartTime(TimeUnit.MILLISECONDS))}
	EndTime: ${dateFormat.format(healthRecord.getEndTime(TimeUnit.MILLISECONDS))} ${timeFormat.format(healthRecord.getEndTime(TimeUnit.MILLISECONDS))}
	HealthRecordDataType: ${healthRecord.dataCollector.dataType.name}
	HealthRecordDataCollectorId: ${healthRecord.dataCollector.dataStreamId}
	metaData: ${healthRecord.metadata}
	FileValueMap: ${healthRecord.fieldValues}""")
            if (healthRecord.subDataSummary != null && !healthRecord.subDataSummary.isEmpty()) {
                val sDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                for (samplePoint in healthRecord.subDataSummary) {
                    logger("Sample point type: " + samplePoint.dataType.name)
                    logger("Start: " + sDateFormat.format(Date(samplePoint.getStartTime(TimeUnit.MILLISECONDS))))
                    logger("End: " + sDateFormat.format(Date(samplePoint.getEndTime(TimeUnit.MILLISECONDS))))
                    for (field in samplePoint.dataType.fields) {
                        logger("Field: " + field.name.toString() + " Value: " + samplePoint.getFieldValue(field))
                    }
                    logger(System.lineSeparator())
                }
            }
        }
    }

    /**
     * Print the SamplePoint in the SampleSet object as an output.
     *
     * @param sampleSet Sampling dataset.
     */
    private fun dumpDataSet(sampleSet: SampleSet) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        for (samplePoint in sampleSet.samplePoints) {
            logger("Sample point type: " + samplePoint.dataType.name)
            logger("Start: " + dateFormat.format(Date(samplePoint.getStartTime(TimeUnit.MILLISECONDS))))
            logger("End: " + dateFormat.format(Date(samplePoint.getEndTime(TimeUnit.MILLISECONDS))))
            for (field in samplePoint.dataType.fields) {
                logger("Field: " + field.name.toString() + " Value: " + samplePoint.getFieldValue(field))
            }
        }
    }

    // Request code for displaying the authorization screen using the startActivityForResult method. The value can be customized.
    private val REQUEST_AUTH = 1002

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