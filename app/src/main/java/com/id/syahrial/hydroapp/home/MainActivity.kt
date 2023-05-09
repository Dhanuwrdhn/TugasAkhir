package com.id.syahrial.hydroapp.home

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.service.controls.Control
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.id.syahrial.hydroapp.R
import com.id.syahrial.hydroapp.control.ControlActivity
import com.id.syahrial.hydroapp.databinding.ActivityMainBinding
import java.io.BufferedWriter
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.security.Permission

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    private lateinit var database: DatabaseReference
    private lateinit var handler: Handler
    private lateinit var textView: TextView
    private var data: String = ""
    private var isHeaderWritten = false
    private lateinit var wifiManager: WifiManager
    private val PERMISSIONREQUESTCODE = 123
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
        val databaseRef = FirebaseDatabase.getInstance().getReference("users")

        // Menghilangkan status bar
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnControl.setOnClickListener {
            val intent = Intent(this, ControlActivity::class.java)
            startActivity(intent)
        }
        // Check if the app has permission to access Wi-Fi state
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            // Request permission to access Wi-Fi state
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_WIFI_STATE),
                PERMISSIONREQUESTCODE
            )
        } else {
            // Wi-Fi state permission has already been granted
            wifiManager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
            // Your code to check Wi-Fi state and display notifications goes here
        }
        binding.btnDownload.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                writeToCSV(data, isHeaderWritten)
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ),
                    1
                )
            }

            databaserealtime()
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSIONREQUESTCODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Wi-Fi state permission has been granted
                    wifiManager =
                        applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
                    // Your code to check Wi-Fi state and display notifications goes here
                } else {
                    // Wi-Fi state permission has been denied
                }
            }

            1 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    writeToCSV(data, isHeaderWritten)
                } else {
                    Toast.makeText(this, "Permission denied33", Toast.LENGTH_SHORT).show()
                }
            }
        }

    }

    private fun writeToCSV(data: String, isHeaderWritten: Boolean) {
        val fileName = "data.csv"
        val filePath = getExternalFilesDir(null)?.absolutePath + "/" + fileName
        val file = File(filePath)
        val outputStream = FileOutputStream(file, true)
        val outputStreamWriter = OutputStreamWriter(outputStream)
        val bufferedWriter = BufferedWriter(outputStreamWriter)

        if (!isHeaderWritten) {
            val header = "pH Air,TDS PPM"
            bufferedWriter.write(header)
            bufferedWriter.newLine()
        }

        bufferedWriter.write(data)
        bufferedWriter.newLine()

        bufferedWriter.close()
        outputStreamWriter.close()
        outputStream.close()

        Toast.makeText(this, "Data saved to $filePath", Toast.LENGTH_SHORT).show()
    }

    private fun databaserealtime() {
        database = FirebaseDatabase.getInstance().reference
        var isToastShown = false
        val postHydro = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val air = snapshot.child("PH/air").getValue(Double::class.java)
                val phair = String.format("%.2f", air)
                binding.tvPHmeter.text = phair

                val tds = snapshot.child("TDS/ppm").getValue(Double::class.java)
                val ppmnutrisi = String.format("%.2f", tds)
                binding.tvTdsmeter2.text = ppmnutrisi
                val isConnected = snapshot.child("devices/esp32_device_id/connected")
                    .getValue(Boolean::class.java)
                if (isConnected != null && isConnected) {
                    if (!isToastShown) {
                        Toast.makeText(
                            this@MainActivity,
                            "ESP 32 Telah Berhasil Terkoneksi",
                            Toast.LENGTH_SHORT
                        ).show()
                        isToastShown = true
                    }
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        "ESP 32 Belum Berhasil Terkoneksi",
                        Toast.LENGTH_SHORT
                    ).show()
                    isToastShown = false
                }

                val newData = "$air,$tds"
                if (data.isEmpty()) {
                    // Jika data masih kosong, set data dengan data baru
                    data = newData
                } else {
                    // Jika data sudah terisi, tambahkan data baru ke data yang sudah ada
                    data += "\n$newData"
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "failed", Toast.LENGTH_SHORT).show()
            }
        }
        database.addValueEventListener(postHydro)
    }
}

