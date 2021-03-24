package com.gardilily.littleplan.activity

import android.app.Activity
import android.app.ActivityOptions
import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.Pair
import android.view.View
import android.widget.*
import com.gardilily.littleplan.R
import com.gardilily.littleplan.activity.About.Companion.THEME_DEFAULT
import com.gardilily.littleplan.tools.CloudApi
import com.gardilily.littleplan.tools.SharedData
import com.gardilily.littleplan.tools.TodoStore
import com.gardilily.littleplan.view.TodoCard
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.thread
import kotlin.math.max
import kotlin.math.min

class MainActivity : Activity() {
    private lateinit var layout: LinearLayout

    private lateinit var app: SharedData
    private lateinit var store: TodoStore

    private val card_task_array = ArrayList<TodoCard>()
    private val card_complete_array = ArrayList<TodoCard>()

    private lateinit var sp: SharedPreferences

    private var activity_theme_resID = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        sp = getSharedPreferences("SP_LittlePlan", MODE_PRIVATE)
        activity_theme_resID = sp.getInt("theme_targetResID", THEME_DEFAULT)
        setTheme(activity_theme_resID)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        app = application as SharedData
        app.initiate(this)
        store = app.store
        store.initiate()

        Log.d("Main.Store", store.getTaskJsonByID(0).toString())

        init_btnAdd()
        layout = findViewById(R.id.home_layout)

        initCardsAndAddToLayout()
        app.localDataLegacy = false

        findViewById<LinearLayout>(R.id.home_titleZone).setOnClickListener {
            val intent = Intent()
            intent.setClass(this, About::class.java)
            val options: ActivityOptions = ActivityOptions.makeSceneTransitionAnimation(this,
                    Pair.create(findViewById(R.id.home_appTitle), "app.title"),
                    Pair.create(findViewById(R.id.home_appTitle_eng), "app.title.eng"))
            startActivity(intent, options.toBundle())
        }

        checkUpdate()
        prepareFirstStartupTutorial()
    }

    private fun prepareFirstStartupTutorial() {
        val tutorialVersion = 1
        if (sp.getInt("tutorial_home", 0) < tutorialVersion) {
            // should show tutorial
            val layout = findViewById<RelativeLayout>(R.id.home_tutorial_layout)
            val btn = findViewById<TextView>(R.id.home_tutorial_button)
            val film1 = findViewById<View>(R.id.home_tutorial_step1_film)
            val film2Lower = findViewById<View>(R.id.home_tutorial_step2_film_lower)
            val film2Upper = findViewById<View>(R.id.home_tutorial_step2_film_upper)

            fun showTutorial() {
                layout.visibility = View.VISIBLE
                film1.visibility = View.GONE
            }
            fun stepOneConfirm() {
                film1.visibility = View.VISIBLE
                film2Upper.visibility = View.GONE
                film2Lower.visibility = View.VISIBLE
                btn.text = getString(R.string.common_okay)
            }
            fun stepTwoConfirm() {
                layout.visibility = View.GONE
                sp.edit().putInt("tutorial_home", tutorialVersion).apply()
            }

            var stepOneConfirmed = false
            showTutorial()
            btn.setOnClickListener {
                if (!stepOneConfirmed) {
                    stepOneConfirm()
                    stepOneConfirmed = true
                } else {
                    stepTwoConfirm()
                }
            }
        }
    }

    private fun prepareFirstCardTutorial() {
        val tutorialVersion = 1
        if (sp.getInt("tutorial_home_card", 0) < tutorialVersion) {
            fun tutorialLongClick() {
                AlertDialog.Builder(this)
                        .setTitle(getString(R.string.common_hint))
                        .setMessage(getString(R.string.home_tutorial_firstCard_longClickToEdit))
                        .setPositiveButton(getString(R.string.common_okay)) { _, _ ->
                            sp.edit().putInt("tutorial_home_card", tutorialVersion).apply()
                        }
                        .create()
                        .show()
            }

            AlertDialog.Builder(this)
                    .setTitle(getString(R.string.common_hint))
                    .setMessage(getString(R.string.home_tutorial_firstCard_clickToQuickFunctions))
                    .setPositiveButton(getString(R.string.common_nextStep)) { _, _ -> tutorialLongClick() }
                    .create()
                    .show()
        }
    }

    private fun initCardsAndAddToLayout() {
        var i = 0
        while (true) {
            var obj = store.getTaskJsonByID(i)
            if (!obj.has("is_complete")) {
                break
            }
            Log.d("Main", obj.toString())

            createCardToArray(i, obj, obj.getBoolean("is_complete"))
            i++
        }

        card_task_array.forEach {
            layout.addView(it)
        }
        card_complete_array.forEach {
            layout.addView(it)
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d("Main.onResume", "${app.localDataLegacy}")

        if (sp.getInt("theme_targetResID", THEME_DEFAULT) != activity_theme_resID) {
            reloadActivity()
        }
        else if (app.localDataLegacy) {
            app.localDataLegacy = false
            layout.removeAllViews()
            card_task_array.clear()
            card_complete_array.clear()
            initCardsAndAddToLayout()
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

    override fun onRestart() {
        super.onRestart()
        Log.d("Main.onRestart", "${sp.getBoolean("flag_clearStore", false)}")
        if (sp.getBoolean("flag_clearStore", false)) {
            card_task_array.clear()
            card_complete_array.clear()
            layout.removeAllViews()
            sp.edit().putBoolean("flag_clearStore", false).apply()
        }
    }

    private fun removeCard(cid: Int, tag: Int, is_complete: Boolean) {
        Log.d("Main.removeCard", "$cid, $tag")
        store.removeByID(cid)
        if (is_complete) {
            layout.removeViewAt(tag + card_task_array.size)
            card_complete_array.removeAt(tag)
        } else {
            layout.removeViewAt(tag)
            card_task_array.removeAt(tag)
        }

        card_task_array.forEach {
            if (it.cid > cid) {
                it.cid--
            }
            if (!is_complete && it.tag > tag)
                it.tag--
        }
        card_complete_array.forEach {
            if (it.cid > cid) {
                it.cid--
            }
            if (is_complete && it.tag > tag)
                it.tag--
        }
    }

    private fun createCardToArray(cid: Int, dataJson: JSONObject, is_complete: Boolean, add_to_start: Boolean = false): TodoCard {
        var tag = if (is_complete) card_complete_array.size else card_task_array.size

        if (add_to_start) {
            if (is_complete) {
                card_complete_array.forEach {
                    it.tag++
                }
            } else {
                card_task_array.forEach {
                    it.tag++
                }
            }
            tag = 0
        }

        val card = object : TodoCard(this) {
            override var dataJson = dataJson
            override var cid = cid
            override var tag = tag
            override fun orderChange(cid: Int, tag: Int, key_up: Boolean, is_complete: Boolean) {
                card_orderChange(cid, tag, key_up, is_complete)
            }

            override fun accuChange(cid: Int, tag: Int, change: Int, is_complete: Boolean) {
                card_accuChange(cid, tag, change, is_complete)
            }

            override fun longClick(cid: Int, tag: Int, is_complete: Boolean) {
                card_longClick(cid, tag, is_complete)
            }

            override fun btnRemove(cid: Int, tag: Int, is_complete: Boolean, is_longClick: Boolean) {
                if (!is_longClick) {
                    Toast.makeText(this@MainActivity, getString(R.string.home_btnRemove_longClickToRemove), Toast.LENGTH_SHORT).show()
                } else {
                    if (sp.getBoolean("flag_removeProtection", true)) {
                        AlertDialog.Builder(this@MainActivity)
                                .setTitle(getString(R.string.home_btnRemove_sureToDeleteQuest))
                                .setMessage(getString(R.string.home_btnRemove_sureWhatURDoing))
                                .setPositiveButton(getString(R.string.home_btnRemove_iKnow)) { _, _ ->
                                    removeCard(cid, tag, is_complete)
                                }
                                .setNegativeButton(getString(R.string.home_btnRemove_dontRemove), null)
                                .create()
                                .show()
                    } else
                        removeCard(cid, tag, is_complete)
                }
            }

            override fun btnDetail(cid: Int, tag: Int, is_complete: Boolean, is_longClick: Boolean, text: String) {
                AlertDialog.Builder(this@MainActivity)
                        .setTitle(getString(R.string.editTodo_param_detail))
                        .setMessage(text)
                        .setPositiveButton(getString(R.string.common_okay), null)
                        .create()
                        .show()
            }
        }
        card.updateContent()
        if (add_to_start) {
            if (is_complete)
                card_complete_array.add(0, card)
            else
                card_task_array.add(0, card)
        } else {
            if (is_complete)
                card_complete_array.add(card)
            else
                card_task_array.add(card)
        }
        return card
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("Main.ActivityResult", "rc: $requestCode, $resultCode; ${data?.getStringExtra("data str")}")

        if (resultCode == RESULT_CANCELED)
            return

        if (!data?.getBooleanExtra("modified", false)!!)
            return
        val dataStr = data.getStringExtra("data str")

        when (requestCode) {
            EDIT_TYPE_NEW -> {
                val mID = store.addNewTaskToStart(JSONObject(dataStr))

                card_complete_array.forEach {
                    it.cid++
                }
                card_task_array.forEach {
                    it.cid++
                }

                val add_index = if (JSONObject(dataStr).getBoolean("is_complete")) card_task_array.size else 0
                thread {
                    runOnUiThread {
                        layout.addView(createCardToArray(mID,
                                JSONObject(dataStr),
                                JSONObject(dataStr).getBoolean("is_complete"),
                                true), add_index)
                    }
                }
                runOnUiThread { prepareFirstCardTutorial() }
            }

            EDIT_TYPE_MODIFY -> {
                val modify_id = data.getIntExtra("modify id", -1)
                store.modifyTaskByID(modify_id, JSONObject(dataStr))
                val ori_tag = data.getIntExtra("tag", -1)
                val ori_is_complete = data.getBooleanExtra("ori is complete", false)
                val json = JSONObject(dataStr)

                val arr_ori = if (ori_is_complete) card_complete_array else card_task_array

                if (json.getBoolean("is_complete") xor ori_is_complete) {
                    // complete status changed
                    /*
                        Sequence
                        1. delete the card
                        2. add the card with id 0
                     */
                    removeCard(modify_id, ori_tag, ori_is_complete)
                    val mID = store.addNewTaskToStart(json)

                    card_complete_array.forEach {
                        it.cid++
                    }
                    card_task_array.forEach {
                        it.cid++
                    }

                    val add_index = if (json.getBoolean("is_complete")) card_task_array.size else 0
                    thread {
                        runOnUiThread {
                            layout.addView(createCardToArray(mID,
                                    json,
                                    !ori_is_complete,
                                    true), add_index)
                        }
                    }
                } else {
                    // complete status unchanged
                    Log.d("Main.modify", "${card_complete_array.size} : ${card_task_array.size} : $ori_tag")
                    arr_ori[ori_tag].updateContent(json)
                }
            }
        }
    }

    private fun init_btnAdd() {
        findViewById<TextView>(R.id.home_add).setOnClickListener {
            val intent = Intent()
            intent.setClass(this, EditTodoActivity::class.java)
            intent.putExtra("edit type", EDIT_TYPE_NEW)
            startActivityForResult(intent, EDIT_TYPE_NEW)
        }
    }

    private fun card_orderChange(cid: Int, tag: Int, key_up: Boolean, is_complete: Boolean) {
        if (tag == 0 && key_up) {
            Toast.makeText(this, getString(R.string.home_accuChange_cantMoveUpper), Toast.LENGTH_SHORT).show()
            return
        }

        val target_array = if (is_complete) card_complete_array else card_task_array

        if (!key_up && tag == target_array.size - 1) {
            Toast.makeText(this, getString(R.string.home_accuChange_cantMoveLower), Toast.LENGTH_SHORT).show()
            return
        }

        val change = if (key_up) -1 else 1
        val cardNext = target_array[tag + change]
        target_array[tag].tag += change
        cardNext.tag -= change
        store.swapOrder(cid, cardNext.cid)

        val t = cardNext.cid
        cardNext.cid = target_array[tag].cid
        target_array[tag].cid = t

        Collections.swap(target_array, tag, tag + change)

        val task_idx_addition = if (is_complete) card_task_array.size else 0

        layout.removeViewAt(min(tag, tag + change) + task_idx_addition)
        layout.removeViewAt(min(tag, tag + change) + task_idx_addition)
        layout.addView(target_array[max(tag, tag + change)], min(tag, tag + change) + task_idx_addition)
        layout.addView(target_array[min(tag, tag + change)], min(tag, tag + change) + task_idx_addition)

    }

    private fun card_accuChange(cid: Int, tag: Int, change: Int, is_complete: Boolean) {
        if (is_complete) {
            val res = store.changeAccuCurrent(cid, change)
            card_complete_array[tag].dataJson = res
            Log.d("Main.card_accuChange", res.toString())
            card_complete_array[tag].updateContent(res)
        } else {
            val res = store.changeAccuCurrent(cid, change)
            card_task_array[tag].dataJson = res
            Log.d("Main.card_accuChange", res.toString())
            card_task_array[tag].updateContent(res)
        }
    }

    private fun card_longClick(cid: Int, tag: Int, is_complete: Boolean) {
        Log.d("Long Click: ", "$cid")
        val card = if (is_complete) card_complete_array[tag] else card_task_array[tag]
        val options: ActivityOptions = ActivityOptions.makeSceneTransitionAnimation(this,
                Pair.create(card.view_btn, "card.background"))

        val intent = Intent()
        intent.setClass(this, EditTodoActivity::class.java)
        intent.putExtra("edit type", EDIT_TYPE_MODIFY)
        intent.putExtra("data json str", store.getTaskJsonByID(cid).toString())
        intent.putExtra("modify id", cid)
        intent.putExtra("tag", tag)
        intent.putExtra("ori is complete", is_complete)
        startActivityForResult(intent, EDIT_TYPE_MODIFY, options.toBundle())
    }


    private fun checkUpdate() {
        CloudApi.checkUpdate(packageManager.getPackageInfo(packageName, 0).longVersionCode, {
            if (it != "1") {
                // update available
                val resArr = it?.split("_div_")
                runOnUiThread {
                    AlertDialog.Builder(this)
                            .setTitle(getString(R.string.home_checkUpdate_newVersionAvailable))
                            .setMessage("${getString(R.string.about_versionShow)}${resArr?.get(2)}\n${getString(R.string.about_buildTimeShow)}${resArr?.get(3)}\n${getString(R.string.home_checkUpdate_detail)}\n\n${resArr?.get(5)}\n")
                            .setPositiveButton(getString(R.string.home_checkUpdate_update)) { _, _ ->
                                val uri = Uri.parse(resArr?.get(4))
                                val intent = Intent(Intent.ACTION_VIEW, uri)
                                startActivity(intent)
                            }
                            .setNegativeButton(getString(R.string.common_cancel), null)
                            .create()
                            .show()
                }
            }
        })
    }

    companion object {
        const val EDIT_TYPE_NEW = 1
        const val EDIT_TYPE_MODIFY = 2
    }
}
