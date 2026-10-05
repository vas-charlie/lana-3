package hr.vascharlie.lana3

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(40, 56, 40, 40)
            setBackgroundColor(Color.rgb(8, 17, 31))
        }
        root.addView(TextView(this).apply {
            text = "LANA 3"
            textSize = 30f
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
        })
        root.addView(TextView(this).apply {
            text = "Developer Preview 0.1"
            textSize = 14f
            setTextColor(Color.rgb(90, 180, 255))
        })
        val avatar = TextView(this).apply {
            text = "LANA"
            textSize = 54f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.rgb(15, 39, 67))
            contentDescription = "Lana avatar placeholder"
        }
        root.addView(avatar, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f
        ).apply { setMargins(0, 36, 0, 28) })
        val status = TextView(this).apply {
            text = "Tu sam, Charlie."
            textSize = 20f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
        }
        root.addView(status)
        root.addView(TextView(this).apply {
            text = "Prvo gradimo Lanu. Zavrsene sposobnosti dodajemo jednu po jednu."
            textSize = 15f
            gravity = Gravity.CENTER
            setPadding(0, 16, 0, 24)
            setTextColor(Color.LTGRAY)
        })
        root.addView(Button(this).apply {
            text = "Smart Ride Acceptance - TESTNO"
            isAllCaps = false
            setOnClickListener {
                status.text = "Smart Ride Acceptance je u jezgri. UI povezivanje slijedi."
            }
        })
        setContentView(root)
    }
}
