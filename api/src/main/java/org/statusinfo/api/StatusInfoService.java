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
package org.statusinfo.api;

/**
 * @author Stanislav Muhametsin
 * 
 */
public interface StatusInfoService
{
    public static final int NO_MAX_STEPS = -1;

    public interface Operation<ReturnType, ExceptionType extends Throwable>
    {
        ReturnType doOperation()
            throws ExceptionType;
    }

    public <ReturnType, ExceptionType extends Throwable> ReturnType performOperation( String name,
        Operation<ReturnType, ExceptionType> operation )
        throws ExceptionType;

    public <ReturnType, ExceptionType extends Throwable> ReturnType performOperation( String name, int maxSteps,
        Operation<ReturnType, ExceptionType> operation )
        throws ExceptionType;

    public void addStatusInfoListener( StatusInfoListener listener );

    public void addStatusInfoListenerUntilEndOfCurrentOperation( StatusInfoListener listener );

    public void removeStatusInfoListener( StatusInfoListener listener );

    public OperationCreationResult startOperation( String name );

    public OperationCreationResult startOperation( String name, int maxSteps );

    public OperationCreationResult startOperation( Thread thread, String name );

    public OperationCreationResult startOperation( Thread thread, String name, int maxSteps );

    /**
     * This method should be used when one wants to start a sub-operation from another thread.
     * 
     * @param receipt The receipt of the parent operation.
     * @param name The name of the operation to begin.
     * @return {@link OperationCreationResult} with information about current operation.
     */
    public OperationCreationResult startSubOperation( String parentReceipt, String name );

    public boolean endOperation( String receipt );

    public void updateCurrentOperation( int amountOfSteps );

    public void updateOperation( String receipt, int amountOfSteps );

    public OperationSnapshot getSnapshotOfCurrentState();
}
