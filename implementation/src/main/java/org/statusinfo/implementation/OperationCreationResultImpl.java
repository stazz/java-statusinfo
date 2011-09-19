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

import org.statusinfo.api.OperationCreationResult;

/**
 * 
 * @author 2011 Stanislav Muhametsin
 */
class OperationCreationResultImpl
    implements OperationCreationResult
{

    private final String _id;
    private final String _receipt;

    public OperationCreationResultImpl( String id, String receipt )
    {
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

}
