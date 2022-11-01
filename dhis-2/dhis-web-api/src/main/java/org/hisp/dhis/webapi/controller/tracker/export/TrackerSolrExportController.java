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

import lombok.RequiredArgsConstructor;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.common.SolrInputDocument;
import org.hisp.dhis.common.DhisApiVersion;
import org.hisp.dhis.webapi.mvc.annotation.ApiVersion;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping( value = RESOURCE_PATH + "/solr" )
@ApiVersion( { DhisApiVersion.DEFAULT, DhisApiVersion.ALL } )
@RequiredArgsConstructor
public class TrackerSolrExportController
{

    private final JdbcTemplate jdbcTemplate;

    @GetMapping( )
    void export()
        throws SolrServerException,
        IOException
    {
        String sql = "select tei.uid as trackedEntity, tet.uid as trackedEntityType, w75KJ2mc4zz.value as w75KJ2mc4zz, zDhUuAYrxNC.value as zDhUuAYrxNC, concat(w75KJ2mc4zz.value, ' ', zDhUuAYrxNC.value) as fullname from trackedentityinstance tei\n"
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

        // TODO setup solrj to connect to solr
        // TODO transform rs into data structure I can push into solr

        // TODO make configurable
        // TODO if this is deprecated which one should I use instead?
        // TODO create as a bean somewhere instead of on every call to export
        // TODO is client.add() batching them? or does it directly make the
        // request
        // Http2SolrClient solrClient2 = new
        // Http2SolrClient.Builder("http://solr:8983/solr")
        // .build();
        // // TODO why do both client builders take the solr url?
        // ConcurrentUpdateHttp2SolrClient foo = new
        // ConcurrentUpdateHttp2SolrClient.Builder("http://solr:8983/solr",
        // solrClient2)
        // .build();
        try ( HttpSolrClient solrClient = new HttpSolrClient.Builder( "http://solr:8983/solr" )
            .withConnectionTimeout( 5000 )
            .withSocketTimeout( 5000 )
            .build() )
        {

            jdbcTemplate.query( sql, rs -> {
                final SolrInputDocument doc = new SolrInputDocument();
                doc.addField( "trackedEntity", rs.getString( "trackedEntity" ) );
                doc.addField( "trackedEntityType", rs.getString( "trackedEntityType" ) );
                doc.addField( "w75KJ2mc4zz", rs.getString( "w75KJ2mc4zz" ) );
                doc.addField( "zDhUuAYrxNC", rs.getString( "zDhUuAYrxNC" ) );
                try
                {
                    solrClient.add( "tracker", doc );
                }
                catch ( SolrServerException e )
                {
                    throw new RuntimeException( e );
                }
                catch ( IOException e )
                {
                    throw new RuntimeException( e );
                }
            } );
            solrClient.commit( "tracker" );
        }
    }
}
