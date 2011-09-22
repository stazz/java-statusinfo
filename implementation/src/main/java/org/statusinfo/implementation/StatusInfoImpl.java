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
import org.statusinfo.api.StatusInfo;

/**
 * @author Stanislav Muhametsin
 * 
 */
final class StatusInfoImpl
    implements StatusInfo
{

    private final StatusInfo _parent;
    private final String _id;
    private final String _name;
    private final Thread _thread;
    private final int _maxSteps;
    private int _currentSteps;

    StatusInfoImpl( StatusInfo parent, String id, String name, Thread thread, int maxSteps )
    {
        this( parent, id, name, thread, maxSteps, 0 );
    }

    StatusInfoImpl( StatusInfo parent, String id, String name, Thread thread, int maxSteps, int currentSteps )
    {
        NullArgumentException.validateNotNull( "ID", id );
        NullArgumentException.validateNotNull( "Thread", thread );

        this._id = id;
        this._parent = parent;
        this._name = name;
        this._thread = thread;
        this._maxSteps = maxSteps;
        this._currentSteps = currentSteps;
    }

    @Override
    public String getName()
    {
        return this._name;
    }

    @Override
    public Thread getThread()
    {
        return this._thread;
    }

    @Override
    public int getMaxSteps()
    {
        return this._maxSteps;
    }

    @Override
    public int getCurrentSteps()
    {
        return this._currentSteps;
    }

    @Override
    public String getID()
    {
        return this._id;
    }

    @Override
    public StatusInfo getParent()
    {
        return this._parent;
    }

    void setCurrentSteps( int newCurrentSteps )
    {
        this._currentSteps = newCurrentSteps;
    }

    void addCurrentSteps( int amount )
    {
        this._currentSteps += amount;
    }

    @Override
    public boolean equals( Object obj )
    {
        return this == obj || (obj instanceof StatusInfo && this._id.equals( ((StatusInfo) obj).getID() ));
    }

    @Override
    public int hashCode()
    {
        return this._id.hashCode();
    }

    @Override
    public String toString()
    {
        return SnapshotToString.toString( this );
    }

}
