package com.example.gamehotkeyoverlay

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val REQ_OVERLAY_PERMISSION = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnStart = findViewById<Button>(R.id.btnStartOverlay)
        btnStart.setOnClickListener {
            launchOverlay()
        }
    }

    private fun launchOverlay() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivityForResult(intent, REQ_OVERLAY_PERMISSION)
        } else {
            startService(Intent(this, FloatingOverlayService::class.java))
            Toast.makeText(this, "Floating Controller active! You can now open Brave.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_OVERLAY_PERMISSION) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(this)) {
                startService(Intent(this, FloatingOverlayService::class.java))
                Toast.makeText(this, "Floating Controller active! You can now open Brave.", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Overlay permission is required to display over Brave!", Toast.LENGTH_LONG).show()
            }
        }
    }
}
