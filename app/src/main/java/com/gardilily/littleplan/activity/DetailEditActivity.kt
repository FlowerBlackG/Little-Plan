package com.gardilily.littleplan.activity

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.gardilily.littleplan.R

class DetailEditActivity : Activity() {
    private val TEXT_MAX_LEN = 1200
    private var lenTooLong = false
    private lateinit var view_count: TextView
    private lateinit var view_editText: EditText

    private lateinit var sp: SharedPreferences
    private var activity_theme_resID = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        sp = getSharedPreferences("SP_LittlePlan", MODE_PRIVATE)
        activity_theme_resID = sp.getInt("theme_targetResID", About.THEME_DEFAULT)
        setTheme(activity_theme_resID)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_todo_edit_detail)

        view_count = findViewById(R.id.editTodo_editDetail_count)
        view_editText = findViewById(R.id.editTodo_editDetail_text)

        view_editText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                runOnUiThread {
                    updateCount()
                }
            }
        })

        findViewById<TextView>(R.id.editTodo_editDetail_done).setOnClickListener {
            // confirm
            confirm()
        }

        updateCount()

        if (intent.getBooleanExtra("is complete", false))
            findViewById<View>(R.id.editTodo_editDetail_background).background = getDrawable(R.drawable.shape_card_complete)
        else
            findViewById<View>(R.id.editTodo_editDetail_background).background = getDrawable(R.drawable.shape_card_task)

        val oriTxt = intent.getStringExtra("ori text")
        if (oriTxt != getString(R.string.editTodo_none))
            view_editText.text = SpannableStringBuilder(oriTxt)
    }

    private fun confirm(byDefaultFloatingBtn: Boolean = true) {
        if (lenTooLong) {
            if (byDefaultFloatingBtn)
                Toast.makeText(this, getString(R.string.editTodo_editDetail_textTooLong), Toast.LENGTH_SHORT).show()
        } else {
            val str = view_editText.text.toString()
            val res = Intent()
            res.putExtra("res text", if (str == "") getString(R.string.editTodo_none) else str)
            setResult(RESULT_OK, res)
            finish()
        }
    }

    private fun updateCount() {
        val str = view_editText.text.toString()
        val len = str.length
        view_count.text = "$len / $TEXT_MAX_LEN"
        if (len > TEXT_MAX_LEN && !lenTooLong) {
            lenTooLong = true
            view_count.setTextColor(Color.rgb(255, 70, 70))
        } else if (len <= TEXT_MAX_LEN && lenTooLong) {
            lenTooLong = false
            view_count.setTextColor(Color.rgb(255, 255, 255))
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
