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
import androidx.core.content.edit
import hr.vascharlie.lana3.core.ride.RideAcceptanceRules
import hr.vascharlie.lana3.core.ride.RideAssessmentResult
import hr.vascharlie.lana3.core.ride.RideCalculationScope
import hr.vascharlie.lana3.core.ride.RideDecisionExplanation
import hr.vascharlie.lana3.core.ride.RideDecisionReason
import hr.vascharlie.lana3.core.ride.RideInvalidOfferReason
import hr.vascharlie.lana3.core.ride.RideInvalidRuleReason
import hr.vascharlie.lana3.core.ride.RideOffer
import hr.vascharlie.lana3.core.ride.RideProfitabilityMetric
import hr.vascharlie.lana3.core.ride.RideRecommendation
import hr.vascharlie.lana3.core.ride.SmartRideAcceptance

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
    private lateinit var emptyReturnKm: EditText
    private lateinit var emptyReturnMinutes: EditText

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
            text = getString(R.string.smart_ride_title)
            textSize = 28f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.WHITE)
        })

        root.addView(TextView(this).apply {
            text = getString(R.string.smart_ride_description)
            textSize = 14f
            setPadding(0, 10, 0, 24)
            setTextColor(Color.rgb(180, 195, 210))
        })

        addSectionTitle(root, getString(R.string.smart_ride_offer_section))
        priceEur = addNumberField(
            root,
            getString(R.string.smart_ride_price_label),
            getString(R.string.smart_ride_price_hint),
        )
        pickupKm = addNumberField(
            root,
            getString(R.string.smart_ride_pickup_km_label),
            getString(R.string.smart_ride_pickup_km_hint),
        )
        tripKm = addNumberField(
            root,
            getString(R.string.smart_ride_trip_km_label),
            getString(R.string.smart_ride_trip_km_hint),
        )
        pickupMinutes = addNumberField(
            root,
            getString(R.string.smart_ride_pickup_minutes_label),
            getString(R.string.smart_ride_pickup_minutes_hint),
        )
        tripMinutes = addNumberField(
            root,
            getString(R.string.smart_ride_trip_minutes_label),
            getString(R.string.smart_ride_trip_minutes_hint),
        )

        addSectionTitle(root, getString(R.string.smart_ride_return_section))
        root.addView(TextView(this).apply {
            text = getString(R.string.smart_ride_return_description)
            textSize = 13f
            setPadding(0, 0, 0, 6)
            setTextColor(Color.rgb(180, 195, 210))
        })
        emptyReturnKm = addNumberField(
            root,
            getString(R.string.smart_ride_return_km_label),
            getString(R.string.smart_ride_return_km_hint),
        )
        emptyReturnMinutes = addNumberField(
            root,
            getString(R.string.smart_ride_return_minutes_label),
            getString(R.string.smart_ride_return_minutes_hint),
        )

        addSectionTitle(root, getString(R.string.smart_ride_thresholds_section))
        considerMinEurPerKm = addNumberField(
            root,
            getString(R.string.smart_ride_consider_km_label),
            getString(R.string.smart_ride_threshold_hint),
        )
        acceptMinEurPerKm = addNumberField(
            root,
            getString(R.string.smart_ride_accept_km_label),
            getString(R.string.smart_ride_threshold_hint),
        )
        considerMinEurPerHour = addNumberField(
            root,
            getString(R.string.smart_ride_consider_hour_label),
            getString(R.string.smart_ride_threshold_hint),
        )
        acceptMinEurPerHour = addNumberField(
            root,
            getString(R.string.smart_ride_accept_hour_label),
            getString(R.string.smart_ride_threshold_hint),
        )

        restoreSavedRules()

        root.addView(Button(this).apply {
            text = getString(R.string.smart_ride_evaluate)
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
            text = getString(R.string.back)
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
            resultText.text = getString(R.string.smart_ride_missing_thresholds)
            return
        }

        saveRules(rules)

        val offer = RideOffer(
            priceEur = priceEur.decimalOrNull(),
            pickupKm = pickupKm.decimalOrNull(),
            tripKm = tripKm.decimalOrNull(),
            pickupMinutes = pickupMinutes.decimalOrNull(),
            tripMinutes = tripMinutes.decimalOrNull(),
            emptyReturnKm = emptyReturnKm.decimalOrNull(),
            emptyReturnMinutes = emptyReturnMinutes.decimalOrNull(),
        )

        resultText.text = when (val result = engine.assess(offer, rules)) {
            is RideAssessmentResult.Assessed -> formatAssessment(result)
            is RideAssessmentResult.InsufficientData ->
                getString(
                    R.string.smart_ride_missing_offer_data,
                    result.missingFields.joinToString { humanFieldName(it) },
                )

            is RideAssessmentResult.InvalidOffer ->
                getString(R.string.smart_ride_invalid_offer_header) +
                    "\n" +
                    result.reasons.joinToString(separator = "\n") {
                        "• " + humanOfferInvalidReason(it)
                    }

            is RideAssessmentResult.InvalidRules ->
                getString(R.string.smart_ride_invalid_rules_header) +
                    "\n" +
                    result.reasons.joinToString(separator = "\n") {
                        "• " + humanRuleInvalidReason(it)
                    }
        }
    }

    private fun formatAssessment(
        result: RideAssessmentResult.Assessed,
    ): String {
        val assessment = result.assessment
        val recommendation = when (assessment.recommendation) {
            RideRecommendation.ACCEPT -> getString(R.string.smart_ride_recommend_accept)
            RideRecommendation.CONSIDER -> getString(R.string.smart_ride_recommend_consider)
            RideRecommendation.SKIP -> getString(R.string.smart_ride_recommend_skip)
        }

        return buildString {
            append(recommendation)
            append("\n")
            append(
                getString(
                    R.string.smart_ride_metrics,
                    assessment.eurPerKm,
                    assessment.eurPerHour,
                )
            )
            append("\n")
            append(
                formatDecisionExplanation(
                    explanation = assessment.explanation,
                    eurPerKm = assessment.eurPerKm,
                    eurPerHour = assessment.eurPerHour,
                )
            )
            append("\n")
            append(
                getString(
                    R.string.smart_ride_total_work,
                    assessment.totalKilometers,
                    assessment.totalMinutes,
                )
            )
            append("\n")
            append(
                when (assessment.calculationScope) {
                    RideCalculationScope.PICKUP_AND_TRIP ->
                        getString(R.string.smart_ride_scope_without_return)

                    RideCalculationScope.PICKUP_TRIP_AND_EMPTY_RETURN ->
                        getString(
                            R.string.smart_ride_scope_with_return,
                            assessment.emptyReturnKilometers,
                            assessment.emptyReturnMinutes,
                        )
                }
            )
            append("\n\n")
            append(getString(R.string.smart_ride_calculation_note))
        }
    }

    private fun formatDecisionExplanation(
        explanation: RideDecisionExplanation,
        eurPerKm: Double,
        eurPerHour: Double,
    ): String = when (explanation.reason) {
        RideDecisionReason.MEETS_ACCEPT_THRESHOLDS ->
            getString(
                R.string.smart_ride_reason_accept,
                explanation.thresholdEurPerKm,
                explanation.thresholdEurPerHour,
            )

        RideDecisionReason.MEETS_CONSIDER_THRESHOLDS ->
            getString(
                R.string.smart_ride_reason_consider,
                explanation.thresholdEurPerKm,
                explanation.thresholdEurPerHour,
            )

        RideDecisionReason.BELOW_CONSIDER_THRESHOLDS -> {
            val failed = explanation.failedMetrics.toSet()
            when {
                failed.containsAll(
                    setOf(
                        RideProfitabilityMetric.EUR_PER_KM,
                        RideProfitabilityMetric.EUR_PER_HOUR,
                    )
                ) ->
                    getString(
                        R.string.smart_ride_reason_skip_both,
                        eurPerKm,
                        explanation.thresholdEurPerKm,
                        eurPerHour,
                        explanation.thresholdEurPerHour,
                    )

                RideProfitabilityMetric.EUR_PER_KM in failed ->
                    getString(
                        R.string.smart_ride_reason_skip_km,
                        eurPerKm,
                        explanation.thresholdEurPerKm,
                    )

                RideProfitabilityMetric.EUR_PER_HOUR in failed ->
                    getString(
                        R.string.smart_ride_reason_skip_hour,
                        eurPerHour,
                        explanation.thresholdEurPerHour,
                    )

                else -> getString(R.string.smart_ride_reason_skip_generic)
            }
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
            field.error = getString(R.string.required)
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
        getSharedPreferences(PREFS, MODE_PRIVATE).edit {
            putString("accept_km", rules.acceptMinEurPerKm.toString())
            putString("consider_km", rules.considerMinEurPerKm.toString())
            putString("accept_hour", rules.acceptMinEurPerHour.toString())
            putString("consider_hour", rules.considerMinEurPerHour.toString())
        }
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
        "priceEur" -> getString(R.string.smart_ride_field_price)
        "pickupKm" -> getString(R.string.smart_ride_field_pickup_km)
        "tripKm" -> getString(R.string.smart_ride_field_trip_km)
        "pickupMinutes" -> getString(R.string.smart_ride_field_pickup_minutes)
        "tripMinutes" -> getString(R.string.smart_ride_field_trip_minutes)
        "emptyReturnKm" -> getString(R.string.smart_ride_field_return_km)
        "emptyReturnMinutes" -> getString(R.string.smart_ride_field_return_minutes)
        else -> id
    }

    private fun humanOfferInvalidReason(
        reason: RideInvalidOfferReason,
    ): String = when (reason) {
        RideInvalidOfferReason.NON_FINITE_METRIC ->
            getString(R.string.smart_ride_invalid_offer_non_finite)

        RideInvalidOfferReason.NEGATIVE_PRICE ->
            getString(R.string.smart_ride_invalid_offer_negative_price)

        RideInvalidOfferReason.NEGATIVE_DISTANCE ->
            getString(R.string.smart_ride_invalid_offer_negative_distance)

        RideInvalidOfferReason.NEGATIVE_TIME ->
            getString(R.string.smart_ride_invalid_offer_negative_time)

        RideInvalidOfferReason.NON_POSITIVE_TOTAL_DISTANCE ->
            getString(R.string.smart_ride_invalid_offer_total_distance)

        RideInvalidOfferReason.NON_POSITIVE_TOTAL_TIME ->
            getString(R.string.smart_ride_invalid_offer_total_time)
    }

    private fun humanRuleInvalidReason(
        reason: RideInvalidRuleReason,
    ): String = when (reason) {
        RideInvalidRuleReason.NON_FINITE_THRESHOLD ->
            getString(R.string.smart_ride_invalid_rule_non_finite)

        RideInvalidRuleReason.NEGATIVE_THRESHOLD ->
            getString(R.string.smart_ride_invalid_rule_negative)

        RideInvalidRuleReason.ACCEPT_KM_BELOW_CONSIDER ->
            getString(R.string.smart_ride_invalid_rule_accept_km)

        RideInvalidRuleReason.ACCEPT_HOUR_BELOW_CONSIDER ->
            getString(R.string.smart_ride_invalid_rule_accept_hour)
    }
}
