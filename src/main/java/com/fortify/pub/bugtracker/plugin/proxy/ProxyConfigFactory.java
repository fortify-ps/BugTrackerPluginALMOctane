/*******************************************************************************
 * (c) Copyright 2017 EntIT Software LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a 
 * copy of this software and associated documentation files (the 
 * "Software"), to deal in the Software without restriction, including without 
 * limitation the rights to use, copy, modify, merge, publish, distribute, 
 * sublicense, and/or sell copies of the Software, and to permit persons to 
 * whom the Software is furnished to do so, subject to the following 
 * conditions:
 * 
 * The above copyright notice and this permission notice shall be included 
 * in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY 
 * KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE 
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, 
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN 
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS 
 * IN THE SOFTWARE.
 ******************************************************************************/
package com.fortify.pub.bugtracker.plugin.proxy;

import java.net.URL;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.glassfish.jersey.internal.util.Producer;

import com.fortify.pub.bugtracker.plugin.fields.IDefaultMethodsBugTrackerConfigDefinition;
import com.fortify.pub.bugtracker.plugin.fields.IDefaultMethodsBugTrackerConfigDefinitionEnum;
import com.fortify.pub.bugtracker.support.BugTrackerConfig;

/**
 * This class provides the following functionality:
 * 
 * <ul>
 *  <li>The {@link SscProxyBugTrackerConfigDefinition} enum provides {@link BugTrackerConfig} instances
 *      that allow for configuring HTTP and HTTPS proxy settings. Note that contrary
 *      to regular {@link BugTrackerConfig} instances, these fields are not displayed
 *      by SSC as part of the bug tracker configuration, but rather are used to instruct 
 *      SSC to pass the global SSC proxy configuration into our plugin configuration.</li>
 *  <li>The {@link #addBugTrackerConfigFields(List)} can be called to add all 
 *      {@link BugTrackerConfig} instances to the given {@link List}.</li>
 *  <li>The various getter methods can be used to get {@link ProxyConfig} instances
 *      based on the given bug tracker configuration {@link Map}.</li> 
 * </ul>
 *  
 * @author Ruud Senden
 *
 */
public class ProxyConfigFactory {
	
	/**
	 * Define the various SSC proxy configuration fields.
	 */
	private static enum SscProxyBugTrackerConfigDefinition implements IDefaultMethodsBugTrackerConfigDefinitionEnum {
        HTTP_PROXY_HOST("httpProxyHost", "HTTP Proxy Host"),
        HTTP_PROXY_PORT("httpProxyPort", "HTTP Proxy Port"),
        HTTP_PROXY_USERNAME("httpProxyUsername", "HTTP Proxy Username"),
        HTTP_PROXY_PASSWORD("httpProxyPassword", "HTTP Proxy Password"),
        HTTPS_PROXY_HOST("httpsProxyHost", "HTTPS Proxy Host"),
        HTTPS_PROXY_PORT("httpsProxyPort", "HTTPS Proxy Port"),
        HTTPS_PROXY_USERNAME("httpsProxyUsername", "HTTPS Proxy Username"),
        HTTPS_PROXY_PASSWORD("httpsProxyPassword", "HTTPS Proxy Password");

        private final Producer<BugTrackerConfig> bugTrackerConfigProducer;
        SscProxyBugTrackerConfigDefinition(final String id, final String displayLabel) {
        	this.bugTrackerConfigProducer = ()->
        		new BugTrackerConfig().setIdentifier(id).setDisplayLabel(displayLabel);
        }
        @Override
        public Producer<BugTrackerConfig> getBugTrackerConfigProducer() {
        	return bugTrackerConfigProducer;
        }
    }
	
	/**
	 * Add the {@link BugTrackerConfig} instances for the various SSC
	 * proxy configuration fields to the given {@link List}. 
	 * 
	 * @param list
	 */
	public static final void addBugTrackerConfigFields(List<BugTrackerConfig> list) {
		IDefaultMethodsBugTrackerConfigDefinition.addFields(list, SscProxyBugTrackerConfigDefinition.values());
	}

	/**
	 * Create the {@link ProxyConfig} instance to be used for HTTP connections,
	 * based on the given bug tracker configuration {@link Map}.
	 * 
	 * @param bugTrackerConfig
	 * @return
	 */
	public static final ProxyConfig createHttpProxyConfig(Map<String, String> bugTrackerConfig) {
		return createProxyConfig(bugTrackerConfig, 
				SscProxyBugTrackerConfigDefinition.HTTP_PROXY_HOST, SscProxyBugTrackerConfigDefinition.HTTP_PROXY_PORT, 
				SscProxyBugTrackerConfigDefinition.HTTP_PROXY_USERNAME, SscProxyBugTrackerConfigDefinition.HTTP_PROXY_PASSWORD);
	}
	
	/**
	 * Create the {@link ProxyConfig} instance to be used for HTTPS connections,
	 * based on the given bug tracker configuration {@link Map}.
	 * 
	 * @param bugTrackerConfig
	 * @return
	 */
	public static final ProxyConfig createHttpsProxyConfig(Map<String, String> bugTrackerConfig) {
		return createProxyConfig(bugTrackerConfig, 
				SscProxyBugTrackerConfigDefinition.HTTPS_PROXY_HOST, SscProxyBugTrackerConfigDefinition.HTTPS_PROXY_PORT, 
				SscProxyBugTrackerConfigDefinition.HTTPS_PROXY_USERNAME, SscProxyBugTrackerConfigDefinition.HTTPS_PROXY_PASSWORD);
	}
	
	/**
	 * Create the {@link ProxyConfig} instance to be used for connecting to the given {@link URL},
	 * based on the given bug tracker configuration {@link Map}. Based on the protocol of the
	 * given {@link URL}, this will invoke either {@link #createHttpProxyConfig(Map)} or 
	 * {@link #createHttpsProxyConfig(Map)}.
	 * 
	 * @param bugTrackerConfig
	 * @param targetBaseUrl
	 * @return
	 */
	public static final ProxyConfig createProxyConfig(Map<String, String> bugTrackerConfig, URL targetBaseUrl) {
		String targetProtocol = targetBaseUrl.getProtocol();
		switch ( targetProtocol ) {
		case "http": return createHttpProxyConfig(bugTrackerConfig);
		case "https": return createHttpsProxyConfig(bugTrackerConfig);
		default: throw new IllegalArgumentException("Unsupported protocol "+targetProtocol);
		}
	}
	
	/**
	 * Create the {@link ProxyConfig} instance based on the given configuration field definitions.
	 * @param config
	 * @param hostField
	 * @param portField
	 * @param usernameField
	 * @param passwordField
	 * @return
	 */
	private static final ProxyConfig createProxyConfig(Map<String, String> config, 
			SscProxyBugTrackerConfigDefinition hostField, SscProxyBugTrackerConfigDefinition portField, 
			SscProxyBugTrackerConfigDefinition usernameField, SscProxyBugTrackerConfigDefinition passwordField) {
		ProxyConfig result = null;
		String host = hostField.getValue(config);
		Integer port = portField.getIntValue(config);
		if ( StringUtils.isNotBlank(host) && port!=null ) {
			String userName = usernameField.getValue(config);
			String password = passwordField.getValue(config);
			result = new ProxyConfig(host, port, userName, password);
		}
		return result;
	}
}
