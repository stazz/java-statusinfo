/*
 * Copyright (c) 2011, Stanislav Muhametsin. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.statusinfo.implementation;

import org.qi4j.api.util.NullArgumentException;
import org.statusinfo.api.OperationCreationResult;

/**
 * 
 * @author 2011 Stanislav Muhametsin
 */
final class OperationCreationResultImpl
    implements OperationCreationResult
{

    private final String _id;
    private final String _receipt;

    public OperationCreationResultImpl( String id, String receipt )
    {
        NullArgumentException.validateNotNull( "ID", id );
        NullArgumentException.validateNotNull( "Receipt", receipt );

        this._id = id;
        this._receipt = receipt;
    }

    @Override
    public String getReceipt()
    {
        return this._receipt;
    }

    @Override
    public String getID()
    {
        return this._id;
    }

    @Override
    public boolean equals( Object obj )
    {
        return this == obj
            || (obj instanceof OperationCreationResult && this._receipt.equals( ((OperationCreationResult) obj)
                .getReceipt() ));
    }

    @Override
    public int hashCode()
    {
        return this._receipt.hashCode();
    }

    @Override
    public String toString()
    {
        return "Operation(id=" + this._id + ",receipt=" + this._receipt + ")";
    }
}
