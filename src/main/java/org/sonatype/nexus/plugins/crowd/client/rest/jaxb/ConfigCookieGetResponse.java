/*
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.nexus.plugins.crowd.client.rest.jaxb;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Issa Gorissen
 */
@XmlRootElement(name="cookie-config")
public class ConfigCookieGetResponse {
    public String domain;
    public boolean secure;
    public String name;


    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ConfigCookieGetResponse [domain=").append(domain)
                .append(", secure=").append(secure).append(", name=")
                .append(name).append("]");
        return builder.toString();
    }

}
