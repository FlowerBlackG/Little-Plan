package com.gardilily.littleplan.activity

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.gardilily.littleplan.R
import com.gardilily.littleplan.activity.cloudservice.Home
import com.gardilily.littleplan.tools.SharedData

class About : Activity() {
    private lateinit var sp: SharedPreferences
    private var activity_theme_resID = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        sp = getSharedPreferences("SP_LittlePlan", MODE_PRIVATE)
        activity_theme_resID = sp.getInt("theme_targetResID", THEME_DEFAULT)
        setTheme(activity_theme_resID)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        showVersionInfo()
        initFunctionBtns()
    }

    override fun onResume() {
        super.onResume()
        if (sp.getInt("theme_targetResID", THEME_DEFAULT) != activity_theme_resID) {
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

    private fun showVersionInfo() {
        var versionInfoStr = "${getString(R.string.about_versionShow)}${packageManager.getPackageInfo(packageName, 0).versionName}\n"
        versionInfoStr += "${getString(R.string.about_buildShow)}${packageManager.getPackageInfo(packageName, 0).longVersionCode}\n"
        versionInfoStr += "${getString(R.string.about_buildTimeShow)}${resources.getString(R.string.app_buildTime)}"
        findViewById<TextView>(R.id.about_versionInfo).text = versionInfoStr
    }

    private fun initFunctionBtns() {
        // cloud services entry
        findViewById<Button>(R.id.about_fun_cloud).setOnClickListener {
            val intent = Intent(this, Home::class.java)
            startActivity(intent)
        }

        // change theme
        findViewById<Button>(R.id.about_fun_changeTheme).setOnClickListener {
            startActivity(Intent(this, ChangeTheme::class.java))
        }

        // remove protection switch
        val btn_removeProtection = findViewById<Button>(R.id.about_fun_switchRemoveProtection)
        if (sp.getBoolean("flag_removeProtection", true))
            btn_removeProtection.text = getString(R.string.about_funList_removeProtect_on)
        else
            btn_removeProtection.text = getString(R.string.about_funList_removeProtect_off)
        btn_removeProtection.setOnClickListener {
            if (!sp.getBoolean("flag_removeProtection", true))
                btn_removeProtection.text = getString(R.string.about_funList_removeProtect_on)
            else
                btn_removeProtection.text = getString(R.string.about_funList_removeProtect_off)
            sp.edit().putBoolean("flag_removeProtection", !sp.getBoolean("flag_removeProtection", true)).apply()
        }

        // floating button
        val floatBtn = findViewById<TextView>(R.id.about_globalBackBtn)
        floatBtn.setOnClickListener { finish() }
        val btn_globalFloatingBackBtnSwitch = findViewById<Button>(R.id.about_fun_switchGlobalFloatingBackBtn)
        if (sp.getBoolean("flag_globalFloatingBackButton", false)) {
            btn_globalFloatingBackBtnSwitch.text = getString(R.string.about_funList_globalFloatingBackBtn_on)
            floatBtn.visibility = View.VISIBLE
        }
        else {
            btn_globalFloatingBackBtnSwitch.text = getString(R.string.about_funList_globalFloatingBackBtn_off)
            floatBtn.visibility = View.GONE
        }
        btn_globalFloatingBackBtnSwitch.setOnClickListener {
            if (!sp.getBoolean("flag_globalFloatingBackButton", false)) {
                btn_globalFloatingBackBtnSwitch.text = getString(R.string.about_funList_globalFloatingBackBtn_on)
                floatBtn.visibility = View.VISIBLE
            }
            else {
                btn_globalFloatingBackBtnSwitch.text = getString(R.string.about_funList_globalFloatingBackBtn_off)
                floatBtn.visibility = View.GONE
            }
            sp.edit().putBoolean("flag_globalFloatingBackButton", !sp.getBoolean("flag_globalFloatingBackButton", false)).apply()
        }

        // clear all data
        findViewById<Button>(R.id.about_fun_clearData).setOnClickListener {
            AlertDialog.Builder(this)
                    .setTitle(getString(R.string.common_warning))
                    .setMessage(getString(R.string.about_funList_clearAllData_dialog_text))
                    .setPositiveButton(getString(R.string.about_funList_clearAllData_dialog_continueRemoving)) { _, _ ->
                        sp.edit().putBoolean("flag_clearStore", true).apply()
                        (application as SharedData).store.removeAll()
                        Toast.makeText(this, getString(R.string.about_funList_clearAllData_dialog_dataCleared), Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton(getString(R.string.common_cancel), null)
                    .create()
                    .show()
        }
    }

    companion object {
        const val THEME_DEFAULT = R.style.Theme_LittlePlan_DeepForest
    }
}
