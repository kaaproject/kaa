/*
 * Copyright 2014 CyberVision, Inc.
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

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;

import org.apache.commons.lang.RandomStringUtils;
import org.kaaproject.kaa.common.dto.KaaAuthorityDto;
import org.kaaproject.kaa.server.admin.services.entity.Authority;
import org.kaaproject.kaa.server.admin.services.entity.CreateUserResult;
import org.kaaproject.kaa.server.admin.services.entity.User;
import org.kaaproject.kaa.server.admin.shared.dto.UserDto;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.stereotype.Repository;

@Repository("userFacade")
@Transactional
public class UserFacade {

    @PersistenceContext(unitName = "kaaSec")
    private EntityManager em;

    public Long save(User user) {
        em.persist(user);
        return user.getId();
    }

    public CreateUserResult saveUserDto(UserDto userDto, PasswordEncoder passwordEncoder)
            throws Exception {

        User user = null;
        String generatedPassword = null;
        if (userDto.getExternalUid() == null || userDto.getExternalUid().isEmpty()) {
            user = new User();
            generatedPassword = RandomStringUtils.randomAlphanumeric(12);
            user.setPassword(passwordEncoder.encodePassword(generatedPassword, null));
            user.setTempPassword(true);
            user.setEnabled(true);
        }
        else {
            user = findById(Long.valueOf(userDto.getExternalUid()));
        }

        user.setUsername(userDto.getUsername());
        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        user.setMail(userDto.getMail());

        if (authorityChanged(user.getAuthorities(), userDto.getAuthority())) {
            if (user.getAuthorities() != null && user.getAuthorities().size()==1) {
                user.getAuthorities().iterator().next().setAuthority(userDto.getAuthority().name());
            }
            else {
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

    private boolean authorityChanged(Collection<Authority> authorities, KaaAuthorityDto authority) {
        if (authorities != null &&
                authorities.size()==1 &&
                authorities.iterator().next().getAuthority().equals(authority.name())) {
            return false;
        }
        else {
            return true;
        }
    }

    public List<User> getAll() {
        return em.createQuery("SELECT u FROM User u", User.class).getResultList();
    }

    public boolean isAuthorityExists(String authority) {
        TypedQuery<Authority> query = em.createQuery("SELECT a FROM Authority a WHERE a.authority = :authority", Authority.class);
        query.setParameter("authority", authority);
        List<Authority> resultList = query.getResultList();
        return !resultList.isEmpty();
    }

    public User findByUserName(String userName) {
        TypedQuery<User> query = em.createQuery("SELECT u FROM User u WHERE u.username = :username", User.class);
        query.setParameter("username", userName);
        List<User> resultList = query.getResultList();
        if (!resultList.isEmpty())
            return resultList.get(0);
        else
            return null;
    }

    public User findById(Long id) {
        TypedQuery<User> query = em.createQuery("SELECT u FROM User u WHERE u.id = :id", User.class);
        query.setParameter("id", id);
        List<User> resultList = query.getResultList();
        if (!resultList.isEmpty())
            return resultList.get(0);
        else
            return null;
    }

    public void deleteUser(Long id) {
        User user = findById(id);
        if (user != null) {
            em.remove(user);
        }
    }

    public User checkUserNameOccupied(String userName, Long userId) {
        TypedQuery<User> query;
        if (userId != null) {
            query = em.createQuery("SELECT u FROM User u WHERE u.username = :username AND u.id != :userId", User.class);
            query.setParameter("userId", userId);
        }
        else {
            query = em.createQuery("SELECT u FROM User u WHERE u.username = :username", User.class);
        }
        query.setParameter("username", userName);
        List<User> resultList = query.getResultList();
        if (!resultList.isEmpty())
            return resultList.get(0);
        else
            return null;
    }

    public User checkEmailOccupied(String mail, Long userId) {
        TypedQuery<User> query;
        if (userId != null) {
            query = em.createQuery("SELECT u FROM User u WHERE u.mail = :mail AND u.id != :userId", User.class);
            query.setParameter("userId", userId);
        }
        else {
            query = em.createQuery("SELECT u FROM User u WHERE u.mail = :mail", User.class);
        }
        query.setParameter("mail", mail);
        List<User> resultList = query.getResultList();
        if (!resultList.isEmpty())
            return resultList.get(0);
        else
            return null;
    }

}
