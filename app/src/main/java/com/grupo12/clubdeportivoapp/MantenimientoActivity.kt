package com.grupo12.clubdeportivoapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.grupo12.clubdeportivoapp.databinding.ActivityMantenimientoBinding

class MantenimientoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMantenimientoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMantenimientoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener {
            finish()
        }
    }
}