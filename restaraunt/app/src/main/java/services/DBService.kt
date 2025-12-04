package services

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.serializer.KotlinXSerializer
import kotlinx.serialization.json.Json
import models.Category
import models.Order
import models.Product
import models.ProductOrder
import models.StateDB
import models.UserDB
import java.util.UUID

class DBService {

    private val supabase = createSupabaseClient(
        supabaseUrl = "https://jqdlkmvhtwwuaxhxuznx.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImpxZGxrbXZodHd3dWF4aHh1em54Iiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTc2NDI0MTc4NSwiZXhwIjoyMDc5ODE3Nzg1fQ.tPglXqvr61BCflB9z-OING_YHkIAwMhjVSy_YqcRsTQ"
    ) {
        install(Postgrest.Companion)
        defaultSerializer = KotlinXSerializer(
            Json {
                ignoreUnknownKeys = true
                isLenient = true
            }
        )
    }

    // -------------------------------------------------- ORDERS --------------------------------------------------

    suspend fun getOrders() =
        supabase.from("orders").select().decodeList<Order>()

    suspend fun getOrderById(id: String) =
        supabase.from("orders").select {
            filter { Order::id eq id }
        }.decodeSingle<Order>()

    suspend fun createOrder(order: Order) =
        supabase.from("orders").insert(order)

    suspend fun updateOrder(order: Order) =
        supabase.from("orders").update(order) {
            filter { Order::id eq order.id }
        }

    suspend fun deleteOrder(id: String) =
        supabase.from("orders").delete {
            filter { Order::id eq id }
        }


    // -------------------------------------------------- PRODUCTS --------------------------------------------------

    suspend fun getProducts() =
        supabase.from("products").select().decodeList<Product>()

    suspend fun getProductById(id: String) =
        supabase.from("products").select {
            filter { Product::id eq id }
        }.decodeSingle<Product>()

    suspend fun getProductsByCategory(categoryId: String) =
        supabase.from("products").select {
            filter { Product::category_id eq categoryId }
        }.decodeList<Product>()

    suspend fun createProduct(product: Product) =
        supabase.from("products").insert(product)

    suspend fun updateProduct(product: Product) =
        supabase.from("products").update(product) {
            filter { Product::id eq product.id }
        }

    suspend fun deleteProduct(id: String) =
        supabase.from("products").delete {
            filter { Product::id eq id }
        }


    // -------------------------------------------------- CATEGORIES --------------------------------------------------

    suspend fun getCategories() =
        supabase.from("categories").select().decodeList<Category>()

    suspend fun getCategoryById(id: String) =
        supabase.from("categories").select {
            filter { Category::id eq id }
        }.decodeSingle<Category>()

    suspend fun createCategory(category: Category) =
        supabase.from("categories").insert(category)

    suspend fun updateCategory(category: Category) =
        supabase.from("categories").update(category) {
            filter { Category::id eq category.id }
        }

    suspend fun deleteCategory(id: String) =
        supabase.from("categories").delete {
            filter { Category::id eq id }
        }


    // -------------------------------------------------- STATES --------------------------------------------------

    suspend fun getStates() =
        supabase.from("states").select().decodeList<StateDB>()

    suspend fun getStateById(id: String) =
        supabase.from("states").select {
            filter { StateDB::id eq id }
        }.decodeSingle<StateDB>()

    suspend fun createState(state: StateDB) =
        supabase.from("states").insert(state)

    suspend fun updateState(state: StateDB) =
        supabase.from("states").update(state) {
            filter { StateDB::id eq state.id }
        }

    suspend fun deleteState(id: String) =
        supabase.from("states").delete {
            filter { StateDB::id eq id }
        }


    // ---------------------------------------------- PRODUCT_ORDERS ----------------------------------------------

    suspend fun getProductOrders() =
        supabase.from("products_orders").select().decodeList<ProductOrder>()

    suspend fun getProductOrdersByOrder(orderId: String) =
        supabase.from("products_orders").select {
            filter { ProductOrder::order_id eq orderId }
        }.decodeList<ProductOrder>()

    suspend fun getProductOrdersByProduct(productId: String) =
        supabase.from("products_orders").select {
            filter { ProductOrder::product_id eq productId }
        }.decodeList<ProductOrder>()

    suspend fun createProductOrder(productOrder: ProductOrder) =
        supabase.from("products_orders").insert(productOrder)

    suspend fun updateProductOrder(productOrder: ProductOrder) =
        supabase.from("products_orders").update(productOrder) {
            filter { ProductOrder::id eq productOrder.id }
        }

    suspend fun deleteProductOrder(id: String) =
        supabase.from("products_orders").delete {
            filter { ProductOrder::id eq id }
        }
    suspend fun addProductToOrder(orderId: String, productId: String, quantity: Int) {
        val productOrder = ProductOrder(
            id = UUID.randomUUID().toString(),
            amount = quantity,
            product_id = productId,
            order_id = orderId
        )

        supabase.from("products_orders").insert(productOrder)
    }

    suspend fun getUserOrderHistory(email: String, password: String): List<Triple<Order, String, List<Pair<Product, Int>>>> {
        val users = supabase.from("users").select {
            filter { UserDB::email eq email }
        }.decodeList<UserDB>()
        if (users.isEmpty()) return emptyList()
        val user = users.first { it.password == password }

        val orders = supabase.from("orders").select {
            filter { Order::user_id eq user.id }
            order("created_at", io.github.jan.supabase.postgrest.query.Order.DESCENDING)
        }.decodeList<Order>()

        val result = mutableListOf<Triple<Order, String, List<Pair<Product, Int>>>>()

        for (order in orders) {
            val status = supabase.from("states").select {
                filter { StateDB::id eq order.state_id }
            }.decodeSingle<StateDB>()?.name ?: "Неизвестно"


            val productOrders = supabase.from("products_orders").select {
                filter { ProductOrder::order_id eq order.id }
            }.decodeList<ProductOrder>()

            val productsWithAmount = productOrders.mapNotNull { po ->
                val product = supabase.from("products").select {
                    filter { Product::id eq po.product_id }
                }.decodeSingle<Product>()
                product?.let { it to po.amount }
            }

            result.add(Triple(order, status, productsWithAmount))
        }

        return result
    }




    // -------------------------------------------------- USERS --------------------------------------------------

    suspend fun getUsers() =
        supabase.from("users").select().decodeList<UserDB>()

    suspend fun getUserById(id: String) =
        supabase.from("users").select {
            filter { UserDB::id eq id }
        }.decodeSingle<UserDB>()

    suspend fun createUser(user: UserDB) =
        supabase.from("users").insert(user)

    suspend fun updateUser(user: UserDB) =
        supabase.from("users").update(user) {
            filter { UserDB::id eq user.id }
        }
    suspend fun checkUser(email: String, password: String): Boolean {

        val users = supabase.from("users").select {
            filter { UserDB::email eq email }
        }.decodeList<UserDB>()
        if (users.isEmpty()) return false
        val user = users.first()
        return user.password == password
    }
    suspend fun getUser(email: String, password: String): UserDB? {
        val users = supabase.from("users").select {
            filter { UserDB::email eq email }
        }.decodeList<UserDB>()
        if (users.isEmpty()) return null
        val user = users.first { userDB -> userDB.password == password }
        return user
    }


    suspend fun registerUser(name: String, email: String, password: String): UserDB? {
        val existingUsers = supabase.from("users").select {
            filter { UserDB::email eq email }
        }.decodeList<UserDB>()

        if (existingUsers.isNotEmpty()) return null

        val newId = UUID.randomUUID().toString()

        supabase.from("users").insert(
            UserDB(
                id = newId,
                name = name,
                email = email,
                password = password
            )
        )

        val insertedUser = supabase.from("users").select {
            filter { UserDB::id eq newId }
        }.decodeList<UserDB>().firstOrNull()

        return insertedUser
    }

    suspend fun deleteUser(id: String) =
        supabase.from("users").delete {
            filter { UserDB::id eq id }
        }
}