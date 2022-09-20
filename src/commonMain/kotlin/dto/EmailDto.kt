package dto

import kotlinx.serialization.Serializable

@Serializable
class EmailDto(override val name: String, val siteDtoList: MutableList<SiteDto>): Named {

    fun findPage(name: String): SiteDto? = siteDtoList.find { it.name == name }

    companion object {
        fun getByEmail(email: String): EmailDto? = emailDtos.find { it.name == email }
    }
}