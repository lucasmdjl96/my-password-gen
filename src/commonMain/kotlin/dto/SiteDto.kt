package dto

import kotlinx.serialization.Serializable

@Serializable
class SiteDto(override val name: String): Named {

    companion object {
        fun findByName(name: String): SiteDto? = siteDtos.find { it.name == name }
    }

}