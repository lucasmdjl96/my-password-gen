package com.mypasswordgen.server.mapper.impl

import com.mypasswordgen.common.dto.client.EmailClientDto
import com.mypasswordgen.server.mapper.EmailMapper
import com.mypasswordgen.server.model.Email
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

}
