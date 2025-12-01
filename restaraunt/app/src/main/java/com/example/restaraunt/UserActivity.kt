package com.example.restaraunt

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import services.JSONChecker

class UserActivity : AppCompatActivity() {

    private val dbService = DBService()
    private var isRegistration = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            val checker = JSONChecker(dbService)
            val authorized = checker.isUserAuthorized(applicationContext)
            if (authorized) {
                showProfile()
            } else {
                showAuthScreen()
            }
        }
    }

    private fun showAuthScreen() {
        setContentView(R.layout.activity_user_login)

        val nameInput = findViewById<EditText>(R.id.loginName)
        val emailInput = findViewById<EditText>(R.id.loginEmail)
        val passwordInput = findViewById<EditText>(R.id.loginPassword)
        val actionButton = findViewById<Button>(R.id.actionButton)
        val toggleButton = findViewById<Button>(R.id.toggleButton)
        val backButton = findViewById<ImageButton>(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }
        fun updateUI() {
            if (isRegistration) {
                nameInput.visibility = View.VISIBLE
                actionButton.text = "Зарегистрироваться"
                toggleButton.text = "Уже есть аккаунт? Войти"
            } else {
                nameInput.visibility = View.GONE
                actionButton.text = "Войти"
                toggleButton.text = "Нет аккаунта? Зарегистрироваться"
            }
        }

        updateUI()

        toggleButton.setOnClickListener {
            isRegistration = !isRegistration
            updateUI()
        }

        actionButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()
            val name = nameInput.text.toString().trim()

            lifecycleScope.launch {
                if (isRegistration) {
                    if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                        runOnUiThread {
                            if (name.isEmpty()) nameInput.error = "Введите имя"
                            if (email.isEmpty()) emailInput.error = "Введите email"
                            if (password.isEmpty()) passwordInput.error = "Введите пароль"
                        }
                        return@launch
                    }

                    val success = dbService.registerUser(name, email, password)
                    if (success != null) {
                        JSONChecker(dbService).saveUserJson(applicationContext, email, password)
                        showProfile()
                    } else {
                        runOnUiThread { emailInput.error = "Ошибка регистрации" }
                    }
                } else {
                    val user = dbService.getUser(email, password)
                    if (user != null) {
                        JSONChecker(dbService).saveUserJson(applicationContext, email, password)
                        showProfile()
                    } else {
                        runOnUiThread { emailInput.error = "Неверные данные" }
                    }
                }
            }
        }
    }

    private fun showProfile() {
        setContentView(R.layout.activity_user_profile)

        val nameText = findViewById<TextView>(R.id.profileName)
        val emailText = findViewById<TextView>(R.id.profileEmail)
        val logoutButton = findViewById<Button>(R.id.logoutButton)
        val backButton = findViewById<ImageButton>(R.id.backButton)
        val orderContainer = findViewById<LinearLayout>(R.id.orderHistoryContainer)

        backButton.setOnClickListener { finish() }

        lifecycleScope.launch {
            val checker = JSONChecker(dbService)
            val json = checker.getUserJson(applicationContext)
            val email = json.optString("email")
            val password = json.optString("password")

            val user = dbService.getUser(email, password)
            if (user != null) {
                nameText.text = user.name
                emailText.text = user.email

                val orderHistory = dbService.getUserOrderHistory(email, password)
                orderContainer.removeAllViews()

                for ((order, status, products) in orderHistory) {
                    val cardView = CardView(this@UserActivity).apply {
                        radius = 16f
                        cardElevation = 6f
                        useCompatPadding = true
                        setContentPadding(24, 24, 24, 24)
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply { setMargins(0, 0, 0, 24) }
                    }

                    val orderLayout = LinearLayout(this@UserActivity).apply {
                        orientation = LinearLayout.VERTICAL
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                    }

                    val orderIdView = TextView(this@UserActivity).apply {
                        text = "Заказ: ${order.id.take(8)}..."
                        textSize = 18f
                        setTypeface(typeface, android.graphics.Typeface.BOLD)
                    }
                    orderLayout.addView(orderIdView)

                    val orderDateView = TextView(this@UserActivity).apply {
                        text = "Дата: ${order.created_at.substringBefore('T')}"
                        textSize = 16f
                    }
                    orderLayout.addView(orderDateView)

                    val orderSumView = TextView(this@UserActivity).apply {
                        text = "Сумма: ${order.order_sum} ₽"
                        textSize = 16f
                        setTypeface(typeface, android.graphics.Typeface.BOLD)
                    }
                    orderLayout.addView(orderSumView)

                    val orderStatusView = TextView(this@UserActivity).apply {
                        text = "Статус: $status"
                        textSize = 16f
                        setTextColor(resources.getColor(android.R.color.holo_blue_dark, theme))
                        setTypeface(typeface, android.graphics.Typeface.BOLD)
                    }
                    orderLayout.addView(orderStatusView)

                    for ((product, amount) in products) {
                        val productView = TextView(this@UserActivity).apply {
                            text = "- ${product.name} x$amount (${product.price} ₽)"
                            textSize = 15f
                            setPadding(8, 2, 0, 2)
                        }
                        orderLayout.addView(productView)
                    }

                    cardView.addView(orderLayout)
                    orderContainer.addView(cardView)
                }
            }
        }

        logoutButton.setOnClickListener {
            JSONChecker(dbService).removeUserJson(applicationContext)
            val prefs = getSharedPreferences("cart", MODE_PRIVATE)
            prefs.edit().clear().apply()
            showAuthScreen()
            (application as? MainActivity)?.refreshCartUI()
        }
    }



}

