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
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaDto;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaExportMethod;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaMetaInfoDto;
import org.kaaproject.kaa.common.dto.file.FileData;
import org.kaaproject.kaa.server.admin.services.util.Utils;
import org.kaaproject.kaa.server.admin.servlet.ServletUtils;
import org.kaaproject.kaa.server.admin.shared.services.KaaAdminServiceException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Api(value = "Common Type Library", description = "Provides function for manage CTL", basePath = "/kaaAdmin/rest")
@Controller
public class CtlController extends AbstractAdminController {

    /**
     * The Constant BUFFER.
     */
    private static final int BUFFER = 1024 * 100;

    /**
     * Saves a CTL schema.
     *
     * @param body             the ctl body
     * @param applicationToken the application token
     * @param tenantId         id of the tenant
     * @return CTL schema info
     * @throws KaaAdminServiceException the kaa admin service exception
     */
    @ApiOperation(value = "Create CTL schema",
            notes = "Creates a CTL schema with application token. Only authorized users are allowed to perform this operation.")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "The CTL schema body provided is invalid"),
            @ApiResponse(code = 401, message = "The user is not authenticated or invalid credentials were provided"),
            @ApiResponse(code = 403, message = "The tenant ID of the CTL schema does not match the tenant ID of the authenticated user"),
            @ApiResponse(code = 500, message = "An unexpected error occurred on the server side")})
    @RequestMapping(value = "CTL/saveSchema", params = {"body"}, method = RequestMethod.POST)
    @ResponseBody
    public CTLSchemaDto saveCTLSchemaWithAppToken(
            @ApiParam(name = "body", value = "The CTL schema structure", required = true)
            @RequestParam String body,
            @ApiParam(name = "tenantId", value = "A unique tenant identifier", required = false)
            @RequestParam(required = false) String tenantId,
            @ApiParam(name = "applicationToken", value = "A unique auto-generated application identifier", required = false)
            @RequestParam(required = false) String applicationToken) throws KaaAdminServiceException {
        return ctlService.saveCTLSchemaWithAppToken(body, tenantId, applicationToken);
    }

    /**
     * Removes a CTL schema by its fully qualified name and version number.
     *
     * @param fqn              the fqn
     * @param version          the version
     * @param tenantId         id of the tenant
     * @param applicationToken the application token
     * @throws KaaAdminServiceException the kaa admin service exception
     */
    @ApiOperation(value = "Delete CTL schema",
            notes = "Deletes a CTL schema. Only authorized users are allowed to perform this operation.")
    @ApiResponses(value = {
            @ApiResponse(code = 401, message = "The user is not authenticated or invalid credentials were provided"),
            @ApiResponse(code = 403, message = "The tenant ID of the CTL schema does not match the tenant ID of the authenticated user"),
            @ApiResponse(code = 404, message = "A CTL schema with the specified fully qualified name, version number, tenant and application identifiers " +
                    "does not exist"),
            @ApiResponse(code = 500, message = "An unexpected error occurred on the server side")})
    @RequestMapping(value = "CTL/deleteSchema", params = {"fqn", "version"}, method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    public void deleteCTLSchemaByFqnVersionTenantIdAndApplicationToken(
            @ApiParam(name = "fqn", value = "The fully qualified name of the CTL schema", required = true)
            @RequestParam String fqn,
            @ApiParam(name = "version", value = "The version number of the CTL schema", required = true)
            @RequestParam int version,
            @ApiParam(name = "tenantId", value = "A unique tenant identifier", required = false)
            @RequestParam(required = false) String tenantId,
            @ApiParam(name = "applicationToken", value = "A unique auto-generated application identifier", required = false)
            @RequestParam(required = false) String applicationToken)
            throws KaaAdminServiceException {
        ctlService.deleteCTLSchemaByFqnVersionTenantIdAndApplicationToken(fqn, version, tenantId, applicationToken);
    }

    /**
     * Retrieves a CTL schema by its fully qualified name and version number.
     *
     * @param fqn              the fqn
     * @param version          the version
     * @param tenantId         id of the tenant
     * @param applicationToken the application token
     * @return CTL schema info
     * @throws KaaAdminServiceException the kaa admin service exception
     */
    @ApiOperation(value = "Get CTL schema",
            notes = "Returns a CTL schema. Only authorized users are allowed to perform this operation.")
    @ApiResponses(value = {
            @ApiResponse(code = 401, message = "The user is not authenticated or invalid credentials were provided"),
            @ApiResponse(code = 403, message = "The tenant ID of the CTL schema does not match the tenant ID of the authenticated user"),
            @ApiResponse(code = 404, message = "A CTL schema with the specified fully qualified name and version number does not exist."),
            @ApiResponse(code = 500, message = "An unexpected error occurred on the server side")})
    @RequestMapping(value = "CTL/getSchema", params = {"fqn", "version"}, method = RequestMethod.GET)
    @ResponseBody
    public CTLSchemaDto getCTLSchemaByFqnVersionTenantIdAndApplicationToken(
            @ApiParam(name = "fqn", value = "The fully qualified name of the CTL schema", required = true)
            @RequestParam String fqn,
            @ApiParam(name = "version", value = "The version number of the CTL schema", required = true)
            @RequestParam int version,
            @ApiParam(name = "tenantId", value = "A unique tenant identifier", required = false)
            @RequestParam(required = false) String tenantId,
            @ApiParam(name = "applicationToken", value = "A unique auto-generated application identifier", required = false)
            @RequestParam(required = false) String applicationToken)
            throws KaaAdminServiceException {
        return ctlService.getCTLSchemaByFqnVersionTenantIdAndApplicationToken(fqn, version, tenantId, applicationToken);
    }

    /**
     * Retrieves a CTL schema by its id.
     *
     * @param id the CTL schema id
     * @return CTL schema info
     * @throws KaaAdminServiceException the kaa admin service exception
     */
    @ApiOperation(value = "Get CTL schema by it's id",
            notes = "Returns a CTL schema. Only authorized users are allowed to perform this operation.")
    @ApiResponses(value = {
            @ApiResponse(code = 401, message = "The user is not authenticated or invalid credentials were provided"),
            @ApiResponse(code = 403, message = "The tenant ID of the CTL schema does not match the tenant ID of the authenticated user"),
            @ApiResponse(code = 404, message = "A CTL schema with the specified id does not exist."),
            @ApiResponse(code = 500, message = "An unexpected error occurred on the server side")})
    @RequestMapping(value = "CTL/getSchemaById", params = {"id"}, method = RequestMethod.GET)
    @ResponseBody
    public CTLSchemaDto getCTLSchemaById(
            @ApiParam(name = "id", value = "A unique CTL schema identifier", required = true)
            @RequestParam String id) throws KaaAdminServiceException {
        return ctlService.getCTLSchemaById(id);
    }

    /**
     * Checks if CTL schema with same fqn is already exists in the sibling application.
     *
     * @param fqn              the fqn
     * @param tenantId         id of the tenant
     * @param applicationToken the application token
     * @return true if CTL schema with same fqn is already exists in other scope
     * @throws KaaAdminServiceException the kaa admin service exception
     */
    @ApiOperation(value = "Check CTL schema FQN",
            notes = "Checks if CTL schema with same fqn is already exists in the sibling application. Only authorized users are allowed to perform this " +
                    "operation.")
    @ApiResponses(value = {
            @ApiResponse(code = 401, message = "The user is not authenticated or invalid credentials were provided"),
            @ApiResponse(code = 403, message = "The Tenant ID of the CTL schema does not match the Tenant ID of the authenticated user"),
            @ApiResponse(code = 500, message = "An unexpected error occurred on the server side")})
    @RequestMapping(value = "CTL/checkFqn", params = {"fqn"}, method = RequestMethod.GET)
    @ResponseBody
    public boolean checkFqnExistsWithAppToken(
            @ApiParam(name = "fqn", value = "The fully qualified name of the CTL schema", required = true)
            @RequestParam String fqn,
            @ApiParam(name = "tenantId", value = "A unique tenant identifier", required = false)
            @RequestParam(required = false) String tenantId,
            @ApiParam(name = "applicationToken", value = "A unique auto-generated application identifier", required = false)
            @RequestParam(required = false) String applicationToken)
            throws KaaAdminServiceException {
        return ctlService.checkFqnExistsWithAppToken(fqn, tenantId, applicationToken);
    }

    /**
     * Promote existing CTL schema meta info from application to tenant scope
     *
     * @param applicationId the id of application where schema was created
     * @param fqn           the fqn of promoting CTL schema
     * @return CTLSchemaMetaInfoDto the promoted CTL schema meta info object.
     * @throws KaaAdminServiceException the kaa admin service exception
     */
    @ApiOperation(value = "Promote CTL schema from application scope to Tenant",
            notes = "Promote existing CTL schema meta info scope from application to tenant. Only authorized users are allowed to perform this operation.")
    @ApiResponses(value = {
            @ApiResponse(code = 401, message = "The user is not authenticated or invalid credentials were provided"),
            @ApiResponse(code = 403, message = "The Tenant ID of the CTL schema does not match the Tenant ID of the authenticated user"),
            @ApiResponse(code = 500, message = "An unexpected error occurred on the server side")})
    @RequestMapping(value = "CTL/promoteScopeToTenant", method = RequestMethod.POST)
    @ResponseBody
    public CTLSchemaMetaInfoDto promoteScopeToTenant(
            @ApiParam(name = "applicationId", value = "A unique application identifier", required = true)
            @RequestParam String applicationId,
            @ApiParam(name = "fqn", value = "The fully qualified name of the CTL schema", required = true)
            @RequestParam String fqn)
            throws KaaAdminServiceException {
        return ctlService.promoteScopeToTenant(applicationId, fqn);
    }

    /**
     * Retrieves a list of available system CTL schemas.
     *
     * @return CTL schema metadata list
     * @throws KaaAdminServiceException the kaa admin service exception
     */
    @ApiOperation(value = "Get system CTL schemas",
            notes = "Returns a list of available system CTL schemas metadata. Only authorized users are allowed to perform this operation.")
    @ApiResponses(value = {
            @ApiResponse(code = 401, message = "The user is not authenticated or invalid credentials were provided"),
            @ApiResponse(code = 500, message = "An unexpected error occurred on the server side")})
    @RequestMapping(value = "CTL/getSystemSchemas", method = RequestMethod.GET)
    @ResponseBody
    public List<CTLSchemaMetaInfoDto> getSystemLevelCTLSchemas() throws KaaAdminServiceException {
        return ctlService.getSystemLevelCTLSchemas();
    }

    /**
     * Retrieves a list of available CTL schemas for tenant.
     *
     * @return CTL schema metadata list
     * @throws KaaAdminServiceException the kaa admin service exception
     */
    @ApiOperation(value = "Get tenant CTL schemas",
            notes = "Returns a list of available CTL schemas metadata for current tenant. The current user must have one of the following roles: " +
                    "TENANT_ADMIN, TENANT_DEVELOPER or TENANT_USER.")
    @ApiResponses(value = {
            @ApiResponse(code = 401, message = "The user is not authenticated or invalid credentials were provided"),
            @ApiResponse(code = 500, message = "An unexpected error occurred on the server side")})
    @RequestMapping(value = "CTL/getTenantSchemas", method = RequestMethod.GET)
    @ResponseBody
    public List<CTLSchemaMetaInfoDto> getTenantLevelCTLSchemas() throws KaaAdminServiceException {
        return ctlService.getTenantLevelCTLSchemas();
    }

    /**
     * Retrieves a list of available CTL schemas for application.
     *
     * @param applicationToken the application token
     * @return CTL schema metadata list
     * @throws KaaAdminServiceException the kaa admin service exception
     */
    @ApiOperation(value = "Get application CTL schemas",
            notes = "Returns a list of available CTL schemas metadata for an application. The current user must have one of the following roles: " +
                    "TENANT_DEVELOPER or TENANT_USER.")
    @ApiResponses(value = {
            @ApiResponse(code = 401, message = "The user is not authenticated or invalid credentials were provided"),
            @ApiResponse(code = 403, message = "The authenticated user does not have the required role (TENANT_DEVELOPER, or TENANT_USER) or Tenant ID of " +
                    "the application does not match the Tenant ID of the user"),
            @ApiResponse(code = 404, message = "An application with the specified applicationToken does not exist"),
            @ApiResponse(code = 500, message = "An unexpected error occurred on the server side")})
    @RequestMapping(value = "CTL/getApplicationSchemas/{applicationToken}", method = RequestMethod.GET)
    @ResponseBody
    public List<CTLSchemaMetaInfoDto> getApplicationLevelCTLSchemasByAppToken(
            @ApiParam(name = "applicationToken", value = "A unique auto-generated application identifier", required = true)
            @PathVariable String applicationToken) throws KaaAdminServiceException {
        return ctlService.getApplicationLevelCTLSchemasByAppToken(applicationToken);
    }

    /**
     * Exports a CTL schema and, depending on the export method specified, all
     * of its dependencies.
     *
     * @param fqn              - the schema fqn
     * @param version          - the schema version
     * @param method           - the schema export method
     * @param applicationToken the application token
     * @param request          - the http request
     * @param response         - the http response
     * @throws KaaAdminServiceException the kaa admin service exception
     * @see CTLSchemaExportMethod
     */
    @ApiOperation(value = "Export CTL schema",
            notes = "Exports a CTL schema and, depending on the export method specified, all of its dependencies. Only authorized users are allowed to " +
                    "perform this operation.")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Unknown export method specified"),
            @ApiResponse(code = 401, message = "The user is not authenticated or invalid credentials were provided"),
            @ApiResponse(code = 403, message = "The authenticated user does not have the required role (TENANT_DEVELOPER or TENANT_USER) or the Tenant ID " +
                    "of the application does not match the Tenant ID of the authenticated user"),
            @ApiResponse(code = 404, message = "The CTL schema with the given fqn, version and application Id does not exist"),
            @ApiResponse(code = 500, message = "An unexpected error occurred on the server side")})
    @RequestMapping(value = "CTL/exportSchema", params = {"fqn", "version", "method"}, method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    public void exportCTLSchemaByAppToken(
            @ApiParam(name = "fqn", value = "The fully qualified name of the CTL schema", required = true)
            @RequestParam String fqn,
            @ApiParam(name = "version", value = "The version number of the CTL schema", required = true)
            @RequestParam int version,
            @ApiParam(name = "method", value = "The schema export method (either SHALLOW, FLAT, DEEP or LIBRARY)", required = true)
            @RequestParam String method,
            @ApiParam(name = "applicationToken", value = "A unique auto-generated application identifier", required = false)
            @RequestParam(required = false) String applicationToken,
            HttpServletRequest request, HttpServletResponse response) throws KaaAdminServiceException {
        try {
            FileData output = ctlService.exportCTLSchemaByAppToken(fqn, version, applicationToken, CTLSchemaExportMethod.valueOf(method.toUpperCase()));
            ServletUtils.prepareDisposition(request, response, output.getFileName());
            response.setContentType(output.getContentType());
            response.setContentLength(output.getFileData().length);
            response.setBufferSize(BUFFER);
            response.getOutputStream().write(output.getFileData());
            response.flushBuffer();
        } catch (Exception cause) {
            throw Utils.handleException(cause);
        }
    }

    /**
     * Get existing flat schema.
     *
     * @param id
     *            of the CTL schema
     * @return the flat schema string
     * @throws KaaAdminServiceException
     *             the kaa admin service exception
     */
    @ApiOperation(value = "Get flat schema by it's id",
            notes = "Returns a flat schema. Only authorized users are allowed to perform this operation.")
    @RequestMapping(value = "CTL/getFlatSchemaByCtlSchemaId", params = { "id" }, method = RequestMethod.GET)
    @ApiResponses(value = {
            @ApiResponse(code = 401, message = "The user is not authenticated or invalid credentials were provided"),
            @ApiResponse(code = 403, message = "The tenant ID of the CTL schema does not match the tenant ID of the authenticated user"),
            @ApiResponse(code = 404, message = "A CTL schema with the specified id does not exist."),
            @ApiResponse(code = 500, message = "An unexpected error occurred on the server side")})
    @ResponseBody
    public String getFlatSchemaByCtlSchemaId(
            @ApiParam(name = "id", value = "A unique CTL schema identifier", required = true)
            @RequestParam String id) throws KaaAdminServiceException {
        return ctlService.getFlatSchemaByCtlSchemaId(id);
    }

}
