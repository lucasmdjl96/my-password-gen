package com.mypasswordgen.server.service

import com.mypasswordgen.common.dto.client.EmailClientDto
import com.mypasswordgen.common.dto.fullServer.FullEmailServerDto
import com.mypasswordgen.common.dto.idb.EmailIDBDto
import com.mypasswordgen.common.dto.server.EmailServerDto
import java.util.*

interface EmailService {

    fun create(emailServerDto: EmailServerDto, sessionId: UUID): EmailClientDto

    fun find(emailServerDto: EmailServerDto, sessionId: UUID): EmailClientDto

    fun delete(emailServerDto: EmailServerDto, sessionId: UUID): EmailClientDto

    fun createFullEmail(fullEmail: FullEmailServerDto, userId: UUID): EmailIDBDto

}
