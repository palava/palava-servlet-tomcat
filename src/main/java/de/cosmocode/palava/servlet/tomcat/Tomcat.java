/**
 * Copyright 2010 CosmoCode GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.cosmocode.palava.servlet.tomcat;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Set;

import org.apache.catalina.Context;
import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.Realm;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.deploy.FilterDef;
import org.apache.catalina.deploy.FilterMap;
import org.apache.catalina.realm.MemoryRealm;
import org.apache.catalina.startup.Embedded;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.internal.Sets;
import com.google.inject.name.Named;
import com.google.inject.servlet.GuiceFilter;

import de.cosmocode.palava.core.lifecycle.AutoStartable;
import de.cosmocode.palava.core.lifecycle.Initializable;
import de.cosmocode.palava.core.lifecycle.LifecycleException;
import de.cosmocode.palava.servlet.Webapp;

/**
 * A service which configures and controls an embedded tomcat.
 *
 * @author Willi Schoenborn
 */
final class Tomcat implements Initializable, AutoStartable, Provider<Embedded> {

    private static final Logger LOG = LoggerFactory.getLogger(Tomcat.class);

    private final Embedded tomcat = new Embedded();
    
    private final File catalinaHome;
    
    private Realm realm = new MemoryRealm();

    private File appBase;
    
    private Set<Webapp> webapps = Sets.newLinkedHashSet();
    
    private InetAddress address;
    
    private int port = 8080;
    
    private boolean secure;
    
    @Inject
    public Tomcat(@Named(TomcatConfig.CATALINA_HOME) File catalinaHome) {
        this.catalinaHome = Preconditions.checkNotNull(catalinaHome, "CatalinaHome");
        this.appBase = new File(catalinaHome, "webapps");
        
        try {
            this.address = InetAddress.getByName("localhost");
        } catch (UnknownHostException e) {
            throw new AssertionError(e);
        }
    }
    
    @Inject(optional = true)
    void setRealm(@Named(TomcatConfig.REALM) Realm realm) {
        this.realm = realm;
    }
    
    @Inject(optional = true)
    void setAppBase(@Named(TomcatConfig.APP_BASE) File appBase) {
        this.appBase = Preconditions.checkNotNull(appBase, "AppBase");
    }
    
    @Inject(optional = true)
    void setWebapps(Set<Webapp> webapps) {
        this.webapps = Preconditions.checkNotNull(webapps, "Webapps");
    }
    
    @Inject(optional = true)
    void setAddress(@Named(TomcatConfig.ADDRESS) InetAddress address) {
        this.address = Preconditions.checkNotNull(address, "Address");
    }
    
    @Inject(optional = true)
    void setPort(@Named(TomcatConfig.PORT) int port) {
        this.port = port;
    }
    
    @Inject(optional = true)
    void setSecure(@Named(TomcatConfig.SECURE) boolean secure) {
        this.secure = secure;
    }
    
    @Override
    public void initialize() throws LifecycleException {
        LOG.info("Catalina home set to {}", catalinaHome);
        tomcat.setCatalinaHome(catalinaHome.getAbsolutePath());
        
        LOG.info("Using realm {}", realm);
        tomcat.setRealm(realm);
        
        final Engine engine = tomcat.createEngine();
        
        LOG.info("Creating host with appBase {}", appBase);
        final Host localhost = tomcat.createHost("localhost", appBase.getAbsolutePath());
        
        engine.addChild(localhost);
        engine.setDefaultHost(localhost.getName());

        final Context root = tomcat.createContext("", new File(appBase, "ROOT").getAbsolutePath());
        localhost.addChild(root);
        
        for (Webapp webapp : webapps) {
            LOG.info("Configuring webapp {}", webapp);
            final Context context = tomcat.createContext(webapp.getContext(), webapp.getLocation());
            
            final FilterDef filterDef = new FilterDef();
            filterDef.setFilterClass(GuiceFilter.class.getName());
            filterDef.setFilterName(GuiceFilter.class.getSimpleName());
            context.addFilterDef(filterDef);
            
            final FilterMap filterMap = new FilterMap();
            filterMap.setFilterName(GuiceFilter.class.getSimpleName());
            filterMap.addURLPattern("/*");
            context.addFilterMap(filterMap);
            
            localhost.addChild(context);
        }

        tomcat.addEngine(engine);
        
        LOG.info("Creating connector on {}:{}", address, port);
        final Connector connector = tomcat.createConnector(address, port, secure);
        LOG.info("Processing secure connections on {}: {}", connector, Boolean.valueOf(secure));
        tomcat.addConnector(connector);
    }
    
    @Override
    public Embedded get() {
        return tomcat;
    }
    
    @Override
    public void start() throws LifecycleException {
        try {
            LOG.info("Starting tomcat {}", tomcat);
            tomcat.start();
        } catch (org.apache.catalina.LifecycleException e) {
            throw new LifecycleException(e);
        }
    }
    
    @Override
    public void stop() throws LifecycleException {
        try {
            LOG.info("Stopping tomcat {}", tomcat);
            tomcat.stop();
        } catch (org.apache.catalina.LifecycleException e) {
            throw new LifecycleException(e);
        }
    }

}
