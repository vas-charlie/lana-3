package hr.vascharlie.lana3.application

import hr.vascharlie.lana3.core.authorization.ActionAuthorizationResult
import hr.vascharlie.lana3.core.authorization.ActionAuthorizer
import hr.vascharlie.lana3.core.authorization.AuthorizationLevel
import hr.vascharlie.lana3.core.authorization.AuthorizedAction
import hr.vascharlie.lana3.core.authorization.AutomationMode
import hr.vascharlie.lana3.core.authorization.ExecutionModeGate
import hr.vascharlie.lana3.core.authorization.ExecutionModeGateResult
import hr.vascharlie.lana3.core.context.LanaContext
import hr.vascharlie.lana3.core.decision.Decision
import hr.vascharlie.lana3.core.decision.DecisionEngine
import hr.vascharlie.lana3.core.diagnostics.DiagnosticEvent
import hr.vascharlie.lana3.core.diagnostics.DiagnosticLevel
import hr.vascharlie.lana3.core.diagnostics.DiagnosticSink
import hr.vascharlie.lana3.core.intent.Intent
import hr.vascharlie.lana3.core.notes.LanaNote
import hr.vascharlie.lana3.core.notes.NoteResult
import hr.vascharlie.lana3.core.notes.NoteService
import hr.vascharlie.lana3.core.task.LanaTask
import hr.vascharlie.lana3.core.task.TaskPriority
import hr.vascharlie.lana3.core.task.TaskState

sealed interface SaveNoteFlowResult {
    data class Completed(
        val note: LanaNote,
        val task: LanaTask,
        val explanation: String,
    ) : SaveNoteFlowResult

    data class AwaitingConfirmation(
        val reason: String,
        val explanation: String,
    ) : SaveNoteFlowResult

    data class Blocked(
        val reason: String,
    ) : SaveNoteFlowResult

    data class Rejected(
        val reason: String,
    ) : SaveNoteFlowResult

    data class Failed(
        val task: LanaTask,
        val reason: String,
    ) : SaveNoteFlowResult
}

/**
 * First complete local-action orchestration slice.
 *
 * Brain:
 * normalized intent -> DecisionEngine -> ActionAuthorizer -> automation-mode gate.
 *
 * Hands:
 * AuthorizedAction -> SaveNoteExecutor -> NoteService -> NoteRepository.
 *
 * Note text is deliberately excluded from diagnostics.
 */
class SaveNoteFlow(
    private val noteService: NoteService,
    private val diagnosticSink: DiagnosticSink,
    private val taskIdFactory: () -> String,
    private val decisionEngine: DecisionEngine = DecisionEngine(),
    private val actionAuthorizer: ActionAuthorizer = ActionAuthorizer(),
    private val executionModeGate: ExecutionModeGate = ExecutionModeGate(),
) {
    private val executor = SaveNoteExecutor(noteService)

    fun run(
        text: String,
        context: LanaContext,
        automationMode: AutomationMode,
        userConfirmed: Boolean = false,
    ): SaveNoteFlowResult {
        val intent = Intent.SaveNote(text)
        val decision = decisionEngine.evaluate(intent, context)

        if (decision is Decision.CannotDecide) {
            record(
                code = "save_note_rejected",
                level = DiagnosticLevel.WARNING,
                message = "Save note request was rejected before execution.",
            )
            return SaveNoteFlowResult.Rejected(decision.reason)
        }

        val proposed = decision as Decision.Proposed
        val authorization = actionAuthorizer.authorize(intent, proposed)

        if (authorization is ActionAuthorizationResult.Rejected) {
            record(
                code = "save_note_authorization_rejected",
                level = DiagnosticLevel.WARNING,
                message = "Save note request did not reach executable authorization.",
            )
            return SaveNoteFlowResult.Blocked(authorization.reason)
        }

        val authorizedAction =
            (authorization as ActionAuthorizationResult.Authorized).action

        when (
            val modeResult = executionModeGate.evaluate(
                mode = automationMode,
                userConfirmed = userConfirmed,
            )
        ) {
            ExecutionModeGateResult.Allowed -> Unit

            is ExecutionModeGateResult.Blocked -> {
                record(
                    code = "save_note_mode_blocked",
                    level = DiagnosticLevel.INFO,
                    message = "Save note execution was blocked by automation mode.",
                    attributes = mapOf(
                        "requires_confirmation" to
                            modeResult.requiresConfirmation.toString(),
                    ),
                )

                return if (modeResult.requiresConfirmation) {
                    SaveNoteFlowResult.AwaitingConfirmation(
                        reason = modeResult.reason,
                        explanation = proposed.explanation,
                    )
                } else {
                    SaveNoteFlowResult.Blocked(modeResult.reason)
                }
            }
        }

        val activeTask = LanaTask(
            id = taskIdFactory(),
            kind = "save-note",
            priority = TaskPriority.NORMAL,
            state = TaskState.ACTIVE,
        )

        record(
            code = "save_note_execution_started",
            level = DiagnosticLevel.INFO,
            message = "Authorized save note execution started.",
            attributes = mapOf("task_id" to activeTask.id),
        )

        return try {
            when (val noteResult = executor.execute(authorizedAction)) {
                is NoteResult.Saved -> {
                    val completed = activeTask.copy(state = TaskState.COMPLETED)

                    record(
                        code = "save_note_completed",
                        level = DiagnosticLevel.INFO,
                        message = "Local note was saved.",
                        attributes = mapOf(
                            "task_id" to completed.id,
                            "note_id" to noteResult.note.id,
                        ),
                    )

                    SaveNoteFlowResult.Completed(
                        note = noteResult.note,
                        task = completed,
                        explanation = proposed.explanation,
                    )
                }

                is NoteResult.Invalid -> fail(
                    activeTask,
                    "Note service rejected the request: " + noteResult.reason,
                )

                is NoteResult.NotFound,
                is NoteResult.Updated,
                is NoteResult.Deleted,
                is NoteResult.Found -> fail(
                    activeTask,
                    "Unexpected note result during save.",
                )
            }
        } catch (_: Throwable) {
            fail(
                activeTask,
                "Local note storage failed.",
            )
        }
    }

    private fun fail(
        activeTask: LanaTask,
        reason: String,
    ): SaveNoteFlowResult.Failed {
        val failed = activeTask.copy(state = TaskState.FAILED)

        record(
            code = "save_note_failed",
            level = DiagnosticLevel.ERROR,
            message = "Save note execution failed.",
            attributes = mapOf("task_id" to failed.id),
        )

        return SaveNoteFlowResult.Failed(
            task = failed,
            reason = reason,
        )
    }

    private fun record(
        code: String,
        level: DiagnosticLevel,
        message: String,
        attributes: Map<String, String> = emptyMap(),
    ) {
        diagnosticSink.record(
            DiagnosticEvent(
                code = code,
                level = level,
                component = "SaveNoteFlow",
                message = message,
                attributes = attributes,
            )
        )
    }
}

class SaveNoteExecutor(
    private val noteService: NoteService,
) {
    fun execute(action: AuthorizedAction): NoteResult {
        if (action.authorization != AuthorizationLevel.EXECUTE) {
            return NoteResult.Invalid("Save note action is not authorized for execution.")
        }

        val saveNote = action.intent as? Intent.SaveNote
            ?: return NoteResult.Invalid("Authorized action is not a save-note intent.")

        return noteService.save(saveNote.text)
    }
}
