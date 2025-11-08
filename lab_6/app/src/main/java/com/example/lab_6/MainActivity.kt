package com.example.lab_6

import android.app.Dialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    private lateinit var textView: TextView
    private lateinit var buttonShowDialog: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        setupClickListeners()
    }

    private fun initViews() {
        textView = findViewById(R.id.textView)
        buttonShowDialog = findViewById(R.id.buttonShowDialog)
    }

    private fun setupClickListeners() {
        buttonShowDialog.setOnClickListener {
            showCustomDialog()
        }
    }

    private fun showCustomDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_layout)
        dialog.setTitle("Ввод текста")

        val editTextInput = dialog.findViewById<EditText>(R.id.editTextInput)
        val buttonExitApp = dialog.findViewById<Button>(R.id.buttonExitApp)
        val buttonCloseDialog = dialog.findViewById<Button>(R.id.buttonCloseDialog)
        val buttonSendText = dialog.findViewById<Button>(R.id.buttonSendText)

        buttonExitApp.setOnClickListener {
            finish()
        }

        buttonCloseDialog.setOnClickListener {
            dialog.dismiss()
        }

        buttonSendText.setOnClickListener {
            val inputText = editTextInput.text.toString()
            if (inputText.isNotEmpty()) {
                textView.text = inputText
                dialog.dismiss()
            } else {
                editTextInput.error = "Введите текст"
            }
        }

        dialog.show()
    }
}