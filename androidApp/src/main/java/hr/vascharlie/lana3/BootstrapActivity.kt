package hr.vascharlie.lana3

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class BootstrapActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val status = TextView(this).apply {
            text = "LANA 3 bootstrap je pokrenut.\n\nAko vidiš ovaj ekran, Android može pokrenuti aplikaciju. Sljedeći test pokreće glavni ekran u odvojenom procesu."
            textSize = 18f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(32, 48, 32, 32)
        }

        val launchMain = Button(this).apply {
            text = "Pokreni glavni ekran"
            isAllCaps = false
            setOnClickListener {
                runCatching {
                    startActivity(
                        Intent().setClassName(
                            packageName,
                            "hr.vascharlie.lana3.MainActivity"
                        )
                    )
                }.onFailure { error ->
                    status.text = buildString {
                        append("START MAIN FAILED\n\n")
                        append(error::class.java.name)
                        append(": ")
                        append(error.message ?: "(no message)")
                    }
                }
            }
        }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(32, 64, 32, 64)
            setBackgroundColor(Color.BLACK)
            addView(
                status,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    0,
                    1f
                )
            )
            addView(
                launchMain,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            )
        }

        setContentView(root)
    }
}
