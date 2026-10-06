package hr.vascharlie.lana3.core.diagnostics

interface DiagnosticSink {
    fun record(event: DiagnosticEvent)
}

object NoOpDiagnosticSink : DiagnosticSink {
    override fun record(event: DiagnosticEvent) = Unit
}
