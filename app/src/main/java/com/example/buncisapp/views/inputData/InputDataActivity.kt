package com.example.buncisapp.views.inputData

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import com.example.buncisapp.R
import com.example.buncisapp.data.ShipPreference
import com.example.buncisapp.data.model.Biodata
import com.example.buncisapp.databinding.ActivityInputDataBinding
import com.example.buncisapp.views.ViewModelFactory
import com.example.buncisapp.views.auth.LoginActivity
import com.example.buncisapp.views.calculator.CalculatorActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class InputDataActivity : AppCompatActivity() {

    private lateinit var inputDataViewModel: InputDataViewModel
    private lateinit var binding: ActivityInputDataBinding
    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityInputDataBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setupViewModel()

        val locale = Locale.US
        Locale.setDefault(locale)


        inputDataViewModel.getShip().observe(this) { ship ->

            binding.lvToolbar.btnAccount.text = ship.username

            inputDataViewModel.fuelType(ship.token) { dataFuelType ->
                val fuelTypeAdapter = ArrayAdapter(this, R.layout.dropdown_items, dataFuelType)
                binding.edBahanBakar.setAdapter(fuelTypeAdapter)
            }
            inputDataViewModel.shipCondition(ship.token) { shipConditionData ->
                val shipConditionAdapter =
                    ArrayAdapter(this, R.layout.dropdown_items, shipConditionData)
                binding.edKondisiKapal.setAdapter(shipConditionAdapter)
            }
            inputDataViewModel.port(ship.token) { portData ->
                val portAdapter = ArrayAdapter(this, R.layout.dropdown_items, portData)
                binding.edNamaPelabuhan.setAdapter(portAdapter)
            }
        }

        binding.mvTimer.setOnClickListener {
            showTimePickerDialog()
        }

        binding.edTanggal.setOnFocusChangeListener { view, focus ->
            if (focus) {
                view.clearFocus()
                showDatePickerDialog()
            }
        }

        binding.lvToolbar.btnAccount.setOnClickListener {
            MaterialAlertDialogBuilder(this@InputDataActivity)
                .setTitle("Peringatan!")
                .setMessage("Apakah anda yakin untuk keluar?")
                .setPositiveButton("Ya") { _, _ ->
                    inputDataViewModel.logout()
                    val intent = Intent(this@InputDataActivity, LoginActivity::class.java)
                    startActivity(intent)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

                }
                .setNegativeButton("Batal") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }

        binding.btnNext.setOnClickListener {

            var depan = 0.0
            var tengah = 0.0
            var belakang = 0.0

            // cek draft kosong
            if (binding.edDraftDepan.text.toString().isNotEmpty()) {
                depan = binding.edDraftDepan.text.toString().toDouble()
            }
            if (binding.edDraftTengah.text.toString().isNotEmpty()) {
                tengah = binding.edDraftTengah.text.toString().toDouble()
            }
            if (binding.edDraftBelakang.text.toString().isNotEmpty()) {
                belakang = binding.edDraftBelakang.text.toString().toDouble()
            }

            // penghitungan trim
            var trim = 0.0
            if (binding.edDraftDepan.text != null && binding.edDraftBelakang.text != null) {
                trim = String.format("%.2f", (belakang - depan)).toDouble()
            } else if (binding.edDraftDepan.text == null) {
                trim = String.format("%.2f", (belakang - tengah)).toDouble()
            } else if (binding.edDraftBelakang.text == null) {
                trim = String.format("%.2f", (tengah - depan)).toDouble()
            }

            // cek data kosong
            if (binding.edNamaPelabuhan.text.toString().isEmpty()) {
                Toast.makeText(
                    this@InputDataActivity,
                    resources.getString(R.string.warning_empty_data, binding.tlNamaPelabuhan.hint),
                    Toast.LENGTH_SHORT
                ).show()
            } else if (binding.edKondisiKapal.text.toString().isEmpty()) {
                Toast.makeText(
                    this@InputDataActivity,
                    resources.getString(R.string.warning_empty_data, binding.tlKondisiKapal.hint),
                    Toast.LENGTH_SHORT
                ).show()
            } else if (binding.edTanggal.text.toString().isEmpty()) {
                Toast.makeText(
                    this@InputDataActivity,
                    resources.getString(R.string.warning_empty_data, binding.edTanggal.hint),
                    Toast.LENGTH_SHORT
                ).show()
            } else if (binding.edBahanBakar.text.toString().isEmpty()) {
                Toast.makeText(
                    this@InputDataActivity,
                    resources.getString(R.string.warning_empty_data, binding.tlBahanBakar.hint),
                    Toast.LENGTH_SHORT
                ).show()
            } else if (binding.tvTime.text.toString().isEmpty()) {
                Toast.makeText(
                    this@InputDataActivity,
                    resources.getString(R.string.warning_empty_data, binding.tvTime.hint),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                val data = Biodata(
                    binding.edNamaPelabuhan.text.toString(),
                    binding.edKondisiKapal.text.toString(),
                    binding.edTanggal.text.toString(),
                    binding.edBahanBakar.text.toString(),
                    binding.tvTime.text.toString(),
                    trim,
                    depan,
                    tengah,
                    belakang
                )
                MaterialAlertDialogBuilder(this@InputDataActivity)
                    .setTitle("Yakin untuk melanjutkan?")
                    .setMessage("Anda yakin ingin melanjutkan ke kalkulator?")
                    .setPositiveButton("Ya") { _, _ ->
                        val intent = Intent(this@InputDataActivity, CalculatorActivity::class.java)
                        intent.putExtra("data", data)
                        startActivity(intent)
                    }
                    .setNegativeButton("Batal") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            }
        }
    }

    private fun setupViewModel() {
        inputDataViewModel = ViewModelProvider(
            this,
            ViewModelFactory(ShipPreference.getInstance(dataStore), this)
        )[InputDataViewModel::class.java]
    }

    private fun showTimePickerDialog() {
        val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hour, minute ->
            calendar.set(Calendar.HOUR_OF_DAY, hour)
            calendar.set(Calendar.MINUTE, minute)
            calendar.set(Calendar.SECOND, 0) // Set seconds to 0
            updateTimer()
        }
        TimePickerDialog(
            this,
            timeSetListener,
            12,
            10,
            true
        ).show()
    }

    private fun showDatePickerDialog() {
        val dateSetListener =
            DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, monthOfYear)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                updateEditText()
            }
        DatePickerDialog(
            this,
            dateSetListener,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun updateTimer() {
        val format = "HH:mm:ss"
        val dateFormat = SimpleDateFormat(format, Locale.getDefault())
        binding.tvTime.text = dateFormat.format(calendar.time)
    }

    private fun updateEditText() {
        val format = "yyyy-MM-dd"
        val sdf = SimpleDateFormat(format, Locale.US)
        binding.edTanggal.setText(sdf.format(calendar.time))
    }


}