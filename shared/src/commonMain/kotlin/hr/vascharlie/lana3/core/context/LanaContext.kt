package hr.vascharlie.lana3.core.context

import hr.vascharlie.lana3.core.location.LocationSnapshot
import hr.vascharlie.lana3.core.model.LanguageContext
import hr.vascharlie.lana3.core.model.PlatformCapability

data class LanaContext(
    val language: LanguageContext,
    val capabilities: List<PlatformCapability> = emptyList(),
    /**
     * Current assessed location state. Null means location has not been observed for this
     * context, not that Lana may infer or guess a position.
     */
    val location: LocationSnapshot? = null,
)
