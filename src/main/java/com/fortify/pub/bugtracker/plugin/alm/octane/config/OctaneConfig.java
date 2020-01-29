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
package com.fortify.pub.bugtracker.plugin.alm.octane.config;

import java.net.URL;

import org.apache.commons.lang3.Validate;

/**
 * Simple data class holding Octane connection details like base URL,
 * shared space id, and workspace id.
 *   
 * @author Ruud Senden
 *
 */
public class OctaneConfig {
    private final URL baseUrl;
    private final String sharedSpaceId;
    private final String workspaceId;
    
	public OctaneConfig(URL baseUrl, String sharedSpaceId, String workspaceId) {
		Validate.notNull(baseUrl, "Octane base URL must be specified");
		Validate.notBlank(sharedSpaceId, "Octane shared space id must be specified");
		Validate.notBlank(workspaceId, "Octane workspace id must be specified");
		this.baseUrl = baseUrl;
		this.sharedSpaceId = sharedSpaceId;
		this.workspaceId = workspaceId;
	}

	public URL getBaseUrl() {
		return baseUrl;
	}

	public String getSharedSpaceId() {
		return sharedSpaceId;
	}

	public String getWorkspaceId() {
		return workspaceId;
	}
}
