package com.lucasmdjl.application.mapper.impl

import com.lucasmdjl.application.mapper.EmailMapper
import com.lucasmdjl.application.model.Email
import com.lucasmdjl.application.model.Site
import dto.EmailDto
import mu.KotlinLogging
import org.jetbrains.exposed.dao.load
import org.jetbrains.exposed.sql.transactions.transaction

private val logger = KotlinLogging.logger("EmailMapperImpl")

object EmailMapperImpl : EmailMapper {

    override fun emailToEmailDto(email: Email): EmailDto = transaction {
        logger.debug { "emailToEmailDto call with email: $email" }
        email.load(Email::sites)
        EmailDto(
            email.emailAddress,
            email.sites.map(Site::name).toMutableList()
        )
    }

    override fun emailIterableToEmailDtoIterable(emailList: Iterable<Email>?): Iterable<EmailDto>? {
        logger.debug { "emailIterableToEmailDtoIterable call with emailList: $emailList" }
        return emailList?.map(EmailMapperImpl::emailToEmailDto)
    }

}