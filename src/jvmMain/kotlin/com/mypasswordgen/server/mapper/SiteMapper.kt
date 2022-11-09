package com.mypasswordgen.server.mapper

import com.mypasswordgen.common.dto.client.SiteClientDto
import com.mypasswordgen.server.model.Site

interface SiteMapper {

    fun siteToSiteClientDto(site: Site): SiteClientDto

    fun Site.toSiteClientDto() = siteToSiteClientDto(this)

}
