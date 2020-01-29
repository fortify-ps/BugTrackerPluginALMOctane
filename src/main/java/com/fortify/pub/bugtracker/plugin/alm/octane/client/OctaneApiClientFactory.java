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
package com.fortify.pub.bugtracker.plugin.alm.octane.client;

import java.util.List;
import java.util.Map;

import com.fortify.pub.bugtracker.plugin.proxy.ProxyConfigFactory;
import com.fortify.pub.bugtracker.plugin.alm.octane.OctaneConfigFactory;
import com.fortify.pub.bugtracker.plugin.proxy.ProxyConfig;
import com.fortify.pub.bugtracker.support.BugTrackerConfig;
import com.fortify.pub.bugtracker.support.UserAuthenticationStore;

/**
 * This class combines functionality provided by {@link OctaneConfigFactory}
 * and {@link ProxyConfigFactory} to provide the following functionality:
 * 
 * <ul>
 *  <li>The {@link #addBugTrackerConfigFields(List)} method can be called to add all 
 *      {@link BugTrackerConfig} instances from both {@link OctaneConfigFactory}
 *      and {@link ProxyConfigFactory} to the given list.</li>
 *  <li>Based on the bug tracker configuration {@link Map} passed in the constructor,
 *      an instance of this class provides access to the corresponding {@link OctaneConfig}
 *      and {@link ProxyConfig}.</li>
 *  <li>Based on the bug tracker configuration {@link Map} passed in the constructor, and
 *      given a {@link UserAuthenticationStore} instance, an instance of this class can 
 *      instantiate {@link OctaneApiClient} instances.</li>
 * </ul>
 *  
 * @author Ruud Senden
 *
 */
public class OctaneApiClientFactory {
	/**
	 * Add {@link BugTrackerConfig} instances from both {@link OctaneConfigFactory}
	 * and {@link ProxyConfigFactory} to the given {@link BugTrackerConfig} {@link List}.
	 * @param list
	 */
    public static final void addBugTrackerConfigFields(List<BugTrackerConfig> list) {
		OctaneConfigFactory.addBugTrackerConfigs(list);
		ProxyConfigFactory.addBugTrackerConfigs(list);
	}

    private final OctaneConfig octaneConfig;
    private final ProxyConfig proxyConfig;
    
    /**
     * Construct an instance of this class with the given bug tracker configuration {@link Map}.
     * 
     * @param bugTrackerConfig
     */
    public OctaneApiClientFactory(Map<String, String> bugTrackerConfig) {
		this.octaneConfig = OctaneConfigFactory.createOctaneConfig(bugTrackerConfig);
		this.proxyConfig = ProxyConfigFactory.createProxyConfig(bugTrackerConfig, this.getOctaneConfig().getBaseUrl());
	}

	/**
	 * Get an {@link OctaneConfig} instance corresponding to the bug tracker configuration 
	 * passed in the constructor.
	 *  
	 * @return
	 */
	public final OctaneConfig getOctaneConfig() {
		return octaneConfig;
	}
	
	/**
	 * Get a {@link ProxyConfig} instance corresponding to the bug tracker configuration 
	 * passed in the constructor.
	 * 
	 * @return
	 */
	public final ProxyConfig getProxyConfig() {
		return proxyConfig;
	}
	
	/**
	 * Create an {@link OctaneApiClient} instance based on the configuration passed in
	 * the constructor and the given {@link UserAuthenticationStore}.
	 * 
	 * @param authStore
	 * @return
	 */
	public final OctaneApiClient createOctaneApiClient(UserAuthenticationStore authStore) {
    	return new OctaneApiClient(getOctaneHttpClient(authStore));
    }

	/**
	 * Create an {@link OctaneHttpClient} instance based on the configuration passed in
	 * the constructor and the given {@link UserAuthenticationStore}.
	 * 
	 * @param authStore
	 * @return
	 */
	private final OctaneHttpClient getOctaneHttpClient(UserAuthenticationStore authStore) {
		return new OctaneHttpClient(getOctaneConfig(), authStore, getProxyConfig());
	}
}
