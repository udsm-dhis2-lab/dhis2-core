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
package org.hisp.dhis.tracker.domain;

import java.util.Objects;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import org.hisp.dhis.attribute.AttributeValue;
import org.hisp.dhis.common.IdentifiableObject;
import org.hisp.dhis.tracker.TrackerIdScheme;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@Value
@AllArgsConstructor( access = AccessLevel.PRIVATE )
public class MetadataIdentifier
{

    @JsonProperty
    private final TrackerIdScheme idScheme;

    @JsonProperty
    private final String value;

    /**
     * Used to gradually migrate JSON test fixtures code over to
     * {@link MetadataIdentifier}. DO NOT USE THIS OTHER THAN IN TEST FIXTURE
     * RELATED CODE!!!
     *
     * @return identifier value of attribute
     */
    // TODO(DHIS2-12563) remove this one before releasing/closing this issue!
    @Deprecated( ) // just so the IDE highlights that it should not be used
    @JsonCreator
    public static MetadataIdentifier of( String uid )
    {
        return MetadataIdentifier.ofUid( uid );
    }

    /**
     * Creates identifier for metadata using idScheme UID and given value.
     *
     * @param idScheme idScheme of metadata identifier
     * @param value value of metadata identifier
     * @return metadata identifier
     */
    @JsonCreator
    public static MetadataIdentifier of( @JsonProperty( "idScheme" ) TrackerIdScheme idScheme,
        @JsonProperty( "value" ) String value )
    {
        return new MetadataIdentifier( idScheme, value );
    }

    /**
     * Creates identifier for metadata using idScheme UID and given uid.
     *
     * @param uid uid of metadata
     * @return metadata identifier representing a UID
     */
    public static MetadataIdentifier ofUid( String uid )
    {
        return new MetadataIdentifier( TrackerIdScheme.UID, uid );
    }

    public <T extends IdentifiableObject> String getIdentifier( T object )
    {
        switch ( idScheme )
        {
        case UID:
            return object.getUid();
        case CODE:
            return object.getCode();
        case NAME:
            return object.getName();
        case ATTRIBUTE:
            return object.getAttributeValues()
                .stream()
                .filter( av -> av.getAttribute().getUid().equals( value ) )
                .map( AttributeValue::getValue )
                .findFirst()
                .orElse( null );
        }

        throw new RuntimeException( "Unhandled identifier type." );
    }

    public <T extends IdentifiableObject> String getIdAndName( T object )
    {
        String identifier = getIdentifier( object );
        return object.getClass().getSimpleName() + " (" + identifier + ")";
    }

    // TODO(DHIS2-12563) write docs and tests
    public boolean isEqualTo( IdentifiableObject that )
    {
        if ( that == null )
        {
            return false;
        }

        return Objects.equals( this.value, this.getIdentifier( that ) );
    }
}
