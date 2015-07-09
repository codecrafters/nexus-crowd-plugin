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

import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.security.authorization.*;

import javax.inject.Inject;
import javax.inject.Named;
import java.rmi.RemoteException;
import java.util.Collections;
import java.util.Set;

/**
 * @author justin
 * @author Issa Gorissen
 */
@Named
@Singleton
public class CrowdAuthorizationManager extends AbstractReadOnlyAuthorizationManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(CrowdAuthorizationManager.class);

    private final CrowdClientHolder crowdClientHolder;

    @Inject
    public CrowdAuthorizationManager(final CrowdClientHolder crowdClientHolder) {
        this.crowdClientHolder = crowdClientHolder;
        LOGGER.info("CrowdAuthorizationManager is starting...");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Privilege getPrivilege(String privilegeId) throws NoSuchPrivilegeException {
        throw new NoSuchPrivilegeException("Crowd plugin doesn't support privileges");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Role getRole(String roleId) throws NoSuchRoleException {
        if (crowdClientHolder.isConfigured()) {
            try {
                Role role = crowdClientHolder.getRestClient().getGroup(roleId);
                role.setSource(getSource());
                return role;
            } catch (RemoteException e) {
                throw new NoSuchRoleException("Failed to get role " + roleId + " from Crowd.", e);
            }
        } else {
            throw new NoSuchRoleException("Crowd plugin is not configured.");
        }
    }

    public String getSource() {
        return CrowdUserManager.SOURCE;
    }

    public Set<Privilege> listPrivileges() {
        return Collections.emptySet();
    }

    public Set<Role> listRoles() {
        if (crowdClientHolder.isConfigured()) {
            try {
                Set<Role> roles = crowdClientHolder.getRestClient().getAllGroups();
                for (Role role : roles) {
                    role.setSource(getSource());
                }
                return roles;
            } catch (RemoteException e) {
                LOGGER.error("Unable to load roles", e);
                return null;
            }
        }
        UnconfiguredNotifier.unconfigured();
        return Collections.emptySet();
    }
}
