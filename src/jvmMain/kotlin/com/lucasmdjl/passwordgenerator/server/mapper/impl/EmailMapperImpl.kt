package com.lucasmdjl.passwordgenerator.server.mapper.impl

import com.lucasmdjl.passwordgenerator.common.dto.client.EmailClientDto
import com.lucasmdjl.passwordgenerator.server.mapper.EmailMapper
import com.lucasmdjl.passwordgenerator.server.model.Email
import mu.KotlinLogging
import org.jetbrains.exposed.dao.load
import org.jetbrains.exposed.sql.transactions.transaction

private val logger = KotlinLogging.logger("EmailMapperImpl")

class EmailMapperImpl : EmailMapper {

    override fun emailToEmailClientDto(email: Email): EmailClientDto = transaction {
        logger.debug { "emailToEmailClientDto" }
        email.load(Email::sites)
        EmailClientDto(
            email.id.value.toString(),
            email.sites.map { site -> site.id.value.toString() }
        )
    }

    override fun loadSiteIdsFrom(email: Email): MutableList<String> = transaction {
        email.load(Email::sites)
        email.sites.map { site -> site.id.value.toString() }.toMutableList()
    }

}
