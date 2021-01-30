package com.makilette.inappupdates

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.ActivityResult
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability

class FlexibleUpdatesActivity : AppCompatActivity() {

    private lateinit var listener: InstallStateUpdatedListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        findViewById<Button>(R.id.button).setOnClickListener {
            startUpdateFlowForResult()
        }
    }

    // アプリがバックグラウンドに行ってしまった場合の復帰処理
    override fun onResume() {
        super.onResume()

        val appUpdateManager = AppUpdateManagerFactory.create(this)
        appUpdateManager.appUpdateInfo
            .addOnSuccessListener { appUpdateInfo ->
                if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                    popupSnackbarForCompleteUpdate()
                }
            }
    }

    /**
     * アップデートの可用性を確認する.
     * 更新をリクエストする前に、アプリで利用できるかどうかを最初に確認する必要があります。
     * 更新を確認するには、AppUpdateManagerを使用します。
     * https://developer.android.com/guide/app-bundle/in-app-updates#update_readiness
     */
    private fun startUpdateFlowForResult() {

        val appUpdateManager = AppUpdateManagerFactory.create(this)

        // 要求状態の更新を追跡するリスナーを作成.
        listener = InstallStateUpdatedListener {
            when (it.installStatus()) {
                InstallStatus.DOWNLOADING -> {
                    // DOWNLOADING: ダウンロード中

                }
                InstallStatus.DOWNLOADED -> {
                    // ダウンロード完了
                    // 更新がダウンロードされた後、通知を表示する
                    // ユーザーの確認をリクエストしてアプリを再起動します。
                    popupSnackbarForCompleteUpdate()

                    // ステータスの更新が不要になった場合は、リスナーを登録解除します。
                    appUpdateManager.unregisterListener(listener)
                }
            }
        }

        // 更新を開始する前に、更新用のリスナーを登録します。
        appUpdateManager.registerListener(listener)


        // 更新の確認に使用するインテントオブジェクトを返します。
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo

        // 更新チェックを行う.
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->

            Log.i(LOG_TAG, "updateAvailability: " + appUpdateInfo.updateAvailability())
            Log.i(LOG_TAG, "isUpdateTypeAllowed: " + appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE))

            // UpdateAvailability.UPDATE_AVAILABLE: 更新が必要な場合
            // UpdateAvailability.UPDATE_NOT_AVAILABLE: 更新が必要ない場合
            // UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS: 現在更新処理中ということを表すフラグ　更新処理を再開する場合などの判定として使う.
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
            ) {
                // アプリの更新が必要なため、更新処理を行う.
                // https://developer.android.com/guide/app-bundle/in-app-updates#start_update
                appUpdateManager!!.startUpdateFlowForResult(
                    appUpdateInfo,
                    AppUpdateType.FLEXIBLE,
                    // 更新要求を行っている現在のアクティビティ
                    this,
                    // onActivityResultで使用するリクエストコード.
                    MY_REQUEST_CODE
                )
            }
        }
    }

    /* スナックバー表示 アプリ再起動を促す. */
    fun popupSnackbarForCompleteUpdate() {
        val appUpdateManager = AppUpdateManagerFactory.create(this)
        Snackbar.make(
            findViewById(R.id.content),
            "アップデートがダウンロードされました。",
            Snackbar.LENGTH_INDEFINITE
        ).apply {
            setAction("RESTART") {
                // 更新処理完了後の処理をここで行う.
                /**
                 * フォアグラウンドでappUpdateManager.completeUpdate（）を呼び出すと、プラットフォームはバックグラウンドでアプリを再起動する全画面UIを表示します。
                 * プラットフォームが更新をインストールした後、アプリはメインアクティビティで再起動します。
                 * 代わりにバックグラウンドでappUpdateManager.completeUpdate（）を呼び出すと、デバイスUIを覆い隠さずに更新がサイレントインストールされます。
                 * ユーザーがアプリをフォアグラウンドに持ってくるとき、アプリにインストール待ちのアップデートがないことを確認することをお勧めします。 アプリにDOWNLOADED状態の更新がある場合、以下に示すように、ユーザーに更新のインストールを要求する通知を表示します。 それ以外の場合、更新データは引き続きユーザーのデバイスストレージを占有します。
                 */
                appUpdateManager.completeUpdate()
            }
            show()
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
