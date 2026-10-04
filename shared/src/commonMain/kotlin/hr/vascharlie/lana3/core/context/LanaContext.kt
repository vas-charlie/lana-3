package hr.vascharlie.lana3.core.context

import hr.vascharlie.lana3.core.model.LanguageContext
import hr.vascharlie.lana3.core.model.PlatformCapability

data class LanaContext(
    val language: LanguageContext,
    val capabilities: List<PlatformCapability> = emptyList(),
)
