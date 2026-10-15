package hr.vascharlie.lana3

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.content.edit

class TaxiTariffsActivity : Activity() {
    companion object { private const val PREFS = "taxi_tariffs" }
    private data class Fields(val name: EditText, val base: EditText, val includedKm: EditText, val perKm: EditText, val perMinute: EditText, val minimum: EditText)
    private val fields = mutableListOf<Fields>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val scroll = ScrollView(this)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(36, 44, 36, 44)
            setBackgroundColor(Color.rgb(8, 17, 31))
        }
        scroll.addView(root)
        root.addView(TextView(this).apply {
            text = "Taxi · Tarife"
            textSize = 28f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.WHITE)
        })
        root.addView(TextView(this).apply {
            text = "Šest podesivih tarifa + zasebna Nagodba. Promjene ostaju spremljene na uređaju."
            textSize = 14f
            setTextColor(Color.rgb(180,195,210))
            setPadding(0,8,0,20)
        })
        repeat(6) { index -> addTariff(root, index) }
        root.addView(TextView(this).apply {
            text = "NAGODBA"
            textSize = 20f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.rgb(90,180,255))
            setPadding(0,24,0,6)
        })
        root.addView(TextView(this).apply {
            text = "Cijena se dogovara za konkretnu vožnju. Nije vezana uz T1–T6."
            textSize = 14f
            setTextColor(Color.WHITE)
        })
        root.addView(Button(this).apply {
            text = "Spremi tarife"
            isAllCaps = false
            setOnClickListener { saveAll(); text = "Spremljeno ✓" }
        })
        root.addView(Button(this).apply {
            text = "Natrag"
            isAllCaps = false
            setOnClickListener { finish() }
        })
        setContentView(scroll)
    }

    private fun addTariff(root: LinearLayout, index: Int) {
        val n = index + 1
        root.addView(TextView(this).apply {
            text = "T" + n
            textSize = 20f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.rgb(90,180,255))
            setPadding(0,22,0,4)
        })
        val name = field(root, "Naziv / namjena", false)
        val base = field(root, "Početna cijena (€)")
        val included = field(root, "Uključeno km")
        val perKm = field(root, "Cijena nakon uključenih km (€/km)")
        val perMinute = field(root, "Cijena vremena (€/min)")
        val minimum = field(root, "Minimalna cijena (€)")
        fields += Fields(name, base, included, perKm, perMinute, minimum)

        val p = getSharedPreferences(PREFS, MODE_PRIVATE)
        name.setText(p.getString("t" + n + "_name", if (n == 1) "1–4 putnika" else ""))
        base.setText(p.getString("t" + n + "_base", if (n == 1) "5" else ""))
        included.setText(p.getString("t" + n + "_included_km", if (n == 1) "3" else ""))
        perKm.setText(p.getString("t" + n + "_per_km", if (n == 1) "1.25" else ""))
        perMinute.setText(p.getString("t" + n + "_per_minute", ""))
        minimum.setText(p.getString("t" + n + "_minimum", ""))
    }

    private fun field(root: LinearLayout, label: String, numeric: Boolean = true): EditText {
        root.addView(TextView(this).apply {
            text = label
            textSize = 13f
            setTextColor(Color.rgb(200,210,220))
            setPadding(0,8,0,2)
        })
        return EditText(this).also {
            if (numeric) it.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            it.setTextColor(Color.WHITE)
            it.setHintTextColor(Color.rgb(120,135,150))
            root.addView(it)
        }
    }

    private fun saveAll() {
        getSharedPreferences(PREFS, MODE_PRIVATE).edit {
            fields.forEachIndexed { index, f ->
                val n = index + 1
                putString("t" + n + "_name", f.name.text.toString().trim())
                putString("t" + n + "_base", normalize(f.base))
                putString("t" + n + "_included_km", normalize(f.includedKm))
                putString("t" + n + "_per_km", normalize(f.perKm))
                putString("t" + n + "_per_minute", normalize(f.perMinute))
                putString("t" + n + "_minimum", normalize(f.minimum))
            }
        }
    }

    private fun normalize(field: EditText): String = field.text.toString().trim().replace(',', '.')
}
