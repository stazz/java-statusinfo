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
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;
import org.statusinfo.api.OperationCreationResult;
import org.statusinfo.api.StatusInfo;
import org.statusinfo.api.StatusInfoListener;
import org.statusinfo.api.StatusInfoListener.ChangeType;

/**
 * 
 * @author 2011 Stanislav Muhametsin
 */
public class ConcurrentStatusInfoListenerTest extends AbstractStatusInfoTest
{

    @Test
    public void endOperationFromAnotherThreadSucceeds()
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
        new Thread( new Runnable()
        {

            @Override
            public void run()
            {
                getStatusInfo().endOperation( creation.getReceipt() );
            }
        } ).start();

        if( !latch.await( 1000, TimeUnit.MILLISECONDS ) )
        {
            throw new Exception( "Did not receive notification in time." );
        }
    }
}
