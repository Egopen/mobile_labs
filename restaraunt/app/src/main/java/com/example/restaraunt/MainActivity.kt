package com.example.restaraunt

import Category
import Product
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import services.JSONChecker

class MainActivity : AppCompatActivity() {

    private val dbService = DBService()
    private lateinit var categorySpinner: Spinner
    private lateinit var container: LinearLayout
    private lateinit var searchEditText: EditText
    private var allCategories = listOf<Category>()
    private var allProducts = listOf<Product>()

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            val checker = JSONChecker(dbService)
            val authorized = checker.isUserAuthorized(applicationContext)
            val cartIcon = findViewById<ImageView>(R.id.cartIcon)
            cartIcon.visibility = if (authorized) android.view.View.VISIBLE else android.view.View.GONE
            loadProducts()
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    101
                )
            }
        }
        val intent = Intent(this, OrderStatusService::class.java)
        ContextCompat.startForegroundService(this, intent)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        container = findViewById(R.id.productContainer)
        categorySpinner = findViewById(R.id.categorySpinner)
        searchEditText = findViewById(R.id.searchEditText)

        val cartIcon = findViewById<ImageView>(R.id.cartIcon)
        val profileIcon = findViewById<ImageView>(R.id.profileIcon)
        cartIcon.setOnClickListener {
            startActivity(Intent(this, CartActivity::class.java))
        }

        profileIcon.setOnClickListener {
            startActivity(Intent(this, UserActivity::class.java))
        }
        lifecycleScope.launch {
            val checker = JSONChecker(dbService)
            val authorized = checker.isUserAuthorized(applicationContext)
            if (!authorized) {
                cartIcon.visibility = android.view.View.GONE
            }
        }
        setupSearch()
        lifecycleScope.launch {
            loadCategories()
            loadProducts()
        }
    }

    private fun setupSearch() {
        searchEditText.setOnEditorActionListener { _, _, _ ->
            performSearch()
            true
        }

        searchEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                performSearch()
            }
        }
    }

    private fun performSearch() {
        val query = searchEditText.text.toString().trim().lowercase()
        val position = categorySpinner.selectedItemPosition

        val categoryFilteredProducts = if (position == 0) {
            allProducts
        } else {
            val selectedCategory = allCategories[position - 1]
            allProducts.filter { it.category_id == selectedCategory.id }
        }

        val finalFilteredProducts = if (query.isEmpty()) {
            categoryFilteredProducts
        } else {
            categoryFilteredProducts.filter {
                it.name.lowercase().contains(query)
            }
        }

        displayProducts(finalFilteredProducts)
    }

    private suspend fun loadCategories() {
        allCategories = dbService.getCategories()
        val categoryNames = allCategories.map { it.name }.toMutableList()
        categoryNames.addFirst("Все")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categoryNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = adapter

        categorySpinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                performSearch()
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {
                performSearch()
            }
        }
    }

    private suspend fun loadProducts() {
        allProducts = dbService.getProducts()
        performSearch()
    }

    private suspend fun loadProductsByCategory(categoryId: String) {
        allProducts = dbService.getProductsByCategory(categoryId)
        performSearch()
    }
    fun refreshCartUI() {
        val prefs = getSharedPreferences("cart", MODE_PRIVATE)
        val cartJson = prefs.getString("cart_items", "{}")
        val cart = JSONObject(cartJson!!)

        for (i in 0 until container.childCount) {
            val itemLayout = container.getChildAt(i) as LinearLayout
            val controlsLayout = itemLayout.getChildAt(3) as LinearLayout
            val quantityView = controlsLayout.getChildAt(1) as TextView
            quantityView.text = "0"
        }
    }
    private fun displayProducts(products: List<Product>) {
        container.removeAllViews()

        val prefs = getSharedPreferences("cart", MODE_PRIVATE)
        val cartJson = prefs.getString("cart_items", "{}")
        val cart = JSONObject(cartJson!!)

        for (product in products) {
            val itemLayout = LinearLayout(this@MainActivity).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(24, 24, 24, 24)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { setMargins(0, 0, 0, 24) }
                setBackgroundResource(R.drawable.card_background)
            }

            val imageView = ImageView(this@MainActivity).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    400
                )
                scaleType = ImageView.ScaleType.CENTER_CROP
                val resourceName = product.photo_ref.substringBeforeLast(".")
                val resourceId = resources.getIdentifier(resourceName, "drawable", packageName)
                if (resourceId != 0) setImageResource(resourceId)
            }

            val nameView = TextView(this@MainActivity).apply {
                text = product.name
                textSize = 18f
                setTypeface(typeface, android.graphics.Typeface.BOLD)
                setPadding(0, 8, 0, 4)
            }

            val priceView = TextView(this@MainActivity).apply {
                text = "${product.price} ₽"
                textSize = 16f
                setPadding(0, 0, 0, 8)
            }

            val controlsLayout = LinearLayout(this@MainActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = android.view.Gravity.CENTER_VERTICAL
            }

            val minusButton = Button(this@MainActivity).apply {
                text = "-"
                textSize = 14f
                setBackgroundResource(R.drawable.round_button_small)
                layoutParams = LinearLayout.LayoutParams(100, 100)
            }

            val quantityView = TextView(this@MainActivity).apply {
                val current = cart.optInt(product.id, 0)
                text = current.toString()
                textSize = 18f
                setPadding(16, 0, 16, 0)
                gravity = android.view.Gravity.CENTER
            }


            val plusButton = Button(this@MainActivity).apply {
                text = "+"
                textSize = 14f
                setBackgroundResource(R.drawable.round_button_small)
                layoutParams = LinearLayout.LayoutParams(100, 100)
            }

            var quantity = cart.optInt(product.id, 0)
            minusButton.setOnClickListener {
                if (quantity > 0) {
                    quantity--
                    quantityView.text = quantity.toString()
                    cart.put(product.id, quantity)
                    prefs.edit().putString("cart_items", cart.toString()).apply()
                }
            }

            plusButton.setOnClickListener {
                quantity++
                quantityView.text = quantity.toString()
                cart.put(product.id, quantity)
                prefs.edit().putString("cart_items", cart.toString()).apply()
            }

            controlsLayout.addView(minusButton)
            controlsLayout.addView(quantityView)
            controlsLayout.addView(plusButton)

            itemLayout.addView(imageView)
            itemLayout.addView(nameView)
            itemLayout.addView(priceView)
            itemLayout.addView(controlsLayout)

            container.addView(itemLayout)
        }

    }
}
