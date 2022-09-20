package dto

import kotlinx.serialization.Serializable

@Serializable
class UserDto(override val name: String, val emailDtoList: MutableList<EmailDto>): Named {

    fun findEmail(email: String): EmailDto? = emailDtoList.find{ it.name == email }

    companion object {
        fun getByName(name: String): UserDto? = users.find { it.name == name }
    }
}





