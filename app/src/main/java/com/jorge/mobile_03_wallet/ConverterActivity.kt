package com.jorge.mobile_03_wallet

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class ConverterActivity : AppCompatActivity() {

    private val moedas = listOf("BRL", "USD", "BTC")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_converter)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.converterRoot)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, moedas)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        val spinnerOrigem = findViewById<Spinner>(R.id.spinnerOrigem)
        val spinnerDestino = findViewById<Spinner>(R.id.spinnerDestino)
        val etValor = findViewById<EditText>(R.id.etValor)
        val btnConfirmar = findViewById<Button>(R.id.btnConfirmarConversao)

        spinnerOrigem.adapter = adapter
        spinnerDestino.adapter = adapter

        btnConfirmar.setOnClickListener {
            val origem = spinnerOrigem.selectedItem.toString()
            val destino = spinnerDestino.selectedItem.toString()
            val valor = etValor.text.toString().toDoubleOrNull()

            if (origem == destino) {
                Toast.makeText(this, "Escolha moedas diferentes", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (valor == null || valor <= 0.0) {
                Toast.makeText(this, "Informe um valor maior que zero", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val saldoOrigem = when (origem) {
                "BRL" -> MainActivity.saldoReal
                "USD" -> MainActivity.saldoDolar
                else -> MainActivity.saldoBitcoin
            }

            if (valor > saldoOrigem) {
                Toast.makeText(this, "Saldo insuficiente em $origem", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
        }
    }
}
