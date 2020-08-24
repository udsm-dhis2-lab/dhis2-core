/*
 * Copyright (c) 2004-2019, University of Oslo
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

package org.hisp.dhis.dxf2.events.trackedentity.store;

import static org.hisp.dhis.dxf2.events.trackedentity.store.query.TrackedEntityInstanceQuery.getQuery;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hisp.dhis.dxf2.events.aggregates.AggregateContext;
import org.hisp.dhis.dxf2.events.trackedentity.Attribute;
import org.hisp.dhis.dxf2.events.trackedentity.ProgramOwner;
import org.hisp.dhis.dxf2.events.trackedentity.TrackedEntityInstance;
import org.hisp.dhis.dxf2.events.trackedentity.store.mapper.OwnedTeiMapper;
import org.hisp.dhis.dxf2.events.trackedentity.store.mapper.ProgramOwnerRowCallbackHandler;
import org.hisp.dhis.dxf2.events.trackedentity.store.mapper.TrackedEntityAttributeRowCallbackHandler;
import org.hisp.dhis.dxf2.events.trackedentity.store.mapper.TrackedEntityInstanceRowCallbackHandler;
import org.hisp.dhis.trackedentity.TrackedEntityInstanceQueryParams;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import com.google.common.collect.Multimap;

/**
 * @author Luciano Fiandesio
 * @author Ameen Mohamed
 */
@Repository
public class DefaultTrackedEntityInstanceStore extends AbstractStore implements TrackedEntityInstanceStore
{
    private final static String GET_TEIS_SQL = getQuery();

    private final static String GET_TEI_ATTRIBUTES = "select tei.uid as teiuid" +
        ", teav.trackedentityinstanceid as id, teav.created, teav.lastupdated, " +
        "teav.value, teav.storedby, t.name as att_name, " +
        "t.uid as att_uid, t.valuetype as att_val_type, " +
        "t.code as att_code, t.skipsynchronization as att_skip_sync " +
        "from trackedentityattributevalue teav " +
        "join trackedentityattribute t on teav.trackedentityattributeid = t.trackedentityattributeid " +
        "join trackedentityinstance tei on teav.trackedentityinstanceid = tei.trackedentityinstanceid " +
        "where teav.trackedentityinstanceid in (:ids)";

    private final static String GET_PROGRAM_OWNERS = "select tei.uid as key, p.uid as prguid, o.uid as ouuid " +
        "from trackedentityprogramowner teop " +
        "join program p on teop.programid = p.programid " +
        "join organisationunit o on teop.organisationunitid = o.organisationunitid " +
        "join trackedentityinstance tei on teop.trackedentityinstanceid = tei.trackedentityinstanceid " +
        "where teop.trackedentityinstanceid in (:ids)";
    
    private final static String GET_OWNERSHIP_DATA_FOR_TEIS_FOR_ALL_PROGRAM = "SELECT tei.uid as tei_uid,tpo.trackedentityinstanceid, tpo.programid, tpo.organisationunitid, p.accesslevel,p.uid as pgm_uid " + 
        "FROM trackedentityprogramowner TPO " + 
        "LEFT JOIN program P on P.programid = TPO.programid " + 
        "LEFT JOIN organisationunit OU on OU.organisationunitid = TPO.organisationunitid " + 
        "LEFT JOIN trackedentityinstance TEI on TEI.trackedentityinstanceid = tpo.trackedentityinstanceid " + 
        "WHERE TPO.trackedentityinstanceid in (:ids) " + 
        "AND p.programid in (SELECT programid FROM program) " + 
        "GROUP BY tei.uid,tpo.trackedentityinstanceid, tpo.programid, tpo.organisationunitid, ou.path, p.accesslevel,p.uid " + 
        "HAVING (P.accesslevel in ('OPEN', 'AUDITED') AND EXISTS(SELECT SS.organisationunitid FROM userteisearchorgunits SS LEFT JOIN organisationunit OU2 ON OU2.organisationunitid = SS.organisationunitid WHERE userinfoid = :userInfoId AND OU.path LIKE CONCAT(OU2.path, '%'))) " + 
        "OR (P.accesslevel in ('CLOSED', 'PROTECTED') AND EXISTS(SELECT CS.organisationunitid FROM usermembership CS LEFT JOIN organisationunit OU2 ON OU2.organisationunitid = CS.organisationunitid WHERE userinfoid = :userInfoId AND OU.path LIKE CONCAT(OU2.path, '%')));";
  
    private final static String GET_OWNERSHIP_DATA_FOR_TEIS_FOR_SPECIFIC_PROGRAM = "SELECT tei.uid as tei_uid,tpo.trackedentityinstanceid, tpo.programid, tpo.organisationunitid, p.accesslevel,p.uid as pgm_uid " + 
        "FROM trackedentityprogramowner TPO " + 
        "LEFT JOIN program P on P.programid = TPO.programid " + 
        "LEFT JOIN organisationunit OU on OU.organisationunitid = TPO.organisationunitid " + 
        "LEFT JOIN trackedentityinstance TEI on TEI.trackedentityinstanceid = tpo.trackedentityinstanceid " + 
        "WHERE TPO.trackedentityinstanceid in (:ids) " + 
        "AND p.uid = :programUid " + 
        "GROUP BY tei.uid,tpo.trackedentityinstanceid, tpo.programid, tpo.organisationunitid, ou.path, p.accesslevel,p.uid " + 
        "HAVING (P.accesslevel in ('OPEN', 'AUDITED') AND EXISTS(SELECT SS.organisationunitid FROM userteisearchorgunits SS LEFT JOIN organisationunit OU2 ON OU2.organisationunitid = SS.organisationunitid WHERE userinfoid = :userInfoId AND OU.path LIKE CONCAT(OU2.path, '%'))) " + 
        "OR (P.accesslevel in ('CLOSED', 'PROTECTED') AND EXISTS(SELECT CS.organisationunitid FROM usermembership CS LEFT JOIN organisationunit OU2 ON OU2.organisationunitid = CS.organisationunitid WHERE userinfoid = :userInfoId AND OU.path LIKE CONCAT(OU2.path, '%')));";

    public DefaultTrackedEntityInstanceStore( @Qualifier( "readOnlyJdbcTemplate" ) JdbcTemplate jdbcTemplate )
    {
        super( jdbcTemplate );
    }

    @Override
    String getRelationshipEntityColumn() {
        return "trackedentityinstanceid";
    }

    @Override
    public Map<String, TrackedEntityInstance> getTrackedEntityInstances( List<Long> ids, AggregateContext ctx )
    {
        TrackedEntityInstanceRowCallbackHandler handler = new TrackedEntityInstanceRowCallbackHandler();

        jdbcTemplate.query( withAclCheck( GET_TEIS_SQL, ctx, "tei.trackedentitytypeid in (:teiTypeIds)" ),
            createIdsParam( ids ).addValue( "teiTypeIds", ctx.getTrackedEntityTypes() ), handler );

        return handler.getItems();
    }

    @Override
    public Multimap<String, Attribute> getAttributes( List<Long> ids )
    {
        return fetch( GET_TEI_ATTRIBUTES, new TrackedEntityAttributeRowCallbackHandler(), ids );
    }

    public Multimap<String, ProgramOwner> getProgramOwners( List<Long> ids )
    {
        return fetch( GET_PROGRAM_OWNERS, new ProgramOwnerRowCallbackHandler(), ids );
    }
    
    @Override
    public Multimap<String, String> getOwnedTeis( List<Long> ids, TrackedEntityInstanceQueryParams queryParams, Long userId )
    {
        OwnedTeiMapper handler = new OwnedTeiMapper();

        MapSqlParameterSource paramSource = createIdsParam( ids ).addValue( "userInfoId", userId );

        String sql;

        if ( queryParams.hasProgram() )
        {
            sql = GET_OWNERSHIP_DATA_FOR_TEIS_FOR_SPECIFIC_PROGRAM;
            paramSource.addValue( "programUid", queryParams.getProgram().getUid() );
        }
        else
        {
            sql = GET_OWNERSHIP_DATA_FOR_TEIS_FOR_ALL_PROGRAM;
        }
        jdbcTemplate.query( sql, paramSource, handler );
        
        String testSql = null;
        
        List<Map<String,Object>> results = jdbcTemplate.queryForList( testSql, new HashMap<>() );

        return handler.getItems();
    }
}
