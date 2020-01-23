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

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.ws.rs.client.WebTarget;

import com.fortify.pub.bugtracker.support.Bug;

/**
 * This class provides functionality for invoking specific ALM Octane
 * REST endpoints.
 */
public class OctaneApiClient implements AutoCloseable {
    private final OctaneHttpClient client;

	public OctaneApiClient(OctaneHttpClient client) {
        this.client = client;
    }

    @Override
    public void close() {
        this.client.close();
    }
    
    private final JsonArray getEntities(String entityName) {
		WebTarget target = client.getApiWorkspaceTarget().path(entityName);
		JsonObject json = client.httpGetRequest(target, JsonObject.class);
		System.out.println(json);
		return json.getJsonArray("data");
	}
    
    public JsonArray getWorkItems() {
		return getEntities("work_items");
	}
    
    public JsonArray getFeatures() {
		return getEntities("features");
	}
    
    public JsonArray getPhases() {
		return getEntities("phases");
	}
	
    /*
	private JSONMap submitOrUpdateIssue(OctaneSharedSpaceAndWorkspaceId sharedSpaceAndWorkspaceId, Map<String, Object> issueData, String httpMethod) {
		JSONMap issueEntry = new JSONMap();
		issueEntry.putPaths(issueData);
		replaceEntityNamesWithIds(sharedSpaceAndWorkspaceId, issueEntry);
		JSONMap data = new JSONMap();
		data.put("data", new JSONMap[]{issueEntry});
		return executeRequest(httpMethod, getBaseResource()
				.path("/api/shared_spaces/")
				.path(sharedSpaceAndWorkspaceId.getSharedSpaceUid())
				.path("/workspaces/")
				.path(sharedSpaceAndWorkspaceId.getWorkspaceId())
				.path("/defects"),
				Entity.entity(data, "application/json"), JSONMap.class);
	}
	*/

    public Bug getIssue(String id) {

        WebTarget webTarget = client
                .getApiWorkspaceTarget()
                .path("issue")
                .path(id);

        JsonObject json = client.httpGetRequest(webTarget, JsonObject.class);
        return deserializeIssue(json);
    }


    private Bug deserializeIssue(JsonObject issue) {

        String issueId = issue.getString("key");
        JsonObject fields = issue.getJsonObject("fields");
        JsonObject status = fields.getJsonObject("status");
        String bugStatus = status.getString("name");
        Object resolutionObj = fields.get("resolution");
        String bugResolution = (resolutionObj instanceof JsonObject) ? ((JsonObject) resolutionObj).getString("name") : resolutionObj.toString();

        return new Bug(issueId, bugStatus, bugResolution);
    }
}
