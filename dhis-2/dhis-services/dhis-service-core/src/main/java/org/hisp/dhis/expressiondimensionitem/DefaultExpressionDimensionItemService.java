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
package org.hisp.dhis.expressiondimensionitem;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.hisp.dhis.hibernate.HibernateGenericStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The expression is a string describing a formula containing data element ids
 * and category option combo ids. The formula can potentially contain references
 * to data element totals.
 */
@Slf4j
@Service( "org.hisp.dhis.expression.ExpressionDimensionItemService" )
public class DefaultExpressionDimensionItemService
    implements ExpressionDimensionItemService
{
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private final HibernateGenericStore<ExpressionDimensionItem> expressionStore;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    public DefaultExpressionDimensionItemService(
        @Qualifier( "org.hisp.dhis.expression.ExpressionDimensionItemStore" ) HibernateGenericStore<ExpressionDimensionItem> expressionStore )
    {
        checkNotNull( expressionStore );

        this.expressionStore = expressionStore;
    }

    // -------------------------------------------------------------------------
    // Expression CRUD operations
    // -------------------------------------------------------------------------

    @Override
    @Transactional
    public long addExpressionDimensionItem( ExpressionDimensionItem expression )
    {
        expressionStore.save( expression );

        return expression.getId();
    }

    @Override
    @Transactional
    public void updateExpressionDimensionItem( ExpressionDimensionItem expression )
    {
        expressionStore.update( expression );
    }

    @Override
    @Transactional
    public void deleteExpressionDimensionItem( ExpressionDimensionItem expression )
    {
        expressionStore.delete( expression );
    }

    @Override
    @Transactional( readOnly = true )
    public ExpressionDimensionItem getExpressionDimensionItem( long id )
    {
        return expressionStore.get( id );
    }

    @Override
    @Transactional( readOnly = true )
    public List<ExpressionDimensionItem> getAllExpressionDimensionItems()
    {
        return expressionStore.getAll();
    }

}
