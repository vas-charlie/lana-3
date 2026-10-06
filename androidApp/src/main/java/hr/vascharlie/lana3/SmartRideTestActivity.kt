package hr.vascharlie.lana3

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import hr.vascharlie.lana3.core.ride.RideAcceptanceRules
import hr.vascharlie.lana3.core.ride.RideAssessmentResult
import hr.vascharlie.lana3.core.ride.RideOffer
import hr.vascharlie.lana3.core.ride.RideRecommendation
import hr.vascharlie.lana3.core.ride.SmartRideAcceptance
import java.util.Locale

class SmartRideTestActivity : Activity() {
    companion object {
        private const val PREFS = "smart_ride_test"
    }

    private val engine = SmartRideAcceptance()

    private lateinit var priceEur: EditText
    private lateinit var pickupKm: EditText
    private lateinit var tripKm: EditText
    private lateinit var pickupMinutes: EditText
    private lateinit var tripMinutes: EditText

    private lateinit var acceptMinEurPerKm: EditText
    private lateinit var considerMinEurPerKm: EditText
    private lateinit var acceptMinEurPerHour: EditText
    private lateinit var considerMinEurPerHour: EditText

    private lateinit var resultText: TextView

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
            text = "Smart Ride Acceptance"
            textSize = 28f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.WHITE)
        })

        root.addView(TextView(this).apply {
            text =
                "Testni ekran. LANA ne prihvaća vožnju automatski. " +
                    "Unesi stvarne podatke ponude i svoje pragove."
            textSize = 14f
            setPadding(0, 10, 0, 24)
            setTextColor(Color.rgb(180, 195, 210))
        })

        addSectionTitle(root, "Ponuda")
        priceEur = addNumberField(root, "Cijena vožnje (€)", "npr. 18,50")
        pickupKm = addNumberField(root, "Dolazak do putnika (km)", "npr. 2,4")
        tripKm = addNumberField(root, "Vožnja s putnikom (km)", "npr. 8,7")
        pickupMinutes = addNumberField(root, "Dolazak do putnika (min)", "npr. 6")
        tripMinutes = addNumberField(root, "Vožnja s putnikom (min)", "npr. 18")

        addSectionTitle(root, "Tvoji pragovi")
        considerMinEurPerKm = addNumberField(
            root,
            "RAZMOTRI od najmanje €/km",
            "upiši svoj prag",
        )
        acceptMinEurPerKm = addNumberField(
            root,
            "PRIHVATI od najmanje €/km",
            "upiši svoj prag",
        )
        considerMinEurPerHour = addNumberField(
            root,
            "RAZMOTRI od najmanje €/h",
            "upiši svoj prag",
        )
        acceptMinEurPerHour = addNumberField(
            root,
            "PRIHVATI od najmanje €/h",
            "upiši svoj prag",
        )

        restoreSavedRules()

        root.addView(Button(this).apply {
            text = "Procijeni vožnju"
            isAllCaps = false
            setOnClickListener { evaluateRide() }
        })

        resultText = TextView(this).apply {
            textSize = 18f
            gravity = Gravity.START
            setTextColor(Color.WHITE)
            setPadding(0, 24, 0, 20)
        }
        root.addView(resultText)

        root.addView(Button(this).apply {
            text = "Natrag"
            isAllCaps = false
            setOnClickListener { finish() }
        })

        setContentView(scroll)
    }

    private fun addSectionTitle(root: LinearLayout, title: String) {
        root.addView(TextView(this).apply {
            text = title
            textSize = 18f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.rgb(90, 180, 255))
            setPadding(0, 16, 0, 8)
        })
    }

    private fun addNumberField(
        root: LinearLayout,
        label: String,
        hintText: String,
    ): EditText {
        root.addView(TextView(this).apply {
            text = label
            textSize = 13f
            setTextColor(Color.rgb(200, 210, 220))
            setPadding(0, 10, 0, 4)
        })

        return EditText(this).also { field ->
            field.hint = hintText
            field.inputType =
                InputType.TYPE_CLASS_NUMBER or
                    InputType.TYPE_NUMBER_FLAG_DECIMAL
            field.setTextColor(Color.WHITE)
            field.setHintTextColor(Color.rgb(120, 135, 150))
            root.addView(
                field,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                )
            )
        }
    }

    private fun evaluateRide() {
        clearRuleErrors()

        val rules = readRulesOrNull() ?: run {
            resultText.text = "Nedostaju pragovi. LANA ih neće izmišljati."
            return
        }

        saveRules(rules)

        val offer = RideOffer(
            priceEur = priceEur.decimalOrNull(),
            pickupKm = pickupKm.decimalOrNull(),
            tripKm = tripKm.decimalOrNull(),
            pickupMinutes = pickupMinutes.decimalOrNull(),
            tripMinutes = tripMinutes.decimalOrNull(),
        )

        resultText.text = when (val result = engine.assess(offer, rules)) {
            is RideAssessmentResult.Assessed -> formatAssessment(result)
            is RideAssessmentResult.InsufficientData ->
                "Nedostaju podaci ponude: " +
                    result.missingFields.joinToString { humanFieldName(it) }

            is RideAssessmentResult.InvalidOffer ->
                "Neispravni podaci ponude:\n" +
                    result.reasons.joinToString(separator = "\n") { "• $it" }

            is RideAssessmentResult.InvalidRules ->
                "Neispravni pragovi:\n" +
                    result.reasons.joinToString(separator = "\n") { "• $it" }
        }
    }

    private fun formatAssessment(
        result: RideAssessmentResult.Assessed,
    ): String {
        val assessment = result.assessment
        val recommendation = when (assessment.recommendation) {
            RideRecommendation.ACCEPT -> "PRIHVATI"
            RideRecommendation.CONSIDER -> "RAZMOTRI"
            RideRecommendation.SKIP -> "PRESKOČI"
        }

        return buildString {
            append(recommendation)
            append("\n")
            append(
                String.format(
                    Locale.getDefault(),
                    "%.2f €/km • %.2f €/h",
                    assessment.eurPerKm,
                    assessment.eurPerHour,
                )
            )
            append("\n\n")
            append("Račun uključuje dolazak do putnika i samu vožnju.")
        }
    }

    private fun readRulesOrNull(): RideAcceptanceRules? {
        val considerKm = requireRule(considerMinEurPerKm)
        val acceptKm = requireRule(acceptMinEurPerKm)
        val considerHour = requireRule(considerMinEurPerHour)
        val acceptHour = requireRule(acceptMinEurPerHour)

        if (
            considerKm == null ||
            acceptKm == null ||
            considerHour == null ||
            acceptHour == null
        ) {
            return null
        }

        return RideAcceptanceRules(
            acceptMinEurPerKm = acceptKm,
            considerMinEurPerKm = considerKm,
            acceptMinEurPerHour = acceptHour,
            considerMinEurPerHour = considerHour,
        )
    }

    private fun requireRule(field: EditText): Double? {
        val value = field.decimalOrNull()
        if (value == null) {
            field.error = "Obavezno"
        }
        return value
    }

    private fun clearRuleErrors() {
        listOf(
            acceptMinEurPerKm,
            considerMinEurPerKm,
            acceptMinEurPerHour,
            considerMinEurPerHour,
        ).forEach { it.error = null }
    }

    private fun saveRules(rules: RideAcceptanceRules) {
        getSharedPreferences(PREFS, MODE_PRIVATE)
            .edit()
            .putString("accept_km", rules.acceptMinEurPerKm.toString())
            .putString("consider_km", rules.considerMinEurPerKm.toString())
            .putString("accept_hour", rules.acceptMinEurPerHour.toString())
            .putString("consider_hour", rules.considerMinEurPerHour.toString())
            .apply()
    }

    private fun restoreSavedRules() {
        val prefs = getSharedPreferences(PREFS, MODE_PRIVATE)

        considerMinEurPerKm.setText(prefs.getString("consider_km", ""))
        acceptMinEurPerKm.setText(prefs.getString("accept_km", ""))
        considerMinEurPerHour.setText(prefs.getString("consider_hour", ""))
        acceptMinEurPerHour.setText(prefs.getString("accept_hour", ""))
    }

    private fun EditText.decimalOrNull(): Double? =
        text
            ?.toString()
            ?.trim()
            ?.replace(',', '.')
            ?.takeIf { it.isNotEmpty() }
            ?.toDoubleOrNull()

    private fun humanFieldName(id: String): String = when (id) {
        "priceEur" -> "cijena"
        "pickupKm" -> "km do putnika"
        "tripKm" -> "km vožnje"
        "pickupMinutes" -> "minute do putnika"
        "tripMinutes" -> "minute vožnje"
        else -> id
    }
}
