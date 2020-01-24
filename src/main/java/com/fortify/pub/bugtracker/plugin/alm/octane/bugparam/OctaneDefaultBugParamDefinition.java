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

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.glassfish.jersey.internal.util.Producer;

import com.fortify.pub.bugtracker.plugin.alm.octane.client.OctaneApiClient;
import com.fortify.pub.bugtracker.plugin.fields.BugParamDefinition;
import com.fortify.pub.bugtracker.plugin.fields.IBugParamDefinitionProvider;
import com.fortify.pub.bugtracker.support.BugParam;
import com.fortify.pub.bugtracker.support.BugParamChoice;
import com.fortify.pub.bugtracker.support.BugParamText;
import com.fortify.pub.bugtracker.support.BugParamTextArea;

/**
 * This enumeration provides definitions for the default Octane bug parameters. For each enum entry,
 * the {@link BugParamDefinition} instance returned by the {@link #definition()} method provides
 * utility methods for working with that parameter definition.
 * 
 * @author Ruud Senden
 *
 */
enum OctaneDefaultBugParamDefinition implements IBugParamDefinitionProvider<IOctaneBugParamChoiceOnChangeHandler> {
	TYPE(OctaneDefaultBugParamDefinition::createBugParamType, null),
	ROOT(OctaneDefaultBugParamDefinition::createBugParamRoot, OctaneDefaultBugParamDefinition::updateEpicParam),
	EPIC(OctaneDefaultBugParamDefinition::createBugParamEpic, OctaneDefaultBugParamDefinition::updateFeatureParam),
	FEATURE(OctaneDefaultBugParamDefinition::createBugParamFeature, null),
    NAME(OctaneDefaultBugParamDefinition::createBugParamName, null),
    DESCRIPTION(OctaneDefaultBugParamDefinition::createBugParamDescription, null),
    ;
	
	private static final BugParamChoice createBugParamType() {
		return (BugParamChoice)new BugParamChoice().setChoiceList(Arrays.asList("Defect"))
				.setDisplayLabel("Type").setValue("Defect").setRequired(true);
	}
	
	private static final BugParamChoice createBugParamRoot() {
		return (BugParamChoice)new BugParamChoice().setDisplayLabel("Root").setRequired(true);
	}
	
	private static final BugParamChoice createBugParamEpic() {
		return (BugParamChoice)new BugParamChoice().setDisplayLabel("Epic");
	}
	
	private static final BugParamChoice createBugParamFeature() {
		return (BugParamChoice)new BugParamChoice().setDisplayLabel("Feature");
	}
	
	private static final BugParam createBugParamName() {
		return new BugParamText().setDisplayLabel("Name").setMaxLength(254).setRequired(true)
				.setValue("Fix $ATTRIBUTE_CATEGORY$ in $ATTRIBUTE_FILE$");
	}
	
	private static final BugParam createBugParamDescription() {
		return new BugParamTextArea().setDisplayLabel("Description").setRequired(true)
				.setValue("Issue Ids: $ATTRIBUTE_INSTANCE_ID$\\n$ISSUE_DEEPLINK$");
	}

	private final BugParamDefinition<IOctaneBugParamChoiceOnChangeHandler> bugParamDefinition;
    OctaneDefaultBugParamDefinition(Producer<? extends BugParam> producer, IOctaneBugParamChoiceOnChangeHandler onChangeHandler) {
    	this.bugParamDefinition = new BugParamDefinition<>(name(),producer, onChangeHandler);
    }
    @Override
    public BugParamDefinition<IOctaneBugParamChoiceOnChangeHandler> definition() {
    	return bugParamDefinition;
    }
    
    static final void updateRootParam(OctaneApiClient client, List<BugParam> bugParams) {
		BugParam rootParam = OctaneDefaultBugParamDefinition.ROOT.definition().getCurrentBugParam(bugParams);
		rootParam.setRequired(true);
		List<String> rootChoiceList = client.getWorkItemRootNames();
		updateChoiceList(rootParam, rootChoiceList);
		updateEpicParam(client, bugParams);
	}
	
	static final void updateEpicParam(OctaneApiClient client, List<BugParam> bugParams) {
		String rootName = OctaneDefaultBugParamDefinition.ROOT.definition().getCurrentBugParam(bugParams).getValue();
		BugParam epicParam = OctaneDefaultBugParamDefinition.EPIC.definition().getCurrentBugParam(bugParams);
		updateChoiceList(epicParam, client.getEpicNames(rootName));
		updateFeatureParam(client, bugParams);
	}
	
	static final void updateFeatureParam(OctaneApiClient client, List<BugParam> bugParams) {
		String rootName = OctaneDefaultBugParamDefinition.ROOT.definition().getCurrentBugParam(bugParams).getValue();
		String epicName = OctaneDefaultBugParamDefinition.EPIC.definition().getCurrentBugParam(bugParams).getValue();
		BugParam featureParam = OctaneDefaultBugParamDefinition.FEATURE.definition().getCurrentBugParam(bugParams);
		updateChoiceList(featureParam, client.getFeatureNames(rootName, epicName));
		featureParam.setRequired(StringUtils.isNotBlank(epicName));
	}
	
	static private final void updateChoiceList(BugParam bugParam, List<String> choiceList) {
		Validate.isInstanceOf(BugParamChoice.class, bugParam, "Cannot update choice list for bug paramater type "+bugParam.getClass().getName());
		((BugParamChoice)bugParam).setChoiceList(choiceList);
		if ( choiceList!=null && choiceList.size()==1 ) {
			bugParam.setValue(choiceList.get(0));
		}
	}
}