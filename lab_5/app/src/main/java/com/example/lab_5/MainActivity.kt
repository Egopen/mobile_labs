package com.example.lab_5

import android.content.res.Configuration
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var myLayout: LinearLayout
    private var isAdaptiveMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        myLayout = findViewById(R.id.root)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (isAdaptiveMode) {
            setAdaptiveBackground()
        }
    }

    private fun setAdaptiveBackground() {
        val orientation = resources.configuration.orientation

        when (orientation) {
            Configuration.ORIENTATION_LANDSCAPE -> {
                myLayout.setBackgroundResource(R.drawable.backgound_land)
            }
            else -> {
                myLayout.setBackgroundResource(R.drawable.background_port)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val itId = item.itemId

        when (itId) {
            R.id.red -> {
                isAdaptiveMode = false
                myLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.red))
                return true
            }
            R.id.green -> {
                isAdaptiveMode = false
                myLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.green))
                return true
            }
            R.id.blue -> {
                isAdaptiveMode = false
                myLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.blue))
                return true
            }
            R.id.adaptee -> {
                isAdaptiveMode = true
                setAdaptiveBackground()
                return true
            }
            R.id.exit -> {
                finish()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }
}