package com.gardilily.littleplan.view

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import com.gardilily.littleplan.R
import org.json.JSONObject

/**
 * A modified view to show a single task as a card.
 *
 * The card has two sides, front for basic information and back for basic functions.
 * If the task the card shows isn't an accumulative one, the back of the card would show the name
 * of the task, show two buttons for order changing and one button for deleting the task.
 * If the task is accumulative, it would show two more buttons which are "add" and "minus" to
 * modify the accumulation. It would also show the accumulation number and target number instead
 * of the name of the task.
 */
abstract class TodoCard(context: Context) : FrameLayout(context) {

    // init views
    val view_btn: View
    private var view_layout_def: RelativeLayout
    val view_title: TextView
    val view_dueDate: TextView
    val view_loc: TextView
    private val view_accuText: TextView

    private val view_layout_accu: RelativeLayout
    private val view_accu_curr: TextView
    private val view_accu_tar: TextView
    private val view_accu_add: TextView
    private val view_accu_minus: TextView
    private val view_accu_up: TextView
    private val view_accu_down: TextView

    /**
     * Json Object of the task the card should present.
     */
    abstract var dataJson: JSONObject

    /**
     * ID of the task the card is showing.
     */
    abstract var cid: Int

    /**
     * An integer for advanced applications.
     */
    abstract var tag: Int

    // init other variables
    val VIEW_MAIN = 1
    val VIEW_MORE = 2
    var current_view = VIEW_MAIN

    init {
        // bind views and set on click listeners

        LayoutInflater.from(context).inflate(R.layout.card_todo, this, true)

        // bind views
        view_btn = findViewById(R.id.card_btn)
        view_layout_def = findViewById(R.id.card_layout_def)
        view_title = findViewById(R.id.card_def_title)
        view_dueDate = findViewById(R.id.card_def_dueDate)
        view_loc = findViewById(R.id.card_def_loc)
        view_accuText = findViewById(R.id.card_def_accuText)

        view_layout_accu = findViewById(R.id.card_layout_accu)
        view_accu_curr = findViewById(R.id.card_accu_curr)
        view_accu_tar = findViewById(R.id.card_accu_tar)

        view_accu_add = findViewById(R.id.card_accu_add)
        view_accu_minus = findViewById(R.id.card_accu_minus)
        view_accu_up = findViewById(R.id.card_accu_up)
        view_accu_down = findViewById(R.id.card_accu_down)

        // init clicks
        view_accu_add.setOnClickListener {
            // accumulate add
            accuChange(cid, tag, 1, dataJson.getBoolean("is_complete"))
        }
        view_accu_minus.setOnClickListener {
            // accumulate minus
            accuChange(cid, tag, -1, dataJson.getBoolean("is_complete"))
        }
        view_accu_up.setOnClickListener {
            // change order up
            orderChange(cid, tag, true, dataJson.getBoolean("is_complete"))
        }
        view_accu_down.setOnClickListener {
            // change order down
            orderChange(cid, tag, false, dataJson.getBoolean("is_complete"))
        }

        view_btn.setOnClickListener {
            when (current_view) {
                VIEW_MAIN -> {
                    view_layout_def.visibility = INVISIBLE
                    view_layout_accu.visibility = VISIBLE
                    current_view = VIEW_MORE
                }
                VIEW_MORE -> {
                    view_layout_def.visibility = VISIBLE
                    view_layout_accu.visibility = INVISIBLE
                    current_view = VIEW_MAIN
                }
            }
        }

        view_btn.setOnLongClickListener {
            longClick(cid, tag, dataJson.getBoolean("is_complete"))
            return@setOnLongClickListener true
        }

        findViewById<TextView>(R.id.card_accu_remove).setOnClickListener {
            btnRemove(cid, tag, dataJson.getBoolean("is_complete"), false)
        }

        findViewById<TextView>(R.id.card_accu_remove).setOnLongClickListener {
            btnRemove(cid, tag, dataJson.getBoolean("is_complete"), true)
            return@setOnLongClickListener true
        }

        findViewById<TextView>(R.id.card_accu_detail).setOnClickListener {
            btnDetail(cid, tag, dataJson.getBoolean("is_complete"), false, dataJson.getString("detail"))
        }

        findViewById<TextView>(R.id.card_accu_detail).setOnLongClickListener {
            btnDetail(cid, tag, dataJson.getBoolean("is_complete"), true, dataJson.getString("detail"))
            return@setOnLongClickListener true
        }
    }

    /**
     * Update the card to the latest task data stored in itself.
     */
    fun updateContent() {
        /////////////////////////////////////////////////////////////////////////////////////////
        /**/ if (true) {                                                                     /**/
        /**/     if (dataJson.toString().indexOf("detail") == -1) {                    /**/
        /**/        // created by older version of the app, should add "detail"              /**/
        /**/         dataJson.put("detail", context.getString(R.string.editTodo_none)) /**/
        /**/     }                                                                           /**/
        /**/ }                                                                               /**/
        /////////////////////////////////////////////////////////////////////////////////////////

        // set content
        Log.d("Todo Card.Update Content", dataJson.toString())

        view_accu_tar.visibility = VISIBLE
        view_accu_add.visibility = VISIBLE
        view_accu_minus.visibility = VISIBLE

        view_title.text = dataJson.getString("title")
        view_loc.text = dataJson.getString("location")
        if (dataJson.getString("due_date") != "0")
            view_dueDate.text = dataJson.getString("due_date")

        if (dataJson.getBoolean("is_accumulate")) {
            if (dataJson.getInt("target") == 0) {
                view_accu_curr.text = "${dataJson.getInt("current")}"
                view_accu_tar.visibility = GONE
                view_accuText.text = "${dataJson.getInt("current")}"
            } else {
                view_accu_curr.text = "${dataJson.getInt("current")}"
                view_accu_tar.text = "${dataJson.getInt("target")}"
                view_accuText.text = "${dataJson.getInt("current")} / ${dataJson.getInt("target")}"
            }
        } else {
            view_accuText.text = ""
            view_accu_curr.text = dataJson.getString("title")
            view_accu_tar.visibility = GONE
            view_accu_add.visibility = INVISIBLE
            view_accu_minus.visibility = INVISIBLE
        }

        // set background
        if (dataJson.getBoolean("is_complete")) {
            view_btn.background = context.getDrawable(R.drawable.shape_card_complete)
            view_accu_up.background = context.getDrawable(R.drawable.shape_card_complete_button)
            view_accu_down.background = context.getDrawable(R.drawable.shape_card_complete_button)
            view_accu_minus.background = context.getDrawable(R.drawable.shape_card_complete_button)
            view_accu_add.background = context.getDrawable(R.drawable.shape_card_complete_button)
            findViewById<TextView>(R.id.card_accu_remove).background = context.getDrawable(R.drawable.shape_card_complete_button)
            findViewById<TextView>(R.id.card_accu_detail).background = context.getDrawable(R.drawable.shape_card_complete_button)
        }
        else {
            view_btn.background = context.getDrawable(R.drawable.shape_card_task)
            view_accu_up.background = context.getDrawable(R.drawable.shape_card_task_button)
            view_accu_down.background = context.getDrawable(R.drawable.shape_card_task_button)
            view_accu_minus.background = context.getDrawable(R.drawable.shape_card_task_button)
            view_accu_add.background = context.getDrawable(R.drawable.shape_card_task_button)
            findViewById<TextView>(R.id.card_accu_remove).background = context.getDrawable(R.drawable.shape_card_task_button)
            findViewById<TextView>(R.id.card_accu_detail).background = context.getDrawable(R.drawable.shape_card_task_button)
        }
    }

    /**
     * Update the card to appointed task data.
     *
     * @param data Json Object of the task.
     */
    fun updateContent(data: JSONObject) {
        dataJson = data
        updateContent()
    }

    /**
     * Called when the buttons that change the order are clicked.
     *
     * @param cid ID of the task the card is showing.
     * @param tag Tag of the card.
     * @param key_up Whether the user want to move the task upper or lower. True means upper.
     * @param is_complete Whether the task the card showing is completed.
     */
    abstract fun orderChange(cid: Int, tag: Int, key_up: Boolean, is_complete: Boolean)

    /**
     * Called when user trys to change the accumulation of the task.
     *
     * @param cid ID of the task the card is showing.
     * @param tag Tag of the card.
     * @param change Number attempts to be added to the accumulation.
     * @param is_complete Whether the task the card showing is completed.
     */
    abstract fun accuChange(cid: Int, tag: Int, change: Int, is_complete: Boolean) // change = -1 / 1

    /**
     * Called when the card has been clicked for a relatively long time.
     *
     * @param cid ID of the task the card is showing.
     * @param tag Tag of the card.
     * @param is_complete Whether the task the card showing is completed.
     */
    abstract fun longClick(cid: Int, tag: Int, is_complete: Boolean)

    /**
     * Function to be called when the remove button clicked.
     *
     * @param cid ID of the task that the remove button has been clicked.
     * @param tag Tag of the card that the remove button has been clicked.
     * @param is_complete Whether the task the card showing is completed.
     * @param is_longClick Whether the click is long click or not.
     */
    abstract fun btnRemove(cid: Int, tag: Int, is_complete: Boolean, is_longClick: Boolean)

    /**
     * Function to be called when the detail button clicked.
     *
     * @param cid ID of the task that the remove button has been clicked.
     * @param tag Tag of the card that the remove button has been clicked.
     * @param is_complete Whether the task the card showing is completed.
     * @param is_longClick Whether the click is long click or not.
     * @param text Detail of the task written by user.
     */
    abstract fun btnDetail(cid: Int, tag: Int, is_complete: Boolean, is_longClick: Boolean, text: String)
}
