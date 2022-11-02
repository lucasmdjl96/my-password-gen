package com.lucasmdjl.passwordgenerator.server.service

import com.lucasmdjl.passwordgenerator.common.dto.client.SiteClientDto
import com.lucasmdjl.passwordgenerator.common.dto.server.SiteServerDto
import java.util.*

interface SiteService {

    fun create(siteServerDto: SiteServerDto, sessionId: UUID): SiteClientDto

    fun find(siteServerDto: SiteServerDto, sessionId: UUID): SiteClientDto

    fun delete(siteServerDto: SiteServerDto, sessionId: UUID): SiteClientDto

}
