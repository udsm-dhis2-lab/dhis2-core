/*
 * Copyright (c) 2004-2021, University of Oslo
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

package org.hisp.dhis.tracker.deduplication.merge;

import com.google.gson.JsonObject;
import org.hisp.dhis.Constants;
import org.hisp.dhis.actions.metadata.ProgramActions;
import org.hisp.dhis.helpers.JsonObjectBuilder;
import org.hisp.dhis.helpers.QueryParamsBuilder;
import org.hisp.dhis.tracker.deduplication.PotentialDuplicatesApiTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class PotentialDuplicatesAttributeMergeTests
    extends PotentialDuplicatesApiTest
{
    private final String program = Constants.TRACKER_PROGRAM_ID;

    private List<String> attributes;

    @BeforeEach
    public void beforeEach()
    {
        loginActions.loginAsSuperUser(); //todo: change to admin when buggy TEA access check is removed

        attributes = new ProgramActions()
            .get( program, new QueryParamsBuilder().addAll( "filter=programTrackedEntityAttributes.valueType:eq:TEXT" ) )
            .extractList( "programTrackedEntityAttributes.trackedEntityAttribute.id", String.class );
    }

    @Test
    public void shouldAutoMergeDifferentAttributes()
    {
        String teiA = createTeiWithAttributes( createAttribute( attributes.get( 1 ), "TEST" ) );
        String teiB = createTeiWithAttributes( createAttribute( attributes.get( 0 ), "TEST" ) );

        String potentialDuplicate = potentialDuplicatesActions.createPotentialDuplicate( teiA, teiB, "OPEN" )
            .validateStatus( 200 )
            .extractString( "id" );

        potentialDuplicatesActions.autoMergePotentialDuplicate( potentialDuplicate )
            .validate().statusCode( 200 );

        trackerActions.getTrackedEntity( teiA )
            .validate()
            .body( "attributes", hasSize( 2 ) );
    }

    @Test
    public void shouldManuallyMergeAttributeWithDifferentValues()
    {
        String teiA = createTeiWithAttributes( createAttribute( attributes.get( 0 ), "attribute 1" ) );
        String teiB = createTeiWithAttributes( createAttribute( attributes.get( 0 ), "attribute 2" ) );

        String potentialDuplicate = potentialDuplicatesActions.createAndValidatePotentialDuplicate( teiA, teiB, "OPEN" );

        potentialDuplicatesActions.manualMergePotentialDuplicate( potentialDuplicate,
            new JsonObjectBuilder().addArray( "trackedEntityAttributes", Arrays.asList( attributes.get( 0 ) ) ).build() )
            .validate().statusCode( 200 );

        trackerActions.getTrackedEntity( teiA )
            .validate()
            .body( "attributes", hasSize( 1 ) )
            .body( "attributes[0].value", equalTo( "attribute 2" ) );
    }

    private JsonObject createAttribute( String tet, String value )
    {
        return new JsonObjectBuilder()
            .addProperty( "attribute", tet )
            .addProperty( "value", value )
            .build();
    }

    private String createTeiWithAttributes( JsonObject... attributes )
    {
        JsonObjectBuilder tei =
            JsonObjectBuilder.jsonObject( trackerActions.buildTei( Constants.TRACKED_ENTITY_TYPE, Constants.ORG_UNIT_IDS[0] ) );

        for ( JsonObject attribute : attributes )
        {
            tei.addOrAppendToArrayByJsonPath( "trackedEntities[0]", "attributes", attribute );
        }

        return trackerActions.postAndGetJobReport( tei.build() ).validateSuccessfulImport().extractImportedTeis().get( 0 );
    }
}
