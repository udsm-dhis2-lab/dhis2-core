/*
 * Copyright (c) 2004-2022, University of Oslo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the HISP project nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.hisp.dhis.webapi.controller.tracker.export;

import static org.hisp.dhis.webapi.controller.tracker.TrackerControllerSupport.RESOURCE_PATH;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.ConcurrentUpdateHttp2SolrClient;
import org.apache.solr.client.solrj.impl.Http2SolrClient;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;
import org.hisp.dhis.common.DhisApiVersion;
import org.hisp.dhis.external.conf.ConfigurationKey;
import org.hisp.dhis.external.conf.DhisConfigurationProvider;
import org.hisp.dhis.webapi.mvc.annotation.ApiVersion;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * This endpoint implements a naive way of indexing all TEIs TEA for firstname
 * and lastname in Apache Solr. Index (assuming SL demo DB) using for example
 *
 * curl -u admin:district -i http://localhost:8080/api/tracker/solr
 *
 * Indexing is idempotent as the TEI uid is used as the unique identifier of the
 * indexed documents.
 *
 * This pull based approach is what the Apache Data Import Handler (DIH) does
 * which was included prior to Apache 9.
 * https://cwiki.apache.org/confluence/display/SOLR/DataImportHandler The DIH
 * first did a full import and then delta imports using dedicated SQL queries.
 * It can also denormalize SQL tables. DIH has been removed in Solr 9 and is now
 * un-maintained by the community. DIH does not currently work with Solr 9. See
 * https://pureinsights.com/blog/2022/apache-solr-removing-data-import-handler/
 * for some more details and alternatives. So we can for example implement our
 * own tracker specific version of the DIH which first does a full import
 * followed by regular delta imports. These could run as jobs, so they only run
 * on the primary DHIS2 instance in case we run in a cluster. We might get away
 * with a relaxed time between imports depending on the requirements. What is
 * the expected time between create TEI to search TEI? Days? Or do users search
 * for a TEI right after creating it? The latter might be solvable with a cache
 * client side or falling back to fetching TEIs from the DB as before.
 *
 * Another alternative is to use CDC with Debezium which is a push based
 * approach (unless it's not technically implemented as such in Postgres). This
 * approach comes with lots of complexities which we might want to avoid at
 * first to validate the overall approach to deduplication on TEI creation.
 */
@Slf4j
@RestController
@RequestMapping( value = RESOURCE_PATH + "/solr" )
@ApiVersion( { DhisApiVersion.DEFAULT, DhisApiVersion.ALL } )
@RequiredArgsConstructor
public class TrackerSolrExportController
{

    public static final String SOLR_COLLECTION_TRACKER = "tracker";

    private final DhisConfigurationProvider dhisConfig;

    private final JdbcTemplate jdbcTemplate;

    @GetMapping( )
    void export()
        throws SolrServerException,
        IOException
    {
        String sql = "select tei.uid as trackedEntity, tet.uid as trackedEntityType, w75KJ2mc4zz.value as w75KJ2mc4zz, zDhUuAYrxNC.value as zDhUuAYrxNC from trackedentityinstance tei\n"
            +
            "\tjoin trackedentitytype tet on tei.trackedentitytypeid = tet.trackedentitytypeid\n" +
            "\tjoin trackedentityattributevalue w75KJ2mc4zz on w75KJ2mc4zz.trackedentityinstanceid = tei.trackedentityinstanceid\n"
            +
            "\tjoin trackedentityattribute w75KJ2mc4zz_att on w75KJ2mc4zz_att.trackedentityattributeid = w75KJ2mc4zz.trackedentityattributeid\n"
            +
            "\t\tand w75KJ2mc4zz_att.uid = 'w75KJ2mc4zz'\n" +
            "\tjoin trackedentityattributevalue zDhUuAYrxNC on zDhUuAYrxNC.trackedentityinstanceid = tei.trackedentityinstanceid\n"
            +
            "\tjoin trackedentityattribute zDhUuAYrxNC_att on zDhUuAYrxNC_att.trackedentityattributeid = zDhUuAYrxNC.trackedentityattributeid\n"
            +
            "\t\tand zDhUuAYrxNC_att.uid = 'zDhUuAYrxNC';";

        // 73076 total in query
        // 73124 total in trackedentityinstance
        // do we have people without a name?

        // TODO create as a bean somewhere instead of on every call to export
        // TODO why do both client builders take the solr url?

        String solrBaseUrl = dhisConfig.getProperty( ConfigurationKey.SOLR_BASE_URL );
        Http2SolrClient solrHttpClient = new Http2SolrClient.Builder( solrBaseUrl )
            .build();
        ConcurrentUpdateHttp2SolrClient solrClient = new ConcurrentUpdateHttp2SolrClient.Builder( solrBaseUrl,
            solrHttpClient )
                .build();

        List<SolrInputDocument> docs = new ArrayList<>();
        jdbcTemplate.query( sql, rs -> {
            final SolrInputDocument doc = new SolrInputDocument();
            doc.addField( "trackedEntity", rs.getString( "trackedEntity" ) );
            doc.addField( "trackedEntityType", rs.getString( "trackedEntityType" ) );
            doc.addField( "w75KJ2mc4zz", rs.getString( "w75KJ2mc4zz" ) );
            doc.addField( "zDhUuAYrxNC", rs.getString( "zDhUuAYrxNC" ) );
            docs.add( doc );
        } );
        UpdateResponse response = solrClient.add( SOLR_COLLECTION_TRACKER, docs, 1000 );
        log.info( "Updated Solr index {} at {}, got response {}", SOLR_COLLECTION_TRACKER, solrBaseUrl,
            response.getStatus() );
    }
}
