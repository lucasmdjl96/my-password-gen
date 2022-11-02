package com.lucasmdjl.passwordgenerator.server.mapper.impl

import com.lucasmdjl.passwordgenerator.common.dto.client.EmailClientDto
import com.lucasmdjl.passwordgenerator.server.mapper.EmailMapper
import com.lucasmdjl.passwordgenerator.server.model.Email
import com.lucasmdjl.passwordgenerator.server.model.Site
import mu.KotlinLogging
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.load
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

private val logger = KotlinLogging.logger("EmailMapperImpl")

class EmailMapperImpl : EmailMapper {

    override fun emailToEmailClientDto(email: Email): EmailClientDto = transaction {
        logger.debug { "emailToEmailClientDto" }
        email.load(Email::sites)
        EmailClientDto(
            email.emailAddress,
            email.sites.map { site -> site.id.value.toString() }.toMutableList()
        )
    }

    override fun loadSiteIdsFrom(email: Email): MutableList<String> = transaction {
        email.load(Email::sites)
        email.sites.map { site -> site.id.value.toString() }.toMutableList()
    }

}
