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
import com.fortify.pub.bugtracker.plugin.fields.IDefaultMethodsBugParamDefinitionEnum;
import com.fortify.pub.bugtracker.support.BugParam;
import com.fortify.pub.bugtracker.support.BugParamChoice;
import com.fortify.pub.bugtracker.support.BugParamText;
import com.fortify.pub.bugtracker.support.BugParamTextArea;

/**
 * This enumeration provides definitions for the default Octane bug parameters. For each enum entry,
 * the {@link IDefaultMethodsBugParamDefinitionEnum} interface provides various utility methods.
 * 
 * @author Ruud Senden
 *
 */
enum OctaneDefaultBugParams implements IDefaultMethodsBugParamDefinitionEnum<IOctaneBugParamChoiceOnChangeHandler> {
	TYPE(OctaneDefaultBugParams::createBugParamType, null),
	ROOT(OctaneDefaultBugParams::createBugParamRoot, OctaneDefaultBugParams::updateEpicParam),
	EPIC(OctaneDefaultBugParams::createBugParamEpic, OctaneDefaultBugParams::updateFeatureParam),
	FEATURE(OctaneDefaultBugParams::createBugParamFeature, null),
    NAME(OctaneDefaultBugParams::createBugParamName, null),
    DESCRIPTION(OctaneDefaultBugParams::createBugParamDescription, null),
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
    OctaneDefaultBugParams(Producer<? extends BugParam> producer, IOctaneBugParamChoiceOnChangeHandler onChangeHandler) {
    	this.bugParamDefinition = new BugParamDefinition<>(producer, onChangeHandler);
    }
    @Override
    public BugParamDefinition<IOctaneBugParamChoiceOnChangeHandler> getBugParamDefinition() {
    	return bugParamDefinition;
    }
    
    static final void updateRootParam(OctaneApiClient client, List<BugParam> bugParams) {
		BugParam rootParam = OctaneDefaultBugParams.ROOT.getCurrentBugParam(bugParams);
		rootParam.setRequired(true);
		updateChoiceList(rootParam, client.getWorkItemRootNames());
		updateEpicParam(client, bugParams);
	}
	
	static final void updateEpicParam(OctaneApiClient client, List<BugParam> bugParams) {
		String rootName = OctaneDefaultBugParams.ROOT.getCurrentBugParam(bugParams).getValue();
		BugParam epicParam = OctaneDefaultBugParams.EPIC.getCurrentBugParam(bugParams);
		updateChoiceList(epicParam, client.getEpicNames(rootName));
		updateFeatureParam(client, bugParams);
	}
	
	static final void updateFeatureParam(OctaneApiClient client, List<BugParam> bugParams) {
		String rootName = OctaneDefaultBugParams.ROOT.getCurrentBugParam(bugParams).getValue();
		String epicName = OctaneDefaultBugParams.EPIC.getCurrentBugParam(bugParams).getValue();
		BugParam featureParam = OctaneDefaultBugParams.FEATURE.getCurrentBugParam(bugParams);
		updateChoiceList(featureParam, client.getFeatureNames(rootName, epicName));
		featureParam.setRequired(StringUtils.isNotBlank(epicName));
	}
	
	static private final void updateChoiceList(BugParam bugParam, List<String> choiceList) {
		Validate.isInstanceOf(BugParamChoice.class, bugParam, "Cannot update choice list for bug paramater type "+bugParam.getClass().getName());
		((BugParamChoice)bugParam).setChoiceList(choiceList);
	}
}