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

import de.cosmocode.palava.servlet.ServletConfig;

/**
 * Static constant holder class for tomcat config key names.
 *
 * @author Willi Schoenborn
 */
public final class TomcatConfig {

    public static final String PREFIX = ServletConfig.PREFIX + "tomcat.";
    
    public static final String CATALINA_HOME = PREFIX + "catalinaHome";
    
    public static final String ADDRESS = PREFIX + "address";
    
    public static final String PORT = PREFIX + "port";
    
    public static final String SECURE = PREFIX + "secure";
    
    public static final String REALM = PREFIX + "realm";
    
    private TomcatConfig() {
        
    }

}
