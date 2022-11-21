package com.mypasswordgen.server.mapper

import com.mypasswordgen.common.dto.client.SiteClientDto
import com.mypasswordgen.common.dto.fullClient.FullSiteClientDto
import com.mypasswordgen.server.model.Site

interface SiteMapper {

    fun siteToSiteClientDto(site: Site): SiteClientDto

    fun Site.toSiteClientDto() = siteToSiteClientDto(this)

    fun siteToFullSiteClientDto(site: Site): FullSiteClientDto

    fun Site.toFullSiteClientDto() = siteToFullSiteClientDto(this)

}
