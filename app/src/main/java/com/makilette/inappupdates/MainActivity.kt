package com.makilette.inappupdates

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.ActivityResult
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.button).setOnClickListener {
            startUpdateFlowForResult()
        }

        findViewById<Button>(R.id.button2).setOnClickListener {
            val intent = Intent(this, FlexibleUpdatesActivity::class.java)
            startActivity(intent)
        }
    }

    // アプリがバックグラウンドに行ってしまった場合の復帰処理
    // https://developer.android.com/guide/app-bundle/in-app-updates#immediate_flow
    override fun onResume() {
        super.onResume()

        val appUpdateManager = AppUpdateManagerFactory.create(applicationContext)
        appUpdateManager
            .appUpdateInfo
            .addOnSuccessListener { appUpdateInfo ->
                if (appUpdateInfo.updateAvailability()
                    == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS
                ) {
                    // アプリ内更新が既に実行されている場合は、更新を再開します。
                    appUpdateManager.startUpdateFlowForResult(
                        appUpdateInfo,
                        AppUpdateType.IMMEDIATE,
                        this,
                        MY_REQUEST_CODE
                    );
                }
            }
    }

    /**
     * アップデート確認を行う.
     * 更新をリクエストする前に、アプリで利用できるかどうかを最初に確認する必要があります。
     * 更新を確認するには、AppUpdateManagerを使用します。
     * https://developer.android.com/guide/app-bundle/in-app-updates#update_readiness
     */
    private fun startUpdateFlowForResult() {
        val appUpdateManager = AppUpdateManagerFactory.create(applicationContext)
        // 更新の確認に使用するインテントオブジェクトを返します。
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo

        // 更新チェックを行う.
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->

            Log.i(LOG_TAG, "availableVersionCode: " + appUpdateInfo.availableVersionCode())
            Log.i(LOG_TAG, "packageName: " + appUpdateInfo.packageName())
            Log.i(LOG_TAG, "updateAvailability: " + appUpdateInfo.updateAvailability())
            Log.i(LOG_TAG, "isUpdateTypeAllowed: " + appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE))
            // UpdateAvailability.UPDATE_AVAILABLE: 更新が必要な場合
            // UpdateAvailability.UPDATE_NOT_AVAILABLE: 更新が必要ない場合
            // UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS: 現在更新処理中ということを表すフラグ　更新処理を再開する場合などの判定として使う.
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                // TODO: Flexibleな更新を行うには、[AppUpdateType.FLEXIBLE] を使用します
                && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
            ) {
                // アプリの更新が必要なため、更新処理を行う.
                // https://developer.android.com/guide/app-bundle/in-app-updates#start_update
                appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    // TODO: Flexibleな更新を行うには、[AppUpdateType.FLEXIBLE] を指定する.
                    AppUpdateType.IMMEDIATE,
                    // 更新要求を行っている現在のアクティビティ
                    this,
                    // onActivityResultで使用するリクエストコード.
                    MY_REQUEST_CODE
                )
            }
        }
    }

    /**
     * アプリの更新処理結果が返る.
     *
     * https://developer.android.com/guide/app-bundle/in-app-updates#status_callback
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        Log.i(LOG_TAG, "requestCode: $resultCode resultCode: $resultCode")
        if (requestCode == MY_REQUEST_CODE) {
            when (resultCode) {
                RESULT_OK -> {
                    // ユーザーは更新を受け入れました。
                    // 即時更新の場合、コントロールがアプリに返されるまでにGoogle Playによって更新が既に完了しているため、
                    // このコールバックを受信しない場合があります。
                }
                // RESULT_OK以外は、更新がキャンセルまたは失敗している。その場合、更新の再開をリクエストできます。
                RESULT_CANCELED -> {
                    // ユーザーが更新を拒否またはキャンセルしました。
                }
                ActivityResult.RESULT_IN_APP_UPDATE_FAILED -> {
                    // 他の何らかのエラーにより、ユーザーが同意することも、更新を続行することもできませんでした。
                }
            }
        }
    }

    companion object {
        private const val LOG_TAG = "MainActivity"
        private const val MY_REQUEST_CODE: Int = 100
    }

}
