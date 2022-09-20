package dto

import kotlinx.serialization.Serializable

@Serializable
data class LoginDto(val username: String, val password: String)