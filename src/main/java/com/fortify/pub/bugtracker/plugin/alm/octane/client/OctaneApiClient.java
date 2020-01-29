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

import java.io.Closeable;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.ws.rs.client.WebTarget;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class provides functionality for invoking specific ALM Octane
 * REST endpoints.
 */
public class OctaneApiClient implements Closeable, AutoCloseable {
	private static final Log LOG = LogFactory.getLog(OctaneApiClient.class);
	private static final MessageFormat FMT_DEEPLINK = new MessageFormat("{0}/ui/entity-navigation?p={1}/{2}&entityType=work_item&id={3}");
	public static final MessageFormat FMT_BUG_ID = new MessageFormat("{0}/{1}/{2}");
    private final OctaneHttpClient client;

	public OctaneApiClient(OctaneHttpClient client) {
        this.client = client;
    }

    @Override
    public void close() {
        this.client.close();
    }
    
    public static final String getWorkItemDeepLink(OctaneConfig octaneConfig, String workItemId) {
        return FMT_DEEPLINK.format(new Object[] {
        		octaneConfig.getBaseUrl().toString(), octaneConfig.getSharedSpaceId(),
        		octaneConfig.getWorkspaceId(), workItemId});
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
    
    public final String fileBug(final JsonObject bugContents) {
    	final JsonObject wrappedBugContents = JsonHelper.wrapAsDataArray(bugContents);
		LOG.info("fileBug: "+wrappedBugContents.toString());
		WebTarget target = client
                .getApiWorkspaceTarget()
                .path("defects");
		JsonObject result = client.httpPostRequest(target, wrappedBugContents, JsonObject.class);
		return result.getJsonArray("data").getValuesAs(this::getEntityIdFromJson).get(0);
	}
    
    public void addCommentToBug(String defectId, String comment) {
    	final JsonObject commentObject = Json.createObjectBuilder()
				.add("owner_work_item", JsonHelper.getReferenceObjectForDefectId(defectId))
				.add("text", comment)
				.build();
    	final JsonObject wrappedCommentObject = JsonHelper.wrapAsDataArray(commentObject);
    	
    	WebTarget target = client
                .getApiWorkspaceTarget()
                .path("comments");
    	
    	client.httpPostRequest(target, wrappedCommentObject, JsonObject.class);
	}

	public void transitionToPhaseId(String defectId, String phaseId) {
		final JsonObject bugUpdateContents = Json.createObjectBuilder()
			.add("phase", JsonHelper.getReferenceObjectForPhaseId(phaseId))
			.build();
		
		WebTarget target = client
                .getApiWorkspaceTarget()
                .path("defects")
                .path(defectId);
		
		client.httpPutRequest(target, bugUpdateContents, JsonObject.class);
	}

    public final String getPhaseIdForDefectId(final String defectId) {
    	return getPhaseForDefectId(defectId).getString("id");
    }
    
    public final String getPhaseNameForDefectId(final String defectId) {
    	return getPhaseForDefectId(defectId).getString("name");
    }
    
    private final JsonObject getPhaseForDefectId(final String defectId) {
        WebTarget target = client
                .getApiWorkspaceTarget()
                .path("defects")
                .path(defectId)
                .queryParam("fields", "phase");

        return client.httpGetRequest(target, JsonObject.class).getJsonObject("phase");
    }

	public final List<String> getWorkItemRootNames() {
		return queryEntityNames("work_item_roots", null);
	}
	
	public final String getIdForWorkItemRootName(String rootName) {
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
	
	public final String getIdForEpicName(String rootName, String epicName) {
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
	
	public final String getIdForFeatureName(String rootName, String epicName, String featureName) {
		final String query = String.format("\"name EQ '%s' ; parent EQ {name EQ '%s' ; parent EQ {name EQ '%s'}}\"", featureName, epicName, rootName);
		return StringUtils.isBlank(rootName) || StringUtils.isBlank(epicName) || StringUtils.isBlank(featureName)
				? null
				: queryEntityIds("features", query).get(0);
	}
	
	private final List<String> queryEntityNames(String entityName, String query) {
		List<String> result = queryEntities(entityName, query, "name").getValuesAs(this::getEntityNameFromJson);
		LOG.info(String.format("queryEntityNames(%s, %s): %s", entityName, query, result.toString()));
		return result;
	}
	
	private final List<String> queryEntityIds(String entityName, String query) {
		List<String> result = queryEntities(entityName, query, "id").getValuesAs(this::getEntityIdFromJson);
		LOG.info(String.format("queryEntityNames(%s, %s): %s", entityName, query, result.toString()));
		return result;
	}
	
	private final String getEntityNameFromJson(JsonObject json) {
		return json.getString("name");
	}
	
	private final String getEntityIdFromJson(JsonObject json) {
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
}
