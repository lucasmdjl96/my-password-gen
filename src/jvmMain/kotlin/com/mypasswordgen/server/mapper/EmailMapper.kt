package com.mypasswordgen.server.mapper

import com.mypasswordgen.common.dto.client.EmailClientDto
import com.mypasswordgen.server.model.Email

interface EmailMapper {

    fun emailToEmailClientDto(email: Email): EmailClientDto

    fun Email.toEmailClientDto() = emailToEmailClientDto(this)

}
