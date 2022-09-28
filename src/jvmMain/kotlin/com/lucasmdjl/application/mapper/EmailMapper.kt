package com.lucasmdjl.application.mapper

import com.lucasmdjl.application.model.Email
import dto.EmailDto

interface EmailMapper {

    fun emailToEmailDto(email: Email): EmailDto

    fun emailIterableToEmailDtoIterable(emailList: Iterable<Email>?): Iterable<EmailDto>?

}