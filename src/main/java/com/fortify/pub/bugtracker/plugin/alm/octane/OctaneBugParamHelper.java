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
package com.fortify.pub.bugtracker.plugin.alm.octane;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonValue;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import com.fortify.pub.bugtracker.plugin.alm.octane.client.OctaneApiClient;
import com.fortify.pub.bugtracker.support.BugParam;
import com.fortify.pub.bugtracker.support.BugParamChoice;
import com.fortify.pub.bugtracker.support.BugParamText;
import com.fortify.pub.bugtracker.support.BugParamTextArea;
import com.fortify.pub.bugtracker.support.BugTrackerConfig;

public class OctaneBugParamHelper {
	@FunctionalInterface
	private static interface BugParamFieldOnChangeHandler {
		public void onChange(OctaneBugParamHelper helper, OctaneApiClient client, List<BugParam> currentValues);
	}
	
	private static enum BugTrackerField {
		TYPE(BugParamChoice.class, "Type", "Defect", null, "Defect"),
		ROOT(BugParamChoice.class, "Root", null, (helper,client,bugParams)->helper.updateEpicParam(client, bugParams)),
		EPIC(BugParamChoice.class, "Epic", null, (helper,client,bugParams)->helper.updateFeatureParam(client, bugParams)),
		FEATURE(BugParamChoice.class, "Feature", null, null),
        NAME(BugParamText.class, "Name", "Fix $ATTRIBUTE_CATEGORY$ in $ATTRIBUTE_FILE$", null),
        DESCRIPTION(BugParamTextArea.class, "Description", "Issue Ids: $ATTRIBUTE_INSTANCE_ID$\n$ISSUE_DEEPLINK$", null),
        ;

		private final Class<? extends BugParam> type;
        private final String displayLabel;
        private final String defaultValue;
        private final BugParamFieldOnChangeHandler onChangeHandler;
        private final List<String> choiceList;
        BugTrackerField(final Class<? extends BugParam> type, final String displayLabel, final String defaultValue, BugParamFieldOnChangeHandler onChangeHandler, String... choiceList) {
        	Validate.isTrue(onChangeHandler==null || BugParamChoice.class.isAssignableFrom(type), 
        			"Only BugParamChoice instances can have an onChange handler");
        	Validate.isTrue(ArrayUtils.isEmpty(choiceList) || BugParamChoice.class.isAssignableFrom(type), 
        			"Only BugParamChoice instances can have a choice list");
        	this.type = type;
            this.displayLabel = displayLabel;
            this.defaultValue = defaultValue;
            this.onChangeHandler = onChangeHandler;
            this.choiceList = Arrays.asList(ArrayUtils.nullToEmpty(choiceList));
        }
        private String getIdentifier() {
            return name();
        }
        public BugParam createBugParam() {
        	try {
				BugParam result = type.newInstance()
					.setIdentifier(getIdentifier())
					.setDisplayLabel(displayLabel)
					.setValue(defaultValue);
				if ( onChangeHandler!=null ) {
					((BugParamChoice)result).setHasDependentParams(true);
				}
				if ( choiceList.size()>0 ) {
					((BugParamChoice)result).setChoiceList(choiceList);
				}
				return result;
			} catch (InstantiationException | IllegalAccessException e) {
				throw new RuntimeException("Error instantiating "+type.getName(), e);
			}
        }
        public BugParam getCurrentBugParam(List<BugParam> currentValues) {
        	return currentValues.stream().filter(this::hasSameId).findFirst().get();
        }
        public String getValue(Map<String, String> values) {
        	return values.get(getIdentifier());
        }
        private boolean hasSameId(BugParam bugParam) {
        	return getIdentifier().equals(bugParam.getIdentifier());
        }
    }
	
	
	/**
	 * Add the {@link BugTrackerConfig} instances for the various ALM Octane
	 * configuration fields to the given {@link List}. Currently this class
	 * doesn't provide any configuration settings.
	 * 
	 * @param list
	 */
    public static final void addBugTrackerConfigFields(List<BugTrackerConfig> list) {}
	
    /**
     * Construct an instance of this class with the given bug tracker configuration {@link Map}.
     * 
     * @param bugTrackerConfig
     */
	public OctaneBugParamHelper(Map<String, String> bugTrackerConfig) {
		// Currently we don't provide/use any configuration settings, so no need to store configuration
		// Potentially we could add configuration settings for default and allowed parameter values though,
		// similar to the Jira bug tracker plugin.
	}

	public final List<BugParam> getBugParameters(OctaneApiClient client) {
		List<BugParam> bugParams = new ArrayList<>(BugTrackerField.values().length);
		for ( BugTrackerField field : BugTrackerField.values() ) {
			bugParams.add(field.createBugParam());
		}
		updateRootParam(client, bugParams);
        return bugParams;
	}

	public List<BugParam> onParameterChange(OctaneApiClient client, String changedParamIdentifier, List<BugParam> currentValues) {
		BugTrackerField.valueOf(changedParamIdentifier).onChangeHandler.onChange(this, client, currentValues);
		return currentValues;
	}
	
	private final void updateRootParam(OctaneApiClient client, List<BugParam> bugParams) {
		BugParam rootParam = BugTrackerField.ROOT.getCurrentBugParam(bugParams);
		rootParam.setRequired(true);
		updateChoiceList(rootParam, client.getWorkItemRootNames());
		updateEpicParam(client, bugParams);
	}
	
	private final void updateEpicParam(OctaneApiClient client, List<BugParam> bugParams) {
		String rootName = BugTrackerField.ROOT.getCurrentBugParam(bugParams).getValue();
		BugParam epicParam = BugTrackerField.EPIC.getCurrentBugParam(bugParams);
		updateChoiceList(epicParam, client.getEpicNames(rootName));
		updateFeatureParam(client, bugParams);
	}
	
	private final void updateFeatureParam(OctaneApiClient client, List<BugParam> bugParams) {
		String rootName = BugTrackerField.ROOT.getCurrentBugParam(bugParams).getValue();
		String epicName = BugTrackerField.EPIC.getCurrentBugParam(bugParams).getValue();
		BugParam featureParam = BugTrackerField.FEATURE.getCurrentBugParam(bugParams);
		updateChoiceList(featureParam, client.getFeatureNames(rootName, epicName));
		featureParam.setRequired(StringUtils.isNotBlank(epicName));
	}
	
	private final void updateChoiceList(BugParam bugParam, List<String> choiceList) {
		Validate.isInstanceOf(BugParamChoice.class, bugParam, "Cannot update choice list for bug paramater type "+bugParam.getClass().getName());
		((BugParamChoice)bugParam).setChoiceList(choiceList);
	}

	public JsonObject getBugContents(OctaneApiClient client, Map<String, String> params) {
		return Json.createObjectBuilder()
				.add("parent", getParent(client, params))
				.add("phase", getPhase(client, params))
				.add("name", BugTrackerField.NAME.getValue(params))
				.add("description", BugTrackerField.DESCRIPTION.getValue(params))
				.build();
	}

	private JsonValue getPhase(OctaneApiClient client, Map<String, String> params) {
		return Json.createObjectBuilder()
			.add("type", "phase")
			.add("id", "phase.defect.new")
			.build();
	}

	private JsonValue getParent(OctaneApiClient client, Map<String, String> params) {
		String rootName = BugTrackerField.ROOT.getValue(params);
		String epicName = BugTrackerField.EPIC.getValue(params);
		String featureName = BugTrackerField.FEATURE.getValue(params);
		if ( StringUtils.isNotBlank(featureName) ) {
			return getParent("feature", client.getFeatureId(rootName, epicName, featureName));
		} else if ( StringUtils.isNotBlank(epicName) ) {
			return getParent("epic", client.getEpicId(rootName, epicName));
		} else if ( StringUtils.isNotBlank(rootName) ) {
			return getParent("work_item_root", client.getWorkItemRootId(rootName));
		} else {
			throw new IllegalArgumentException("No parent defined for defect");
		}
	}

	private JsonValue getParent(String type, String id) {
		return Json.createObjectBuilder()
				.add("type", type)
				.add("id", id)
				.build();
	}
}
