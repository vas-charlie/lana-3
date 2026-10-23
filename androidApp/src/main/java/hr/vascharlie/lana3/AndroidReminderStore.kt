package hr.vascharlie.lana3

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import hr.vascharlie.lana3.core.reminders.LanaReminder
import hr.vascharlie.lana3.core.reminders.ReminderRepository

/** App-private, offline-only reminder storage. Does not touch the notes database. */
class AndroidReminderStore(context: Context) :
    SQLiteOpenHelper(context.applicationContext, "lana_reminders.db", null, 1),
    ReminderRepository {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE reminders (
                id TEXT PRIMARY KEY NOT NULL,
                title TEXT NOT NULL,
                category TEXT NOT NULL,
                due_at INTEGER NOT NULL,
                notify_at INTEGER,
                created_at INTEGER NOT NULL,
                completed INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX idx_reminders_due ON reminders(completed, due_at)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion != newVersion) error("No destructive reminder migration permitted.")
    }

    override fun insert(reminder: LanaReminder) {
        writableDatabase.insertOrThrow("reminders", null, reminder.toValues())
    }

    override fun findById(id: String): LanaReminder? =
        readableDatabase.query(
            "reminders", null, "id = ?", arrayOf(id), null, null, null, "1"
        ).use { c -> if (c.moveToFirst()) c.asReminder() else null }

    override fun listAll(): List<LanaReminder> =
        readableDatabase.query(
            "reminders", null, null, null, null, null, "due_at ASC, id ASC"
        ).use { c -> buildList { while (c.moveToNext()) add(c.asReminder()) } }

    override fun update(reminder: LanaReminder): Boolean =
        writableDatabase.update(
            "reminders", reminder.toValues(includeId = false), "id = ?", arrayOf(reminder.id)
        ) == 1

    override fun delete(id: String): Boolean =
        writableDatabase.delete("reminders", "id = ?", arrayOf(id)) == 1

    private fun LanaReminder.toValues(includeId: Boolean = true) = ContentValues().apply {
        if (includeId) put("id", id)
        put("title", title)
        put("category", category)
        put("due_at", dueAtEpochMillis)
        if (notifyAtEpochMillis == null) putNull("notify_at") else put("notify_at", notifyAtEpochMillis)
        put("created_at", createdAtEpochMillis)
        put("completed", if (completed) 1 else 0)
    }

    private fun Cursor.asReminder() = LanaReminder(
        id = getString(getColumnIndexOrThrow("id")),
        title = getString(getColumnIndexOrThrow("title")),
        category = getString(getColumnIndexOrThrow("category")),
        dueAtEpochMillis = getLong(getColumnIndexOrThrow("due_at")),
        notifyAtEpochMillis = getColumnIndexOrThrow("notify_at").let { i ->
            if (isNull(i)) null else getLong(i)
        },
        createdAtEpochMillis = getLong(getColumnIndexOrThrow("created_at")),
        completed = getInt(getColumnIndexOrThrow("completed")) != 0,
    )
}
