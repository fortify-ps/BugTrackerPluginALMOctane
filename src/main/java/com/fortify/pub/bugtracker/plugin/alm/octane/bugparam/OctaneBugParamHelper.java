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
package com.fortify.pub.bugtracker.plugin.alm.octane.bugparam;

import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonValue;

import org.apache.commons.lang3.StringUtils;

import com.fortify.pub.bugtracker.plugin.alm.octane.client.OctaneApiClient;
import com.fortify.pub.bugtracker.plugin.alm.octane.client.OctaneEntity;
import com.fortify.pub.bugtracker.plugin.fields.IBugParamDefinitionProvider;
import com.fortify.pub.bugtracker.support.BugParam;

public class OctaneBugParamHelper {
	public final List<BugParam> getBugParameters(OctaneApiClient client) {
		List<BugParam> bugParams = IBugParamDefinitionProvider.getBugParams(OctaneDefaultBugParamDefinition.values());
		OctaneDefaultBugParamDefinition.updateRootParam(client, bugParams);
		return bugParams;
	}

	public List<BugParam> onParameterChange(OctaneApiClient client, String changedParamIdentifier, List<BugParam> currentValues) {
		OctaneDefaultBugParamDefinition.valueOf(changedParamIdentifier).definition().getOnChangeHandler().onChange(client, currentValues);
		return currentValues;
	}

	public JsonObject getBugContents(OctaneApiClient client, Map<String, String> params) {
		return Json.createObjectBuilder()
				.add("parent", getParent(client, params))
				.add("phase", OctaneEntity.PHASE.getReferenceObjectForId("phase.defect.new"))
				.add("name", OctaneDefaultBugParamDefinition.NAME.definition().getNormalizedValue(params, 254))
				.add("description", OctaneDefaultBugParamDefinition.DESCRIPTION.definition().getNormalizedValue(params))
				.build();
	}

	private JsonValue getParent(OctaneApiClient client, Map<String, String> params) {
		String rootName = OctaneDefaultBugParamDefinition.ROOT.definition().getNormalizedValue(params);
		String epicName = OctaneDefaultBugParamDefinition.EPIC.definition().getNormalizedValue(params);
		String featureName = OctaneDefaultBugParamDefinition.FEATURE.definition().getNormalizedValue(params);
		if ( StringUtils.isNotBlank(featureName) ) {
			return getReferenceObjectForFeatureName(client, rootName, epicName, featureName);
		} else if ( StringUtils.isNotBlank(rootName) ) {
			return getReferenceObjectForWorkItemRootName(client, rootName);
		} else {
			throw new IllegalArgumentException("No parent defined for defect");
		}
	}

	private JsonValue getReferenceObjectForWorkItemRootName(OctaneApiClient client, String rootName) {
		return OctaneEntity.WORK_ITEM_ROOT.getReferenceObjectForId(client.getIdForWorkItemRootName(rootName));
	}

	private JsonValue getReferenceObjectForFeatureName(OctaneApiClient client, String rootName, String epicName, String featureName) {
		return OctaneEntity.FEATURE.getReferenceObjectForId(client.getIdForFeatureName(rootName, epicName, featureName));
	}
}
