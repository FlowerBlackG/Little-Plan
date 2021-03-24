package com.gardilily.littleplan.activity.cloudservice

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.gardilily.littleplan.R
import com.gardilily.littleplan.activity.About
import com.gardilily.littleplan.tools.CloudApi
import com.gardilily.littleplan.tools.Cryptography.Companion.MD5

class ChangePW : Activity() {
    private lateinit var sp: SharedPreferences
    private var activity_theme_resID = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        sp = getSharedPreferences("SP_LittlePlan", MODE_PRIVATE)
        activity_theme_resID = sp.getInt("theme_targetResID", About.THEME_DEFAULT)
        setTheme(activity_theme_resID)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cloud_changepw)

        initGlobalFloatingBackBtn()
        initSubmitButton()
    }

    private fun initGlobalFloatingBackBtn() {
        if (sp.getBoolean("flag_globalFloatingBackButton", false)) {
            val globalBackBtn = findViewById<TextView>(R.id.cloud_changePW_globalBackBtn)
            globalBackBtn.visibility = View.VISIBLE
            globalBackBtn.setOnClickListener { finish() }
        }
    }

    private fun initSubmitButton() {
        findViewById<Button>(R.id.cloud_changePW_submit).setOnClickListener {
            val oldpw = findViewById<EditText>(R.id.cloud_changePW_old).text.toString()
            val newpw = findViewById<EditText>(R.id.cloud_changePW_new).text.toString()
            if (oldpw.isEmpty()) {
                Toast.makeText(this, getString(R.string.cloud_changePW_pleaseInputOldPW), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (newpw.isEmpty()) {
                Toast.makeText(this, getString(R.string.cloud_changePW_pleaseInputNewPW), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (newpw.length < 6) {
                Toast.makeText(this, getString(R.string.cloud_login_PWLenShouldLargerThanSix), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val uname = intent.getStringExtra("uname")
            val encOldPw = MD5("${MD5(uname!!)}${MD5("grlps")}${MD5(oldpw)}")
            val encNewPw = MD5("${MD5(uname)}${MD5("grlps")}${MD5(newpw)}")

            CloudApi.changePW(uname, encOldPw, encNewPw, {
                if (it == "1") {
                    // change password successfully
                    sp.edit().putString("cloud_upw", encNewPw).apply()
                    runOnUiThread {
                        Toast.makeText(this, getString(R.string.cloud_changePW_editSuccess), Toast.LENGTH_SHORT).show()
                        finish()
                    }
                } else if (it == "-2") {
                    runOnUiThread {
                        Toast.makeText(this, getString(R.string.cloud_changePW_oldPWWrong), Toast.LENGTH_SHORT).show()
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
