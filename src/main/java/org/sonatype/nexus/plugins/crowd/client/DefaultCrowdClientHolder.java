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
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.sonatype.nexus.plugins.crowd.caching.AuthBasicCache;
import org.sonatype.nexus.plugins.crowd.caching.AuthBasicCacheImpl;
import org.sonatype.nexus.plugins.crowd.caching.CachingAuthenticationManager;
import org.sonatype.nexus.plugins.crowd.client.rest.CachingRestClient;
import org.sonatype.nexus.plugins.crowd.client.rest.RestClient;
import org.sonatype.nexus.plugins.crowd.config.CrowdPluginConfiguration;
import org.sonatype.nexus.plugins.crowd.config.model.v1_0_0.Configuration;

import javax.inject.Inject;
import javax.inject.Named;
import java.net.URISyntaxException;

/**
 * Implementation of the CrowdClientHolder which uses caching wherever possible.
 *
 * @author Justin Edelson
 * @author Issa Gorissen
 */
@Named
@Singleton
public class DefaultCrowdClientHolder extends AbstractLogEnabled implements CrowdClientHolder, Initializable {

    private boolean configured = false;

    private CachingAuthenticationManager authManager;

    private RestClient restClient;

    private final CrowdPluginConfiguration crowdPluginConfiguration;

    @Inject
    public DefaultCrowdClientHolder(final CrowdPluginConfiguration crowdPluginConfiguration) {
        this.crowdPluginConfiguration = crowdPluginConfiguration;
    }

    @Override
    public void initialize() throws InitializationException {
        final Configuration configuration = crowdPluginConfiguration.getConfiguration();
        if (configuration != null) {
            final AuthBasicCache basicCache = new AuthBasicCacheImpl(60 * configuration.getSessionValidationInterval());
            try {
                restClient = new CachingRestClient(configuration);
            } catch (URISyntaxException use) {
                throw new InitializationException("Rest client init failed", use);
            }
            authManager = new CachingAuthenticationManager(restClient, basicCache);
            configured = true;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isConfigured() {
        return configured;
    }

    public CachingAuthenticationManager getAuthenticationManager() {
        return authManager;
    }

    public RestClient getRestClient() {
        return restClient;
    }
}
