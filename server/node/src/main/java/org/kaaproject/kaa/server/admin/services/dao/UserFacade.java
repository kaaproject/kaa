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

package org.kaaproject.kaa.server.admin.services.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.RandomStringUtils;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.kaaproject.kaa.common.dto.KaaAuthorityDto;
import org.kaaproject.kaa.common.dto.admin.UserDto;
import org.kaaproject.kaa.server.admin.services.entity.Authority;
import org.kaaproject.kaa.server.admin.services.entity.CreateUserResult;
import org.kaaproject.kaa.server.admin.services.entity.User;
import org.kaaproject.kaa.server.admin.services.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@SuppressWarnings("unchecked")
@Repository("userFacade")
@Transactional("admin")
public class UserFacade {

    private static final String ID_PROPERTY = "id";
    private static final String PASSWORD_RESET_HASH_PROPERTY = "passwordResetHash";
    private static final String MAIL_PROPERTY = "mail";
    private static final String USERNAME_PROPERTY = "username";
    private static final String AUTHORITY_PROPERTY = "authority";
    
    @Autowired
    private SessionFactory adminSessionFactory;
    
    protected Session getSession() {
        return adminSessionFactory.getCurrentSession();
    }
    
    private Criteria getCriteria() {
        return getSession().createCriteria(User.class);
    }

    public Long save(User user) {
        user = (User) getSession().merge(user);
        return user.getId();
    }

    public CreateUserResult saveUserDto(UserDto userDto,
            PasswordEncoder passwordEncoder) throws Exception {

        User user = null;
        String generatedPassword = null;
        if (userDto.getExternalUid() == null
                || userDto.getExternalUid().isEmpty()) {
            user = new User();
            generatedPassword = RandomStringUtils.randomAlphanumeric(User.TEMPORARY_PASSWORD_LENGTH);
            user.setPassword(passwordEncoder.encode(generatedPassword));
            user.setTempPassword(true);
            user.setEnabled(true);
        } else {
            user = findById(Long.valueOf(userDto.getExternalUid()));
        }
        Utils.checkNotNull(user);

        user.setUsername(userDto.getUsername());
        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        user.setMail(userDto.getMail());

        if (authorityChanged(user.getAuthorities(), userDto.getAuthority())) {
            if (user.getAuthorities() != null
                    && user.getAuthorities().size() == 1) {
                user.getAuthorities().iterator().next()
                        .setAuthority(userDto.getAuthority().name());
            } else {
                Authority authority = new Authority();
                authority.setAuthority(userDto.getAuthority().name());
                authority.setUser(user);
                Collection<Authority> authorities = new ArrayList<Authority>();
                authorities.add(authority);
                user.setAuthorities(authorities);
            }
        }

        Long id = save(user);
        CreateUserResult result = new CreateUserResult(id, generatedPassword);
        return result;
    }

    private boolean authorityChanged(Collection<Authority> authorities,
            KaaAuthorityDto authority) {
        if (authorities != null
                && authorities.size() == 1
                && authorities.iterator().next().getAuthority()
                        .equals(authority.name())) {
            return false;
        } else {
            return true;
        }
    }

    public List<User> getAll() {
        return getCriteria().list();
    }

    public boolean isAuthorityExists(String authority) {
        Criteria criteria = getSession().createCriteria(Authority.class);
        criteria.add(Restrictions.eq(AUTHORITY_PROPERTY, authority));
        List<Authority> resultList = criteria.list();
        return !resultList.isEmpty();
    }

    public User findByUserName(String userName) {
        Criteria criteria = getCriteria();
        criteria.add(Restrictions.eq(USERNAME_PROPERTY, userName));
        return (User) criteria.uniqueResult();
    }
    
    public User findById(Long id) {
        return findById(id, false);
    }

    public User findById(Long id, boolean lazy) {
        if (lazy) {
            return (User) getSession().load(User.class, id);
        } else {
            return (User) getSession().get(User.class, id);
        }
    }

    public User findByUsernameOrMail(String usernameOrMail) {
        Criteria criteria = getCriteria();
        Criterion usernameCriterion = Restrictions.eq(USERNAME_PROPERTY, usernameOrMail);
        Criterion mailCriterion = Restrictions.eq(MAIL_PROPERTY, usernameOrMail);
        criteria.add(Restrictions.or(usernameCriterion, mailCriterion));
        return (User) criteria.uniqueResult();
    }

    public User findByPasswordResetHash(String passwordResetHash) {
        Criteria criteria = getCriteria();
        criteria.add(Restrictions.eq(PASSWORD_RESET_HASH_PROPERTY, passwordResetHash));
        return (User) criteria.uniqueResult();
    }

    public void deleteUser(Long id) {
        User user = findById(id, true);
        if (user != null) {
            getSession().delete(user);
        }
    }

    public User checkUserNameOccupied(String userName, Long userId) {
        Criteria criteria = getCriteria();
        Criterion usernameCriterion = Restrictions.eq(USERNAME_PROPERTY, userName);
        if (userId != null) {
            criteria.add(Restrictions.and(usernameCriterion, Restrictions.not(Restrictions.eq(ID_PROPERTY, userId))));
        } else {
            criteria.add(usernameCriterion);
        }
        return (User) criteria.uniqueResult();
    }

    public User checkEmailOccupied(String mail, Long userId) {
        Criteria criteria = getCriteria();
        Criterion mailCriterion = Restrictions.eq(MAIL_PROPERTY, mail);
        if (userId != null) {
            criteria.add(Restrictions.and(mailCriterion, Restrictions.not(Restrictions.eq(ID_PROPERTY, userId))));
        } else {
            criteria.add(mailCriterion);
        }
        return (User) criteria.uniqueResult();
    }

}
