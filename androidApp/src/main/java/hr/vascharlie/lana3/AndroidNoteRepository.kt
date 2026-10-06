package hr.vascharlie.lana3

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import hr.vascharlie.lana3.core.notes.LanaNote
import hr.vascharlie.lana3.core.notes.NoteRepository

class AndroidNoteRepository(
    context: Context,
) : SQLiteOpenHelper(
    context.applicationContext,
    DATABASE_NAME,
    null,
    DATABASE_VERSION,
), NoteRepository {
    companion object {
        private const val DATABASE_NAME = "lana_notes.db"
        private const val DATABASE_VERSION = 1

        private const val TABLE_NOTES = "notes"
        private const val COL_ID = "id"
        private const val COL_TEXT = "text"
        private const val COL_CREATED_AT = "created_at"
        private const val COL_UPDATED_AT = "updated_at"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE $TABLE_NOTES (
                $COL_ID TEXT PRIMARY KEY NOT NULL,
                $COL_TEXT TEXT NOT NULL,
                $COL_CREATED_AT INTEGER NOT NULL,
                $COL_UPDATED_AT INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE INDEX idx_notes_updated_at
            ON $TABLE_NOTES($COL_UPDATED_AT DESC)
            """.trimIndent()
        )
    }

    override fun onUpgrade(
        db: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int,
    ) {
        if (oldVersion == newVersion) return

        throw IllegalStateException(
            "No destructive notes database migration is defined from " +
                oldVersion + " to " + newVersion + "."
        )
    }

    override fun insert(note: LanaNote): LanaNote {
        writableDatabase.insertOrThrow(
            TABLE_NOTES,
            null,
            note.toContentValues(),
        )
        return note
    }

    override fun update(note: LanaNote): LanaNote? {
        val rows = writableDatabase.update(
            TABLE_NOTES,
            note.toContentValues(includeId = false),
            "$COL_ID = ?",
            arrayOf(note.id),
        )

        return note.takeIf { rows == 1 }
    }

    override fun delete(id: String): Boolean =
        writableDatabase.delete(
            TABLE_NOTES,
            "$COL_ID = ?",
            arrayOf(id),
        ) == 1

    override fun findById(id: String): LanaNote? =
        readableDatabase.query(
            TABLE_NOTES,
            columns,
            "$COL_ID = ?",
            arrayOf(id),
            null,
            null,
            null,
            "1",
        ).use { cursor ->
            if (!cursor.moveToFirst()) {
                null
            } else {
                cursor.toNote()
            }
        }

    override fun listAll(): List<LanaNote> =
        readableDatabase.query(
            TABLE_NOTES,
            columns,
            null,
            null,
            null,
            null,
            "$COL_UPDATED_AT DESC, $COL_CREATED_AT DESC, $COL_ID ASC",
        ).use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    add(cursor.toNote())
                }
            }
        }

    private val columns: Array<String>
        get() = arrayOf(
            COL_ID,
            COL_TEXT,
            COL_CREATED_AT,
            COL_UPDATED_AT,
        )

    private fun LanaNote.toContentValues(
        includeId: Boolean = true,
    ): ContentValues =
        ContentValues().apply {
            if (includeId) put(COL_ID, id)
            put(COL_TEXT, text)
            put(COL_CREATED_AT, createdAtEpochMillis)
            put(COL_UPDATED_AT, updatedAtEpochMillis)
        }

    private fun android.database.Cursor.toNote(): LanaNote =
        LanaNote(
            id = getString(getColumnIndexOrThrow(COL_ID)),
            text = getString(getColumnIndexOrThrow(COL_TEXT)),
            createdAtEpochMillis =
                getLong(getColumnIndexOrThrow(COL_CREATED_AT)),
            updatedAtEpochMillis =
                getLong(getColumnIndexOrThrow(COL_UPDATED_AT)),
        )
}
