/*
 * Copyright 2014-2016 CyberVision, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kaaproject.kaa.server.control;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.common.dto.ApplicationDto;

/**
 * The Class ControlServerApplicationIT.
 */
public class ControlServerApplicationIT extends AbstractTestControlServer {

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.control.AbstractTestControlServer#createTenantDeveloperNeeded()
     */
    @Override
    protected boolean createTenantDeveloperNeeded() {
        return false;
    }

    /**
     * Test create application.
     *
     * @throws Exception the exception
     */
    @Test
    public void testCreateApplication() throws Exception {
        ApplicationDto application = createApplication();
        Assert.assertFalse(strIsEmpty(application.getId()));
        Assert.assertFalse(strIsEmpty(application.getApplicationToken()));
    }

    /**
     * Test get application.
     *
     * @throws Exception the exception
     */
    @Test
    public void testGetApplication() throws Exception {
        ApplicationDto application = createApplication();

        ApplicationDto storedApplication = client.getApplicationByApplicationToken(application.getApplicationToken());

        Assert.assertNotNull(storedApplication);
        assertApplicationsEquals(application, storedApplication);
    }

    /**
     * Test get applications.
     *
     * @throws Exception the exception
     */
    @Test
    public void testGetApplications() throws Exception {
        List<ApplicationDto> applications  = new ArrayList<ApplicationDto>(10);
        for (int i=0;i<10;i++) {
            ApplicationDto application = createApplication(tenantAdminDto);
            applications.add(application);
        }

        Collections.sort(applications, new IdComparator());

        List<ApplicationDto> storedApplications = client.getApplications();

        Collections.sort(storedApplications, new IdComparator());

        Assert.assertEquals(applications.size(), storedApplications.size());
        for (int i=0;i<applications.size();i++) {
            ApplicationDto application = applications.get(i);
            ApplicationDto storedApplication = storedApplications.get(i);
            assertApplicationsEquals(application, storedApplication);
        }
    }

    /**
     * Test update application.
     *
     * @throws Exception the exception
     */
    @Test
    public void testUpdateApplication() throws Exception {
        ApplicationDto application = createApplication();

        application.setName(generateString(APPLICATION));

        ApplicationDto updatedApplication = client
                .editApplication(application);

        assertApplicationsEquals(updatedApplication, application);
    }

    /**
     * Assert applications equals.
     *
     * @param application the application
     * @param storedApplication the stored application
     */
    private void assertApplicationsEquals(ApplicationDto application, ApplicationDto storedApplication) {
        Assert.assertEquals(application.getId(), storedApplication.getId());
        Assert.assertEquals(application.getName(), storedApplication.getName());
        Assert.assertEquals(application.getTenantId(), storedApplication.getTenantId());
        Assert.assertEquals(application.getApplicationToken(), storedApplication.getApplicationToken());
    }

}
