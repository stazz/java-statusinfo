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

import org.statusinfo.api.StatusInfoSnapshot;
import org.statusinfo.api.ThreadSnapshot;

/**
 * 
 * @author 2011 Stanislav Muhametsin
 */
final class ThreadSnapshotImpl
    implements ThreadSnapshot
{
    private final List<StatusInfoSnapshot> _statuses;
    private final Thread _thread;

    ThreadSnapshotImpl( Thread thread, List<StatusInfoSnapshot> statuses )
    {
        this._thread = thread;
        this._statuses = Collections.unmodifiableList( statuses );
    }

    @Override
    public List<StatusInfoSnapshot> getOperationStatuses()
    {
        return this._statuses;
    }

    @Override
    public Thread getThread()
    {
        return this._thread;
    }

    @Override
    public boolean equals( Object obj )
    {
        return this == obj
            || (obj instanceof ThreadSnapshot && this._thread.equals( ((ThreadSnapshot) obj).getThread() ) && this._statuses
                .equals( ((ThreadSnapshot) obj).getOperationStatuses() ));
    }

    @Override
    public int hashCode()
    {
        return this._thread.hashCode();
    }

    @Override
    public String toString()
    {
        return SnapshotToString.toString( this );
    }
}
