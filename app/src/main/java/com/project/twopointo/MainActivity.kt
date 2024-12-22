package com.project.twopointo

import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
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
import androidx.core.view.children
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
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
import kotlin.math.abs

class MainActivity : AppCompatActivity() {
    private lateinit var dateTextView: TextView
    private lateinit var timeTextView: TextView
    private lateinit var tableLayout: TableLayout
    private lateinit var viewPager2: ViewPager2
    private lateinit var handlerVP: Handler
    private lateinit var adapter: URLImageAdapter
    private lateinit var textView: TextView

    private val apiService by lazy { APIConfig.getService() }
    private val handler = Handler(Looper.getMainLooper())
    private var lastFetchedData: List<Data>? = null
    private var lastFetchedImageUrls: List<String>? = null
    private var lastDayOfWeek: String? = null
    private var currentIndex = 0
    private val maxDisplayCount = 8
    private val imageUpdateInterval = 10000L

    private val updateRunnable = object : Runnable {
        override fun run() {
            val currentDayOfWeek = getCurrentDayOfWeek()

            if (currentDayOfWeek != lastDayOfWeek) {
                lastDayOfWeek = currentDayOfWeek
                currentIndex = 0
                fetchSchedules()
            } else {
                fetchSchedules(onlyCheckChanges = true)
            }
//untuk mengatur waktu table
            handler.postDelayed(this, 3000)
        }
    }

    private val imageUpdateRunnable = object : Runnable {
        override fun run() {
            fetchUKMImages()
            handler.postDelayed(this, imageUpdateInterval)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dateTextView = findViewById(R.id.tv_date)
        timeTextView = findViewById(R.id.tv_time)
        tableLayout = findViewById(R.id.tablelayout_jadwal)
        textView = findViewById(R.id.tv_tahunajaran)

        updateDateTime()
        handler.postDelayed(object : Runnable {
            override fun run() {
                updateDateTime()
                handler.postDelayed(this, 3000)
            }
        }, 1000)

        setSchoolYear()
        lastDayOfWeek = getCurrentDayOfWeek()

        handler.post(updateRunnable)

        // Start fetching UKM images periodically
        handler.post(imageUpdateRunnable)

        viewPager2 = findViewById(R.id.viewpager_ormawa)
        handlerVP = Handler(Looper.myLooper()!!)
        imageSlider()
        setUpTransformer()
        viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                handlerVP.removeCallbacks(runnable)
                handlerVP.postDelayed(runnable, 10000)
            }
        })
    }

    private fun setSchoolYear() {
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH) + 1 // Calendar is 0-based

        val schoolYear: String = if (currentMonth in 8..12) {
            // August to December
            "GANJIL $currentYear/${currentYear + 1}"
        } else if (currentMonth in 2..7) {
            // February to July
            "GENAP ${currentYear - 1}/$currentYear"
        } else {
            // January
            "GANJIL ${currentYear - 1}/$currentYear"
        }

        textView.text = schoolYear
    }

    override fun onPause() {
        super.onPause()
        handlerVP.removeCallbacks(runnable)
        handler.removeCallbacks(imageUpdateRunnable)
    }

    override fun onResume() {
        super.onResume()
        handlerVP.postDelayed(runnable, 5000)
        handler.post(imageUpdateRunnable)
    }

    private val runnable = Runnable {
        viewPager2.currentItem = viewPager2.currentItem + 1
    }

    private fun setUpTransformer() {
        val transformer = CompositePageTransformer()
        transformer.addTransformer(MarginPageTransformer(40))
        transformer.addTransformer { page, position ->
            val scale = 0.85f + (1 - abs(position)) * 0.15f
            page.scaleY = scale.coerceAtMost(1f)
        }
        viewPager2.setPageTransformer(transformer)
    }

    private fun imageSlider() {
        fetchUKMImages()
    }

    private fun fetchUKMImages() {
        apiService.getUkm().enqueue(object : Callback<UKM> {
            override fun onResponse(call: Call<UKM>, response: Response<UKM>) {
                if (response.isSuccessful) {
                    val ukmList = response.body()
                    if (ukmList != null && ukmList.isNotEmpty()) {
                        val newImageUrls = ukmList.mapNotNull { it.url }

                        // Check if the image URLs have changed
                        if (newImageUrls != lastFetchedImageUrls) {
                            lastFetchedImageUrls = newImageUrls
                            updateImageSlider(newImageUrls)
                        }
                    }
                } else {
                    Toast.makeText(this@MainActivity, "Failed to load images", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<UKM>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateImageSlider(imageUrls: List<String>) {
        if (::adapter.isInitialized) {
            adapter.updateImages(imageUrls)
        } else {
            adapter = URLImageAdapter(imageUrls, viewPager2)
            viewPager2.adapter = adapter
        }
    }

    private fun fetchSchedules(onlyCheckChanges: Boolean = false) {
        val currentDayOfWeek = getCurrentDayOfWeek()

        apiService.getSchedules().enqueue(object : Callback<Schedule> {
            override fun onResponse(call: Call<Schedule>, response: Response<Schedule>) {
                if (response.isSuccessful) {
                    response.body()?.let { schedule ->
                        val filteredData = schedule.data?.filterNotNull()?.filter { it.day == currentDayOfWeek }

                        // Urutkan data berdasarkan startTime
                        val sortedData = filteredData?.sortedBy { it.startTime }

                        if (!onlyCheckChanges || sortedData != lastFetchedData) {
                            lastFetchedData = sortedData
                            updateTable(sortedData)
                        } else {
                            updateTable(sortedData, shouldUpdateIndex = true)
                        }
                    }
                }
            }

            override fun onFailure(call: Call<Schedule>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Gagal mengambil data", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun formatTime(time: String?): String {
        return if (!time.isNullOrEmpty() && time.length >= 5) {
            time.substring(0, 5) // Ambil karakter dari indeks 0 hingga 4 (HH:mm)
        } else {
            "Invalid Time"
        }
    }
    private fun formatTimeRange(startTime: String?, endTime: String?): String {
        val startFormatted = formatTime(startTime)
        val endFormatted = formatTime(endTime)
        return "$startFormatted-$endFormatted"
    }

    private fun updateTable(data: List<Data>?, shouldUpdateIndex: Boolean = false) {
        data?.let {
            // Bersihkan tabel kecuali header
            tableLayout.removeViewsInLayout(1, tableLayout.childCount - 1)

            if (it.isNotEmpty()) {
                // Periksa dan perbarui currentIndex
                if (shouldUpdateIndex) {
                    currentIndex = if (currentIndex + maxDisplayCount >= it.size) {
                        0 // Reset jika sudah mencapai akhir
                    } else {
                        currentIndex + maxDisplayCount
                    }
                }

                // Ambil data untuk ditampilkan
                val dataToDisplay = it.subList(currentIndex, minOf(currentIndex + maxDisplayCount, it.size))
                val displayMetrics = resources.displayMetrics
                val widthPixels = displayMetrics.widthPixels
                val heightPixels = displayMetrics.heightPixels

                // Menentukan params berdasarkan resolusi
                val paramsReso = if (widthPixels == 1920 && heightPixels == 1080) {
                    TableRow.LayoutParams(325, 200, 1f) // FHD
                } else {
                    TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT) // Default
                }
                for (item in dataToDisplay) {
                    val tableRow = TableRow(this)
                    tableRow.layoutParams = TableRow.LayoutParams(
                        TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(8, 0, 4, 0)
                    }

                    val rowData = listOf(
                        item.course?.name.orEmpty(),
                        item.lecturer?.name.orEmpty(),
                        formatTimeRange(item.startTime.orEmpty(), item.endTime.orEmpty()),
                        item.room.orEmpty(),
                        item.classX?.name.orEmpty()
                    )

                    for (cellData in rowData) {
                        val textView = TextView(this)
                        textView.text = formatTextToTwoWordsPerLine(cellData)
                        textView.gravity=Gravity.CENTER
                        textView.setPadding(12, 8, 12, 6)
                        textView.setTextColor(ContextCompat.getColor(this, R.color.colorBlack))
                        tableRow.addView(textView)
                        tableRow.gravity = Gravity.CENTER
                        val params = paramsReso
                        textView.layoutParams = params
                        tableRow.setBackgroundResource(R.drawable.table_cell_bg)
                    }

                    val statusTextView = createStatusTextView(item.status?.name)
                    tableRow.addView(statusTextView)

                    tableLayout.addView(tableRow)
                }
            } else {
                currentIndex = 0 // Reset jika data kosong
            }
        }
    }


    // Function to create a TextView for the status
    private fun createStatusTextView(statusName: String?): TextView {
        val statusTextView = TextView(this)

        statusTextView.layoutParams = TableRow.LayoutParams(
            TableRow.LayoutParams.WRAP_CONTENT,
            TableRow.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(0, 0, 0, 0)

        }


        when (statusName) {
            "Ada" -> {
                statusTextView.setBackgroundResource(R.drawable.ada)
                statusTextView.visibility = View.VISIBLE
            }
            "Dimulai" -> {
                statusTextView.setBackgroundResource(R.drawable.dimulai)
                statusTextView.visibility = View.VISIBLE
            }
            "Selesai" -> {
                statusTextView.setBackgroundResource(R.drawable.selesai)
                statusTextView.visibility = View.VISIBLE
            }
            "Tidak Ada" -> {
                statusTextView.setBackgroundResource(R.drawable.tidakada)
                statusTextView.visibility = View.VISIBLE
            }
            else -> {
                statusTextView.visibility = View.GONE
            }
        }
        statusTextView.scaleX = 0.65f
        statusTextView.scaleY = 0.65f
        return statusTextView
    }
    private fun formatTextToTwoWordsPerLine(text: String): String {
        val words = text.split(" ")
        val formattedText = StringBuilder()

        for (i in words.indices) {
            formattedText.append(words[i])
            if ((i + 1) % 2 == 0) {
                formattedText.append("\n")
            } else if (i != words.lastIndex) {
                formattedText.append(" ")
            }
        }

        return formattedText.toString()
    }

    private fun getCurrentDayOfWeek(): String {
        val calendar = Calendar.getInstance()
        val daysOfWeek = arrayOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
        return daysOfWeek[calendar.get(Calendar.DAY_OF_WEEK) - 1]
    }



    private fun updateDateTime() {
        val calendar = Calendar.getInstance()

        val dateFormat = SimpleDateFormat("EEEE, dd MMMM", Locale.getDefault())
        val formattedDate = dateFormat.format(calendar.time)

        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val formattedTime = timeFormat.format(calendar.time)

        dateTextView.text = formattedDate
        timeTextView.text = formattedTime
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateRunnable)
        handler.removeCallbacks(imageUpdateRunnable)
    }
}