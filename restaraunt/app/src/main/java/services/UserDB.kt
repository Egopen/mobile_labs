package services

import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class UserDB(
    val id: String,
    val email: String,
    val password: String,
    val name: String
)