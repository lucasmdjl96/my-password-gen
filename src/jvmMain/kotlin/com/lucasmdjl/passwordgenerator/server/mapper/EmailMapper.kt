package com.lucasmdjl.passwordgenerator.server.mapper

import com.lucasmdjl.passwordgenerator.common.dto.EmailDto
import com.lucasmdjl.passwordgenerator.server.model.Email

interface EmailMapper {

    fun emailToEmailDto(email: Email): EmailDto

    fun emailIterableToEmailDtoIterable(emailList: Iterable<Email>?): Iterable<EmailDto>?

}
