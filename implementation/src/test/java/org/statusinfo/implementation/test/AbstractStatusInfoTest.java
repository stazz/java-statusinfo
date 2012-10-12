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

import org.qi4j.api.common.Visibility;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;
import org.statusinfo.api.StatusInfoService;
import org.statusinfo.api.bootstrap.StatusInfoAssemblerProvider;

/**
 * @author Stanislav Muhametsin
 * 
 */
public class AbstractStatusInfoTest extends AbstractQi4jTest
{

    public static final String OPERATION_NAME = "Testing operations.";

    private StatusInfoService _statusInfo;

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        StatusInfoAssemblerProvider.DEFAULT.getAssembler( Visibility.module ).assemble( module );
    }

    @Override
    public void setUp()
        throws Exception
    {
        super.setUp();
        this._statusInfo = this.module.findService( StatusInfoService.class ).get();
    }

    @Override
    public void tearDown()
        throws Exception
    {
        this._statusInfo = null;

        super.tearDown();
    }

    protected StatusInfoService getStatusInfo()
    {
        return this._statusInfo;
    }
}
