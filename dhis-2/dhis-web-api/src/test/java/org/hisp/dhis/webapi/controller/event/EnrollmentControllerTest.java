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
package org.hisp.dhis.webapi.controller.event;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.wildfly.common.Assert.assertFalse;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.hisp.dhis.common.CodeGenerator;
import org.hisp.dhis.dxf2.events.TrackedEntityInstanceParams;
import org.hisp.dhis.dxf2.events.enrollment.AbstractEnrollmentService;
import org.hisp.dhis.dxf2.events.enrollment.Enrollment;
import org.hisp.dhis.dxf2.events.event.Event;
import org.hisp.dhis.dxf2.events.event.EventService;
import org.hisp.dhis.dxf2.events.relationship.RelationshipService;
import org.hisp.dhis.program.Program;
import org.hisp.dhis.program.ProgramInstance;
import org.hisp.dhis.program.ProgramInstanceService;
import org.hisp.dhis.program.ProgramStageInstance;
import org.hisp.dhis.program.ProgramStatus;
import org.hisp.dhis.relationship.Relationship;
import org.hisp.dhis.relationship.RelationshipItem;
import org.hisp.dhis.relationship.RelationshipType;
import org.hisp.dhis.trackedentity.TrackedEntityInstance;
import org.hisp.dhis.trackedentity.TrackerAccessManager;
import org.hisp.dhis.user.CurrentUserService;
import org.hisp.dhis.user.User;
import org.hisp.dhis.webapi.controller.exception.NotFoundException;
import org.hisp.dhis.webapi.service.ContextService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith( MockitoExtension.class )
public class EnrollmentControllerTest
{

    @InjectMocks
    EnrollmentController enrollmentController;

    @Mock
    ContextService contextService;

    @Mock
    CurrentUserService currentUserService;

    @Mock
    EventService eventService;

    @Mock
    RelationshipService relationshipService;

    @Mock
    AbstractEnrollmentService enrollmentService;

    @Mock
    ProgramInstanceService programInstanceService;

    @Mock
    TrackerAccessManager trackerAccessManager;

    @Mock
    ProgramInstance programInstance;

    @Mock
    User user;

    @Captor
    ArgumentCaptor<TrackedEntityInstanceParams> entityInstanceParamsArgumentCaptor;

    String enrollmentId = CodeGenerator.generateUid();

    @BeforeEach
    void setUp()
    {
        mock( AbstractEnrollmentService.class, Mockito.CALLS_REAL_METHODS );
        ReflectionTestUtils.setField( enrollmentService, "currentUserService", currentUserService );
        ReflectionTestUtils.setField( enrollmentService, "trackerAccessManager", trackerAccessManager );
        ReflectionTestUtils.setField( enrollmentService, "eventService", eventService );
        ReflectionTestUtils.setField( enrollmentService, "relationshipService", relationshipService );

        when( programInstance.getProgram() ).thenReturn( new Program() );
        when( programInstance.getStatus() ).thenReturn( ProgramStatus.ACTIVE );
        when( programInstance.getUid() ).thenReturn( enrollmentId );

        when( currentUserService.getCurrentUser() ).thenReturn( user );
        when( programInstanceService.getProgramInstance( enrollmentId ) ).thenReturn( programInstance );
        when( enrollmentService.getEnrollment( eq( user ), eq( programInstance ), any(), anyBoolean() ) )
            .thenCallRealMethod();
        when( enrollmentService.getEnrollment( eq( programInstance ), any() ) ).thenCallRealMethod();
    }

    @Test
    void shouldSearchWithIncludeRelationships()
        throws NotFoundException
    {
        when( contextService.getParameterValues( eq( "fields" ) ) ).thenReturn( singletonList( "relationships" ) );

        when( programInstance.getRelationshipItems() ).thenReturn( getRelationshipItems() );
        when( relationshipService.getRelationship( any(), any(), any() ) )
            .thenReturn( new org.hisp.dhis.dxf2.events.trackedentity.Relationship() );

        Enrollment enrollment = enrollmentController.getEnrollment( enrollmentId, new HashMap<>(), null );

        verify( enrollmentService ).getEnrollment( eq( programInstance ),
            entityInstanceParamsArgumentCaptor.capture() );

        TrackedEntityInstanceParams trackedEntityInstanceParams = entityInstanceParamsArgumentCaptor.getValue();

        assertAll(
            () -> assertEquals( enrollmentId, enrollment.getEnrollment() ),
            () -> assertEquals( 1, enrollment.getRelationships().size() ),
            () -> assertEquals( 0, enrollment.getEvents().size() ),
            () -> assertTrue( trackedEntityInstanceParams.isIncludeRelationships() ),
            () -> assertFalse( trackedEntityInstanceParams.isIncludeEvents() ) );

        verify( relationshipService, atLeastOnce() ).getRelationship( any(), any(), any() );
    }

    @Test
    void shouldSearchWithIncludeEvents()
        throws NotFoundException
    {
        when( contextService.getParameterValues( eq( "fields" ) ) ).thenReturn( singletonList( "events" ) );

        when( programInstance.getProgramStageInstances() ).thenReturn( Set.of( new ProgramStageInstance() ) );
        when( eventService.getEvent( any(), anyBoolean(), anyBoolean(), anyBoolean() ) ).thenReturn( new Event() );

        Enrollment enrollment = enrollmentController.getEnrollment( enrollmentId, new HashMap<>(), null );

        verify( enrollmentService ).getEnrollment( eq( programInstance ),
            entityInstanceParamsArgumentCaptor.capture() );

        TrackedEntityInstanceParams trackedEntityInstanceParams = entityInstanceParamsArgumentCaptor.getValue();

        assertAll(
            () -> assertEquals( enrollmentId, enrollment.getEnrollment() ),
            () -> assertEquals( 1, enrollment.getEvents().size() ),
            () -> assertEquals( 0, enrollment.getRelationships().size() ),
            () -> assertFalse( trackedEntityInstanceParams.isIncludeRelationships() ),
            () -> assertTrue( trackedEntityInstanceParams.isIncludeEvents() ) );

        verify( eventService, atLeastOnce() ).getEvent( any(), anyBoolean(), anyBoolean(), anyBoolean() );
    }

    @Test
    void shouldSearchWithIncludeEventsAndRelationships()
        throws NotFoundException
    {
        when( contextService.getParameterValues( eq( "fields" ) ) )
            .thenReturn( List.of( "events", "relationships" ) );

        when( programInstance.getProgramStageInstances() ).thenReturn( Set.of( new ProgramStageInstance() ) );
        when( eventService.getEvent( any(), anyBoolean(), anyBoolean(), anyBoolean() ) ).thenReturn( new Event() );

        when( programInstance.getRelationshipItems() ).thenReturn( getRelationshipItems() );
        when( relationshipService.getRelationship( any(), any(), any() ) )
            .thenReturn( new org.hisp.dhis.dxf2.events.trackedentity.Relationship() );

        Enrollment enrollment = enrollmentController.getEnrollment( enrollmentId, new HashMap<>(), null );

        verify( enrollmentService ).getEnrollment( eq( programInstance ),
            entityInstanceParamsArgumentCaptor.capture() );

        TrackedEntityInstanceParams trackedEntityInstanceParams = entityInstanceParamsArgumentCaptor.getValue();

        assertAll(
            () -> assertEquals( enrollmentId, enrollment.getEnrollment() ),
            () -> assertEquals( 1, enrollment.getEvents().size() ),
            () -> assertEquals( 1, enrollment.getRelationships().size() ),
            () -> assertTrue( trackedEntityInstanceParams.isIncludeRelationships() ),
            () -> assertTrue( trackedEntityInstanceParams.isIncludeEvents() ) );

        verify( eventService, atLeastOnce() ).getEvent( any(), anyBoolean(), anyBoolean(), anyBoolean() );
        verify( relationshipService, atLeastOnce() ).getRelationship( any(), any(), any() );
    }

    private Set<RelationshipItem> getRelationshipItems()
    {
        Relationship relationship = new Relationship();
        relationship.setRelationshipType( new RelationshipType() );

        RelationshipItem from = new RelationshipItem();
        RelationshipItem to = new RelationshipItem();
        from.setTrackedEntityInstance( new TrackedEntityInstance() );
        to.setTrackedEntityInstance( new TrackedEntityInstance() );
        from.setRelationship( relationship );
        to.setRelationship( relationship );

        return Set.of( from, to );
    }
}
