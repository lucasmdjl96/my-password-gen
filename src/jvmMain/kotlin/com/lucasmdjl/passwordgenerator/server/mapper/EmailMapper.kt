package com.lucasmdjl.passwordgenerator.server.mapper

import com.lucasmdjl.passwordgenerator.common.dto.client.EmailClientDto
import com.lucasmdjl.passwordgenerator.server.model.Email

interface EmailMapper {

    fun emailToEmailClientDto(email: Email): EmailClientDto

    fun Email.toEmailClientDto() = emailToEmailClientDto(this)

    fun emailIterableToEmailClientDtoIterable(emailIterable: Iterable<Email>): Iterable<EmailClientDto>

    fun Iterable<Email>.toEmailClientDtoIterable() = emailIterableToEmailClientDtoIterable(this)

}
