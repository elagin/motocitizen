@file:JvmName("Utils")

package motocitizen.utils

import android.app.Activity
import android.content.Context
import android.os.Build
import android.util.DisplayMetrics
import android.view.View
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import java.util.regex.Pattern

fun getPhonesFromText(string: String): List<String> {
    val out = ArrayList<String>()
    val matcher = Pattern.compile("[7|8][ (-]?[\\d]{3}[ )-]?[\\d]{3}[ -]?[\\d]{2}[ -]?[\\d]{2}[\\D]").matcher(string + ".")
    while (matcher.find()) {
        out.add("+7" + matcher.group().replace("[^0-9]".toRegex(), "").substring(1))
    }
    return out
}

fun newId(): Int {
    if (Build.VERSION.SDK_INT > 16) {
        return View.generateViewId()
    } else {
        val sNextGeneratedId = AtomicInteger(1)
        while (true) {
            val result = sNextGeneratedId.get()
            var newValue = result + 1
            if (newValue > 0x00FFFFFF)
                newValue = 1
            if (sNextGeneratedId.compareAndSet(result, newValue)) {
                return result
            }
        }
    }
}

fun getWidth(context: Context): Int {
    val displaymetrics = DisplayMetrics()
    (context as Activity).windowManager.defaultDisplay.getMetrics(displaymetrics)
    return displaymetrics.widthPixels
}