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
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var dateTextView: TextView
    private lateinit var timeTextView: TextView
    private lateinit var tableLayout: TableLayout
    private lateinit var ivOrmawa: ImageView
    private val data = listOf(
        listOf("Pemrograman Mobile Kotlin dan Java jadi satu", "Dr. Andi", "Lab 2", "A", "Praktikum"),
        listOf("Algoritma", "Dr. Budi Siregar", "Ruang 305", "B", "Kuliah"),
        listOf("Matematika", "Dr. Cindy", "Ruang 101", "C", "Teori"),
        listOf("Pemrograman", "Dr. Andi", "Lab 2", "A", "Praktikum"),
        listOf("Algoritma", "Dr. Budi", "Ruang 305", "B", "Kuliah"),
        listOf("Matematika", "Dr. Cindy", "Ruang 101", "C", "Teori"),
        listOf("Pemrograman", "Dr. Andi", "Lab 2", "A", "Praktikum"),
        listOf("Algoritma", "Dr. Budi", "Ruang 305", "B", "Kuliah"),
        listOf("Matematika", "Dr. Cindy", "Ruang 101", "C", "Teori"),
        listOf("Pemrograman", "Dr. Andi", "Lab 2", "A", "Praktikum"),
        listOf("AlgoritmaA", "Dr. Budi", "Ruang 305", "B", "Kuliah"),
        listOf("MatematikaX", "Dr. Cindy", "Ruang 101", "C", "Teori"),
        listOf("PemrogramanW", "Dr. Andi", "Lab 2", "A", "Praktikum"),
        listOf("AlgoritmaLast", "Dr. Budi", "Ruang 305", "B", "Kuliah"),
        listOf("Matematika", "Dr. Cindy", "Ruang 101", "C", "Teori"),
        listOf("Algoritma2", "Dr. Budi", "Ruang 305", "B", "Kuliah"),
        listOf("Matematika3", "Dr. Cindy", "Ruang 101", "C", "Teori"),
        listOf("Pemrograman4", "Dr. Andi", "Lab 2", "A", "Praktikum"),
        listOf("AlgoritmaLast5", "Dr. Budi", "Ruang 305", "B", "Kuliah"),
        listOf("Matematika", "Dr. Cindy", "Ruang 101", "C", "Teori"),
    )

    private val imageUrls = listOf(
        "https://res.cloudinary.com/dbxqrwwml/image/upload/v1731920723/jftztixl5rjmjy3cupby.png",  // Ganti dengan URL gambar yang valid
        "https://res.cloudinary.com/dbxqrwwml/image/upload/v1731920767/game-console_1_lhvjtq.png",
        "https://res.cloudinary.com/dbxqrwwml/image/upload/v1731920784/Logo_OSIS.svg_lwhnpf.png"

    )

    private var currentIndex = 0
    private val limit = 8
    private val handler = Handler(Looper.getMainLooper())
    private val updateRunnable = object : Runnable {
        override fun run() {
            updateTable()
            updateImageView()
            handler.postDelayed(this, 5000) // Update every 15 seconds
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dateTextView = findViewById(R.id.tv_date)
        timeTextView = findViewById(R.id.tv_time)
        tableLayout = findViewById(R.id.tablelayout_jadwal)
        ivOrmawa = findViewById(R.id.iv_ormawa)
        // Menambahkan header tabel
        addTableHeader()
        updateDateTime()
        handler.postDelayed(object : Runnable {
            override fun run() {
                updateDateTime()
                handler.postDelayed(this, 1000) // Update every second
            }
        }, 1000)
        handler.post(updateRunnable)
        updateTable()
    }

    private fun updateDateTime() {
        val calendar = Calendar.getInstance()

        // Format tanggal
        val dateFormat = SimpleDateFormat("dd MMMM", Locale.getDefault())
        val formattedDate = dateFormat.format(calendar.time)
        dateTextView.text = formattedDate

        // Format waktu
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val formattedTime = timeFormat.format(calendar.time)
        timeTextView.text = formattedTime    }

    private fun addTableHeader() {
        val headerRow = TableRow(this)
        headerRow.layoutParams = TableRow.LayoutParams(
            TableRow.LayoutParams.MATCH_PARENT,
            TableRow.LayoutParams.WRAP_CONTENT
        )

        // Menambahkan header kolom
        val headers = listOf("Mata Kuliah", "Dosen", "Ruang", "Kelas", "Tipe")
        for (header in headers) {
            val textView = TextView(this)
            textView.text = header
            textView.setPadding(24, 24, 24, 24)
            textView.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary))
            textView.setTypeface(null, Typeface.BOLD)
            headerRow.addView(textView)
        }

        tableLayout.addView(headerRow) // Menambahkan header ke dalam TableLayout
    }

    private fun updateTable() {
        // Kosongkan TableLayout sebelum menambahkan data baru
        // Kecuali header yang sudah ditambahkan sebelumnya
        tableLayout.removeViewsInLayout(1, tableLayout.childCount - 1)

        // Ambil data yang sesuai dengan index saat ini
        val limitedData = data.drop(currentIndex).take(limit)

        // Menambahkan setiap baris ke tabel
        for (rowData in limitedData) {
            val tableRow = TableRow(this)
            tableRow.layoutParams = TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
            )

            for (cellData in rowData) {
                val textView = TextView(this)
                textView.text = cellData
                textView.setPadding(24, 24, 24, 24)
                textView.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary))
                tableRow.addView(textView)
            }

            tableLayout.addView(tableRow)
        }

        // Update index untuk data berikutnya
        currentIndex += limit
        if (currentIndex >= data.size) {
            currentIndex = 0
        }
    }
    private fun updateImageView() {
        // Ambil gambar dari URL sesuai dengan index saat ini
        val imageUrl = imageUrls[currentIndex % imageUrls.size]

        // Gunakan Glide untuk memuat gambar ke ImageView
        Glide.with(this)
            .load(imageUrl)
//            .placeholder(R.drawable.placeholder) // Tambahkan placeholder jika gambar belum ada
//            .error(R.drawable.error_image) // Tambahkan gambar error jika gagal memuat
            .into(ivOrmawa)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateRunnable) // Hentikan pembaruan saat aktivitas dihancurkan
    }
}
