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

import org.statusinfo.api.StatusInfo;
import org.statusinfo.api.StatusInfoSnapshot;

/**
 * 
 * @author 2011 Stanislav Muhametsin
 */
final class StatusInfoSnapshotImpl
    implements StatusInfoSnapshot
{
    private final StatusInfo _statusInfo;
    private final int _amountOfDedicatedListeners;

    StatusInfoSnapshotImpl( StatusInfo statusInfo, int amountOfDedicatedListeners )
    {
        this._statusInfo = statusInfo;
        this._amountOfDedicatedListeners = amountOfDedicatedListeners;
    }

    @Override
    public StatusInfo getStatusInfo()
    {
        return this._statusInfo;
    }

    @Override
    public int getAmountOfDedicatedListeners()
    {
        return this._amountOfDedicatedListeners;
    }

    @Override
    public boolean equals( Object obj )
    {
        return this == obj
            || (obj instanceof StatusInfoSnapshot && this._amountOfDedicatedListeners == ((StatusInfoSnapshot) obj)
                .getAmountOfDedicatedListeners())
            && this._statusInfo.equals( ((StatusInfoSnapshot) obj).getStatusInfo() );
    }

    @Override
    public int hashCode()
    {
        return this._statusInfo.hashCode();
    }

    @Override
    public String toString()
    {
        return SnapshotToString.toString( this );
    }

}
