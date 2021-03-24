package com.gardilily.littleplan.activity.cloudservice

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.gardilily.littleplan.R
import com.gardilily.littleplan.activity.About
import com.gardilily.littleplan.tools.CloudApi
import com.gardilily.littleplan.tools.Cryptography.Companion.MD5

class Login : Activity() {
    private lateinit var sp: SharedPreferences
    private var activity_theme_resID = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        sp = getSharedPreferences("SP_LittlePlan", MODE_PRIVATE)
        activity_theme_resID = sp.getInt("theme_targetResID", About.THEME_DEFAULT)
        setTheme(activity_theme_resID)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cloud_login)
        sp = getSharedPreferences("SP_LittlePlan", MODE_PRIVATE)

        initGlobalFloatingBackBtn()
        prepareRegisterTutorial()
        initSubmitButton()
    }

    private fun initGlobalFloatingBackBtn() {
        if (sp.getBoolean("flag_globalFloatingBackButton", false)) {
            val globalBackBtn = findViewById<TextView>(R.id.cloud_login_globalBackBtn)
            globalBackBtn.visibility = View.VISIBLE
            globalBackBtn.setOnClickListener { finish() }
        }
    }

    private fun prepareRegisterTutorial() {
        val tutoVer = 1
        if (sp.getInt("tutorial_register", 0) < tutoVer) {
            AlertDialog.Builder(this)
                    .setTitle(getString(R.string.common_hint))
                    .setMessage(getString(R.string.cloud_login_tutor_registerLoginTwoInOneText))
                    .setPositiveButton(getString(R.string.common_okay)) { _, _ ->
                        sp.edit().putInt("tutorial_register", tutoVer).apply()
                    }
                    .create()
                    .show()
        }
    }

    private fun initSubmitButton() {
        findViewById<Button>(R.id.cloud_login_submit).setOnClickListener {
            val uname = findViewById<EditText>(R.id.cloud_login_username).text.toString()
            val upw = findViewById<EditText>(R.id.cloud_login_password).text.toString()
            if (uname.isEmpty() || upw.isEmpty()) {
                Toast.makeText(this, getString(R.string.cloud_login_dontLeaveBlankEmpty), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (upw.length < 6) {
                Toast.makeText(this, getString(R.string.cloud_login_PWLenShouldLargerThanSix), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val encPw = MD5("${MD5(uname)}${MD5("grlps")}${MD5(upw)}")

            Log.d("Cloud Login", "CP1 $uname $encPw")

            CloudApi.login(uname, encPw, {
                // success
                val result = it
                Log.d("Cloud Login", "CP3: $result")
                if (!result?.startsWith('-')!!) {
                    // login successfully
                    sp.edit().putBoolean("cloud_lastLogin", true).apply()
                    sp.edit().putString("cloud_uname", uname)
                        .putString("cloud_upw", encPw).apply()
                    runOnUiThread {
                        Toast.makeText(this, getString(R.string.cloud_login_loginSuccess), Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
                else if (result == "-1") {
                    Log.d("Cloud Login", "CP2")
                    runOnUiThread {
                        AlertDialog.Builder(this)
                            .setTitle(getString(R.string.common_register))
                            .setMessage(getString(R.string.cloud_login_registerSuggest))
                            .setPositiveButton(getString(R.string.common_yes)) { _, _ ->
                                CloudApi.register(uname, encPw, { s ->
                                    // success
                                    if (!s?.startsWith('-')!!) {
                                        // register successfully
                                        sp.edit().putBoolean("cloud_lastLogin", true).apply()
                                        sp.edit().putString("cloud_uname", uname)
                                            .putString("cloud_upw", encPw).apply()
                                        runOnUiThread {
                                            Toast.makeText(this, getString(R.string.cloud_login_registerSuccess), Toast.LENGTH_SHORT).show()
                                            finish()
                                        }
                                    } else {
                                        runOnUiThread {
                                            Toast.makeText(this, getString(R.string.cloud_login_unknownError), Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                })
                            }
                            .setNegativeButton(getString(R.string.common_no)) { _, _ -> }
                            .create()
                            .show()
                    }
                }
                else if (result == "-2") {
                    runOnUiThread {
                        Toast.makeText(this, getString(R.string.cloud_login_wrongPW), Toast.LENGTH_SHORT).show()
                    }
                }
                else if (result == "-10") {
                    runOnUiThread {
                        Toast.makeText(this, getString(R.string.cloud_login_unknownError), Toast.LENGTH_SHORT).show()
                    }
                }
            })
        }
    }

    override fun onResume() {
        super.onResume()
        if (sp.getInt("theme_targetResID", About.THEME_DEFAULT) != activity_theme_resID) {
            reloadActivity()
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
}
