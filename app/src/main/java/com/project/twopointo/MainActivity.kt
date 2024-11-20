package com.project.twopointo

import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.project.twopointo.ui.Data
import com.project.twopointo.ui.Schedule
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class MainActivity : AppCompatActivity() {
    private lateinit var dateTextView: TextView
    private lateinit var timeTextView: TextView
    private lateinit var tableLayout: TableLayout
    private lateinit var ivOrmawa: ImageView

    private val apiService by lazy { APIConfig.getService() }

    private val handler = Handler(Looper.getMainLooper())
    private val updateRunnable = object : Runnable {
        override fun run() {
            fetchSchedules()
            handler.postDelayed(this, 5000) // Update setiap 5 detik
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dateTextView = findViewById(R.id.tv_date)
        timeTextView = findViewById(R.id.tv_time)
        tableLayout = findViewById(R.id.tablelayout_jadwal)
        ivOrmawa = findViewById(R.id.iv_ormawa)

        addTableHeader()
        updateDateTime()
        handler.postDelayed(object : Runnable {
            override fun run() {
                updateDateTime()
                handler.postDelayed(this, 1000) // Update setiap detik
            }
        }, 1000)
        handler.post(updateRunnable)
    }
    private fun getCurrentDayOfWeek(): String {
        val calendar = Calendar.getInstance()
        val daysOfWeek = arrayOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
        return daysOfWeek[calendar.get(Calendar.DAY_OF_WEEK) - 1]
    }
    private fun addTableHeader() {
        val headerRow = TableRow(this)
        headerRow.layoutParams = TableRow.LayoutParams(
            TableRow.LayoutParams.MATCH_PARENT,
            TableRow.LayoutParams.WRAP_CONTENT
        )

        val headers = listOf("Mata Kuliah", "Dosen", "Ruang", "Kelas", "Tipe")
        for (header in headers) {
            val textView = TextView(this)
            textView.text = header
            textView.setPadding(24, 24, 24, 24)
            textView.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary))
            textView.setTypeface(null, Typeface.BOLD)
            headerRow.addView(textView)
        }
        tableLayout.addView(headerRow)
    }

    private fun updateDateTime() {
        // Set the desired time zone here (e.g., "Asia/Jakarta" for Jakarta)
        val timeZone = TimeZone.getTimeZone("Asia/Jakarta") // Change this to your desired time zone

        // Create a Calendar instance and set its time zone
        val calendar = Calendar.getInstance(timeZone)

        // Format the date
        val dateFormat = SimpleDateFormat("dd MMMM ", Locale.getDefault())
        dateFormat.timeZone = timeZone // Set time zone for date format
        val formattedDate = dateFormat.format(calendar.time)

        // Format the time
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        timeFormat.timeZone = timeZone // Set time zone for time format
        val formattedTime = timeFormat.format(calendar.time)

        // Update the TextViews with formatted date and time
        dateTextView.text = formattedDate
        timeTextView.text = formattedTime
    }


    private var currentIndex = 0
    private val maxDisplayCount = 4

    private fun fetchSchedules() {
        val currentDayOfWeek = getCurrentDayOfWeek()

        apiService.getSchedules().enqueue(object : Callback<Schedule> {
            override fun onResponse(call: Call<Schedule>, response: Response<Schedule>) {
                if (response.isSuccessful) {
                    response.body()?.let { schedule ->
                        val filteredData = schedule.data?.filterNotNull()?.filter { data -> data.day == currentDayOfWeek }
                        updateTable(filteredData)
                    }
                }
            }

            override fun onFailure(call: Call<Schedule>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Gagal mengambil data", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateTable(data: List<Data>?) {
        data?.let {
            // Clear existing rows except for the header
            tableLayout.removeViewsInLayout(1, tableLayout.childCount - 1)

            // Calculate the next set of data to display
            val dataToDisplay = it.drop(currentIndex).take(maxDisplayCount)

            // Add the new rows to the table
            for (item in dataToDisplay) {
                val tableRow = TableRow(this)
                tableRow.layoutParams = TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT
                )

                val rowData = listOf(
                    item.course?.name.orEmpty(),
                    item.lecturer?.name.orEmpty(),
                    item.room.orEmpty(),
                    item.course?.classX.orEmpty(),
                    item.day.orEmpty()
                )

                for (cellData in rowData) {
                    val textView = TextView(this)
                    textView.text = formatTextToTwoWordsPerLine(cellData)
                    textView.setPadding(24, 24, 24, 24)
                    textView.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary))
                    tableRow.addView(textView)
                }

                tableLayout.addView(tableRow)
            }

            // Update the index for the next display
            currentIndex += maxDisplayCount
            if (currentIndex >= it.size) {
                currentIndex = 0 // Reset to the beginning if we've reached the end of the data
            }
        }
    }
    private fun formatTextToTwoWordsPerLine(text: String): String {
        val words = text.split(" ")
        val formattedText = StringBuilder()

        for (i in words.indices) {
            formattedText.append(words[i])
            if ((i + 1) % 2 == 0) {
                formattedText.append("\n") // Pindah ke baris baru setiap dua kata
            } else if (i != words.lastIndex) {
                formattedText.append(" ") // Tambahkan spasi jika bukan kata terakhir
            }
        }

        return formattedText.toString()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateRunnable)
    }
}
