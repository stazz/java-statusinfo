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

import java.util.concurrent.CountDownLatch;

import org.junit.Assert;
import org.junit.Test;
import org.statusinfo.api.OperationCreationResult;
import org.statusinfo.api.StatusInfo;
import org.statusinfo.api.StatusInfoListener;

/**
 * 
 * @author 2011 Stanislav Muhametsin
 */
public class StatusInfoListenerTest extends AbstractStatusInfoTest
{

    @Test
    public void testEndingOperation()
        throws Exception
    {
        final CountDownLatch latch = new CountDownLatch( 1 );
        final OperationCreationResult creation = this.getStatusInfo().startOperation( OPERATION_NAME );
        this.getStatusInfo().addStatusInfoListener( new StatusInfoListener()
        {

            @Override
            public void operationChanged( StatusInfo statusInfo, ChangeType changeType, int stepsAdded )
            {
                if( statusInfo.getID().equals( creation.getID() ) && OPERATION_NAME.equals( statusInfo.getName() )
                    && ChangeType.ENDED.equals( changeType ) )
                {
                    latch.countDown();
                }
            }

            @Override
            public boolean isInterestedInStatusInfo( StatusInfo statusInfo )
            {
                return true;
            }
        } );
        this.getStatusInfo().endOperation( creation.getReceipt() );
        Assert.assertEquals( "Amount of triggers must be zero.", 0, latch.getCount() );
    }

    @Test
    public void testEndingOperationWithDedicatedListener()
        throws Exception
    {
        final CountDownLatch latch = new CountDownLatch( 2 );
        final OperationCreationResult creation = this.getStatusInfo().startOperation( OPERATION_NAME );
        this.getStatusInfo().addStatusInfoListenerUntilEndOfCurrentOperation( new StatusInfoListener()
        {

            @Override
            public void operationChanged( StatusInfo statusInfo, ChangeType changeType, int stepsAdded )
            {
                latch.countDown();
            }

            @Override
            public boolean isInterestedInStatusInfo( StatusInfo statusInfo )
            {
                return true;
            }
        } );
        this.getStatusInfo().endOperation( creation.getReceipt() );
        this.getStatusInfo().endOperation( this.getStatusInfo().startOperation( OPERATION_NAME ).getReceipt() );
        Assert.assertEquals( "Amount of triggers must be one.", 1, latch.getCount() );
    }

    @Test
    public void testNestedOperationsWithDedicatedListener()
        throws Exception
    {
        final CountDownLatch latch = new CountDownLatch( 3 );
        final OperationCreationResult creation = this.getStatusInfo().startOperation( OPERATION_NAME );
        this.getStatusInfo().addStatusInfoListenerUntilEndOfCurrentOperation( new StatusInfoListener()
        {

            @Override
            public void operationChanged( StatusInfo statusInfo, ChangeType changeType, int stepsAdded )
            {
                latch.countDown();
            }

            @Override
            public boolean isInterestedInStatusInfo( StatusInfo statusInfo )
            {
                return true;
            }
        } );
        OperationCreationResult creation2 = this.getStatusInfo().startOperation( OPERATION_NAME );
        Assert.assertEquals( "Amount of triggers must be two.", 2, latch.getCount() );
        this.getStatusInfo().endOperation( creation2.getReceipt() );
        this.getStatusInfo().endOperation( creation.getReceipt() );
        Assert.assertEquals( "Amount of triggers must be zero.", 0, latch.getCount() );
    }

    @Test
    public void testNestedOperationsWithDedicatedListenerEndingOuterOperationWithoutEndingInner()
        throws Exception
    {
        final CountDownLatch latch = new CountDownLatch( 3 );
        final OperationCreationResult creation = this.getStatusInfo().startOperation( OPERATION_NAME );
        this.getStatusInfo().addStatusInfoListenerUntilEndOfCurrentOperation( new StatusInfoListener()
        {

            @Override
            public void operationChanged( StatusInfo statusInfo, ChangeType changeType, int stepsAdded )
            {
                latch.countDown();
            }

            @Override
            public boolean isInterestedInStatusInfo( StatusInfo statusInfo )
            {
                return true;
            }
        } );
        OperationCreationResult creation2 = this.getStatusInfo().startOperation( OPERATION_NAME );
        Assert.assertEquals( "Amount of triggers must be two.", 2, latch.getCount() );
        this.getStatusInfo().endOperation( creation.getReceipt() );
        Assert.assertEquals( "Amount of triggers must be zero.", 0, latch.getCount() );
    }
}
