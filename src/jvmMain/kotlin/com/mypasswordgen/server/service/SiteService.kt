package com.mypasswordgen.server.service

import com.mypasswordgen.common.dto.client.SiteClientDto
import com.mypasswordgen.common.dto.server.SiteServerDto
import java.util.*

interface SiteService {

    fun create(siteServerDto: SiteServerDto, sessionId: UUID): SiteClientDto

    fun find(siteServerDto: SiteServerDto, sessionId: UUID): SiteClientDto

    fun delete(siteServerDto: SiteServerDto, sessionId: UUID): SiteClientDto

}
