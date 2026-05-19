package com.jorge.mobile_03_wallet

import MoedaApi
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ConverterActivity : AppCompatActivity() {

    private val moedas = listOf("BRL", "USD", "BTC")
    private lateinit var progressBar: ProgressBar
    private lateinit var moedaApi: MoedaApi

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_converter)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.converterRoot)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val tvResultado = findViewById<TextView>(R.id.tvResultado)
        progressBar = findViewById(R.id.progressConversao)

        val retrofit = Retrofit.Builder()
            .baseUrl("https://economia.awesomeapi.com.br/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        moedaApi = retrofit.create(MoedaApi::class.java)

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
            progressBar.visibility = ProgressBar.VISIBLE
            lifecycleScope.launch (Dispatchers.IO) {
                try {
                    val pair = "${origem}-${destino}"
                    val response = moedaApi.getExchange(pair)
                    val chave = "${origem}${destino}"
                    val moeda = response[chave]
                    val cotacao = moeda?.bid?.toDoubleOrNull()
                    if (cotacao == null) {
                        withContext(Dispatchers.Main) {
                            progressBar.visibility = ProgressBar.GONE
                            Toast.makeText(
                                this@ConverterActivity,
                                "Conversão indisponível",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        return@launch
                    }
                    val valorConvertido = valor * cotacao
                    when (origem) {
                        "BRL" -> MainActivity.saldoReal -= valor
                        "USD" -> MainActivity.saldoDolar -= valor
                        "BTC" -> MainActivity.saldoBitcoin -= valor
                    }
                    when (destino) {
                        "BRL" -> MainActivity.saldoReal += valorConvertido
                        "USD" -> MainActivity.saldoDolar += valorConvertido
                        "BTC" -> MainActivity.saldoBitcoin += valorConvertido
                    }

                    val resultadoFormatado =
                        if (destino == "BTC") {
                            "%.6f".format(valorConvertido)
                        } else {
                            "%.2f".format(valorConvertido)
                        }

                    withContext(Dispatchers.Main){
                        progressBar.visibility = ProgressBar.GONE
                        tvResultado.text = "$valor $origem = $resultadoFormatado $destino"
                        tvResultado.postDelayed({
                            finish()
                        },2000)
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        progressBar.visibility = ProgressBar.GONE
                        Toast.makeText(
                            this@ConverterActivity,
                            "Erro ao consultar a API",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }
}
