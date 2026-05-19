package com.jorge.mobile_03_wallet

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

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

            val progressBar = findViewById<ProgressBar>(R.id.progressConversao)
            progressBar.visibility = View.VISIBLE
            btnConfirmar.isEnabled = false

            lifecycleScope.launch {
                try {
                    val par = "${origem}-${destino}"

                    var valorConvertido = 0.0
                    if ((origem == "USD" && destino == "BTC") || (origem == "BTC" && destino == "USD")) {

                        val respUSD = RetrofitClient.api.getCotacao("USD-BRL")
                        val respBTC = RetrofitClient.api.getCotacao("BTC-BRL")

                        val cotacaoUSD = respUSD["USDBRL"]?.bid?.toDoubleOrNull()
                        val cotacaoBTC = respBTC["BTCBRL"]?.bid?.toDoubleOrNull()

                        if (cotacaoUSD == null || cotacaoBTC == null) {
                            throw Exception("Não foi possível obter as cotações para triangulação.")
                        }

                        valorConvertido = if (origem == "USD") {
                            // USD -> BRL -> BTC
                            (valor * cotacaoUSD) / cotacaoBTC
                        } else {
                            // BTC -> BRL -> USD
                            (valor * cotacaoBTC) / cotacaoUSD
                        }
                    }

                    else if (origem == "BRL" && destino == "BTC") {
                        val resp = RetrofitClient.api.getCotacao("BTC-BRL")
                        val cotacao = resp["BTCBRL"]?.bid?.toDoubleOrNull() ?: throw Exception("Erro na cotação do BTC.")

                        valorConvertido = valor / cotacao // Divide porque estamos comprando BTC com Real
                    }

                    else {
                        val response = RetrofitClient.api.getCotacao(par)
                        val chave = par.replace("-", "")
                        val cotacao = response[chave]?.bid?.toDoubleOrNull()
                            ?: throw Exception("Par de conversão $par não suportado ou indisponível.")

                        valorConvertido = valor * cotacao
                    }

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

                    val texto = when (destino) {
                        "BTC" -> "Convertido: ${"%.6f".format(valorConvertido)} BTC"
                        "USD" -> "Convertido: ${"$ %.2f".format(valorConvertido)}"
                        else  -> "Convertido: ${"R$ %.2f".format(valorConvertido)}"
                    }
                    Toast.makeText(this@ConverterActivity, texto, Toast.LENGTH_LONG).show()

                } catch (e: Exception) {
                    Toast.makeText(this@ConverterActivity,
                        "Erro: ${e.message}", Toast.LENGTH_SHORT).show()
                } finally {
                    progressBar.visibility = View.GONE
                    btnConfirmar.isEnabled = true
                    finish()
                }
            }
        }
    }
}
