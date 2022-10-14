package com.lucasmdjl.passwordgenerator.server.mapper.impl

import com.lucasmdjl.passwordgenerator.common.dto.client.EmailClientDto
import com.lucasmdjl.passwordgenerator.server.mapper.EmailMapper
import com.lucasmdjl.passwordgenerator.server.model.Email
import com.lucasmdjl.passwordgenerator.server.model.Site
import mu.KotlinLogging
import org.jetbrains.exposed.dao.load
import org.jetbrains.exposed.sql.transactions.transaction

private val logger = KotlinLogging.logger("EmailMapperImpl")

class EmailMapperImpl : EmailMapper {

    override fun emailToEmailClientDto(email: Email): EmailClientDto = transaction {
        logger.debug { "emailToEmailClientDto" }
        email.load(Email::sites)
        EmailClientDto(
            email.emailAddress,
            email.sites.map(Site::name).toMutableList()
        )
    }

}
