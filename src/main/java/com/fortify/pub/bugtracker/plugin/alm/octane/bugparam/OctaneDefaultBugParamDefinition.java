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
import java.util.function.BiConsumer;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.glassfish.jersey.internal.util.Producer;

import com.fortify.pub.bugtracker.plugin.alm.octane.client.api.OctaneApiClient;
import com.fortify.pub.bugtracker.plugin.bugparam.BugParamDefinition;
import com.fortify.pub.bugtracker.plugin.bugparam.IBugParamDefinitionProvider;
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
enum OctaneDefaultBugParamDefinition implements IBugParamDefinitionProvider<BiConsumer<OctaneApiClient, List<BugParam>>> {
	TYPE(OctaneDefaultBugParamDefinition::createBugParamType, null),
	ROOT(OctaneDefaultBugParamDefinition::createBugParamRoot, OctaneDefaultBugParamDefinition::updateEpicParam),
	EPIC(OctaneDefaultBugParamDefinition::createBugParamEpic, OctaneDefaultBugParamDefinition::updateFeatureParam),
	FEATURE(OctaneDefaultBugParamDefinition::createBugParamFeature, null),
    NAME(OctaneDefaultBugParamDefinition::createBugParamName, null),
    DESCRIPTION(OctaneDefaultBugParamDefinition::createBugParamDescription, null),
    ;
	
	/**
	 * Create 'Type' parameter, for now just listing 'Defect' as the only available issue type 
	 * @return
	 */
	private static final BugParamChoice createBugParamType() {
		return (BugParamChoice)new BugParamChoice().setChoiceList(Arrays.asList("Defect"))
				.setDisplayLabel("Type").setValue("Defect").setRequired(true);
	}
	
	/**
	 * Create 'Root' parameter, listing all ALM Octane work item root entity names
	 * @return
	 */
	private static final BugParamChoice createBugParamRoot() {
		return (BugParamChoice)new BugParamChoice().setDisplayLabel("Root").setRequired(true);
	}
	
	/**
	 * Create 'Epic' parameter, listing all ALM Octane epic entity names for the currently
	 * selected 'Root' parameter
	 * @return
	 */
	private static final BugParamChoice createBugParamEpic() {
		return (BugParamChoice)new BugParamChoice().setDisplayLabel("Epic");
	}
	
	/**
	 * Create 'Feature' parameter, listing all ALM Octane feature entity names for the currently
	 * selected 'Epic' parameter
	 * @return
	 */
	private static final BugParamChoice createBugParamFeature() {
		return (BugParamChoice)new BugParamChoice().setDisplayLabel("Feature");
	}
	
	/**
	 * Create 'Name' parameter, used to generate the defect title
	 * @return
	 */
	private static final BugParam createBugParamName() {
		return new BugParamText().setDisplayLabel("Name").setMaxLength(254).setRequired(true)
				.setValue("Fix $ATTRIBUTE_CATEGORY$ in $ATTRIBUTE_FILE$");
	}
	
	/**
	 * Create 'Description' parameter, used to generate the defect description
	 * @return
	 */
	private static final BugParam createBugParamDescription() {
		return new BugParamTextArea().setDisplayLabel("Description").setRequired(true)
				.setValue("Issue Ids: $ATTRIBUTE_INSTANCE_ID$\\n$ISSUE_DEEPLINK$");
	}

	private final BugParamDefinition<BiConsumer<OctaneApiClient, List<BugParam>>> bugParamDefinition;
	
	/**
	 * Constructor for enumeration entries, requiring a {@link BugParam} {@link Producer}, and
	 * an onChange handler in the form of a {@link BiConsumer} instance that accepts an 
	 * {@link OctaneApiClient} instance and a {@link List} of {@link BugParam} instances.
	 * 
	 * @param producer
	 * @param onChangeHandler
	 */
    OctaneDefaultBugParamDefinition(Producer<? extends BugParam> producer, BiConsumer<OctaneApiClient, List<BugParam>> onChangeHandler) {
    	this.bugParamDefinition = new BugParamDefinition<>(name(),producer, onChangeHandler);
    }
    
    /**
     * Get the {@link BugParamDefinition} instance for the current enumeration entry.
     */
    @Override
    public BugParamDefinition<BiConsumer<OctaneApiClient, List<BugParam>>> definition() {
    	return bugParamDefinition;
    }
    
    /**
     * Update the choice list of the 'Root' parameter in the given {@link List} 
     * of {@link BugParam} instances, by loading the available work item root names 
     * from ALM Octane. As this update may have an effect on dependent fields, this
     * method will call {@link #updateEpicParam(OctaneApiClient, List)} after the
     * 'Root' parameter has been updated.
     * 
     * @param client
     * @param bugParams
     */
    static final void updateRootParam(OctaneApiClient client, List<BugParam> bugParams) {
		BugParam rootParam = OctaneDefaultBugParamDefinition.ROOT.definition().getCurrentBugParam(bugParams);
		rootParam.setRequired(true);
		List<String> rootChoiceList = client.getWorkItemRootNames();
		updateChoiceList(rootParam, rootChoiceList);
		updateEpicParam(client, bugParams);
	}
	
    /**
     * Update the choice list of the 'Epic' parameter in the given {@link List} 
     * of {@link BugParam} instances, by loading the available epic names from 
     * ALM Octane. As this update may have an effect on dependent fields, this
     * method will call {@link #updateFeatureParam(OctaneApiClient, List)} after the
     * 'Epic' parameter has been updated.
     * 
     * @param client
     * @param bugParams
     */
	static final void updateEpicParam(OctaneApiClient client, List<BugParam> bugParams) {
		String rootName = OctaneDefaultBugParamDefinition.ROOT.definition().getCurrentBugParam(bugParams).getValue();
		BugParam epicParam = OctaneDefaultBugParamDefinition.EPIC.definition().getCurrentBugParam(bugParams);
		updateChoiceList(epicParam, client.getEpicNames(rootName));
		updateFeatureParam(client, bugParams);
	}
	
	/**
     * Update the choice list of the 'Feature' parameter in the given {@link List} 
     * of {@link BugParam} instances, by loading the available feature names from 
     * ALM Octane. If an epic has been selected, the 'Feature' parameter is set to
     * required, as defects cannot have an epic as their parent.
     * 
     * @param client
     * @param bugParams
     */
	static final void updateFeatureParam(OctaneApiClient client, List<BugParam> bugParams) {
		String rootName = OctaneDefaultBugParamDefinition.ROOT.definition().getCurrentBugParam(bugParams).getValue();
		String epicName = OctaneDefaultBugParamDefinition.EPIC.definition().getCurrentBugParam(bugParams).getValue();
		BugParam featureParam = OctaneDefaultBugParamDefinition.FEATURE.definition().getCurrentBugParam(bugParams);
		updateChoiceList(featureParam, client.getFeatureNames(rootName, epicName));
		featureParam.setRequired(StringUtils.isNotBlank(epicName));
	}
	
	/**
	 * Helper method to update the choice list of a {@link BugParamChoice} instance
	 * @param bugParam
	 * @param choiceList
	 */
	static private final void updateChoiceList(BugParam bugParam, List<String> choiceList) {
		Validate.isInstanceOf(BugParamChoice.class, bugParam, "Cannot update choice list for bug paramater type "+bugParam.getClass().getName());
		((BugParamChoice)bugParam).setChoiceList(choiceList);
		if ( choiceList!=null && choiceList.size()==1 ) {
			bugParam.setValue(choiceList.get(0));
		}
	}
}