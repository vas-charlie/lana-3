package hr.vascharlie.lana3

import android.app.Activity
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.WindowInsets
import android.widget.*
import android.content.res.ColorStateList

/** Read-only dashboard over the existing task database; no schema changes. */
class TaskDashboardActivity : Activity() {
    private val projects = arrayOf("Općenito", "Taksi i klijenti", "BUSINESS OS", "MASTER Excel", "Marketing i web", "LANA 3", "Platforma za vozače", "Online zarada", "Strip i brend", "Administracija i financije")
    private val statuses = arrayOf("Otvoreno", "Čeka se", "Dovršeno")
    private lateinit var content: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.rgb(8, 17, 31))
        }
        root.addView(TextView(this).apply {
            text = "LANA 3 · Statistika"
            textSize = 26f
            setTextColor(Color.WHITE)
        })
        root.addView(TextView(this).apply {
            text = "Pregled zadataka po statusima i projektima · lokalni podaci"
            setTextColor(Color.LTGRAY)
        })
        val scroll = ScrollView(this).apply { isFillViewport = false }
        content = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        scroll.addView(content)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        root.addView(Button(this).apply { text = "Natrag"; setOnClickListener { finish() } })
        root.setOnApplyWindowInsetsListener { view, insets ->
            val top = if (Build.VERSION.SDK_INT >= 30) insets.getInsets(WindowInsets.Type.statusBars()).top else insets.systemWindowInsetTop
            val bottom = if (Build.VERSION.SDK_INT >= 30) insets.getInsets(WindowInsets.Type.navigationBars()).bottom else insets.systemWindowInsetBottom
            view.setPadding(24, top + 24, 24, bottom + 16)
            insets
        }
        setContentView(root)
    }

    override fun onResume() {
        super.onResume()
        render()
    }

    private fun line(value: String, size: Float = 16f, color: Int = Color.WHITE) {
        content.addView(TextView(this).apply {
            text = value
            textSize = size
            setTextColor(color)
            setPadding(2, 10, 2, 10)
        })
    }

    private fun bar(label: String, value: Int, maxValue: Int, color: Int) {
        line("$label: $value", 15f)
        content.addView(ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
            max = maxValue.coerceAtLeast(1)
            progress = value
            progressTintList = ColorStateList.valueOf(color)
            progressBackgroundTintList = ColorStateList.valueOf(Color.DKGRAY)
        }, LinearLayout.LayoutParams(-1, 20).apply { bottomMargin = 14 })
    }

    private fun render() {
        content.removeAllViews()
        val counts = mutableMapOf<String, MutableMap<String, Int>>()
        var total = 0
        val totals = statuses.associateWith { 0 }.toMutableMap()
        val helper = object : SQLiteOpenHelper(this, "lana_manual_tasks.db", null, 1) {
            override fun onCreate(db: SQLiteDatabase) {
                db.execSQL("CREATE TABLE tasks (id TEXT PRIMARY KEY, title TEXT NOT NULL, project TEXT NOT NULL, state TEXT NOT NULL)")
            }
            override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
                throw IllegalStateException("Explicit migration required")
            }
        }
        helper.use { dbHelper ->
            dbHelper.readableDatabase.query("tasks", arrayOf("project", "state"), null, null, null, null, null).use { cursor ->
                while (cursor.moveToNext()) {
                    val project = cursor.getString(0)
                    val state = cursor.getString(1)
                    total++
                    totals[state] = (totals[state] ?: 0) + 1
                    val row = counts.getOrPut(project) { mutableMapOf() }
                    row[state] = (row[state] ?: 0) + 1
                }
            }
        }
        val done = totals["Dovršeno"] ?: 0
        line("SAŽETAK", 21f)
        line("Ukupno: $total    Dovršenost: ${if (total == 0) 0 else done * 100 / total}%")
        statuses.forEachIndexed { index, status ->
            val color = intArrayOf(Color.rgb(86, 169, 231), Color.rgb(245, 191, 83), Color.rgb(96, 200, 150))[index]
            bar(status, totals[status] ?: 0, total, color)
        }
        line("TABLICA PO PROJEKTIMA", 21f)
        projects.forEach { project ->
            val row = counts[project].orEmpty()
            val sum = row.values.sum()
            val finished = row["Dovršeno"] ?: 0
            line(project, 17f)
            line("Ukupno $sum  ·  Otvoreno ${row["Otvoreno"] ?: 0}  ·  Čeka ${row["Čeka se"] ?: 0}  ·  Dovršeno $finished", 14f, Color.LTGRAY)
            bar("Dovršenost ${if (sum == 0) 0 else finished * 100 / sum}%", finished, sum, Color.rgb(96, 200, 150))
        }
        line("Grafikoni predstavljaju broj zadataka. Financijski podaci i vremenski trendovi nisu uključeni.", 13f, Color.LTGRAY)
    }
}
