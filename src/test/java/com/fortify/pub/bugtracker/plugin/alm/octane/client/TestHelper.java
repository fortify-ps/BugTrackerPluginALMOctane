/*******************************************************************************
 * (c) Copyright 2020 Micro Focus or one of its affiliates
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

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.lang3.StringUtils;

import com.fortify.pub.bugtracker.plugin.alm.octane.client.api.OctaneApiClient;
import com.fortify.pub.bugtracker.plugin.alm.octane.client.http.OctaneHttpClient;
import com.fortify.pub.bugtracker.plugin.alm.octane.config.OctaneConfig;
import com.fortify.pub.bugtracker.support.UserAuthenticationStore;

public class TestHelper {

	static final OctaneHttpClient getOctaneHttpClient() throws MalformedURLException {
		if ( StringUtils.isBlank(System.getProperty("octaneUrl")) ) {
			return null;
		} else {
			OctaneConfig octaneConfig = new OctaneConfig(
				new URL(System.getProperty("octaneUrl")),
				System.getProperty("octaneSharedSpaceId"),
				System.getProperty("octaneWorkspaceId")
			);
			UserAuthenticationStore authStore = new UserAuthenticationStore() {
				
				@Override
				public String getUserName() {
					return System.getProperty("octaneUserName");
				}
				
				@Override
				public String getPassword() {
					return System.getProperty("octanePassword");
				}
			};
			return new OctaneHttpClient(octaneConfig, authStore, null);
		}
	}
	
	static final OctaneApiClient getOctaneApiClient() throws MalformedURLException {
		OctaneHttpClient httpClient = getOctaneHttpClient();
		return httpClient==null ? null : new OctaneApiClient(httpClient); 
	}

}
