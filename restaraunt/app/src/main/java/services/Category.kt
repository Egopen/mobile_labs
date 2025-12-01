import kotlinx.serialization.Serializable

@Serializable
data class Category(
    val id: String,
    val name: String
)