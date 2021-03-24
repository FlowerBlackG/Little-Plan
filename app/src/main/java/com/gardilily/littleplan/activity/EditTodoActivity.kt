package com.gardilily.littleplan.activity

import android.app.Activity
import android.app.ActivityOptions
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.TransitionDrawable
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.util.Log
import android.util.Pair
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.*
import com.gardilily.littleplan.R
import org.json.JSONObject
import java.util.*

class EditTodoActivity : Activity() {
    private var edit_type = -1 //= intent.getIntExtra("edit type", -1)

    private lateinit var view_title: EditText
    private lateinit var view_loc: EditText
    private lateinit var view_layout_isComplete: RelativeLayout
    private lateinit var view_txt_isComplete: TextView
    private lateinit var view_layout_isAccumulate: RelativeLayout
    private lateinit var view_txt_isAccumulate: TextView
    private lateinit var view_layout_target: RelativeLayout
    private lateinit var view_txt_target: TextView
    private lateinit var view_layout_current: RelativeLayout
    private lateinit var view_txt_current: TextView
    private lateinit var view_layout_dueDate: RelativeLayout
    private lateinit var view_txt_dueDate: TextView
    private lateinit var view_txt_detail: TextView

    private lateinit var float_positive: TextView
    private lateinit var float_negative: TextView

    private var dataJson = JSONObject("{}")
    private var modify_id: Int = -1

    private lateinit var sp: SharedPreferences
    private var activity_theme_resID = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        sp = getSharedPreferences("SP_LittlePlan", MODE_PRIVATE)
        activity_theme_resID = sp.getInt("theme_targetResID", About.THEME_DEFAULT)
        setTheme(activity_theme_resID)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_todo)

        edit_type = intent.getIntExtra("edit type", -1)

        bindViews()
        initListClick()
        initFloatingBtnClick()

        if (edit_type == MainActivity.EDIT_TYPE_MODIFY) {
            dataJson = JSONObject(intent.getStringExtra("data json str"))
            Log.d("Edit Todo.dataJson", dataJson.toString())
            modify_id = intent.getIntExtra("modify id", -1)
            view_txt_isComplete.text = if (dataJson.getBoolean("is_complete")) getString(R.string.common_yes) else getString(R.string.common_no)
            view_txt_isAccumulate.text = if (dataJson.getBoolean("is_accumulate")) getString(R.string.common_yes) else getString(R.string.common_no)
            view_title.text = SpannableStringBuilder(dataJson.getString("title"))
            view_txt_target.text = dataJson.getInt("target").toString()
            view_txt_current.text = dataJson.getInt("current").toString()
            view_loc.text = SpannableStringBuilder(dataJson.getString("location"))
            view_txt_dueDate.text = dataJson.getString("due_date")

            ///////////////////////////////////////////////////////////////////////////////////////
            /**/ if (true) {                                                                   /**/
            /**/     if (dataJson.toString().indexOf("detail") == -1) {                  /**/
            /**/        // created by older version of the app, should add "detail"            /**/
            /**/         dataJson.put("detail", getString(R.string.editTodo_none))       /**/
            /**/     }                                                                         /**/
            /**/ }                                                                             /**/
            ///////////////////////////////////////////////////////////////////////////////////////

            view_txt_detail.text = dataJson.getString("detail")

            if (dataJson.getBoolean("is_complete"))
                findViewById<View>(R.id.editTodo_background).background = getDrawable(R.drawable.shape_card_complete)
        }

        if (view_txt_isAccumulate.text == getString(R.string.common_yes)) {
            view_layout_target.visibility = VISIBLE
            view_layout_current.visibility = VISIBLE
        } else {
            view_layout_target.visibility = GONE
            view_layout_current.visibility = GONE
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

    private fun bindViews() {
        view_title = findViewById(R.id.editTodo_title)
        view_loc = findViewById(R.id.editTodo_loc)
        view_layout_isComplete = findViewById(R.id.editTodo_isComplete)
        view_txt_isComplete = findViewById(R.id.editTodo_isComplete_text)
        view_layout_isAccumulate = findViewById(R.id.editTodo_isAccumulate)
        view_txt_isAccumulate = findViewById(R.id.editTodo_isAccumulate_text)
        view_layout_target = findViewById(R.id.editTodo_target)
        view_txt_target = findViewById(R.id.editTodo_target_text)
        view_layout_current = findViewById(R.id.editTodo_current)
        view_txt_current = findViewById(R.id.editTodo_current_text)
        view_layout_dueDate = findViewById(R.id.editTodo_dueDate)
        view_txt_dueDate = findViewById(R.id.editTodo_dueDate_text)
        view_txt_detail = findViewById(R.id.editTodo_detail_text)

        float_positive = findViewById(R.id.editTodo_float_positive)
        float_negative = findViewById(R.id.editTodo_float_negative)
    }

    private fun initListClick() {
        view_layout_isComplete.setOnClickListener {
            if (view_txt_isComplete.text == getString(R.string.common_no)) {
                view_txt_isComplete.text = getString(R.string.common_yes)
                val trans = TransitionDrawable(arrayOf(getDrawable(R.drawable.shape_card_task), getDrawable(R.drawable.shape_card_complete)))
                findViewById<View>(R.id.editTodo_background).background = trans
                trans.startTransition(100)
            } else {
                view_txt_isComplete.text = getString(R.string.common_no)
                val trans = TransitionDrawable(arrayOf(getDrawable(R.drawable.shape_card_complete), getDrawable(R.drawable.shape_card_task)))
                findViewById<View>(R.id.editTodo_background).background = trans
                trans.startTransition(100)
            }
        }
        view_layout_isAccumulate.setOnClickListener {
            if (view_txt_isAccumulate.text == getString(R.string.common_no)) {
                view_txt_isAccumulate.text = getString(R.string.common_yes)
                view_layout_target.visibility = VISIBLE
                view_layout_current.visibility = VISIBLE
            } else {
                view_txt_isAccumulate.text = getString(R.string.common_no)
                view_layout_target.visibility = GONE
                view_layout_current.visibility = GONE
            }
        }

        view_layout_target.setOnClickListener {
            val intent = Intent()
            Log.d("edit todo.target click", view_txt_target.text.toString())
            intent.putExtra("ori int", view_txt_target.text.toString().toInt())
            intent.putExtra("is complete", view_txt_isComplete.text == getString(R.string.common_yes))
            intent.setClass(this, IntegerEditActivity::class.java)
            val options: ActivityOptions = ActivityOptions.makeSceneTransitionAnimation(this,
                    Pair.create(view_txt_target, "edit.int"),
                    Pair.create(float_positive, "edit.done"))
            startActivityForResult(intent, EDIT_INT_TARGET, options.toBundle())
        }

        view_layout_current.setOnClickListener {
            val intent = Intent()
            Log.d("edit todo.curr click", view_txt_current.text.toString())
            intent.putExtra("ori int", view_txt_current.text.toString().toInt())
            intent.putExtra("is complete", view_txt_isComplete.text == getString(R.string.common_yes))
            intent.setClass(this, IntegerEditActivity::class.java)
            val options: ActivityOptions = ActivityOptions.makeSceneTransitionAnimation(this,
                    Pair.create(view_txt_current, "edit.int"),
                    Pair.create(float_positive, "edit.done"))
            startActivityForResult(intent, EDIT_INT_CURRENT, options.toBundle())
        }

        view_layout_dueDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            var mYear = calendar[Calendar.YEAR]
            var mMonth = calendar[Calendar.MONTH]
            var mDay = calendar[Calendar.DAY_OF_MONTH]

            val datePickerDialog = DatePickerDialog(
                    this,
                    { _, year, month, dayOfMonth ->
                        mYear = year
                        mMonth = month + 1
                        mDay = dayOfMonth

                        val calendar = Calendar.getInstance()
                        var mHour = calendar[Calendar.HOUR_OF_DAY]
                        var mMinute = calendar[Calendar.MINUTE]
                        val timePickerDialog = TimePickerDialog(
                                this,
                                { _, hourOfDay, minute ->
                                    mHour = hourOfDay
                                    mMinute = minute

                                    var str = "$mYear"
                                    str += ".${digit2doubleWidthString(mMonth)}"
                                    str += ".${digit2doubleWidthString(mDay)}"
                                    str += "-${digit2doubleWidthString(mHour)}"
                                    str += ":${digit2doubleWidthString(mMinute)}"
                                    view_txt_dueDate.text = str
                                },
                                mHour, mMinute, true
                        )
                        timePickerDialog.show()
                    },
                    mYear, mMonth, mDay
            )
            datePickerDialog.show()
        }

        // edit detail
        findViewById<RelativeLayout>(R.id.editTodo_detail).setOnClickListener {
            val intent = Intent(this, DetailEditActivity::class.java)
            intent.putExtra("ori text", view_txt_detail.text.toString())
            intent.putExtra("is complete", view_txt_isComplete.text == getString(R.string.common_yes))
            val options: ActivityOptions = ActivityOptions.makeSceneTransitionAnimation(this,
                    Pair.create(float_positive, "edit.done"))
            startActivityForResult(intent, EDIT_DETAIL, options.toBundle())
        }
    }

    private fun initFloatingBtnClick() {
        float_positive.setOnClickListener {
            if (view_title.text.isEmpty()) {
                Toast.makeText(this, getString(R.string.editTodo_whyNotWriteATitle), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            dataJson.put("is_complete", view_txt_isComplete.text == getString(R.string.common_yes))
                    .put("is_accumulate", view_txt_isAccumulate.text == getString(R.string.common_yes))
                    .put("title", view_title.text)
                    .put("target", view_txt_target.text)
                    .put("current", view_txt_current.text)
                    .put("location", view_loc.text)
                    .put("due_date", view_txt_dueDate.text)
                    .put("detail", view_txt_detail.text)

            val res = Intent()
            res.putExtra("modified", true)
            res.putExtra("data str", dataJson.toString())
            res.putExtra("modify id", modify_id)
            res.putExtra("tag", intent.getIntExtra("tag", -2))
            res.putExtra("ori is complete", intent.getBooleanExtra("ori is complete", false))
            setResult(RESULT_OK, res)
            finish()
        }
        float_negative.setOnClickListener {
            val res = Intent()
            res.putExtra("modified", false)
            setResult(RESULT_OK, res)
            finish()
        }
    }

    private fun digit2doubleWidthString(d: Int): String {
        if (d >= 10)
            return "$d"
        return "0$d"
    }

    private val EDIT_INT_TARGET = 1
    private val EDIT_INT_CURRENT = 2
    private val EDIT_DETAIL = 3
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_CANCELED)
            return
        when (requestCode) {
            EDIT_INT_TARGET -> {
                view_txt_target.text = data?.getIntExtra("res int", -1).toString()
            }
            EDIT_INT_CURRENT -> {
                view_txt_current.text = data?.getIntExtra("res int", -1).toString()
            }
            EDIT_DETAIL -> {
                view_txt_detail.text = data?.getStringExtra("res text")
            }
        }
    }
}
