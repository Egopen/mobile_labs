import kotlinx.serialization.Serializable

@Serializable
data class Product(
    val id: String,
    val photo_ref: String,
    val name: String,
    val description: String,
    val category_id: String,
    val price: Long
)