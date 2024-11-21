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
    private lateinit var ivOrmawa: ImageView
    private lateinit var viewPager2: ViewPager2
    private lateinit var handlerVP: Handler
    private lateinit var imageList: ArrayList<Int>
    private lateinit var adapter: ImageAdapter

    private val apiService by lazy { APIConfig.getService() }

    private val handler = Handler(Looper.getMainLooper())
    private var lastFetchedData: List<Data>? = null
    private var lastDayOfWeek: String? = null
    private var currentIndex = 0
    private val maxDisplayCount = 5

    private val updateRunnable = object : Runnable {
        override fun run() {
            val currentDayOfWeek = getCurrentDayOfWeek()

            if (currentDayOfWeek != lastDayOfWeek) {
                lastDayOfWeek = currentDayOfWeek
                currentIndex = 0 // Reset indeks jika hari berubah
                fetchSchedules()
            } else {
                fetchSchedules(onlyCheckChanges = true)
            }

            handler.postDelayed(this, 5000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dateTextView = findViewById(R.id.tv_date)
        timeTextView = findViewById(R.id.tv_time)
        tableLayout = findViewById(R.id.tablelayout_jadwal)


        addTableHeader()
        updateDateTime()
        handler.postDelayed(object : Runnable {
            override fun run() {
                updateDateTime()
                handler.postDelayed(this, 1000)
            }
        }, 1000)

        lastDayOfWeek = getCurrentDayOfWeek()

        handler.post(updateRunnable)
//Image
        imageSlider()
        setUpTransformer()
        viewPager2.registerOnPageChangeCallback(object :ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                handlerVP.removeCallbacks(runnable)
                handlerVP.postDelayed(runnable,5000)
            }
        })
    }

    override fun onPause() {
        super.onPause()
        handlerVP.removeCallbacks(runnable)
    }

    override fun onResume() {
        super.onResume()
        handlerVP.postDelayed(runnable,5000)

    }
    private val runnable=Runnable{
        viewPager2.currentItem=viewPager2.currentItem+1
    }

    private fun setUpTransformer() {
        val transformer=CompositePageTransformer()
        transformer.addTransformer(MarginPageTransformer(40))
        transformer.addTransformer{page,position->
            val r=1- abs(position)
            page.scaleY=0.85f+r+0.14f
        }
        viewPager2.setPageTransformer(transformer)
    }

    private fun imageSlider() {
        viewPager2=findViewById(R.id.viewpager_ormawa)
        handlerVP=Handler(Looper.myLooper()!!)
        imageList= ArrayList()
        imageList.add(R.drawable.img)
        imageList.add(R.drawable.img_1)
        imageList.add(R.drawable.img_2)
        imageList.add(R.drawable.img_3)

        adapter= ImageAdapter(imageList,viewPager2)
        viewPager2.adapter=adapter
        viewPager2.offscreenPageLimit=2
        viewPager2.clipToPadding=false
        viewPager2.clipChildren=false
        viewPager2.getChildAt(0).overScrollMode=RecyclerView.OVER_SCROLL_NEVER
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
        val timeZone = TimeZone.getTimeZone("Asia/Jakarta")
        val calendar = Calendar.getInstance(timeZone)

        val dateFormat = SimpleDateFormat("dd MMMM ", Locale.getDefault())
        dateFormat.timeZone = timeZone
        val formattedDate = dateFormat.format(calendar.time)

        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        timeFormat.timeZone = timeZone
        val formattedTime = timeFormat.format(calendar.time)

        dateTextView.text = formattedDate
        timeTextView.text = formattedTime
    }

    private fun fetchSchedules(onlyCheckChanges: Boolean = false) {
        val currentDayOfWeek = getCurrentDayOfWeek()

        apiService.getSchedules().enqueue(object : Callback<Schedule> {
            override fun onResponse(call: Call<Schedule>, response: Response<Schedule>) {
                if (response.isSuccessful) {
                    response.body()?.let { schedule ->
                        val filteredData = schedule.data?.filterNotNull()?.filter { it.day == currentDayOfWeek }

                        if (!onlyCheckChanges || filteredData != lastFetchedData) {
                            lastFetchedData = filteredData
                            updateTable(filteredData)
                        } else {
                            // Jika hanya ingin menggilir data
                            updateTable(filteredData, shouldUpdateIndex = true)
                        }
                    }
                }
            }

            override fun onFailure(call: Call<Schedule>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Gagal mengambil data", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateTable(data: List<Data>?, shouldUpdateIndex: Boolean = false) {
        data?.let {
            tableLayout.removeViewsInLayout(1, tableLayout.childCount - 1)

            // Jika diminta menggilir data, perbarui indeks
            if (shouldUpdateIndex) {
                currentIndex = (currentIndex + maxDisplayCount) % it.size
            }

            // Ambil subset data untuk ditampilkan
            val dataToDisplay = it.drop(currentIndex).take(maxDisplayCount)

            // Tambahkan baris baru ke tabel
            for (item in dataToDisplay) {
                val tableRow = TableRow(this)
                tableRow.layoutParams = TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 0, 0)
                }

                val rowData = listOf(
                    item.course?.name.orEmpty(),
                    item.lecturer?.name.orEmpty(),
                    item.room.orEmpty(),
                    item.course?.classX.orEmpty(),
                    item.statusId.toString()
                )

                for (cellData in rowData) {
                    val textView = TextView(this)
                    textView.text = formatTextToTwoWordsPerLine(cellData)
                    textView.textAlignment
                    textView.setPadding(12, 12, 12, 12)
                    textView.setTextColor(ContextCompat.getColor(this, R.color.colorBlack))
                    tableRow.addView(textView)
                    tableRow.setBackgroundResource(R.drawable.table_cell_bg)

                }

                tableLayout.addView(tableRow)
            }
        }
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

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateRunnable)
    }
}
