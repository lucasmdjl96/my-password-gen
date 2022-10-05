package com.lucasmdjl.passwordgenerator.server.service

import com.lucasmdjl.passwordgenerator.server.model.Site
import java.util.*

interface SiteService {

    fun create(siteName: String, emailAddress: String, username: String, sessionId: UUID): Site?

    fun find(siteName: String, emailAddress: String, username: String, sessionId: UUID): Site?

    fun delete(siteName: String, emailAddress: String, username: String, sessionId: UUID): Unit?

}
