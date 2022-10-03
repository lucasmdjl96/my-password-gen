package com.lucasmdjl.passwordgenerator.server.service

import com.lucasmdjl.passwordgenerator.server.model.Email
import com.lucasmdjl.passwordgenerator.server.model.Site

interface SiteService {

    fun addSiteToEmail(siteName: String, email: Email): Site?

    fun getSiteFromEmail(siteName: String, email: Email): Site?

    fun removeSiteFromEmail(siteName: String, email: Email): Unit?

}