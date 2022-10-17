package com.lucasmdjl.passwordgenerator.server.service

import com.lucasmdjl.passwordgenerator.common.dto.server.SiteServerDto
import com.lucasmdjl.passwordgenerator.server.model.Site
import java.util.*

interface SiteService {

    fun create(siteServerDto: SiteServerDto, sessionId: UUID): Site

    fun find(siteServerDto: SiteServerDto, sessionId: UUID): Site

    fun delete(siteServerDto: SiteServerDto, sessionId: UUID)

}
