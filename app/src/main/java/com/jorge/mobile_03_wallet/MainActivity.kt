package com.jorge.mobile_03_wallet

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    companion object {
        var saldoReal = 100000.0
        var saldoDolar = 50000.0
        var saldoBitcoin = 0.5
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<Button>(R.id.btnConverter).setOnClickListener {
            startActivity(Intent(this, ConverterActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        findViewById<TextView>(R.id.tvSaldoBRL).text = "R$ %.2f".format(saldoReal)
        findViewById<TextView>(R.id.tvSaldoUSD).text = "$ %.2f".format(saldoDolar)
        findViewById<TextView>(R.id.tvSaldoBTC).text = "BTC %.6f".format(saldoBitcoin)
    }
}
