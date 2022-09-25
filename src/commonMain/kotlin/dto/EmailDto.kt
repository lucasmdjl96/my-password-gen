package dto

import kotlinx.serialization.Serializable

@Serializable
class EmailDto(override val name: String, val siteDtoList: MutableList<SiteDto>) : Named {
    fun hasSite(name: String): Boolean = siteDtoList.find { it.name == name } != null

    fun addSite(siteDto: SiteDto) = siteDtoList.add(siteDto)

}