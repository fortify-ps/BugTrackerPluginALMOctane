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

import com.fortify.pub.bugtracker.plugin.config.IBugTrackerConfigField;
import com.fortify.pub.bugtracker.support.BugTrackerConfig;

/**
 * This class provides the following functionality:
 * 
 * <ul>
 *  <li>The {@link SscProxyField} enum provides {@link BugTrackerConfig} instances
 *      that allow for configuring HTTP and HTTPS proxy settings. Note that contrary
 *      to regular {@link BugTrackerConfig} instances, these fields are not displayed
 *      by SSC as part of the bug tracker configuration, but rather instructs SSC
 *      to pass the global SSC proxy configuration into our plugin configuration.</li>
 *  <li>The {@link #addBugTrackerConfigFields(List)} can be called to add all 
 *      {@link BugTrackerConfig} instances to the given {@link List}.</li>
 *  <li>The various getter methods can be used to get {@link ProxyConfig} instances
 *      based on the given bug tracker configuration {@link Map}.</li> 
 * </ul>
 *  
 * @author Ruud Senden
 *
 */
public class BugTrackerProxyConfigFactory {
	
	/**
	 * Define the various SSC proxy configuration fields.
	 */
	private static enum SscProxyField implements IBugTrackerConfigField {
        HTTP_PROXY_HOST("httpProxyHost", "HTTP Proxy Host"),
        HTTP_PROXY_PORT("httpProxyPort", "HTTP Proxy Port"),
        HTTP_PROXY_USERNAME("httpProxyUsername", "HTTP Proxy Username"),
        HTTP_PROXY_PASSWORD("httpProxyPassword", "HTTP Proxy Password"),
        HTTPS_PROXY_HOST("httpsProxyHost", "HTTPS Proxy Host"),
        HTTPS_PROXY_PORT("httpsProxyPort", "HTTPS Proxy Port"),
        HTTPS_PROXY_USERNAME("httpsProxyUsername", "HTTPS Proxy Username"),
        HTTPS_PROXY_PASSWORD("httpsProxyPassword", "HTTPS Proxy Password");

        private final BugTrackerConfig bugTrackerConfig;
        SscProxyField(final String id, final String displayLabel) {
        	this.bugTrackerConfig = new BugTrackerConfig()
        			.setIdentifier(id)
        			.setDisplayLabel(displayLabel);
        }
        @Override
		public BugTrackerConfig getBugTrackerConfig() {
        	return this.bugTrackerConfig;
        }
    }
	
	/**
	 * Add the {@link BugTrackerConfig} instances for the various SSC
	 * proxy configuration fields to the given {@link List}. 
	 * 
	 * @param list
	 */
	public static final void addBugTrackerConfigFields(List<BugTrackerConfig> list) {
		IBugTrackerConfigField.addFields(list, SscProxyField.values());
	}

	/**
	 * Get the {@link ProxyConfig} instance to be used for HTTP connections,
	 * based on the given bug tracker configuration {@link Map}.
	 * 
	 * @param bugTrackerConfig
	 * @return
	 */
	public static final ProxyConfig getHttpProxyConfig(Map<String, String> bugTrackerConfig) {
		return getProxyConfig(bugTrackerConfig, 
				SscProxyField.HTTP_PROXY_HOST, SscProxyField.HTTP_PROXY_PORT, 
				SscProxyField.HTTP_PROXY_USERNAME, SscProxyField.HTTP_PROXY_PASSWORD);
	}
	
	/**
	 * Get the {@link ProxyConfig} instance to be used for HTTPS connections,
	 * based on the given bug tracker configuration {@link Map}.
	 * 
	 * @param bugTrackerConfig
	 * @return
	 */
	public static final ProxyConfig getHttpsProxyConfig(Map<String, String> bugTrackerConfig) {
		return getProxyConfig(bugTrackerConfig, 
				SscProxyField.HTTPS_PROXY_HOST, SscProxyField.HTTPS_PROXY_PORT, 
				SscProxyField.HTTPS_PROXY_USERNAME, SscProxyField.HTTPS_PROXY_PASSWORD);
	}
	
	/**
	 * Get the {@link ProxyConfig} instance to be used for connecting to the given {@link URL},
	 * based on the given bug tracker configuration {@link Map}. Based on the protocol of the
	 * given {@link URL}, this will invoke either {@link #getHttpProxyConfig(Map)} or 
	 * {@link #getHttpsProxyConfig(Map)}.
	 * 
	 * @param bugTrackerConfig
	 * @param targetBaseUrl
	 * @return
	 */
	public static final ProxyConfig getProxyConfig(Map<String, String> bugTrackerConfig, URL targetBaseUrl) {
		String targetProtocol = targetBaseUrl.getProtocol();
		switch ( targetProtocol ) {
		case "http": return getHttpProxyConfig(bugTrackerConfig);
		case "https": return getHttpsProxyConfig(bugTrackerConfig);
		default: throw new IllegalArgumentException("Unsupported protocol "+targetProtocol);
		}
	}
	
	private static final ProxyConfig getProxyConfig(Map<String, String> config, 
			SscProxyField hostField, SscProxyField portField, 
			SscProxyField usernameField, SscProxyField passwordField) {
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
