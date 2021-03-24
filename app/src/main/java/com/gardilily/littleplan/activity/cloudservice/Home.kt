package com.gardilily.littleplan.activity.cloudservice

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.*
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import com.gardilily.littleplan.R
import com.gardilily.littleplan.activity.About
import com.gardilily.littleplan.tools.CloudApi
import com.gardilily.littleplan.tools.SharedData
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class Home : Activity() {
    private lateinit var sp: SharedPreferences

    private val VIEW_UPLOAD = 0
    private val VIEW_DOWNLOAD = 1
    private val VIEW_CHANGEPW = 2
    private val VIEW_LOGOUT = 3
    private val VIEW_REMOVEACCOUNT = 4
    private val VIEW_LOGIN = 5

    private val view_list = ArrayList<RelativeLayout>()

    private lateinit var view_username: TextView
    private lateinit var view_dataDate: TextView

    private var activity_theme_resID = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        sp = getSharedPreferences("SP_LittlePlan", MODE_PRIVATE)
        activity_theme_resID = sp.getInt("theme_targetResID", About.THEME_DEFAULT)
        setTheme(activity_theme_resID)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cloud_home)

        initGlobalFloatingBackBtn()
        bindViews()
        initClicks()
        switchLoginStatus(false)
    }

    private fun initGlobalFloatingBackBtn() {
        if (sp.getBoolean("flag_globalFloatingBackButton", false)) {
            val globalBackBtn = findViewById<TextView>(R.id.cloud_home_globalBackBtn)
            globalBackBtn.visibility = View.VISIBLE
            globalBackBtn.setOnClickListener { finish() }
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d("Cloud Home", "On Resume")
        if (sp.getInt("theme_targetResID", About.THEME_DEFAULT) != activity_theme_resID) {
            reloadActivity()
        }
        else if (sp.getBoolean("cloud_lastLogin", false)) {
            tryToLogin()
        }
    }

    private fun reloadActivity() {
        val intent = intent
        overridePendingTransition(0, 0)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        finish()

        overridePendingTransition(0, 0)
        startActivity(intent)
    }

    private fun tryToLogin() {
        CloudApi.login(sp.getString("cloud_uname", "")!!,
                sp.getString("cloud_upw", "")!!,
                {
                    // success
                    if (!it?.startsWith('-')!!) {
                        // login successfully
                        sp.edit().putBoolean("cloud_lastLogin", true).apply()
                        runOnUiThread {
                            switchLoginStatus(true,
                                    sp.getString("cloud_uname", "")!!,
                                    it)
                        }
                    }
                })
    }

    private fun bindViews() {
        view_username = findViewById(R.id.cloud_home_username)
        view_dataDate = findViewById(R.id.cloud_home_dataDate)

        view_list.add(findViewById(R.id.cloud_home_upload))
        view_list.add(findViewById(R.id.cloud_home_download))
        view_list.add(findViewById(R.id.cloud_home_changePW))
        view_list.add(findViewById(R.id.cloud_home_logout))
        view_list.add(findViewById(R.id.cloud_home_removeAccount))
        view_list.add(findViewById(R.id.cloud_home_login))
    }

    private fun initClicks() {
        view_list[VIEW_UPLOAD].setOnClickListener {
            val json = (application as SharedData).store.getFullJsonObj().toString()
            fun upload() {
                CloudApi.upload(sp.getString("cloud_uname", "")!!,
                        sp.getString("cloud_upw", "")!!,
                        json,
                        getNowTimeStr(),
                        {
                            if (!it?.startsWith('-')!!) {
                                // upload successfully
                                runOnUiThread {
                                    Toast.makeText(this, getString(R.string.cloud_home_uploadSuccess), Toast.LENGTH_SHORT).show()
                                    view_dataDate.text = it
                                }
                            }
                        }
                )
            }
            Log.d("Cloud.Home.UploadClick",
                    "$json, ${(application as SharedData).store.getTaskJsonByID(0)}")
            if (json == "{}") {
                AlertDialog.Builder(this)
                        .setTitle(getString(R.string.common_notice))
                        .setMessage(getString(R.string.cloud_home_uploadData_localDataEmptyWarning))
                        .setPositiveButton(getString(R.string.common_continue)) {_, _ ->
                            upload()
                        }
                        .setNegativeButton(getString(R.string.common_cancel), null)
                        .create()
                        .show()
            }
            else
                upload()
        }

        view_list[VIEW_DOWNLOAD].setOnClickListener {
            AlertDialog.Builder(this)
                    .setTitle(getString(R.string.common_notice))
                    .setMessage("${getString(R.string.cloud_home_downloadData_actionWillCoverLocalDataWarning)}\n\n${getString(R.string.cloud_home_cloudDataUpdateTimeShow)}\n${view_dataDate.text}")
                    .setPositiveButton(getString(R.string.common_continue)) { _, _ ->
                        CloudApi.download(sp.getString("cloud_uname", "")!!,
                                sp.getString("cloud_upw", "")!!,
                                {
                                    // success
                                    if (!it?.startsWith('-')!!) {
                                        // download successfully
                                        runOnUiThread {
                                            if (it == "{}") {
                                                AlertDialog.Builder(this)
                                                        .setTitle(getString(R.string.common_notice))
                                                        .setMessage(getString(R.string.cloud_home_downloadData_cloudDataEmptyWarning))
                                                        .setPositiveButton(getString(R.string.common_continue)) { _, _ ->
                                                            (application as SharedData).store.updateFullJsonObj(JSONObject(it))
                                                            (application as SharedData).localDataLegacy = true
                                                            Toast.makeText(this, getString(R.string.common_complete), Toast.LENGTH_SHORT).show()
                                                        }
                                                        .setNegativeButton(getString(R.string.common_cancel), null)
                                                        .create()
                                                        .show()
                                            } else {
                                                Toast.makeText(this, getString(R.string.cloud_home_downloadSuccess), Toast.LENGTH_SHORT).show()
                                                (application as SharedData).store.updateFullJsonObj(JSONObject(it))
                                                (application as SharedData).localDataLegacy = true
                                            }
                                        }
                                    }
                                })
                    }
                    .setNegativeButton(getString(R.string.common_cancel), null)
                    .create()
                    .show()
        }

        view_list[VIEW_CHANGEPW].setOnClickListener {
            val intent = Intent(this, ChangePW::class.java)
            intent.putExtra("uname", sp.getString("cloud_uname", ""))
            startActivity(intent)
        }

        view_list[VIEW_LOGOUT].setOnClickListener {
            sp.edit().putBoolean("cloud_lastLogin", false).apply()
            switchLoginStatus(false)
        }

        view_list[VIEW_REMOVEACCOUNT].setOnClickListener {
            AlertDialog.Builder(this)
                    .setTitle(getString(R.string.common_warning))
                    .setMessage(getString(R.string.cloud_home_removeAccount_removeWarning))
                    .setPositiveButton(getString(R.string.common_sure)) { _, _ ->
                        CloudApi.removeAccount(
                            sp.getString("cloud_uname", "")!!,
                            sp.getString("cloud_upw", "")!!,
                            {
                                // success
                                if (it == "1") {
                                    // remove successfully
                                    sp.edit().putBoolean("cloud_lastLogin", false).apply()
                                    switchLoginStatus(false)
                                }
                            })
                    }
                .setNegativeButton(getString(R.string.common_cancel), null)
                    .create()
                    .show()
        }

        view_list[VIEW_LOGIN].setOnClickListener {
            val intent = Intent()
            intent.setClass(this, Login::class.java)
            startActivity(intent)
        }
    }

    private fun getNowTimeStr(): String {
        return SimpleDateFormat("yyyy.MM.dd-HH:mm").format(Date())
    }

    private fun switchLoginStatus(isLogin: Boolean, username: String = "", date: String = "") {
        runOnUiThread {
            var visiA = VISIBLE
            var visiB = GONE
            if (!isLogin) {
                visiA = GONE
                visiB = VISIBLE
            }

            for (i in VIEW_UPLOAD..VIEW_REMOVEACCOUNT) {
                view_list[i].visibility = visiA
            }

            if (visiA == GONE)
                visiA = INVISIBLE
            if (visiB == GONE)
                visiB = INVISIBLE

            view_list[VIEW_LOGIN].visibility = visiB
            view_dataDate.visibility = visiA


            if (isLogin) {
                view_username.text = username
                view_dataDate.text = date
            } else
                view_username.text = getString(R.string.cloud_home_didntSignIn)
        }
    }
}
