package com.gardilily.littleplan.activity

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.View
import android.widget.EditText
import android.widget.TextView
import com.gardilily.littleplan.R

class IntegerEditActivity : Activity() {
    private lateinit var view_done: TextView
    private lateinit var view_int: EditText

    private lateinit var sp: SharedPreferences
    private var activity_theme_resID = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        sp = getSharedPreferences("SP_LittlePlan", MODE_PRIVATE)
        activity_theme_resID = sp.getInt("theme_targetResID", About.THEME_DEFAULT)
        setTheme(activity_theme_resID)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_todo_edit_int)

        view_done = findViewById(R.id.editTodo_editInt_done)
        view_done.setOnClickListener { returnRes() }

        view_int = findViewById(R.id.editTodo_editInt_int)
        view_int.text = SpannableStringBuilder(intent.getIntExtra("ori int", 0).toString())

        if (intent.getBooleanExtra("is complete", false))
            findViewById<View>(R.id.editTodo_editInt_background).background = getDrawable(R.drawable.shape_card_complete)
        else
            findViewById<View>(R.id.editTodo_editInt_background).background = getDrawable(R.drawable.shape_card_task)
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

    private fun returnRes() {
        val res = Intent()
        res.putExtra("res int", view_int.text.toString().toInt())
        setResult(RESULT_OK, res)
        finish()
    }
}
