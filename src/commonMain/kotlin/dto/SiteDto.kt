package dto

import kotlinx.serialization.Serializable

@Serializable
class SiteDto(override val name: String) : Named