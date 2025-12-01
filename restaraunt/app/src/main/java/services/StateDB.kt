import kotlinx.serialization.Serializable

@Serializable
data class StateDB(
    val id: String,
    val name: String
)