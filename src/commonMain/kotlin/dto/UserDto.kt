package dto

import kotlinx.serialization.Serializable

@Serializable
class UserDto(override val name: String, val emailDtoList: MutableList<EmailDto>) : Named {

    fun hasEmail(emailAddress: String): Boolean = emailDtoList.find { it.name == emailAddress } != null

    fun addEmail(emailDto: EmailDto) = emailDtoList.add(emailDto)
}





