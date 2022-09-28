package com.lucasmdjl.application.mapper.impl

import com.lucasmdjl.application.mapper.EmailMapper
import com.lucasmdjl.application.mapper.SiteMapper
import com.lucasmdjl.application.model.Email
import dto.EmailDto
import org.jetbrains.exposed.dao.load
import org.jetbrains.exposed.sql.transactions.transaction

object EmailMapperImpl : EmailMapper {

    private val siteMapper: SiteMapper = SiteMapperImpl

    override fun emailToEmailDto(email: Email): EmailDto = transaction {
        email.load(Email::sites)
        EmailDto(
            email.emailAddress,
            siteMapper.siteIterableToSiteDtoIterable(email.sites)?.toMutableList() ?: mutableListOf()
        )
    }

    override fun emailIterableToEmailDtoIterable(emailList: Iterable<Email>?): Iterable<EmailDto>? {
        return emailList?.map(EmailMapperImpl::emailToEmailDto)
    }

}