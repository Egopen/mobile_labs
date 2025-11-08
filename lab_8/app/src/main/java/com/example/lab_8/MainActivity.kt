package com.example.lab_8

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.lab_8.R

class MainActivity : AppCompatActivity() {

    private lateinit var logView: TextView
    private val PHONE_NUMBER = "1234567890"
    private val REQUEST_CALL = 1
    private val REQUEST_CONTACTS = 2

    private fun log(message: String) {
        logView.append("$message\n")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        logView = findViewById(R.id.lifecycleLog)

        findViewById<Button>(R.id.btnSecondActivity).setOnClickListener {
            startActivity(Intent(this, SecondActivity::class.java))
        }

        findViewById<Button>(R.id.btnCall).setOnClickListener {
            makeCall()
        }

        findViewById<Button>(R.id.btnReadContacts).setOnClickListener {
            readContacts()
        }

        log("onCreate()")
    }

    override fun onStart() {
        super.onStart()
        log("onStart()")
    }

    override fun onResume() {
        super.onResume()
        log("onResume()")
    }

    override fun onPause() {
        super.onPause()
        log("onPause()")
    }

    override fun onStop() {
        super.onStop()
        log("onStop()")
    }

    override fun onDestroy() {
        super.onDestroy()
        log("onDestroy()")
    }

    private fun makeCall() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CALL_PHONE), REQUEST_CALL)
        } else {
            val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$PHONE_NUMBER"))
            startActivity(intent)
        }
    }

    private fun readContacts() {
        log("readContacts()")
        val intent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
        startActivity(intent)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CALL -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) makeCall()
            REQUEST_CONTACTS -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) readContacts()
        }
    }
}
