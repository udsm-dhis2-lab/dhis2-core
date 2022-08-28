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
package org.hisp.dhis.datastore;

import static org.hisp.dhis.utils.Assertions.assertContainsOnly;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.hisp.dhis.test.integration.NonTransactionalIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Stian Sandvold.
 */
class DatastoreEntryStoreTest extends NonTransactionalIntegrationTest
{

    @Autowired
    private DatastoreStore store;

    @Test
    void testAddKeyJsonValue()
    {
        DatastoreEntry entry = addEntry( "A", "1" );
        assertNotNull( entry );
        assertEquals( entry, store.get( entry.getId() ) );
    }

    @Test
    void testAddKeyJsonValuesAndGetNamespaces()
    {
        addEntry( "A", "1" );
        addEntry( "B", "1" );
        assertContainsOnly( store.getNamespaces(), "A", "B" );
    }

    @Test
    void testAddKeyJsonValuesAndGetKeysFromNamespace()
    {
        addEntry( "A", "1" );
        addEntry( "A", "2" );
        addEntry( "B", "1" );
        assertContainsOnly( store.getKeysInNamespace( "A" ), "1", "2" );
    }

    @Test
    void testAddKeyJsonValueAndGetKeyJsonValue()
    {
        DatastoreEntry entryA = addEntry( "A", "1" );
        assertEquals( store.getEntry( "A", "1" ), entryA );
    }

    @Test
    void testGetKeyJsonValuesByNamespace()
    {
        DatastoreEntry entryA1 = addEntry( "A", "1" );
        DatastoreEntry entryA2 = addEntry( "A", "2" );
        DatastoreEntry entryA3 = addEntry( "A", "3" );
        DatastoreEntry entryB1 = addEntry( "B", "1" );
        assertContainsOnly( store.getEntryByNamespace( "A" ), entryA1, entryA2, entryA3 );
        assertFalse( store.getEntryByNamespace( "A" ).contains( entryB1 ) );
    }

    @Test
    void testCountKeysInNamespace()
    {
        addEntry( "A", "1" );
        addEntry( "A", "2" );
        addEntry( "A", "3" );
        addEntry( "B", "1" );
        assertEquals( 3, store.countKeysInNamespace( "A" ) );
        assertEquals( 1, store.countKeysInNamespace( "B" ) );
        assertEquals( 0, store.countKeysInNamespace( "C" ) );
    }

    @Test
    void deleteNamespace()
    {
        addEntry( "A", "1" );
        addEntry( "A", "3" );
        addEntry( "B", "1" );
        addEntry( "C", "1" );
        store.deleteNamespace( "A" );
        assertContainsOnly( store.getNamespaces(), "B", "C" );
    }

    private DatastoreEntry addEntry( String ns, String key )
    {
        DatastoreEntry entry = new DatastoreEntry( ns, key );
        store.save( entry );
        return entry;
    }

}
