package com.lucasmdjl.application.service

import dto.EmailDto
import dto.SiteDto

interface SiteService {

    fun addSiteToEmail(siteName: String, emailAddress: String): EmailDto

    fun getSiteFromEmail(siteName: String, emailAddress: String): SiteDto?

}