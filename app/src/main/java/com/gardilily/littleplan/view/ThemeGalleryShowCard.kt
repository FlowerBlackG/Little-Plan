package com.gardilily.littleplan.view

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.util.Log
import android.util.TypedValue
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.gardilily.littleplan.R

/**
 * A card shows basic information of the theme.
 * With appearance of a rectangle with all angles smoothed, and a name tag fits perfectly to
 * the button of the rectangle with half-apparent background.
 *
 * Suggested to be used together with Theme Gallery Shelf.
 *
 * @param onClick Function to be called then the card is clicked.
 *                Parameter Res ID will pass the Resource ID of the theme that the card is showing.
 */
class ThemeGalleryShowCard(context: Context, onClick: (resID: Int)->Unit) : FrameLayout(context) {

    private val view_btn: View
    private val view_image: RelativeLayout
    private val view_text: TextView

    private val c = context

    /**
     * Text shown on the name tag.
     */
    var themeName = ""
        get() {
            return field
        }
        set(value) {
            field = value
            view_text.text = value
        }

    /**
     * Resource ID of the theme to be shown.
     */
    var themeResID = 0
        get() {
            return field
        }
        set(value) {
            Log.d("TG S Card", "CP0: $value")
            field = value
            val typedValueA = TypedValue()
            val typedValueB = TypedValue()
            val showTheme = ContextThemeWrapper(c, value).theme
            showTheme.resolveAttribute(R.attr.theme_main_background_color, typedValueA, true)
            showTheme.resolveAttribute(R.attr.theme_main_background_color_gradient, typedValueB, true)
            val colorArr = IntArray(2)
            colorArr[0] = typedValueA.data
            colorArr[1] = typedValueB.data
            val drawable = GradientDrawable()
            drawable.shape = GradientDrawable.RECTANGLE
            drawable.colors = colorArr
            drawable.orientation = GradientDrawable.Orientation.TL_BR

            val rad = 16.0f * resources.displayMetrics.scaledDensity

            val radArr = FloatArray(8)
            for (i in 0..7) {
                radArr[i] = rad
            }
            drawable.cornerRadii = radArr
            view_image.background = drawable
        }

    init {
        LayoutInflater.from(context).inflate(R.layout.card_theme_gallery_item, this, true)
        view_btn = findViewById(R.id.card_galleryItem_btn)
        view_image = findViewById(R.id.card_galleryItem_image)
        view_text = findViewById(R.id.card_galleryItem_text)
        view_btn.setOnClickListener { onClick(themeResID) }
    }
}
