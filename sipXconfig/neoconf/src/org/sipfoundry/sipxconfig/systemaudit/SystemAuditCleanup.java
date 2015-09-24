/**
 *
 *
 * Copyright (c) 2015 eZuce Corp. All rights reserved.
 * Contributed to sipXcom under a Contributor Agreement
 *
 * This software is free software; you can redistribute it and/or modify it under
 * the terms of the Affero General Public License (AGPL) as published by the
 * Free Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 */
package org.sipfoundry.sipxconfig.systemaudit;

import java.util.Calendar;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.sipfoundry.sipxconfig.admin.AdminContext;
import org.sipfoundry.sipxconfig.admin.AdminSettings;
import org.sipfoundry.sipxconfig.elasticsearch.ElasticsearchService;
import org.springframework.beans.factory.annotation.Required;

public class SystemAuditCleanup {

    private static final Log LOG = LogFactory.getLog(SystemAuditCleanup.class);

    private ElasticsearchService m_elasticsearchService;
    private AdminContext m_adminContext;

    public void run() {
        LOG.warn("Starting System Audit cleanup");
        try {
            BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
            RangeQueryBuilder dateQuery = QueryBuilders
                    .rangeQuery(ConfigChange.DATE_TIME);
            dateQuery.to(getDeleteDate().getTime());
            queryBuilder.must(dateQuery);
            m_elasticsearchService.deleteDocs(ConfigChangeContext.SYSTEM_AUDIT,
                    queryBuilder);
        } catch (Exception e) {
            LOG.error("Error while doing System Audit cleanup: ", e);
        }
    }

    private Date getDeleteDate() {
        AdminSettings adminSettings = m_adminContext.getSettings();
        int daysToKeep = adminSettings.getSystemAuditKeepChanges();
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -daysToKeep);
        return calendar.getTime();
    }

    @Required
    public void setElasticsearchService(ElasticsearchService elasticsearchService) {
        m_elasticsearchService = elasticsearchService;
    }

    @Required
    public void setAdminContext(AdminContext adminContext) {
        m_adminContext = adminContext;
    }
}