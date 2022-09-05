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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.hisp.dhis.common.IllegalQueryException;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementService;
import org.hisp.dhis.dxf2.events.event.EventSearchParams;
import org.hisp.dhis.dxf2.util.InputUtils;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.program.Program;
import org.hisp.dhis.program.ProgramService;
import org.hisp.dhis.program.ProgramStage;
import org.hisp.dhis.program.ProgramStageService;
import org.hisp.dhis.schema.SchemaService;
import org.hisp.dhis.security.acl.AclService;
import org.hisp.dhis.trackedentity.TrackedEntityInstance;
import org.hisp.dhis.trackedentity.TrackedEntityInstanceService;
import org.hisp.dhis.user.CurrentUserService;
import org.hisp.dhis.user.User;
import org.hisp.dhis.webapi.controller.event.webrequest.tracker.TrackerEventCriteria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@MockitoSettings( strictness = Strictness.LENIENT )
@ExtendWith( MockitoExtension.class )
class EventRequestToSearchParamsMapperTest
{

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private ProgramService programService;

    @Mock
    private OrganisationUnitService organisationUnitService;

    @Mock
    private ProgramStageService programStageService;

    @Mock
    private AclService aclService;

    @Mock
    private TrackedEntityInstanceService entityInstanceService;

    @Mock
    private DataElementService dataElementService;

    @Mock
    private InputUtils inputUtils;

    @Mock
    private SchemaService schemaService;

    private EventRequestToSearchParamsMapper requestToSearchParamsMapper;

    private Program program;

    private ProgramStage programStage;

    @BeforeEach
    public void setUp()
    {
        requestToSearchParamsMapper = new EventRequestToSearchParamsMapper( currentUserService, programService,
            organisationUnitService, programStageService, aclService, entityInstanceService, dataElementService,
            inputUtils, schemaService );

        User user = new User();
        when( currentUserService.getCurrentUser() ).thenReturn( user );

        program = new Program();
        program.setUid( "programuid" );
        when( programService.getProgram( "programuid" ) ).thenReturn( program );
        when( aclService.canDataRead( user, program ) ).thenReturn( true );

        programStage = new ProgramStage();
        programStage.setUid( "programstageuid" );
        when( programStageService.getProgramStage( "programstageuid" ) ).thenReturn( programStage );
        when( aclService.canDataRead( user, programStage ) ).thenReturn( true );

        OrganisationUnit ou = new OrganisationUnit();
        TrackedEntityInstance tei = new TrackedEntityInstance();
        DataElement de = new DataElement();

        when( organisationUnitService.getOrganisationUnit( any() ) ).thenReturn( ou );

        when( organisationUnitService.isInUserHierarchy( ou ) ).thenReturn( true );
        when( entityInstanceService.getTrackedEntityInstance( any() ) ).thenReturn( tei );
        when( dataElementService.getDataElement( any() ) ).thenReturn( de );
    }

    @Test
    void testEventRequestToSearchParamsMapperSuccess()
    {

        TrackerEventCriteria eventCriteria = new TrackerEventCriteria();
        eventCriteria.setProgram( "programuid" );
        eventCriteria.setProgramStage( "programstageuid" );

        EventSearchParams params = requestToSearchParamsMapper.map( eventCriteria );

        assertEquals( program, params.getProgram() );
        assertEquals( programStage, params.getProgramStage() );
    }

    @Test
    void testMutualExclusionOfEventsAndFilter()
    {

        TrackerEventCriteria eventCriteria = new TrackerEventCriteria();
        eventCriteria.setFilter( Set.of( "qrur9Dvnyt5:ge:1:le:2" ) );
        eventCriteria.setEvent( "XKrcfuM4Hcw;M4pNmLabtXl" );

        Exception exception = assertThrows( IllegalQueryException.class,
            () -> requestToSearchParamsMapper.map( eventCriteria ) );
        assertEquals( "Event UIDs and filters can not be specified at the same time", exception.getMessage() );
    }
}
