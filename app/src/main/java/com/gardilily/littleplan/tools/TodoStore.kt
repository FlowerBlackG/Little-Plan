package com.gardilily.littleplan.tools

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import org.json.JSONObject

/**
 * A customized class based on Shared Preferences to store all data of tasks.
 * There should be only one instance of this class in the whole project in case data confusion.
 */
class TodoStore(c: Context) {

    /**
     * Basic Shared Preferences to store data.
     */
    private lateinit var sp: SharedPreferences //= context.getSharedPreferences("SP_LittlePlan", Context.MODE_PRIVATE)

    /**
     * Local data of all tasks.
     * Should be initiated as soon as possible.
     */
    private lateinit var jsonObj: JSONObject //= JSONObject(sp.getString("task_jsonString", "{}"))
    private val context = c

    /**
     * Should be called as soon as the instance of TodoStore created.
     */
    fun initiate() {
        sp = context.getSharedPreferences("SP_LittlePlan", Context.MODE_PRIVATE)
        jsonObj = JSONObject(sp.getString("task_jsonString", "{}")!!)
    }

    /**
     * Store latest json data to local storage (Shared Preference).
     * Should be called after every modification related jsonObject.
     */
    private fun update() {
        sp.edit().putString("task_jsonString", jsonObj.toString()).apply()
    }

    /**
     * Remove a task by its ID.
     *
     * @param rid ID of the task to be removed.
     */
    fun removeByID(rid: Int){
        if (!jsonObj.has("todo_id_$rid"))
            return
        jsonObj.remove("todo_id_$rid")
        var i = rid + 1
        while (jsonObj.has("todo_id_$i")){
            val t = jsonObj.getJSONObject("todo_id_$i")
            jsonObj.put("todo_id_${i-1}", t)
            jsonObj.remove("todo_id_$i")
            i++
        }
        update()
    }

    /**
     * Remove all data stored locally
     */
    fun removeAll() {
        jsonObj = JSONObject("{}")
        update()
    }

    /**
     * Add a task after the end of task list.
     *
     * @param new_obj Json Object of the new task.
     *                It should be a Json Object with following values:
     *                * is_complete
     *                * is_accumulate
     *                * title
     *                * target
     *                * current
     *                * location
     *                * due_date
     * @return ID of the newly added task.
     */
    fun addNewTask(new_obj: JSONObject): Int{
        Log.d("Store.addNewTask", new_obj.toString())
        val new_id = jsonObj.length()
        Log.d("Store.addNewTask", "new id = $new_id")
        jsonObj.put("todo_id_$new_id", new_obj)
        update()
        return new_id
    }

    /**
     * Add a task with ID 0.
     *
     * @param new_obj Json Object of the new task.
     *                It should be a Json Object with following values:
     *                * is_complete
     *                * is_accumulate
     *                * title
     *                * target
     *                * current
     *                * location
     *                * due_date
     * @return ID of the newly added task. It would always be 0.
     */
    fun addNewTaskToStart(new_obj: JSONObject): Int{
        Log.d("Store.addNewTask", new_obj.toString())
        val end_id = jsonObj.length() - 1
        for (i in end_id downTo 0) {
            val t = jsonObj.getJSONObject("todo_id_$i")
            jsonObj.remove("todo_id_$i")
            jsonObj.put("todo_id_${i+1}", t)
        }
        jsonObj.put("todo_id_0", new_obj)
        update()
        return 0
    }

    /**
     * Modify a task by its ID.
     *
     * @param id ID of the task to be modified.
     * @param new_obj Json Object to replace which of the existing task.
     */
    fun modifyTaskByID(id: Int, new_obj: JSONObject){
        jsonObj.remove("todo_id_$id")
        jsonObj.put("todo_id_$id", new_obj)
        update()
    }

    /**
     * Get the Json Object of specified task.
     *
     * @param rid ID of which the Json Object is wanted
     * @return The Json Object of specified task
     */
    fun getTaskJsonByID(rid: Int): JSONObject {
        if (!jsonObj.has("todo_id_$rid"))
            return JSONObject("{}")
        return jsonObj.getJSONObject("todo_id_$rid")
    }

    /**
     * Swap the order of two tasks.
     *
     * @param a ID of the first task
     * @param b ID of the second task
     */
    fun swapOrder(a: Int, b: Int){
        val ja = jsonObj.getJSONObject("todo_id_$a")
        val jb = jsonObj.getJSONObject("todo_id_$b")
        jsonObj.remove("todo_id_$a")
        jsonObj.remove("todo_id_$b")
        jsonObj.put("todo_id_$a", jb)
               .put("todo_id_$b", ja)
        update()
    }

    /**
     * The function is designed for accumulative tasks. Call this function to change the number
     * of current accumulation.
     * Nothing would happen if the modified accumulation is less than 0.
     *
     * @param id ID of the task to be modified.
     * @param change Number should be add to the accumulation.
     *
     * @return Json Object of modified task.
     */
    fun changeAccuCurrent(id: Int, change: Int): JSONObject {
        val obj = jsonObj.getJSONObject("todo_id_$id")
        var curr = obj.getInt("current")
        curr += change
        if (curr < 0)
            return obj
        obj.remove("current")
        obj.put("current", curr)
        jsonObj.remove("todo_id_$id")
        jsonObj.put("todo_id_$id", obj)
        update()
        return obj
    }

    /**
     * Get the original stored data in form of Json Object
     *
     * @return Full Json Object in the store
     */
    fun getFullJsonObj(): JSONObject {
        return jsonObj
    }

    /**
     * Replace stored data with new data.
     *
     * @param json Json Object of data to replace existed data.
     */
    fun updateFullJsonObj(json: JSONObject) {
        jsonObj = json
        update()
    }
}
