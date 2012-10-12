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

package org.statusinfo.implementation.test;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.statusinfo.api.OperationCreationResult;
import org.statusinfo.api.OperationSnapshot;
import org.statusinfo.api.StatusInfo;
import org.statusinfo.api.StatusInfoListener;
import org.statusinfo.api.StatusInfoService;
import org.statusinfo.api.StatusInfoSnapshot;
import org.statusinfo.api.ThreadSnapshot;
import org.statusinfo.implementation.SnapshotToString;

/**
 * 
 * @author 2011 Stanislav Muhametsin
 */
public class OperationSnapshotTest extends AbstractStatusInfoTest
{

    @Test
    public void testEmptySnapshot()
    {
        this.verifyEmptySnapshot();
    }

    @Test
    public void testSnapshotDuringSimpleOperation()
        throws Exception
    {
        this.verifyEmptySnapshot();
        OperationCreationResult creation = this.getStatusInfo().startOperation( OPERATION_NAME );
        this.verifyOperationSnapshot( this.toOperationSnapshot( 0, this.toThreadSnapshot( Thread.currentThread(), this
            .toSnapshot( 0, null, creation.getID(), OPERATION_NAME, Thread.currentThread(),
                StatusInfoService.NO_MAX_STEPS, 0 ) ) ) );
        StatusInfoListener listener = new StatusInfoListener()
        {

            @Override
            public void operationChanged( StatusInfo statusInfo, ChangeType changeType, int stepsAdded )
            {
            }

            @Override
            public boolean isInterestedInStatusInfo( StatusInfo statusInfo )
            {
                return true;
            }
        };
        this.getStatusInfo().addStatusInfoListener( listener );
        this.verifyOperationSnapshot( this.toOperationSnapshot( 1, this.toThreadSnapshot( Thread.currentThread(), this
            .toSnapshot( 0, null, creation.getID(), OPERATION_NAME, Thread.currentThread(),
                StatusInfoService.NO_MAX_STEPS, 0 ) ) ) );
        this.getStatusInfo().addStatusInfoListenerUntilEndOfCurrentOperation( new StatusInfoListener()
        {

            @Override
            public void operationChanged( StatusInfo statusInfo, ChangeType changeType, int stepsAdded )
            {
            }

            @Override
            public boolean isInterestedInStatusInfo( StatusInfo statusInfo )
            {
                return true;
            }
        } );
        this.verifyOperationSnapshot( this.toOperationSnapshot( 2, this.toThreadSnapshot( Thread.currentThread(), this
            .toSnapshot( 1, null, creation.getID(), OPERATION_NAME, Thread.currentThread(),
                StatusInfoService.NO_MAX_STEPS, 0 ) ) ) );
        this.getStatusInfo().endOperation( creation.getReceipt() );
        this.verifyOperationSnapshot( this.toOperationSnapshot( 1 ) );
        this.getStatusInfo().removeStatusInfoListener( listener );
        this.verifyEmptySnapshot();
    }

    protected void verifyOperationSnapshot( OperationSnapshot generatedSnapshot )
    {
        Assert.assertEquals( "Both snapshots must be the same", this.getStatusInfo().getSnapshotOfCurrentState(),
            generatedSnapshot );
    }

    protected void verifyEmptySnapshot()
    {
        this.verifyOperationSnapshot( this.toOperationSnapshot( 0 ) );
    }

    protected OperationSnapshot toOperationSnapshot( final int amountOfListeners, ThreadSnapshot... threadSnapshots )
    {
        final List<ThreadSnapshot> list = Arrays.asList( threadSnapshots );
        return new OperationSnapshot()
        {

            @Override
            public Iterable<ThreadSnapshot> getThreadSnapshots()
            {
                return list;
            }

            @Override
            public int getAmountOfAllListeners()
            {
                return amountOfListeners;
            }

            @Override
            public String toString()
            {
                return SnapshotToString.toString( this );
            }
        };
    }

    protected ThreadSnapshot toThreadSnapshot( final Thread thread, StatusInfoSnapshot... snapshots )
    {
        final List<StatusInfoSnapshot> list = Arrays.asList( snapshots );
        return new ThreadSnapshot()
        {

            @Override
            public Thread getThread()
            {
                return thread;
            }

            @Override
            public List<StatusInfoSnapshot> getOperationStatuses()
            {
                return list;
            }

            @Override
            public String toString()
            {
                return SnapshotToString.toString( this );
            }
        };
    }

    protected StatusInfoSnapshot toSnapshot( final int amountOfDedicatedListeners, final StatusInfo parent,
        final String ID, final String name, final Thread thread, final int maxSteps, final int currentSteps )
    {
        return new StatusInfoSnapshot()
        {

            @Override
            public StatusInfo getStatusInfo()
            {
                return new StatusInfo()
                {

                    @Override
                    public Thread getThread()
                    {
                        return thread;
                    }

                    @Override
                    public String getName()
                    {
                        return name;
                    }

                    @Override
                    public int getMaxSteps()
                    {
                        return maxSteps;
                    }

                    @Override
                    public String getID()
                    {
                        return ID;
                    }

                    @Override
                    public int getCurrentSteps()
                    {
                        return currentSteps;
                    }

                    @Override
                    public StatusInfo getParent()
                    {
                        return parent;
                    }

                    @Override
                    public String toString()
                    {
                        return SnapshotToString.toString( this );
                    }
                };
            }

            @Override
            public int getAmountOfDedicatedListeners()
            {
                return amountOfDedicatedListeners;
            }

            @Override
            public String toString()
            {
                return SnapshotToString.toString( this );
            }
        };
    }

}
