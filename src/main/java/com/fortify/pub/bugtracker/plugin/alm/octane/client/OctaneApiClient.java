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

import java.util.Collections;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.ws.rs.client.WebTarget;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fortify.pub.bugtracker.support.Bug;

/**
 * This class provides functionality for invoking specific ALM Octane
 * REST endpoints.
 */
public class OctaneApiClient implements AutoCloseable {
	private static final Log LOG = LogFactory.getLog(OctaneApiClient.class);
    private final OctaneHttpClient client;

	public OctaneApiClient(OctaneHttpClient client) {
        this.client = client;
    }

    @Override
    public void close() {
        this.client.close();
    }
    
    /**
     * This method performs a simple, lightweight REST call to validate the connection
     */
    public void validateConnection() {
    	WebTarget target = client.getApiWorkspaceTarget()
    			.path("work_item_roots")
    			.queryParam("fields", "id")
    			.queryParam("limit", 1);
    	client.httpGetRequest(target, JsonObject.class);
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
    
    public final String fileBug(final JsonObject bugContents) {
    	final JsonObject wrappedBugContents = wrapAsDataArray(bugContents);
		LOG.info("fileBug: "+wrappedBugContents.toString());
		WebTarget target = client
                .getApiWorkspaceTarget()
                .path("defects");
		JsonObject result = client.httpPostRequest(target, wrappedBugContents, JsonObject.class);
		return result.getJsonArray("data").getValuesAs(this::getEntityId).get(0);
	}

    public Bug getIssue(String id) {

        WebTarget target = client
                .getApiWorkspaceTarget()
                .path("issue")
                .path(id);

        JsonObject json = client.httpGetRequest(target, JsonObject.class);
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

	public final List<String> getWorkItemRootNames() {
		return queryEntityNames("work_item_roots", null);
	}
	
	public final String getWorkItemRootId(String rootName) {
		final String query = String.format("\"name EQ '%s'\"", rootName);
		return StringUtils.isBlank(rootName)
				? null
				: queryEntityIds("work_item_roots", query).get(0);
	}

	public final List<String> getEpicNames(String rootName) {
		final String query = String.format("\"parent EQ {name EQ '%s'}\"", rootName);
		return StringUtils.isBlank(rootName) 
				? Collections.emptyList()
				: queryEntityNames("epics", query);
	}
	
	public final String getEpicId(String rootName, String epicName) {
		final String query = String.format("\"name EQ '%s' ; parent EQ {name EQ '%s'}\"", epicName, rootName);
		return StringUtils.isBlank(rootName) || StringUtils.isBlank(epicName)
				? null
				: queryEntityIds("epics", query).get(0);
	}

	public final List<String> getFeatureNames(String rootName, String epicName) {
		final String query = String.format("\"parent EQ {name EQ '%s' ; parent EQ {name EQ '%s'}}\"", epicName, rootName);
		return StringUtils.isBlank(rootName) || StringUtils.isBlank(epicName)
				? Collections.emptyList()
				: queryEntityNames("features", query);
	}
	
	public final String getFeatureId(String rootName, String epicName, String featureName) {
		final String query = String.format("\"name EQ '%s' ; parent EQ {name EQ '%s' ; parent EQ {name EQ '%s'}}\"", featureName, epicName, rootName);
		return StringUtils.isBlank(rootName) || StringUtils.isBlank(epicName) || StringUtils.isBlank(featureName)
				? null
				: queryEntityIds("features", query).get(0);
	}
	
	private final List<String> queryEntityNames(String entityName, String query) {
		List<String> result = queryEntities(entityName, query, "name").getValuesAs(this::getEntityName);
		LOG.info(String.format("queryEntityNames(%s, %s): %s", entityName, query, result.toString()));
		return result;
	}
	
	private final List<String> queryEntityIds(String entityName, String query) {
		List<String> result = queryEntities(entityName, query, "id").getValuesAs(this::getEntityId);
		LOG.info(String.format("queryEntityNames(%s, %s): %s", entityName, query, result.toString()));
		return result;
	}
	
	private final String getEntityName(JsonObject json) {
		return json.getString("name");
	}
	
	private final String getEntityId(JsonObject json) {
		return json.getString("id");
	}
	
	// Note that this currently does not handle paging
    private final JsonArray queryEntities(String entityName, String query, String... fields) {
		WebTarget target = client.getApiWorkspaceTarget().path(entityName);
		if ( StringUtils.isNotBlank(query) ) {
			// Somewhat strange construct to avoid Jersey interpreting the Octane query as a Jersey template
			target = target.queryParam("query", "{query}").resolveTemplate("query", query);
		}
		if ( fields!=null ) {
			target = target.queryParam("fields", String.join(",", fields));
		}
		JsonObject json = client.httpGetRequest(target, JsonObject.class);
		return json.getJsonArray("data");
	}
    
    private JsonObject wrapAsDataArray(JsonObject json) {
		return Json.createObjectBuilder()
				.add("data", Json.createArrayBuilder().add(json).build())
				.build();
	}
}
