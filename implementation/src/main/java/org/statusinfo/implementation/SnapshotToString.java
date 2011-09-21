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

import org.statusinfo.api.OperationSnapshot;
import org.statusinfo.api.StatusInfo;
import org.statusinfo.api.StatusInfoSnapshot;
import org.statusinfo.api.ThreadSnapshot;

/**
 * 
 * @author 2011 Stanislav Muhametsin
 */
public final class SnapshotToString
{
    public static String toString( OperationSnapshot snapshot )
    {
        return "Operation state(listeners=" + snapshot.getAmountOfAllListeners() + ",threadStates="
            + snapshot.getThreadSnapshots() + ")";
    }

    public static String toString( ThreadSnapshot threadSnapshot )
    {
        return threadSnapshot.getThread().toString() + threadSnapshot.getOperationStatuses();
    }

    public static String toString( StatusInfoSnapshot snapshot )
    {
        return snapshot.getStatusInfo() + "(dedicatedListeners=" + snapshot.getAmountOfDedicatedListeners() + ")";
    }

    public static String toString( StatusInfo info )
    {
        return info.getName() + "(id=" + info.getID() + ",thread=" + info.getThread() + ",steps="
            + info.getCurrentSteps() + "/" + info.getMaxSteps() + ")";
    }
}
