package net.ddns.raspi_server.rezeptbuch.ui

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val intent = Intent(this, MainActivity::class.java)
    startActivity(intent)
    finish()
  }
}
