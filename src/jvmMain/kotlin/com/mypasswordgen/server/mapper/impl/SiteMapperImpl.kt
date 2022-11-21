package com.mypasswordgen.server.mapper.impl

import com.mypasswordgen.common.dto.client.SiteClientDto
import com.mypasswordgen.common.dto.fullClient.FullSiteClientDto
import com.mypasswordgen.server.mapper.SiteMapper
import com.mypasswordgen.server.model.Site
import mu.KotlinLogging
import org.jetbrains.exposed.sql.transactions.transaction

private val logger = KotlinLogging.logger("SiteMapperImpl")

class SiteMapperImpl : SiteMapper {

    override fun siteToSiteClientDto(site: Site): SiteClientDto {
        logger.debug { "siteToSiteClientDto" }
        return SiteClientDto(site.id.value.toString())
    }

    override fun siteToFullSiteClientDto(site: Site) = transaction {
        logger.debug { "siteToFullSiteClientDto" }
        FullSiteClientDto(site.id.value.toString())
    }

}
