package com.mypasswordgen.common.dto.client

import kotlinx.serialization.Serializable

@Serializable
class UserClientDto(val id: String, val emailIdList: List<String>)
