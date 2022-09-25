package com.lucasmdjl.application.service

import dto.EmailDto
import dto.SiteDto
import java.util.*

interface SiteService {

    fun addSiteToEmail(siteName: String, emailAddress: String, username: String, sessionId: UUID): EmailDto

    fun getSiteFromEmail(siteName: String, emailAddress: String, username: String, sessionId: UUID): SiteDto?

    fun removeSiteFromEmail(siteName: String, emailAddress: String, username: String, sessionId: UUID): EmailDto

}