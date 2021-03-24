package com.gardilily.littleplan.activity

import android.app.Activity
import android.content.SharedPreferences
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.TransitionDrawable
import android.os.Bundle
import android.util.TypedValue
import android.view.ContextThemeWrapper
import android.view.View
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.gardilily.littleplan.R
import com.gardilily.littleplan.view.ThemeGalleryShelf
import com.gardilily.littleplan.view.ThemeGalleryShowCard

class ChangeTheme : Activity() {
    private lateinit var sp: SharedPreferences
    private var activity_theme_resID = 0
    private lateinit var shelf: ThemeGalleryShelf

    override fun onCreate(savedInstanceState: Bundle?) {
        sp = getSharedPreferences("SP_LittlePlan", MODE_PRIVATE)
        activity_theme_resID = sp.getInt("theme_targetResID", About.THEME_DEFAULT)
        setTheme(activity_theme_resID)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_theme)

        initGlobalFloatingBackBtn()
        initShelf()
        addCards()
    }

    private fun initGlobalFloatingBackBtn() {
        if (sp.getBoolean("flag_globalFloatingBackButton", false)) {
            val globalBackBtn = findViewById<TextView>(R.id.changeTheme_globalBackBtn)
            globalBackBtn.visibility = View.VISIBLE
            globalBackBtn.setOnClickListener { finish() }
        }
    }

    private fun addCards() {
        addCardToShelf(getString(R.string.changeTheme_themeName_PrimaryGradient), R.style.Theme_LittlePlan_PrimaryGradient)
        addCardToShelf(getString(R.string.changeTheme_themeName_PureBlack), R.style.Theme_LittlePlan_PureBlack)
        addCardToShelf(getString(R.string.changeTheme_themeName_HumbleGreen), R.style.Theme_LittlePlan_HumbleGreen)
        addCardToShelf(getString(R.string.changeTheme_themeName_TeenageBlue), R.style.Theme_LittlePlan_TeenageBlue)
        addCardToShelf(getString(R.string.changeTheme_themeName_TeenagePink), R.style.Theme_LittlePlan_TeenagePink)
        addCardToShelf(getString(R.string.changeTheme_themeName_SkyBlue), R.style.Theme_LittlePlan_SkyBlue)
        addCardToShelf(getString(R.string.changeTheme_themeName_MaterialOrange), R.style.Theme_LittlePlan_MaterialOrange)
        addCardToShelf(getString(R.string.changeTheme_themeName_DeepDive), R.style.Theme_LittlePlan_DeepDive)
        addCardToShelf(getString(R.string.changeTheme_themeName_DeepForest), R.style.Theme_LittlePlan_DeepForest)
    }

    private fun initShelf() {
        shelf = ThemeGalleryShelf(this)
        val spMultiply = resources.displayMetrics.scaledDensity
        val params = LinearLayout.LayoutParams((380 * spMultiply).toInt(), LinearLayout.LayoutParams.WRAP_CONTENT)
        shelf.layoutParams = params
        shelf.orientation = LinearLayout.VERTICAL
        findViewById<ScrollView>(R.id.changeTheme_scrollView).addView(shelf)
    }

    private fun addCardToShelf(name: String, resID: Int) {
        val card = createCard(name, resID)
        shelf.addCard(card)
    }

    private fun createCard(name: String, resID: Int): ThemeGalleryShowCard {
        val card = ThemeGalleryShowCard(this) {
            cardClick(it)
        }
        card.themeName = name
        card.themeResID = resID
        return card
    }

    private fun cardClick(resID: Int) {
        sp.edit().putInt("theme_targetResID", resID).apply()

        fun createDrawable(resID: Int): GradientDrawable {
            val typedValueA = TypedValue()
            val typedValueB = TypedValue()
            val showTheme = ContextThemeWrapper(this, resID).theme
            showTheme.resolveAttribute(R.attr.theme_main_background_color, typedValueA, true)
            showTheme.resolveAttribute(R.attr.theme_main_background_color_gradient, typedValueB, true)
            val colorArr = IntArray(2)
            colorArr[0] = typedValueA.data
            colorArr[1] = typedValueB.data
            val drawable = GradientDrawable()
            drawable.shape = GradientDrawable.RECTANGLE
            drawable.colors = colorArr
            drawable.orientation = GradientDrawable.Orientation.TL_BR
            return drawable
        }

        val backgroundCurr = createDrawable(activity_theme_resID)
        val backgroundTar = createDrawable(resID)

        val trans = TransitionDrawable(arrayOf(backgroundCurr, backgroundTar))
        findViewById<View>(R.id.changeTheme_background).background = trans
        trans.startTransition(100)
        activity_theme_resID = resID
    }
}
