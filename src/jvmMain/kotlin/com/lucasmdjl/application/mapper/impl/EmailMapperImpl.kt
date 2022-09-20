package com.lucasmdjl.application.mapper.impl

import com.lucasmdjl.application.mapper.EmailMapper
import com.lucasmdjl.application.mapper.SiteMapper
import com.lucasmdjl.application.model.Email
import dto.EmailDto

object EmailMapperImpl : EmailMapper {

    private val siteMapper: SiteMapper = SiteMapperImpl

    override fun emailToEmailDto(email: Email): EmailDto {
        return EmailDto(email.emailAddress, siteMapper.siteListToSiteDtoList(email.sites)?.toMutableList() ?: mutableListOf())
    }

    override fun emailListToEmailDtoList(emailList: Iterable<Email>?): Iterable<EmailDto>? {
        return emailList?.map(EmailMapperImpl::emailToEmailDto)
    }

}