package com.example.restaraunt

import Order
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.setPadding
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import services.JSONChecker
import java.time.LocalDateTime
import java.util.UUID

class CartActivity : AppCompatActivity() {

    private val dbService = DBService()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        val cartContainer = findViewById<LinearLayout>(R.id.cartContainer)
        val cartTotalView = findViewById<TextView>(R.id.cartTotal)
        val orderButton = findViewById<Button>(R.id.orderButton)
        val backButton = findViewById<ImageButton>(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }
        val prefs = getSharedPreferences("cart", MODE_PRIVATE)
        val cartJson = prefs.getString("cart_items", "{}")
        val cart = JSONObject(cartJson!!)

        fun refreshCart() {
            cartContainer.removeAllViews()
            lifecycleScope.launch {
                val prefs = getSharedPreferences("cart", MODE_PRIVATE)
                val cartJson = prefs.getString("cart_items", "{}")
                val cart = JSONObject(cartJson!!)

                var totalSum = 0.0

                for (key in cart.keys()) {
                    val quantity = cart.optInt(key, 0)
                    if (quantity > 0) {
                        val product = dbService.getProductById(key)
                        if (product != null) {
                            val itemLayout = LinearLayout(this@CartActivity).apply {
                                orientation = LinearLayout.HORIZONTAL
                                setPadding(32)
                                layoutParams = LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                                ).apply { setMargins(0, 0, 0, 24) }
                                gravity = Gravity.CENTER_VERTICAL
                                setBackgroundResource(R.drawable.card_background)
                            }

                            val imageView = ImageView(this@CartActivity).apply {
                                layoutParams = LinearLayout.LayoutParams(200, 200)
                                val resourceName = product.photo_ref.substringBeforeLast(".")
                                val resourceId = resources.getIdentifier(resourceName, "drawable", packageName)
                                if (resourceId != 0) setImageResource(resourceId)
                            }

                            val textLayout = LinearLayout(this@CartActivity).apply {
                                orientation = LinearLayout.VERTICAL
                                setPadding(24, 0, 0, 0)
                                layoutParams = LinearLayout.LayoutParams(
                                    0,
                                    LinearLayout.LayoutParams.WRAP_CONTENT,
                                    1f
                                )
                            }

                            val nameQuantityView = TextView(this@CartActivity).apply {
                                text = "${product.name} x$quantity"
                                textSize = 18f
                            }

                            val priceView = TextView(this@CartActivity).apply {
                                text = "${product.price * quantity} ₽"
                                textSize = 18f
                            }

                            textLayout.addView(nameQuantityView)
                            textLayout.addView(priceView)

                            val deleteButton = Button(this@CartActivity).apply {
                                text = "Удалить"
                                textSize = 14f
                                setPadding(16)
                                setOnClickListener {
                                    cart.put(product.id, 0)
                                    prefs.edit().putString("cart_items", cart.toString()).apply()
                                    refreshCart()
                                    (application as? MainActivity)?.refreshCartUI()
                                }
                            }

                            itemLayout.addView(imageView)
                            itemLayout.addView(textLayout)
                            itemLayout.addView(deleteButton)

                            cartContainer.addView(itemLayout)

                            totalSum += product.price * quantity
                        }
                    }
                }

                cartTotalView.text = "Итого: $totalSum ₽"
            }
        }


        refreshCart()


        orderButton.setOnClickListener {
            lifecycleScope.launch {
                val json = JSONChecker(dbService).getUserJson(applicationContext)
                val email = json.optString("email")
                val password = json.optString("password")

                val user = dbService.getUser(email, password)
                if (user == null) {
                    Toast.makeText(this@CartActivity, "Ошибка: пользователь не найден", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val prefs = getSharedPreferences("cart", MODE_PRIVATE)
                val cartJson = prefs.getString("cart_items", "{}")
                val cart = JSONObject(cartJson!!)
                var totalSum = 0.0
                val productsInOrder = mutableListOf<Pair<String, Int>>()

                for (key in cart.keys()) {
                    val quantity = cart.optInt(key, 0)
                    if (quantity > 0) {
                        val product = dbService.getProductById(key)
                        if (product != null) {
                            totalSum += product.price * quantity
                            productsInOrder.add(product.id to quantity)
                        }
                    }
                }

                if (totalSum <= 0.0) {
                    Toast.makeText(this@CartActivity, "Корзина пуста", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val newOrder = Order(
                    id = UUID.randomUUID().toString(),
                    created_at = LocalDateTime.now().toString(),
                    updated_at = LocalDateTime.now().toString(),
                    order_sum = totalSum,
                    state_id = "25e6e9ac-e40e-487b-98bd-abd5bd43e1d9",
                    user_id = user.id
                )

                val orderCreated = dbService.createOrder(newOrder)
                if (orderCreated == null) {
                    Toast.makeText(this@CartActivity, "Ошибка при создании заказа", Toast.LENGTH_SHORT).show()
                    return@launch
                }
                for ((productId, quantity) in productsInOrder) {
                    dbService.addProductToOrder(newOrder.id, productId, quantity)
                }

                prefs.edit().clear().apply()
                refreshCart()

                Toast.makeText(this@CartActivity, "Заказ успешно создан!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
