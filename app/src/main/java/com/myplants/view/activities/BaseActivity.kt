package com.myplants.view.activities

import android.graphics.Rect
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

/**
 * This class is the base activity for all activities in the app.
 * We use it for common functionality that we want to be available in all activities.
 */
open class BaseActivity : AppCompatActivity() {

    // This function is called when the user taps outside of an EditText, which causes the keyboard to be hidden.
    // https://dev.to/ahmmedrejowan/hide-the-soft-keyboard-and-remove-focus-from-edittext-in-android-ehp
    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val v = currentFocus
            if (v is EditText) {
                val outRect = Rect()
                v.getGlobalVisibleRect(outRect)
                if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    v.clearFocus()
                    val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v.windowToken, 0)
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }
}
