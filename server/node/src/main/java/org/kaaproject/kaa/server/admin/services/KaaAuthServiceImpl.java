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

package org.kaaproject.kaa.server.admin.services;

import static org.kaaproject.kaa.server.admin.shared.util.Utils.isEmpty;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.lang.RandomStringUtils;
import org.kaaproject.kaa.common.dto.KaaAuthorityDto;
import org.kaaproject.kaa.common.dto.admin.AuthResultDto;
import org.kaaproject.kaa.common.dto.admin.AuthResultDto.Result;
import org.kaaproject.kaa.common.dto.admin.ResultCode;
import org.kaaproject.kaa.server.admin.services.dao.UserFacade;
import org.kaaproject.kaa.server.admin.services.entity.AuthUserDto;
import org.kaaproject.kaa.server.admin.services.entity.Authority;
import org.kaaproject.kaa.server.admin.services.entity.User;
import org.kaaproject.kaa.server.admin.services.messaging.MessagingService;
import org.kaaproject.kaa.server.admin.shared.services.KaaAdminServiceException;
import org.kaaproject.kaa.server.admin.shared.services.KaaAuthService;
import org.kaaproject.kaa.server.admin.shared.services.ServiceErrorCode;
import org.kaaproject.kaa.server.admin.shared.util.UrlParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service("kaaAuthService")
public class KaaAuthServiceImpl implements KaaAuthService {

    private static final String ANONYMOUS_USER = "anonymousUser";

    @Autowired
    private UserFacade userFacade;
    
    @Autowired
    private MessagingService messagingService;

    private PasswordEncoder passwordEncoder;

    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public AuthResultDto checkAuth() throws Exception {

        AuthResultDto result = new AuthResultDto();

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (!isLoggedIn(authentication)){
            if (userFacade.isAuthorityExists(KaaAuthorityDto.KAA_ADMIN.name())) {
                result.setAuthResult(Result.NOT_LOGGED_IN);
            } else {
                result.setAuthResult(Result.KAA_ADMIN_NOT_EXISTS);
            }
        } else {
            AuthUserDto authUser = (AuthUserDto)authentication.getPrincipal();
            result.setAuthResult(Result.OK);
            result.setAuthority(authUser.getAuthority());
            result.setTenantId(authUser.getTenantId());
            result.setUsername(authUser.getUsername());
            String displayName = authUser.getUsername();
            if (!isEmpty(authUser.getFirstName()) || !isEmpty(authUser.getLastName())) {
                String name = authUser.getFirstName() + " " + authUser.getLastName();
                displayName += " (" + name.trim() + ")";
            }
            result.setDisplayName(displayName);
        }
        return result;
    }

    private static boolean isLoggedIn(Authentication authentication) {
        if (authentication != null) {
            Object principal = authentication.getPrincipal();
            if (principal != null) {
                String userName;
                if (principal instanceof AuthUserDto) {
                    userName = ((AuthUserDto) principal).getUsername();
                } else {
                    return false;
                }
                return !userName.equals(ANONYMOUS_USER);
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    @Transactional(propagation=Propagation.REQUIRED, rollbackFor=Exception.class)
    public void createKaaAdmin(String username, String password)
            throws KaaAdminServiceException {

        org.kaaproject.kaa.server.admin.services.entity.User userEntity =
                new org.kaaproject.kaa.server.admin.services.entity.User();

        if (!checkPasswordStrength(password)) {
            throw new KaaAdminServiceException("Bad password strength. Password length must be greater than 5 characters.", ServiceErrorCode.GENERAL_ERROR);
        }
        
        userEntity.setUsername(username);
        userEntity.setPassword(passwordEncoder.encode(password));
        userEntity.setEnabled(true);
        userEntity.setTempPassword(false);

        Authority authority = new Authority();
        authority.setAuthority(KaaAuthorityDto.KAA_ADMIN.name());
        authority.setUser(userEntity);
        Collection<Authority> authorities = new ArrayList<Authority>();
        authorities.add(authority);
        userEntity.setAuthorities(authorities);

        userFacade.save(userEntity);
    }

    @Transactional(propagation=Propagation.REQUIRED, rollbackFor=Exception.class)
    public ResultCode changePassword(String username, String oldPassword,
            String newPassword) throws Exception {
        User userEntity = userFacade.findByUserName(username);
        if (userEntity == null) {
            return ResultCode.USER_NOT_FOUND;
        }
        if (!passwordEncoder.matches(oldPassword, userEntity.getPassword())) {
            return ResultCode.OLD_PASSWORD_MISMATCH;
        }
        if (!checkPasswordStrength(newPassword)) {
            return ResultCode.BAD_PASSWORD_STRENGTH;
        }
        userEntity.setPassword(passwordEncoder.encode(newPassword));
        userEntity.setTempPassword(false);
        userFacade.save(userEntity);
        return ResultCode.OK;
    }

    private boolean checkPasswordStrength(String password) {
        return password.length()>=6;
    }

    @Transactional(propagation=Propagation.REQUIRED, rollbackFor=Exception.class)
    public ResultCode checkUserNameOccupied(String username, Long userId) throws Exception {
        User userEntity = userFacade.checkUserNameOccupied(username, userId);
        if (userEntity == null) {
            return ResultCode.OK;
        } else {
            return ResultCode.USERNAME_EXISTS;
        }
    }

    @Transactional(propagation=Propagation.REQUIRED, rollbackFor=Exception.class)
    public ResultCode checkEmailOccupied(String email, Long userId) throws Exception {
        User userEntity = userFacade.checkEmailOccupied(email, userId);
        if (userEntity == null) {
            return ResultCode.OK;
        } else {
            return ResultCode.EMAIL_EXISTS;
        }
    }

    @Transactional(propagation=Propagation.REQUIRED, rollbackFor=Exception.class)
    public ResultCode checkUsernameOrEmailExists(String usernameOrEmail)
            throws Exception {
        return checkUserAndEmailExists(userFacade.findByUsernameOrMail(usernameOrEmail));
    }

    @Transactional(propagation=Propagation.REQUIRED, rollbackFor=Exception.class)
    public ResultCode sendPasswordResetLinkByEmail(String usernameOrEmail)
            throws Exception {
        User userEntity = userFacade.findByUsernameOrMail(usernameOrEmail);
        ResultCode result = checkUserAndEmailExists(userEntity);
        if (result == ResultCode.OK) {
            String passwordResetHash = RandomStringUtils.randomAlphanumeric(UrlParams.PASSWORD_RESET_HASH_LENGTH);
            userEntity.setPasswordResetHash(passwordResetHash);
            userFacade.save(userEntity);
            messagingService.sendPasswordResetLink(passwordResetHash, userEntity.getUsername(), userEntity.getMail());
        }
        return result;
    }
    
    private ResultCode checkUserAndEmailExists(User userEntity) {
        if (userEntity == null) {
            return ResultCode.USER_OR_EMAIL_NOT_FOUND;
        } else if (isEmpty(userEntity.getMail())) {
            return ResultCode.USER_EMAIL_NOT_DEFINED;
        } else {
            return ResultCode.OK;
        }
    }

    @Transactional(propagation=Propagation.REQUIRED, rollbackFor=Exception.class)
    public ResultCode resetPasswordByResetHash(String passwordResetHash)
            throws Exception {
        User userEntity = userFacade.findByPasswordResetHash(passwordResetHash);
        if (userEntity == null) {
            return ResultCode.USER_NOT_FOUND;
        } else if (isEmpty(userEntity.getMail())) {
            return ResultCode.USER_EMAIL_NOT_DEFINED;
        }
        userEntity.setPasswordResetHash(null);
        String generatedPassword = RandomStringUtils.randomAlphanumeric(User.TEMPORARY_PASSWORD_LENGTH);
        userEntity.setPassword(passwordEncoder.encode(generatedPassword));
        userEntity.setTempPassword(true);
        userFacade.save(userEntity);
        messagingService.sendPasswordAfterReset(userEntity.getUsername(), generatedPassword, userEntity.getMail());
        return ResultCode.OK;
    }

}
