/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.cas.services;

import org.apache.commons.io.FileUtils;
import org.jasig.cas.util.LockedOutputStream;
import org.jasig.cas.util.JsonSerializer;
import org.jasig.cas.util.services.RegisteredServiceJsonSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of <code>ServiceRegistryDao</code> that reads services definition from JSON
 * configuration file at the Spring Application Context initialization time. JSON files are
 * expected to be found inside a directory location and this DAO will recursively look through
 * the directory structure to find relevant JSON files. Files are expected to have the
 * {@value #FILE_EXTENSION} extension. An example of the JSON file is included here:
 *
 * <pre>
 {
     "@class" : "org.jasig.cas.services.RegexRegisteredService",
     "id" : 103935657744185,
     "description" : "This is the application description",
     "serviceId" : "https://app.school.edu",
     "name" : "testSaveAttributeReleasePolicyAllowedAttrRulesAndFilter",
     "theme" : "testtheme",
     "proxyPolicy" : {
        "@class" : "org.jasig.cas.services.RegexMatchingRegisteredServiceProxyPolicy",
        "pattern" : "https://.+"
     },
     "enabled" : true,
     "ssoEnabled" : false,
     "evaluationOrder" : 1000,
     "usernameAttributeProvider" : {
        "@class" : "org.jasig.cas.services.DefaultRegisteredServiceUsernameProvider"
     },
     "logoutType" : "BACK_CHANNEL",
     "requiredHandlers" : [ "java.util.HashSet", [ "handler1", "handler2" ] ],
     "attributeReleasePolicy" : {
        "@class" : "org.jasig.cas.services.ReturnAllowedAttributeReleasePolicy",
        "attributeFilter" : {
            "@class" : "org.jasig.cas.services.support.RegisteredServiceRegexAttributeFilter",
            "pattern" : "\\w+"
        },
        "allowedAttributes" : [ "java.util.ArrayList", [ "uid", "sn", "cn" ] ]
     }
 }
 * </pre>
 *
 * @author Dmitriy Kopylenko
 * @author Marvin S. Addison
 * @author Misagh Moayyed
 * @since 4.1.0
 */
public class JsonServiceRegistryDao implements ServiceRegistryDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonServiceRegistryDao.class);

    /**
     * File extension of registered service JSON files.
     */
    private static final String FILE_EXTENSION = "json";

    /**
     * Map of service ID to registered service.
     */
    private Map<Long, RegisteredService> serviceMap = new ConcurrentHashMap<>();

    /**
     * The Service registry directory.
     */
    private final File serviceRegistryDirectory;

    /**
     * The Registered service json serializer.
     */
    private final JsonSerializer<RegisteredService> registeredServiceJsonSerializer;

    /**
     * Instantiates a new Json service registry dao.
     *
     * @param configDirectory                 the config directory
     * @param registeredServiceJsonSerializer the registered service json serializer
     */
    public JsonServiceRegistryDao(final File configDirectory, final JsonSerializer<RegisteredService> registeredServiceJsonSerializer) {
        this.serviceRegistryDirectory = configDirectory;
        Assert.isTrue(this.serviceRegistryDirectory.exists(), serviceRegistryDirectory + " does not exist");
        Assert.isTrue(this.serviceRegistryDirectory.isDirectory(), serviceRegistryDirectory + " is not a directory");
        this.registeredServiceJsonSerializer = registeredServiceJsonSerializer;
    }

    /**
     * Instantiates a new Json service registry dao.
     * Sets the path to the directory where JSON service registry entries are
     * stored. Uses the {@link RegisteredServiceJsonSerializer} by default.
     *
     * @param configDirectory the config directory where service registry files can be found.
     */
    public JsonServiceRegistryDao(final File configDirectory) {
        this(configDirectory, new RegisteredServiceJsonSerializer());
    }

    @Override
    public final RegisteredService save(final RegisteredService service) {
        if (service.getId() == RegisteredService.INITIAL_IDENTIFIER_VALUE && service instanceof AbstractRegisteredService) {
            LOGGER.debug("Service id not set. Calculating id based on system time...");
            ((AbstractRegisteredService) service).setId(System.nanoTime());
        }
        final File f = makeFile(service);
        try (final LockedOutputStream out = new LockedOutputStream(new FileOutputStream(f));) {
            this.registeredServiceJsonSerializer.toJson(out, service);

            if (this.serviceMap.containsKey(service.getId())) {
                LOGGER.debug("Found existing service definition by id [{}]. Saving...", service.getId());
            }
            this.serviceMap.put(service.getId(), service);
            LOGGER.debug("Saved service to [{}]", f.getCanonicalPath());
        } catch (final IOException e) {
            throw new RuntimeException("IO error opening file stream.", e);
        }
        return findServiceById(service.getId());
    }

    @Override
    public final synchronized boolean delete(final RegisteredService service) {
        try {
            final File f = makeFile(service);
            final boolean result = f.delete();
            if (!result) {
                LOGGER.warn("Failed to delete service definition file [{}]", f.getCanonicalPath());
            } else {
                serviceMap.remove(service.getId());
                LOGGER.debug("Successfully deleted service definition file [{}]", f.getCanonicalPath());
            }
            return result;
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public final synchronized List<RegisteredService> load() {
        final Map<Long, RegisteredService> temp = new ConcurrentHashMap<>();
        int errorCount = 0;
        final Collection<File> c = FileUtils.listFiles(this.serviceRegistryDirectory, new String[] {FILE_EXTENSION}, true);
        for (final File file : c) {
            if (file.length() > 0) {
                try (final BufferedInputStream in = new BufferedInputStream(new FileInputStream(file))) {
                    final RegisteredService service = this.registeredServiceJsonSerializer.fromJson(in);

                    if (temp.containsKey(service.getId())) {
                        LOGGER.warn("Found a service definition [{}] with a duplicate id [{}]. "
                                + "This will overwrite previous service definitions and is likely a "
                                + "configuration problem. Make sure all services have a unique id and try again.",
                                service.getServiceId(), service.getId());
                    }
                    temp.put(service.getId(), service);
                } catch (final Exception e) {
                    errorCount++;
                    LOGGER.error("Error reading configuration file", e);
                }
            }
        }
        if (errorCount == 0) {
            this.serviceMap = temp;
        }
        return new ArrayList<>(this.serviceMap.values());
    }

    @Override
    public final RegisteredService findServiceById(final long id) {
        return serviceMap.get(id);
    }

    /**
     * Creates a JSON file for a registered service.
     * The file is named as <code>[SERVICE-NAME]-[SERVICE-ID]-.{@value #FILE_EXTENSION}</code>
     *
     * @param service Registered service.
     * @return JSON file in service registry directory.
     * @throws IllegalArgumentException if file name is invalid
     */
    protected File makeFile(final RegisteredService service) {
        final String fileName = service.getName() + "-" + service.getId() + "." + FILE_EXTENSION;
        try {
            final File svcFile = new File(serviceRegistryDirectory, fileName);
            LOGGER.debug("Using [{}] as the service definition file", svcFile.getCanonicalPath());
            return svcFile;
        } catch (final IOException e) {
            LOGGER.warn("Service file name {} is invalid; Examine for illegal characters in the name.", fileName);
            throw new IllegalArgumentException(e);
        }
    }
}
