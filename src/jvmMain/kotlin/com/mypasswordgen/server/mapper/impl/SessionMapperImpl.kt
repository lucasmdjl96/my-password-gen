package com.mypasswordgen.server.mapper.impl


import com.mypasswordgen.common.dto.FullSessionClientDto
import com.mypasswordgen.server.dto.SessionDto
import com.mypasswordgen.server.mapper.SessionMapper
import com.mypasswordgen.server.mapper.UserMapper
import com.mypasswordgen.server.model.Session
import mu.KotlinLogging
import org.jetbrains.exposed.sql.transactions.transaction

private val logger = KotlinLogging.logger("SessionMapperImpl")

class SessionMapperImpl(private val userMapper: UserMapper) : SessionMapper {

    override fun sessionToSessionDto(session: Session): SessionDto {
        logger.debug { "sessionToSessionDto" }
        return SessionDto(session.id.value)
    }

    override fun sessionToFullSessionClientDto(session: Session) = transaction {
        logger.debug { "sessionToFullSessionClientDto" }
        FullSessionClientDto {
            session.users.forEach { user ->
                with(userMapper) {
                    +user.toFullUserClientDto()
                }
            }
        }
    }

}
