import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
data class Order(
    val id: String,
    val created_at: String,
    val updated_at: String,
    val order_sum: Double,
    val state_id: String,
    val user_id: String
)