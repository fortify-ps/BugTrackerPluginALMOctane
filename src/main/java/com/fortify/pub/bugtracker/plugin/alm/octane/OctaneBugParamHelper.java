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

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Validate;

import com.fortify.pub.bugtracker.plugin.alm.octane.client.OctaneApiClient;
import com.fortify.pub.bugtracker.support.BugParam;
import com.fortify.pub.bugtracker.support.BugParamChoice;
import com.fortify.pub.bugtracker.support.BugParamText;
import com.fortify.pub.bugtracker.support.BugParamTextArea;
import com.fortify.pub.bugtracker.support.BugTrackerConfig;
import com.fortify.pub.bugtracker.support.IssueDetail;

public class OctaneBugParamHelper {
	@FunctionalInterface
	private static interface BugParamFieldOnChangeHandler {
		public void onChange(OctaneBugParamHelper helper, OctaneApiClient client, List<BugParam> currentValues);
	}
	
	private static enum BugTrackerField {
		TYPE(BugParamChoice.class, "Type", "Defect", null, "Defect"),
		ROOT(BugParamChoice.class, "Root", null, (helper,client,bugParams)->helper.updateEpicChoiceList(client, bugParams)),
		EPIC(BugParamChoice.class, "Epic", null, (helper,client,bugParams)->helper.updateFeatureChoiceList(client, bugParams)),
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
        public String getIdentifier() {
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
        public BugParam getCurrentValue(List<BugParam> currentValues) {
        	return currentValues.stream().filter(this::hasSameId).findFirst().get();
        }
        public boolean hasSameId(BugParam bugParam) {
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

	public final List<BugParam> getBugParameters(OctaneApiClient client, IssueDetail issueDetail) {
		List<BugParam> bugParams = new ArrayList<>(BugTrackerField.values().length);
		for ( BugTrackerField field : BugTrackerField.values() ) {
			bugParams.add(field.createBugParam());
		}
		updateRootChoiceList(client, bugParams);
        return bugParams;
	}

	public List<BugParam> onParameterChange(OctaneApiClient client, IssueDetail issueDetail, String changedParamIdentifier, List<BugParam> currentValues) {
		BugTrackerField.valueOf(changedParamIdentifier).onChangeHandler.onChange(this, client, currentValues);
		return currentValues;
	}
	
	private final void updateRootChoiceList(OctaneApiClient client, List<BugParam> bugParams) {
		updateChoiceList(BugTrackerField.ROOT.getCurrentValue(bugParams), client.getWorkItemRootNames());
		updateEpicChoiceList(client, bugParams);
	}
	
	private final void updateEpicChoiceList(OctaneApiClient client, List<BugParam> bugParams) {
		String rootName = BugTrackerField.ROOT.getCurrentValue(bugParams).getValue();
		updateChoiceList(BugTrackerField.EPIC.getCurrentValue(bugParams), client.getEpicNames(rootName));
		updateFeatureChoiceList(client, bugParams);
	}
	
	private final void updateFeatureChoiceList(OctaneApiClient client, List<BugParam> bugParams) {
		String rootName = BugTrackerField.ROOT.getCurrentValue(bugParams).getValue();
		String epicName = BugTrackerField.EPIC.getCurrentValue(bugParams).getValue();
		updateChoiceList(BugTrackerField.FEATURE.getCurrentValue(bugParams), client.getFeatureNames(rootName, epicName));
	}
	
	private final void updateChoiceList(BugParam bugParam, List<String> choiceList) {
		Validate.isInstanceOf(BugParamChoice.class, bugParam, "Cannot update choice list for bug paramater type "+bugParam.getClass().getName());
		((BugParamChoice)bugParam).setChoiceList(choiceList);
	}
}
