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

package org.kaaproject.kaa.server.admin.controller;

import io.swagger.annotations.Api;
import org.kaaproject.kaa.server.admin.services.dao.UserFacade;
import org.kaaproject.kaa.server.admin.services.util.Utils;
import org.kaaproject.kaa.server.admin.shared.services.AdminUIService;
import org.kaaproject.kaa.server.admin.shared.services.ApplicationService;
import org.kaaproject.kaa.server.admin.shared.services.ConfigurationService;
import org.kaaproject.kaa.server.admin.shared.services.CtlService;
import org.kaaproject.kaa.server.admin.shared.services.DeviceManagementService;
import org.kaaproject.kaa.server.admin.shared.services.EventService;
import org.kaaproject.kaa.server.admin.shared.services.GroupService;
import org.kaaproject.kaa.server.admin.shared.services.KaaAdminServiceException;
import org.kaaproject.kaa.server.admin.shared.services.KaaAuthService;
import org.kaaproject.kaa.server.admin.shared.services.LoggingService;
import org.kaaproject.kaa.server.admin.shared.services.NotificationService;
import org.kaaproject.kaa.server.admin.shared.services.ProfileService;
import org.kaaproject.kaa.server.admin.shared.services.SdkService;
import org.kaaproject.kaa.server.admin.shared.services.ServiceErrorCode;
import org.kaaproject.kaa.server.admin.shared.services.TenantService;
import org.kaaproject.kaa.server.admin.shared.services.UserService;
import org.kaaproject.kaa.server.admin.shared.services.VerifierService;
import org.kaaproject.kaa.server.control.service.ControlService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * The Class AbstractAdminController.
 */
@Api(value = "Admin REST API",
        description = "Provides function for manage Kaa cluster", basePath = "/kaaAdmin/rest")
@Controller
@RequestMapping("api")
public abstract class AbstractAdminController {

    /**
     * The Constant LOG.
     */
    private static final Logger LOG = LoggerFactory.getLogger(AbstractAdminController.class);

    /**
     * The kaa admin UI service.
     */
    @Autowired
    AdminUIService adminUIService;

    /**
     * The kaa application service.
     */
    @Autowired
    ApplicationService applicationService;

    /**
     * The kaa configuration service.
     */
    @Autowired
    ConfigurationService configurationService;

    /**
     * The kaa CTL service.
     */
    @Autowired
    CtlService ctlService;

    /**
     * The kaa device management service.
     */
    @Autowired
    DeviceManagementService deviceManagementService;

    /**
     * The kaa event service.
     */
    @Autowired
    EventService eventService;

    /**
     * The kaa group service.
     */
    @Autowired
    GroupService groupService;

    /**
     * The kaa logging service.
     */
    @Autowired
    LoggingService loggingService;

    /**
     * The kaa notification service.
     */
    @Autowired
    NotificationService notificationService;

    /**
     * The kaa profile service.
     */
    @Autowired
    ProfileService profileService;

    /**
     * The kaa sdk service.
     */
    @Autowired
    SdkService sdkService;

    /**
     * The kaa tenant service.
     */
    @Autowired
    TenantService tenantService;

    /**
     * The kaa user service.
     */
    @Autowired
    UserService userService;

    /**
     * The kaa verifier service.
     */
    @Autowired
    VerifierService verifierService;

    /**
     * The kaa auth service.
     */
    @Autowired
    KaaAuthService kaaAuthService;

    /**
     * The user facade.
     */
    @Autowired
    UserFacade userFacade;
    /**
     * The control service.
     */
    @Autowired
    ControlService controlService;

    /**
     * The password encoder.
     */
    @Autowired
    @Qualifier("encoder")
    PasswordEncoder passwordEncoder;


    /**
     * Gets the file content.
     *
     * @param file the file
     * @return the file content
     * @throws KaaAdminServiceException the kaa admin service exception
     */
    protected byte[] getFileContent(MultipartFile file) throws KaaAdminServiceException {
        if (!file.isEmpty()) {
            LOG.debug("Uploading file with name '{}'", file.getOriginalFilename());
            try {
                return file.getBytes();
            } catch (IOException e) {
                throw Utils.handleException(e);
            }
        } else {
            LOG.error("No file found in post request!");
            throw new KaaAdminServiceException("No file found in post request!", ServiceErrorCode.FILE_NOT_FOUND);
        }
    }

}
