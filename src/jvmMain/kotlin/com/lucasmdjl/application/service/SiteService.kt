package com.lucasmdjl.application.service

import com.lucasmdjl.application.model.Email
import com.lucasmdjl.application.model.Site

interface SiteService {

    fun addSiteToEmail(siteName: String, email: Email): Site?

    fun getSiteFromEmail(siteName: String, email: Email): Site?

    fun removeSiteFromEmail(siteName: String, email: Email): Unit?

}