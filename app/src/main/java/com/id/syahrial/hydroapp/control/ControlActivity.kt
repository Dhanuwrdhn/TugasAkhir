package com.id.syahrial.hydroapp.control

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.id.syahrial.hydroapp.R
import com.id.syahrial.hydroapp.databinding.ActivityControlBinding
import com.id.syahrial.hydroapp.databinding.ActivityMainBinding

@Suppress("DEPRECATION")
class ControlActivity : AppCompatActivity() {
    lateinit var binding: ActivityControlBinding
    private lateinit var database: FirebaseDatabase
    private lateinit var refData: DatabaseReference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityControlBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.etControl.inputType =
            InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        database = FirebaseDatabase.getInstance()
        refData = database.reference

        refData.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val datappm = snapshot.child("TDS/batas_nutrisi").value.toString().toFloat()
                val datappmformatted = String.format("%,.2f", datappm).replace(",", ".")
                binding.etControl.setText(datappmformatted)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ControlActivity, "Terjadi kesalahan", Toast.LENGTH_SHORT).show()

            }
        })
        val editControl = binding.etControl
        editControl.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Aksi sebelum perubahan teks
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Aksi saat terjadi perubahan teks
                val inputText = editControl.text.toString()
                // Lakukan tindakan yang sesuai dengan perubahan input
                // Misalnya, periksa panjang teks dan lakukan sesuatu jika mencapai panjang tertentu
                if (inputText.length >= 10) {
                    // Lakukan sesuatu
                }
            }

            override fun afterTextChanged(s: Editable?) {
                // Aksi setelah perubahan teks
            }
        })
        binding.btnControl.setOnClickListener {
            val ppm = binding.etControl.text.toString().trim()
            if (ppm.isEmpty()) {
                Toast.makeText(this, "Data tidak boleh kosong", Toast.LENGTH_SHORT).show()
            } else {
                val floatDatappm = ppm.toFloatOrNull()
                if (floatDatappm == null) {
                    Toast.makeText(this, "Data harus berupa angka", Toast.LENGTH_SHORT).show()
                } else {
                    refData.child("TDS/batas_nutrisi").setValue(floatDatappm)
                        .addOnCompleteListener { task1 ->
                            if (task1.isSuccessful)
                                Toast.makeText(
                                    this,
                                    "Data berhasil dikirim",
                                    Toast.LENGTH_SHORT
                                ).show()
                        }
                }
            }

        }
    }
}
