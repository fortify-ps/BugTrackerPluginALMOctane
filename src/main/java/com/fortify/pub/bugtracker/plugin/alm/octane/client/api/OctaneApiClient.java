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
package com.fortify.pub.bugtracker.plugin.alm.octane.client.api;

import java.io.Closeable;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.client.WebTarget;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fortify.pub.bugtracker.plugin.alm.octane.client.http.OctaneHttpClient;
import com.fortify.pub.bugtracker.plugin.alm.octane.config.OctaneConfig;

/**
 * Based on the low-level HTTP access provided by the {@link OctaneHttpClient} passed to
 * the constructor of this class, this class provides higher-level functionality for
 * working with the various ALM Octane REST API endpoints. This includes querying ALM
 * Octane entities, and creating and updating ALM Octane work items.
 */
public class OctaneApiClient implements Closeable, AutoCloseable {
	private static final Log LOG = LogFactory.getLog(OctaneApiClient.class);
	private static final MessageFormat FMT_DEEPLINK = new MessageFormat("{0}/ui/entity-navigation?p={1}/{2}&entityType=work_item&id={3}");
	public static final MessageFormat FMT_BUG_ID = new MessageFormat("{0}/{1}/{2}");
    private final OctaneHttpClient client;

    /**
     * Configure this instance with the given {@link OctaneHttpClient}
     * @param client
     */
	public OctaneApiClient(OctaneHttpClient client) {
        this.client = client;
    }

	/**
	 * Close the underlying {@link OctaneHttpClient}
	 */
    @Override
    public void close() {
        this.client.close();
    }
    
    /**
     * Static method for building a deep link to the given Octane work item id, based on
     * URL, shared space and workspace specified in the given {@link OctaneConfig}.
     *  
     * @param octaneConfig
     * @param workItemId
     * @return
     */
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
    			.path(OctaneEntity.WORK_ITEM_ROOT.plural())
    			.queryParam("fields", "id")
    			.queryParam("limit", 1);
    	client.httpRequest(HttpMethod.GET, target, JsonObject.class);
    }
    
    /**
     * Submit a defect to ALM Octane, using the given bug contents. The given
     * {@link JsonObject} should describe a single defect; this method will 
     * wrap the bug contents in a data array as required by ALM Octane.
     * @param bugContents
     * @return
     */
    public final String fileBug(final JsonObject bugContents) {
    	final JsonObject wrappedBugContents = wrapAsDataArray(bugContents);
		LOG.info("fileBug: "+wrappedBugContents.toString());
		WebTarget target = client
                .getApiWorkspaceTarget()
                .path(OctaneEntity.DEFECT.plural());
		JsonObject result = client.httpRequest(HttpMethod.POST, target, wrappedBugContents, JsonObject.class);
		return result.getJsonArray("data").getValuesAs(this::getEntityIdFromJson).get(0);
	}
    
    /**
     * Add the given comment to the defect identified by the given defect id.
     * 
     * @param defectId
     * @param comment
     */
    public void addCommentToBug(String defectId, String comment) {
    	final JsonObject commentObject = Json.createObjectBuilder()
				.add("owner_work_item", OctaneEntity.DEFECT.getReferenceObjectForId(defectId))
				.add("text", comment)
				.build();
    	final JsonObject wrappedCommentObject = wrapAsDataArray(commentObject);
    	
    	WebTarget target = client
                .getApiWorkspaceTarget()
                .path(OctaneEntity.COMMENT.plural());
    	
    	client.httpRequest(HttpMethod.POST, target, wrappedCommentObject, JsonObject.class);
	}

    /**
     * Transition the defect identified by the given defect id to the given phase id.
     * @param defectId
     * @param phaseId
     */
	public void transitionToPhaseId(String defectId, String phaseId) {
		final JsonObject bugUpdateContents = Json.createObjectBuilder()
			.add("phase", OctaneEntity.PHASE.getReferenceObjectForId(phaseId))
			.build();
		
		WebTarget target = client
                .getApiWorkspaceTarget()
                .path(OctaneEntity.DEFECT.plural())
                .path(defectId);
		
		client.httpRequest(HttpMethod.PUT, target, bugUpdateContents, JsonObject.class);
	}

	/**
	 * Get the current phase id for the defect identified by the given defect id
	 * @param defectId
	 * @return
	 */
    public final String getPhaseIdForDefectId(final String defectId) {
    	return getEntityIdFromJson(getPhaseForDefectId(defectId));
    }
    
    /**
	 * Get the current phase name for the defect identified by the given defect id
	 * @param defectId
	 * @return
	 */
    public final String getPhaseNameForDefectId(final String defectId) {
    	return getEntityNameFromJson(getPhaseForDefectId(defectId));
    }
    
    /**
     * Get the current phase {@link JsonObject} for the defect identified by the given defect id
     * @param defectId
     * @return
     */
    private final JsonObject getPhaseForDefectId(final String defectId) {
        WebTarget target = client
                .getApiWorkspaceTarget()
                .path(OctaneEntity.DEFECT.plural())
                .path(defectId)
                .queryParam("fields", "phase");

        return client.httpRequest(HttpMethod.GET, target, JsonObject.class).getJsonObject("phase");
    }

    /**
     * Get the {@link List} of work item root names
     * @return
     */
	public final List<String> getWorkItemRootNames() {
		return queryEntityNames(OctaneEntity.WORK_ITEM_ROOT, null);
	}
	
	/**
	 * Get the id for the given work item root name
	 * @param rootName
	 * @return
	 */
	public final String getIdForWorkItemRootName(String rootName) {
		final String query = String.format("\"name EQ '%s'\"", rootName);
		return StringUtils.isBlank(rootName)
				? null
				: queryEntityIds(OctaneEntity.WORK_ITEM_ROOT, query).get(0);
	}

	/**
	 * Get the {@link List} of epic names for the given work item root name 
	 * @param rootName
	 * @return
	 */
	public final List<String> getEpicNames(String rootName) {
		final String query = String.format("\"parent EQ {name EQ '%s'}\"", rootName);
		return StringUtils.isBlank(rootName) 
				? Collections.emptyList()
				: queryEntityNames(OctaneEntity.EPIC, query);
	}
	
	/**
	 * Get the id for the given epic name in the given work item root name
	 * @param rootName
	 * @param epicName
	 * @return
	 */
	public final String getIdForEpicName(String rootName, String epicName) {
		final String query = String.format("\"name EQ '%s' ; parent EQ {name EQ '%s'}\"", epicName, rootName);
		return StringUtils.isBlank(rootName) || StringUtils.isBlank(epicName)
				? null
				: queryEntityIds(OctaneEntity.EPIC, query).get(0);
	}

	/**
	 * Get the {@link List} of feature names for the given epic name and work item root name
	 * @param rootName
	 * @param epicName
	 * @return
	 */
	public final List<String> getFeatureNames(String rootName, String epicName) {
		final String query = String.format("\"parent EQ {name EQ '%s' ; parent EQ {name EQ '%s'}}\"", epicName, rootName);
		return StringUtils.isBlank(rootName) || StringUtils.isBlank(epicName)
				? Collections.emptyList()
				: queryEntityNames(OctaneEntity.FEATURE, query);
	}
	
	/**
	 * Get the if for the given feature name in the given epic name in the given work item root name
	 * @param rootName
	 * @param epicName
	 * @param featureName
	 * @return
	 */
	public final String getIdForFeatureName(String rootName, String epicName, String featureName) {
		final String query = String.format("\"name EQ '%s' ; parent EQ {name EQ '%s' ; parent EQ {name EQ '%s'}}\"", featureName, epicName, rootName);
		return StringUtils.isBlank(rootName) || StringUtils.isBlank(epicName) || StringUtils.isBlank(featureName)
				? null
				: queryEntityIds(OctaneEntity.FEATURE, query).get(0);
	}
	
	/**
	 * Query the given {@link OctaneEntity} using the given query by calling the
	 * {@link #queryEntities(OctaneEntity, String, String...)} method, returning 
	 * a {@link List} of names for entities matching the query.
	 * 
	 * @param entity
	 * @param query
	 * @return
	 */
	private final List<String> queryEntityNames(OctaneEntity entity, String query) {
		List<String> result = queryEntities(entity, query, "name").getValuesAs(this::getEntityNameFromJson);
		LOG.info(String.format("queryEntityNames(%s, %s): %s", entity.plural(), query, result.toString()));
		return result;
	}
	
	/**
	 * Query the given {@link OctaneEntity} using the given query by calling the
	 * {@link #queryEntities(OctaneEntity, String, String...)} method, returning 
	 * a {@link List} of id's for entities matching the query.
	 * 
	 * @param entity
	 * @param query
	 * @return
	 */
	private final List<String> queryEntityIds(OctaneEntity entity, String query) {
		List<String> result = queryEntities(entity, query, "id").getValuesAs(this::getEntityIdFromJson);
		LOG.info(String.format("queryEntityIds(%s, %s): %s", entity.plural(), query, result.toString()));
		return result;
	}
	
	/**
	 * Get the 'name' property from the given {@link JsonObject}
	 * @param json
	 * @return
	 */
	private final String getEntityNameFromJson(JsonObject json) {
		return json.getString("name");
	}
	
	/**
	 * Get the 'id' property from the given {@link JsonObject}
	 * @param json
	 * @return
	 */
	private final String getEntityIdFromJson(JsonObject json) {
		return json.getString("id");
	}
	
	/**
	 * Query the given {@link OctaneEntity}. If the given query is not null, the query will
	 * be passed to ALM Octane to filter the results. If the given fields array is not null,
	 * ALM Octane will be requested to return the specified fields only. 
	 * 
	 * TODO Currently this doesn't handle paging, so this method may not return all 
	 *      results if the given query matches too many entries.
	 * @param entity
	 * @param query
	 * @param fields
	 * @return
	 */
    private final JsonArray queryEntities(OctaneEntity entity, String query, String... fields) {
		WebTarget target = client.getApiWorkspaceTarget().path(entity.plural());
		if ( StringUtils.isNotBlank(query) ) {
			// Somewhat strange construct to avoid Jersey interpreting the Octane query as a Jersey template
			target = target.queryParam("query", "{query}").resolveTemplate("query", query);
		}
		if ( fields!=null ) {
			target = target.queryParam("fields", String.join(",", fields));
		}
		JsonObject json = client.httpRequest(HttpMethod.GET, target, JsonObject.class);
		return json.getJsonArray("data");
	}
    
    /**
     * Wrap the given JsonObject as the only entry in a 'data' array
     * @param json
     * @return
     */
    private JsonObject wrapAsDataArray(JsonObject json) {
		return Json.createObjectBuilder()
				.add("data", Json.createArrayBuilder().add(json).build())
				.build();
	}
}
