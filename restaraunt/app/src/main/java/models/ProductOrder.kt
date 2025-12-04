package models

import kotlinx.serialization.Serializable

@Serializable
data class ProductOrder(
    val id: String,
    val amount: Int,
    val product_id: String,
    val order_id: String
)