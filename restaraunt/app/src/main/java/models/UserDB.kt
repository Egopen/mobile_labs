package models

import kotlinx.serialization.Serializable

@Serializable
data class UserDB(
    val id: String,
    val email: String,
    val password: String,
    val name: String
)