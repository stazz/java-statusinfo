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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.statusinfo.api.NoOperationInProgressException;
import org.statusinfo.api.OperationCreationResult;
import org.statusinfo.api.StatusInfo;
import org.statusinfo.api.StatusInfoListener;
import org.statusinfo.api.StatusInfoListener.ChangeType;
import org.statusinfo.api.StatusInfoService;

/**
 * @author Stanislav Muhametsin
 * 
 */
public class StatusInfoServiceImpl
    implements StatusInfoService, Activatable
{

    @Mixins(
    {
        StatusInfoServiceImpl.class
    })
    public interface StatusInfoServiceComposite
        extends StatusInfoService, ServiceComposite
    {

    }

    static final int NO_MAX_STEPS = -1;

    private static class StatusInfoInfo
    {
        private final StatusInfoInfo _parent;
        private final StatusInfoImpl _statusInfo;
        private final String _receipt;

        public StatusInfoInfo( StatusInfoImpl statusInfo, String receipt )
        {
            this( null, statusInfo, receipt );
        }

        public StatusInfoInfo( StatusInfoInfo parent, StatusInfoImpl statusInfo, String receipt )
        {
            this._parent = parent;
            this._statusInfo = statusInfo;
            this._receipt = receipt;
        }

        public StatusInfoImpl getStatusInfo()
        {
            return this._statusInfo;
        }

        public String getReceipt()
        {
            return this._receipt;
        }

        public StatusInfoInfo getParent()
        {
            return this._parent;
        }
    }

    private static class StatusListenerInfo
    {
        private final StatusInfoListener _listener;
        private final String _associatedStatusReceipt;

        public StatusListenerInfo( StatusInfoListener listener )
        {
            this( listener, null );
        }

        public StatusListenerInfo( StatusInfoListener listener, String associatedStatusReceipt )
        {
            this._listener = listener;
            this._associatedStatusReceipt = associatedStatusReceipt;
        }

        public StatusInfoListener getListener()
        {
            return this._listener;
        }

        public String getAssociatedStatusReceipt()
        {
            return this._associatedStatusReceipt;
        }
    }

    private List<StatusListenerInfo> _listeners;
    private Object _listenersLock;
    private Deque<StatusInfoInfo> _statuses;
    private Object _statusesLock;

    @Override
    public void activate()
        throws Exception
    {
        this._listenersLock = new Object();
        this._statusesLock = new Object();
        this._listeners = new ArrayList<StatusListenerInfo>();
        this._statuses = new ArrayDeque<StatusInfoInfo>();
    }

    @Override
    public void passivate()
        throws Exception
    {
        this._listeners.clear();
        this._statuses.clear();
        this._statuses = null;
        this._statuses = null;
        this._listenersLock = null;
        this._statusesLock = null;
    }

    @Override
    public void addStatusInfoListener( StatusInfoListener listener )
    {
        synchronized( this._listenersLock )
        {
            this._listeners.add( new StatusListenerInfo( listener ) );
        }
    }

    @Override
    public void addStatusInfoListenerUntilEndOfCurrentOperation( StatusInfoListener listener )
    {
        synchronized( this._statusesLock )
        {
            StatusInfoInfo info = this._statuses.peek();
            if( info != null )
            {
                String associatedReceipt = info.getReceipt();
                synchronized( this._listenersLock )
                {
                    this._listeners.add( new StatusListenerInfo( listener, associatedReceipt ) );
                }
            }
            else
            {
                throw new NoOperationInProgressException( "No operation currently in progress." );
            }
        }
    }

    @Override
    public void removeStatusInfoListener( StatusInfoListener listener )
    {
        synchronized( this._listenersLock )
        {
            this._listeners.remove( listener );
        }
    }

    @Override
    public OperationCreationResult startOperation( String name )
    {
        return this.startOperation( Thread.currentThread(), name, NO_MAX_STEPS );
    }

    @Override
    public OperationCreationResult startOperation( String name, int maxSteps )
    {
        return this.startOperation( Thread.currentThread(), name, maxSteps );
    }

    @Override
    public OperationCreationResult startOperation( Thread thread, String name )
    {
        return this.startOperation( thread, name, NO_MAX_STEPS );
    }

    @Override
    public OperationCreationResult startOperation( Thread thread, String name, int maxSteps )
    {
        synchronized( this._statusesLock )
        {
            return this.doStartOperation( thread, name, maxSteps );
        }
    }

    @Override
    public void endOperation( String receipt )
    {
        this.doEndOperation( receipt );
    }

    @Override
    public void updateCurrentOperation( int amountOfSteps )
    {
        this.doUpdateOperation( null, amountOfSteps );
    }

    @Override
    public void updateOperation( String receipt, int amountOfSteps )
    {
        this.doUpdateOperation( receipt, amountOfSteps );
    }

    protected void doUpdateOperation( String receipt, int amountOfSteps )
    {
        StatusInfoInfo info = null;
        boolean notify = false;
        synchronized( this._statusesLock )
        {
            if( receipt == null )
            {
                info = this._statuses.peek();
            }
            else
            {
                for( StatusInfoInfo tst : this._statuses )
                {
                    if( receipt.equals( tst.getReceipt() ) )
                    {
                        info = tst;
                        break;
                    }
                }
            }
            notify = info != null;
            if( notify )
            {
                info.getStatusInfo().addCurrentSteps( amountOfSteps );
            }
            else
            {
                throw new NoOperationInProgressException( "No operation in progress "
                    + (receipt == null ? " currently" : "with receipt " + receipt) + "." );
            }
        }
        if( notify )
        {
            this.notifyListeners( info, ChangeType.CHANGED );
        }
    }

    protected OperationCreationResult doStartOperation( Thread thread, String name, int maxSteps )
    {
        StatusInfoInfo info = new StatusInfoInfo( new StatusInfoImpl( this.newID(), name, thread, maxSteps ), UUID
            .randomUUID().toString() );
        this._statuses.push( info );
        this.notifyListeners( info, ChangeType.BEGAN );
        return new OperationCreationResultImpl( info.getStatusInfo().getID(), info.getReceipt() );
    }

    protected void doEndOperation( String receipt )
    {
        List<StatusInfoInfo> endedOperations = new LinkedList<StatusInfoInfo>();
        Set<String> endedOperationReceipts = new HashSet<String>();
        StatusInfoInfo info = null;
        synchronized( this._statusesLock )
        {
            for( StatusInfoInfo tst : this._statuses )
            {
                if( tst.getReceipt().equals( receipt ) )
                {
                    info = tst;
                    break;
                }
            }
            if( info != null )
            {
                info = this._statuses.peek();
                boolean matched = false;
                while( !matched )
                {
                    info = this._statuses.pop();
                    matched = info.getReceipt().equals( receipt );
                    endedOperations.add( info );
                    endedOperationReceipts.add( info.getReceipt() );
                }
            }
            else
            {
                // TODO maybe just ignore instead of throwing?
                throw new NoOperationInProgressException( "Could not find operation with receipt " + receipt
                    + " to end." );
            }
        }

        // Notify listeners
        this.notifyListeners( endedOperations, ChangeType.ENDED );

        // Remove dedicated listeners
        synchronized( this._listenersLock )
        {
            int idx = 0;
            while( idx < this._listeners.size() )
            {
                String currentReceipt = this._listeners.get( idx ).getAssociatedStatusReceipt();
                if( currentReceipt != null && endedOperationReceipts.contains( currentReceipt ) )
                {
                    this._listeners.remove( idx );
                    endedOperationReceipts.remove( currentReceipt );
                }
                else
                {
                    ++idx;
                }
            }
        }
    }

    protected void notifyListeners( StatusInfoInfo info, ChangeType type )
    {
        // Notify listeners
        List<StatusListenerInfo> list = null;
        synchronized( this._listenersLock )
        {
            list = new ArrayList<StatusListenerInfo>( this._listeners );
        }
        for( StatusListenerInfo listenerInfo : list )
        {
            StatusInfoListener listener = listenerInfo.getListener();
            StatusInfo statusInfo = info.getStatusInfo();
            if( listener.isInterestedInStatusInfo( statusInfo ) )
            {
                listener.operationChanged( statusInfo, type );
            }
        }
    }

    protected void notifyListeners( Iterable<StatusInfoInfo> infos, ChangeType type )
    {
        // Notify listeners
        List<StatusListenerInfo> list = null;
        synchronized( this._listenersLock )
        {
            list = new ArrayList<StatusListenerInfo>( this._listeners );
        }
        for( StatusInfoInfo info : infos )
        {
            for( StatusListenerInfo listenerInfo : list )
            {
                StatusInfoListener listener = listenerInfo.getListener();
                StatusInfo statusInfo = info.getStatusInfo();
                if( listener.isInterestedInStatusInfo( statusInfo ) )
                {
                    listener.operationChanged( statusInfo, type );
                }
            }
        }
    }

    protected String newID()
    {
        return UUID.randomUUID().toString();
    }
}
