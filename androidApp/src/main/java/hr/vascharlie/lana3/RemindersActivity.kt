package hr.vascharlie.lana3

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.TextView
import hr.vascharlie.lana3.core.reminders.LanaReminder
import hr.vascharlie.lana3.core.reminders.ReminderResult
import hr.vascharlie.lana3.core.reminders.ReminderService
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID

/** Developer-preview offline task/reminder UI. No ChatGPT import or multi-device sync. */
class RemindersActivity : Activity() {
    private lateinit var store: AndroidReminderStore
    private lateinit var service: ReminderService
    private lateinit var titleEditor: EditText
    private lateinit var categorySpinner: Spinner
    private lateinit var advanceSpinner: Spinner
    private lateinit var dateButton: Button
    private lateinit var status: TextView
    private lateinit var tasksContainer: LinearLayout
    private var selectedDate: LocalDate = LocalDate.now().plusDays(7)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        store = AndroidReminderStore(this)
        service = ReminderService(
            repository = store,
            clock = { System.currentTimeMillis() },
            newId = { UUID.randomUUID().toString() },
        )
        val scroll = ScrollView(this)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(30, 35, 30, 40)
            setBackgroundColor(Color.rgb(8, 17, 31))
        }
        scroll.addView(root)
        root.addView(heading(getString(R.string.reminders_title), 26))
        root.addView(label(getString(R.string.reminders_description), 14))
        titleEditor = EditText(this).apply {
            hint = getString(R.string.reminders_title_hint)
            setSingleLine(true)
            setTextColor(Color.WHITE)
            setHintTextColor(Color.LTGRAY)
        }
        root.addView(titleEditor)
        root.addView(label(getString(R.string.reminders_category_label), 14))
        categorySpinner = Spinner(this).apply {
            adapter = ArrayAdapter(
                this@RemindersActivity,
                android.R.layout.simple_spinner_dropdown_item,
                resources.getStringArray(R.array.reminder_categories),
            )
        }
        root.addView(categorySpinner)
        dateButton = Button(this).apply {
            isAllCaps = false
            setOnClickListener { pickDate() }
        }
        root.addView(dateButton)
        updateDateButton()
        root.addView(label(getString(R.string.reminders_advance_label), 14))
        advanceSpinner = Spinner(this).apply {
            adapter = ArrayAdapter(
                this@RemindersActivity,
                android.R.layout.simple_spinner_dropdown_item,
                resources.getStringArray(R.array.reminder_advance_options),
            )
        }
        root.addView(advanceSpinner)
        root.addView(Button(this).apply {
            text = getString(R.string.reminders_add)
            isAllCaps = false
            setOnClickListener { saveReminder() }
        })
        status = label("", 14).apply { setTextColor(Color.CYAN) }
        root.addView(status)
        tasksContainer = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        root.addView(tasksContainer)
        root.addView(Button(this).apply {
            text = getString(R.string.back)
            isAllCaps = false
            setOnClickListener { finish() }
        })
        setContentView(scroll)
        render()
    }

    override fun onResume() {
        super.onResume()
        if (::service.isInitialized) render()
    }

    override fun onDestroy() {
        store.close()
        super.onDestroy()
    }

    private fun pickDate() {
        DatePickerDialog(
            this,
            { _, year, month, day ->
                selectedDate = LocalDate.of(year, month + 1, day)
                updateDateButton()
            },
            selectedDate.year,
            selectedDate.monthValue - 1,
            selectedDate.dayOfMonth,
        ).show()
    }

    private fun updateDateButton() {
        dateButton.text = getString(
            R.string.reminders_due_date,
            selectedDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy.")),
        )
    }

    private fun saveReminder() {
        val zone = ZoneId.systemDefault()
        val due = selectedDate.atTime(9, 0).atZone(zone).toInstant().toEpochMilli()
        val days = when (advanceSpinner.selectedItemPosition) {
            0 -> 7L
            1 -> 1L
            2 -> 0L
            else -> null
        }
        val notify = days?.let {
            selectedDate.minusDays(it).atTime(9, 0).atZone(zone).toInstant().toEpochMilli()
        }
        when (val result = service.add(
            titleEditor.text.toString(),
            categorySpinner.selectedItem.toString(),
            due,
            notify,
        )) {
            is ReminderResult.Saved -> {
                titleEditor.setText("")
                AndroidReminderAlarm.schedule(this, result.reminder)
                status.text = if (notify != null && notify <= System.currentTimeMillis()) {
                    getString(R.string.reminders_saved_notice_elapsed)
                } else getString(R.string.reminders_saved)
                if (notify != null) requestNotificationPermission()
                render()
            }
            else -> status.text = getString(R.string.reminders_invalid)
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
        ) requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 701)
    }

    private fun render() {
        tasksContainer.removeAllViews()
        val now = LocalDate.now()
        val zone = ZoneId.systemDefault()
        fun start(date: LocalDate) =
            date.atStartOfDay(zone).toInstant().toEpochMilli()
        val tomorrow = start(now.plusDays(1))
        val nextMonday = start(now.plusDays((8 - now.dayOfWeek.value).toLong()))
        val open = service.openAll()
        section(getString(R.string.reminders_today_overdue),
            open.filter { it.dueAtEpochMillis < tomorrow })
        section(getString(R.string.reminders_this_week),
            open.filter { it.dueAtEpochMillis >= tomorrow && it.dueAtEpochMillis < nextMonday })
        section(getString(R.string.reminders_later),
            open.filter { it.dueAtEpochMillis >= nextMonday })
        section(getString(R.string.reminders_completed), service.doneAll())
    }

    private fun section(title: String, items: List<LanaReminder>) {
        tasksContainer.addView(heading(title + " (" + items.size + ")", 20))
        if (items.isEmpty()) {
            tasksContainer.addView(label(getString(R.string.reminders_none), 14))
            return
        }
        items.forEach { item ->
            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(12, 10, 12, 10)
                setBackgroundColor(Color.rgb(15, 39, 67))
            }
            card.addView(heading(item.title, 17))
            card.addView(label(item.category + " · " + formatDate(item.dueAtEpochMillis), 13))
            val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
            if (!item.completed) row.addView(Button(this).apply {
                isAllCaps = false
                text = getString(R.string.reminders_mark_done)
                setOnClickListener {
                    service.complete(item.id)
                    AndroidReminderAlarm.cancel(this@RemindersActivity, item.id)
                    render()
                }
            })
            row.addView(Button(this).apply {
                isAllCaps = false
                text = getString(R.string.reminders_delete)
                setOnClickListener {
                    AlertDialog.Builder(this@RemindersActivity)
                        .setMessage(getString(R.string.reminders_confirm_delete, item.title))
                        .setNegativeButton(android.R.string.cancel, null)
                        .setPositiveButton(R.string.reminders_delete) { _, _ ->
                            service.delete(item.id)
                            AndroidReminderAlarm.cancel(this@RemindersActivity, item.id)
                            render()
                        }.show()
                }
            })
            card.addView(row)
            tasksContainer.addView(card, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            ).apply { bottomMargin = 10 })
        }
    }

    private fun formatDate(epoch: Long) =
        java.time.Instant.ofEpochMilli(epoch).atZone(ZoneId.systemDefault())
            .format(DateTimeFormatter.ofPattern("dd.MM.yyyy. HH:mm", Locale.getDefault()))

    private fun heading(text: String, size: Int) = label(text, size).apply {
        setTypeface(null, Typeface.BOLD)
        setTextColor(Color.WHITE)
        setPadding(0, 14, 0, 7)
    }

    private fun label(text: String, size: Int) = TextView(this).apply {
        this.text = text
        textSize = size.toFloat()
        setTextColor(Color.rgb(172, 187, 204))
        setPadding(0, 4, 0, 5)
    }
}
