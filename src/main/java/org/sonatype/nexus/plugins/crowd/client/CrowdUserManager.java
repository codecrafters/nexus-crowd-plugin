/*
 * Copyright (c) 2010 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.nexus.plugins.crowd.client;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.configuration.validation.InvalidConfigurationException;
import org.sonatype.security.usermanagement.*;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import java.rmi.RemoteException;
import java.util.Collections;
import java.util.Set;

/**
 * @author justin
 * @author Issa Gorissen
 */
@Named(CrowdUserManager.REALM_NAME)
@Singleton
public class CrowdUserManager extends AbstractReadOnlyUserManager implements UserManager, RoleMappingUserManager {

    protected static final String REALM_NAME = "Crowd";

    protected static final String SOURCE = "Crowd";

    private static final Logger LOGGER = LoggerFactory.getLogger(CrowdUserManager.class);

    private final CrowdClientHolder crowdClientHolder;

    @Inject
    public CrowdUserManager(final CrowdClientHolder crowdClientHolder) {
        this.crowdClientHolder = crowdClientHolder;
        LOGGER.info("CrowdUserManager is starting...");
    }

    /**
     * {@inheritDoc}
     */

    public String getAuthenticationRealmName() {
        return REALM_NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSource() {
        return SOURCE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public User getUser(String userId) throws UserNotFoundException {
        if (crowdClientHolder.isConfigured()) {
            try {
                User user = crowdClientHolder.getRestClient().getUser(userId);
                return completeUserRolesAndSource(user);
            } catch (RemoteException e) {
                LOGGER.error("Unable to look up user " + userId, e);
                throw new UserNotFoundException(userId, e.getMessage(), e);
            }
        } else {
            throw new UserNotFoundException("Crowd plugin is not configured.");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<RoleIdentifier> getUsersRoles(final String userId, final String userSource) throws UserNotFoundException {
        if (SOURCE.equals(userSource)) {
            if (crowdClientHolder.isConfigured()) {
                Set<String> roleNames;
                try {
                    roleNames = crowdClientHolder.getRestClient().getNestedGroups(userId);
                } catch (RemoteException e) {
                    LOGGER.error("Unable to look up user " + userId, e);
                    return Collections.emptySet();
                }
                return Sets.newHashSet(Iterables.transform(roleNames, new Function<String, RoleIdentifier>() {
                    @Nullable
                    @Override
                    public RoleIdentifier apply(String s) {
                        return new RoleIdentifier(SOURCE, s);
                    }
                }));
            } else {
                throw new UserNotFoundException("Crowd plugin is not configured.");
            }
        } else {
            return Collections.emptySet();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> listUserIds() {
        if (crowdClientHolder.isConfigured()) {
            try {
                return crowdClientHolder.getRestClient().getAllUsernames();
            } catch (Exception e) {
                LOGGER.error("Unable to get username list", e);
                return Collections.emptySet();
            }
        } else {
            UnconfiguredNotifier.unconfigured();
            return Collections.emptySet();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<User> listUsers() {
        return searchUsers(new UserSearchCriteria());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<User> searchUsers(UserSearchCriteria criteria) {
        if (!crowdClientHolder.isConfigured()) {
            UnconfiguredNotifier.unconfigured();
            return Collections.emptySet();
        }

        if (!SOURCE.equals(criteria.getSource())) {
            return Collections.emptySet();
        }

        try {
            Set<User> result = crowdClientHolder.getRestClient().searchUsers(
                    criteria.getUserId(),
                    criteria.getEmail(),
                    criteria.getOneOfRoleIds(),
                    100);

            for (User user : result) {
                completeUserRolesAndSource(user);
            }

            return result;

        } catch (Exception e) {
            LOGGER.error("Unable to get userlist", e);
            return Collections.emptySet();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setUsersRoles(final String userId, final String userSource, final Set<RoleIdentifier> roleIdentifiers)
            throws UserNotFoundException, InvalidConfigurationException {
        getUser(userId).addAllRoles(roleIdentifiers);
    }

    private User completeUserRolesAndSource(final User user) throws UserNotFoundException {
        user.setSource(SOURCE);
        user.setRoles(getUsersRoles(user.getUserId(), SOURCE));
        return user;
    }
}
