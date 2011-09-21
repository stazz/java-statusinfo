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

import java.util.Collections;
import java.util.List;

import org.statusinfo.api.OperationSnapshot;
import org.statusinfo.api.ThreadSnapshot;

/**
 * 
 * @author 2011 Stanislav Muhametsin
 */
final class OperationSnapshotImpl
    implements OperationSnapshot
{

    private final List<ThreadSnapshot> _snapshots;
    private final int _amountOfListeners;

    OperationSnapshotImpl( List<ThreadSnapshot> snapshots, int amountOfListeners )
    {
        this._snapshots = Collections.unmodifiableList( snapshots );
        this._amountOfListeners = amountOfListeners;
    }

    @Override
    public Iterable<ThreadSnapshot> getThreadSnapshots()
    {
        return this._snapshots;
    }

    @Override
    public int getAmountOfAllListeners()
    {
        return this._amountOfListeners;
    }

    @Override
    public boolean equals( Object obj )
    {
        return this == obj
            || (obj instanceof OperationSnapshot
                && this._amountOfListeners == ((OperationSnapshot) obj).getAmountOfAllListeners() && this._snapshots
                    .equals( ((OperationSnapshot) obj).getThreadSnapshots() ));
    }

    @Override
    public int hashCode()
    {
        return this._snapshots.hashCode();
    }

    @Override
    public String toString()
    {
        return SnapshotToString.toString( this );
    }

}
