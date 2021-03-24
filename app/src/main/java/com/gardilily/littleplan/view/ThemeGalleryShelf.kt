package com.gardilily.littleplan.view

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.gardilily.littleplan.R

/**
 * A customized shelf based on Linear Layout to display contents in rows, two in a row.
 */
class ThemeGalleryShelf(context: Context): LinearLayout(context) {
    private val c = context
    private val layout: LinearLayout
    private var card_count = 0
    private var row_layout: LinearLayout? = null
    init {
        LayoutInflater.from(context).inflate(R.layout.cardshelf_theme_gallery_shelf, this, true)
        layout = findViewById(R.id.cardshelf_layout)
    }

    /**
     * Add a card to display in the shelf. If all the rows are filled, it will create a new row
     * for the card, or it will add the card to the last empty place.
     *
     * @param v Card to be added.
     */
    fun addCard(v: View) {
        Log.d("TG Shelf", "CP0: $card_count")
        if (card_count % 2 == 0) {
            row_layout = createLinearLayout()
            layout.addView(row_layout)
        }
        row_layout!!.addView(v)
        card_count++
    }

    private fun createLinearLayout(): LinearLayout {
        val linearLayout = LinearLayout(c)
        val spMultiply = resources.displayMetrics.scaledDensity
        val params = LayoutParams((380f * spMultiply).toInt(), (190f * spMultiply).toInt())
        linearLayout.layoutParams = params
        linearLayout.orientation = HORIZONTAL
        return linearLayout
    }
}
