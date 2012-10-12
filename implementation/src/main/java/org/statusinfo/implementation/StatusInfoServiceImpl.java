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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.functional.Iterables;
import org.qi4j.functional.Specification;
import org.statusinfo.api.NoOperationInProgressException;
import org.statusinfo.api.OperationCreationResult;
import org.statusinfo.api.OperationSnapshot;
import org.statusinfo.api.StatusInfo;
import org.statusinfo.api.StatusInfoListener;
import org.statusinfo.api.StatusInfoListener.ChangeType;
import org.statusinfo.api.StatusInfoService;
import org.statusinfo.api.StatusInfoSnapshot;
import org.statusinfo.api.ThreadSnapshot;

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

    private static class StatusInfoInfo
    {
        // Parent must never be this
        private final StatusInfoInfo _parent;

        // This object must never be in _children
        private final Set<StatusInfoInfo> _children;
        private final StatusInfoImpl _statusInfo;
        private final String _receipt;

        public StatusInfoInfo( StatusInfoImpl statusInfo, String receipt )
        {
            this( null, statusInfo, receipt );
        }

        public StatusInfoInfo( StatusInfoInfo parent, StatusInfoImpl statusInfo, String receipt )
        {
            this._parent = parent;
            this._children = new HashSet<StatusInfoInfo>();
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

        public Set<StatusInfoInfo> getChildren()
        {
            return this._children;
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
    private Map<String, StatusInfoInfo> _statuses;
    private Object _statusesLock;

    @Override
    public void activate()
        throws Exception
    {
        this._listenersLock = new Object();
        this._statusesLock = new Object();
        this._listeners = new ArrayList<StatusListenerInfo>();
        this._statuses = new HashMap<String, StatusInfoInfo>();
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
    public <ReturnType, ExceptionType extends Throwable> ReturnType performOperation( String name,
        Operation<ReturnType, ExceptionType> operation )
        throws ExceptionType
    {
        return this.performOperation( name, NO_MAX_STEPS, operation );
    }

    @Override
    public <ReturnType, ExceptionType extends Throwable> ReturnType performOperation( String name, int maxSteps,
        Operation<ReturnType, ExceptionType> operation )
        throws ExceptionType
    {
        OperationCreationResult creation = this.startOperation( name, maxSteps );
        try
        {
            return operation.doOperation();
        }
        finally
        {
            this.endOperation( creation.getReceipt() );
        }
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
            // Find operation with no children in this thread
            StatusInfoInfo info = this.currentChildlessStatusInThisThread();
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
    public void removeStatusInfoListener( final StatusInfoListener listener )
    {
        synchronized( this._listenersLock )
        {
            this._listeners.remove( Iterables.first( Iterables.filter( new Specification<StatusListenerInfo>()
            {
                @Override
                public boolean satisfiedBy( StatusListenerInfo item )
                {
                    return item.getListener().equals( listener );
                }
            }, this._listeners ) ) );
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
        return this.doStartOperation( null, thread, name, maxSteps );
    }

    @Override
    public OperationCreationResult startSubOperation( String parentReceipt, String name )
    {
        return this.doStartOperation( parentReceipt, Thread.currentThread(), name, NO_MAX_STEPS );
    }

    @Override
    public boolean endOperation( String receipt )
    {
        return this.doEndOperation( receipt );
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

    @Override
    public OperationSnapshot getSnapshotOfCurrentState()
    {
        return this.doGetSnapshot();
    }

    protected void doUpdateOperation( String receipt, int amountOfSteps )
    {
        StatusInfoInfo info = null;
        boolean notify = false;
        synchronized( this._statusesLock )
        {
            if( receipt == null )
            {
                info = this.currentChildlessStatusInThisThread();
            }
            else
            {
                info = this._statuses.get( receipt );
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
            this.notifyListeners( info, ChangeType.CHANGED, amountOfSteps );
        }
    }

    protected OperationCreationResult doStartOperation( String parentReceipt, Thread thread, String name, int maxSteps )
    {
        StatusInfoInfo info = null;
        synchronized( this._statusesLock )
        {
            StatusInfoInfo parent = null;
            if( parentReceipt == null )
            {
                parent = this.currentChildlessStatusInThisThread();
            }
            else
            {
                parent = this._statuses.get( parentReceipt );
            }
            info = new StatusInfoInfo( parent, new StatusInfoImpl( parent == null ? null : parent.getStatusInfo(),
                this.newID(), name, thread, maxSteps ), UUID.randomUUID().toString() );
            if( parent != null )
            {
                parent.getChildren().add( info );
            }

            this._statuses.put( info.getReceipt(), info );
        }

        this.notifyListeners( info, ChangeType.BEGAN, 0 );
        return new OperationCreationResultImpl( info.getStatusInfo().getID(), info.getReceipt() );
    }

    protected boolean doEndOperation( String receipt )
    {
        List<StatusInfoInfo> endedOperations = new LinkedList<StatusInfoInfo>();
        Set<String> endedOperationReceipts = new HashSet<String>();
        boolean result = false;
        synchronized( this._statusesLock )
        {
            result = this._statuses.containsKey( receipt );
            if( result )
            {
                StatusInfoInfo info = this.currentChildlessStatusInSameThread( receipt );
                boolean matched = false;
                while( info != null && !matched )
                {
                    if( info.getChildren().size() <= 1 )
                    {
                        this._statuses.remove( info.getReceipt() );
                        endedOperations.add( info );
                        endedOperationReceipts.add( info.getReceipt() );
                        if( info.getParent() != null )
                        {
                            info.getParent().getChildren().remove( info );
                        }
                    }
                    matched = info.getReceipt().equals( receipt );
                    info = info.getParent();
                }
            }
            //            else
            //            {
            // TODO maybe just ignore instead of throwing?
            //throw new NoOperationInProgressException( "Could not find operation with receipt " + receipt
            //    + " to end." );
            //            }
        }

        // Notify listeners
        this.notifyListeners( endedOperations, ChangeType.ENDED, 0 );

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

        return result;
    }

    protected OperationSnapshot doGetSnapshot()
    {
        synchronized( this._statusesLock )
        {
            Set<String> receipts = new HashSet<String>( this._statuses.keySet() );
            Map<String, Integer> dedicatedListenerAmounts = new HashMap<String, Integer>();
            int amountOfListeners = 0;
            synchronized( this._listenersLock )
            {
                amountOfListeners = this._listeners.size();
                for( StatusListenerInfo info : this._listeners )
                {
                    String receipt = info.getAssociatedStatusReceipt();
                    if( receipt != null )
                    {
                        Integer current = dedicatedListenerAmounts.get( receipt );
                        if( current == null )
                        {
                            current = 0;
                        }
                        ++current;
                        dedicatedListenerAmounts.put( receipt, current );
                    }
                }
            }

            List<ThreadSnapshot> threadSnapshots = new ArrayList<ThreadSnapshot>();
            while( !receipts.isEmpty() )
            {
                List<StatusInfoSnapshot> infoSnapshots = new ArrayList<StatusInfoSnapshot>();
                StatusInfoInfo info = this.currentChildlessStatusInSameThread( receipts.iterator().next() );
                Thread startThread = info.getStatusInfo().getThread();
                while( info != null && info.getStatusInfo().getThread().equals( startThread ) )
                {
                    String receipt = info.getReceipt();
                    infoSnapshots.add( new StatusInfoSnapshotImpl( info.getStatusInfo(), dedicatedListenerAmounts
                        .containsKey( receipt ) ? dedicatedListenerAmounts.get( receipt ) : 0 ) );
                    receipts.remove( receipt );
                    info = info.getParent();
                }
                threadSnapshots.add( new ThreadSnapshotImpl( startThread, infoSnapshots ) );
            }

            return new OperationSnapshotImpl( threadSnapshots, amountOfListeners );
        }
    }

    protected void notifyListeners( StatusInfoInfo info, ChangeType type, int stepsAdded )
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
                listener.operationChanged( statusInfo, type, stepsAdded );
            }
        }
    }

    protected void notifyListeners( Iterable<StatusInfoInfo> infos, ChangeType type, int stepsAdded )
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
                    listener.operationChanged( statusInfo, type, stepsAdded );
                }
            }
        }
    }

    protected String newID()
    {
        return UUID.randomUUID().toString();
    }

    protected StatusInfoInfo currentChildlessStatusInThisThread()
    {
        return this.currentChildlessStatusInSameThread( Thread.currentThread() );
    }

    protected StatusInfoInfo currentChildlessStatusInSameThread( String receipt )
    {
        StatusInfoInfo result = this._statuses.get( receipt );
        if( result != null )
        {
            result = this.currentChildlessStatusInSameThread( result.getStatusInfo().getThread() );
        }

        return result;
    }

    protected StatusInfoInfo currentChildlessStatusInSameThread( final Thread thread )
    {
        return Iterables.first( Iterables.filter( new Specification<StatusInfoInfo>()
        {
            @Override
            public boolean satisfiedBy( StatusInfoInfo item )
            {
                return item.getChildren().isEmpty() && thread.equals( item.getStatusInfo().getThread() );
            }
        }, this._statuses.values() ) );
    }
}
