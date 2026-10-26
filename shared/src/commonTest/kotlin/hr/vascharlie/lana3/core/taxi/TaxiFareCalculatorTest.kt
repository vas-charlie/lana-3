package hr.vascharlie.lana3.core.taxi

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class TaxiFareCalculatorTest {
    private val calculator = TaxiFareCalculator()

    @Test
    fun t1IsFiveEurosFromZeroThroughThreeKm() {
        listOf(0.0, 1.0, 2.99, 3.0).forEach { km ->
            val result = assertIs<TaxiFareResult.Calculated>(
                calculator.calculate(DefaultTaxiTariffs.T1, km)
            )
            assertEquals(5.0, result.amountEur, 0.0001)
        }
    }

    @Test
    fun t1FiveKmIsSevenFifty() {
        val result = assertIs<TaxiFareResult.Calculated>(
            calculator.calculate(DefaultTaxiTariffs.T1, 5.0)
        )
        assertEquals(7.5, result.amountEur, 0.0001)
    }

    @Test
    fun t1TenKmIsThirteenSeventyFive() {
        val result = assertIs<TaxiFareResult.Calculated>(
            calculator.calculate(DefaultTaxiTariffs.T1, 10.0)
        )
        assertEquals(13.75, result.amountEur, 0.0001)
    }

    @Test
    fun t1TwentyKmIsTwentySixTwentyFive() {
        val result = assertIs<TaxiFareResult.Calculated>(
            calculator.calculate(DefaultTaxiTariffs.T1, 20.0)
        )
        assertEquals(26.25, result.amountEur, 0.0001)
    }

    @Test
    fun negativeDistanceIsRejected() {
        val result = assertIs<TaxiFareResult.Invalid>(
            calculator.calculate(DefaultTaxiTariffs.T1, -1.0)
        )
        assertTrue("invalid_distance" in result.reasons)
    }

    @Test
    fun negotiatedPriceMustBeExplicit() {
        val result = assertIs<TaxiFareResult.Calculated>(
            calculator.negotiated(18.5)
        )
        assertEquals(18.5, result.amountEur, 0.0001)
    }

    @Test
    fun invalidNegotiatedPriceIsRejected() {
        val result = assertIs<TaxiFareResult.Invalid>(
            calculator.negotiated(-1.0)
        )
        assertEquals(listOf("invalid_negotiated_price"), result.reasons)
    }
}
