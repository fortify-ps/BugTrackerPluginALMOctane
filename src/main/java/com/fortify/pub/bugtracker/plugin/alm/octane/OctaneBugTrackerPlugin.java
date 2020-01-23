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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fortify.pub.bugtracker.plugin.AbstractBatchBugTrackerPlugin;
import com.fortify.pub.bugtracker.plugin.BugTrackerPluginImplementation;
import com.fortify.pub.bugtracker.plugin.alm.octane.client.OctaneApiClient;
import com.fortify.pub.bugtracker.support.Bug;
import com.fortify.pub.bugtracker.support.BugParam;
import com.fortify.pub.bugtracker.support.BugParamChoice;
import com.fortify.pub.bugtracker.support.BugSubmission;
import com.fortify.pub.bugtracker.support.BugTrackerConfig;
import com.fortify.pub.bugtracker.support.IssueDetail;
import com.fortify.pub.bugtracker.support.MultiIssueBugSubmission;
import com.fortify.pub.bugtracker.support.UserAuthenticationStore;

/**
 * Implementation of SSC bug tracker plugin API for ALM Octane.
 */
@BugTrackerPluginImplementation
public class OctaneBugTrackerPlugin extends AbstractBatchBugTrackerPlugin {
    private static final Log LOG = LogFactory.getLog(OctaneBugTrackerPlugin.class);

    /* Config as injected by setConfiguration. */
    private BugTrackerOctaneApiClientFactory octaneApiClientFactory;

    public OctaneBugTrackerPlugin() {}

    @Override
    public Bug fetchBugDetails(String bugId, UserAuthenticationStore credentials) {
        LOG.info("XXX OctaneBugTrackerPlugin::fetchBugDetails");
        return null;
    }

    @Override
    public Bug fileBug(BugSubmission bugSubmission, UserAuthenticationStore credentials) {
        LOG.info("XXX OctaneBugTrackerPlugin::fileBug");
        return null;
    }

    @Override
    public List<BugParam> getBugParameters(IssueDetail issueDetail, UserAuthenticationStore credentials) {
        LOG.info("XXX OctaneBugTrackerPlugin::getBugParameters");
        /* You need at least one bug parameter, otherwise you get an NPE in SSC. */
        List<BugParam> bugParams = new ArrayList<>();

        BugParam projectParam = new BugParamChoice().setHasDependentParams(false).setIdentifier("PROJECT").setDisplayLabel("Octane Project")
                .setRequired(true).setDescription("Octane Project against which bug is to be filed");


        ((BugParamChoice)projectParam).setChoiceList(Arrays.asList("Project A", "Project B"));
        bugParams.add(projectParam);

        return bugParams;
    }

    @Override
    public List<BugTrackerConfig> getConfiguration() {
        List<BugTrackerConfig> result = new ArrayList<>();
        BugTrackerOctaneApiClientFactory.addBugTrackerConfigFields(result);
        pluginHelper.populateWithDefaultsIfAvailable(result);
        return result;
    }

    @Override
    public void setConfiguration(Map<String, String> config) {
        this.octaneApiClientFactory = new BugTrackerOctaneApiClientFactory(config);
    }

    @Override
    public boolean requiresAuthentication() {
        return true;
    }

    @Override
    public String getLongDisplayName() {
    	final StringBuilder sb = new StringBuilder(getShortDisplayName());
        if (octaneApiClientFactory!=null ) {
            sb.append(" (").append(octaneApiClientFactory.getOctaneConfig().getBaseUrl().toString()).append(')');
        }
        return sb.toString();
    }

    @Override
    public String getShortDisplayName() {
        return "Octane";
    }

    @Override
    public List<BugParam> onParameterChange(IssueDetail issueDetail, String changedParamIdentifier
            , List<BugParam> currentValues, UserAuthenticationStore credentials) {
        LOG.info("XXX OctaneBugTrackerPlugin::onParameterChange");
        List<BugParam> returnParams = new ArrayList<>();
        return returnParams;
    }

    @Override
    public void testConfiguration(com.fortify.pub.bugtracker.support.UserAuthenticationStore credentials) {
        LOG.info("XXX OctaneBugTrackerPlugin::testConfiguration");
        validateCredentials(credentials);
    }

    @Override
    public void validateCredentials(UserAuthenticationStore credentials) throws RuntimeException {
        LOG.info("XXX OctaneBugTrackerPlugin::validateCredentials");
    }

    @Override
    public String getBugDeepLink(String bugId) {
        LOG.info("XXX OctaneBugTrackerPlugin::getBugDeepLink");
        return "http://dummy";
    }

    @Override
    public List<BugParam> getBatchBugParameters(UserAuthenticationStore credentials) {
        LOG.info("XXX OctaneBugTrackerPlugin::getBatchBugParameters");
        return getBugParameters(null, credentials);
    }

    @Override
    public List<BugParam> onBatchBugParameterChange(String changedParamIdentifier, List<BugParam> currentValues, UserAuthenticationStore credentials) {
        LOG.info("XXX OctaneBugTrackerPlugin::onBatchBugParameterChange");
        return onParameterChange(null, changedParamIdentifier, currentValues, credentials);
    }

    @Override
    public Bug fileMultiIssueBug(MultiIssueBugSubmission bugSubmission, UserAuthenticationStore authStore) {
        LOG.info("XXX OctaneBugTrackerPlugin::fileMultiIssueBug");
        try ( OctaneApiClient conn = getOctaneRestConnector(authStore) ) {
        	//conn.
        	/*
        	

        EntityList defectList = octane.entityList("defects");

        FieldModel<String> nameField = new StringFieldModel("name", bugSubmission.getIssueDetails().get(0).getCategory());
        FieldModel<EntityModel> parentField =
                new ReferenceFieldModel("parent",
                        new EntityModel(new HashSet<>(Arrays.asList(
                                new StringFieldModel("type", "work_item_root"),
                                new StringFieldModel("id", "1001")))));
        Set<FieldModel> entityFields = new HashSet<>(Arrays.asList(nameField, parentField));
        EntityModel entityModel = new EntityModel(entityFields);
        Collection<EntityModel> createdEntities =
                defectList.create()
                        .entities(new ArrayList<>(Collections.singletonList(entityModel)))
                        .execute();

        return new Bug(createdEntities.iterator().next().getId(), "open");
        	 */
        }

        return null;
    }

	@Override
    public boolean isBugOpen(Bug bug, UserAuthenticationStore credentials) {
        LOG.info("XXX OctaneBugTrackerPlugin::isBugOpen");
        return true;
    }

    @Override
    public boolean isBugClosed(Bug bug, UserAuthenticationStore credentials) {
        LOG.info("XXX OctaneBugTrackerPlugin::isBugClosed");
        return false;
    }

    @Override
    public boolean isBugClosedAndCanReOpen(Bug bug, UserAuthenticationStore credentials) {
        LOG.info("XXX OctaneBugTrackerPlugin::isBugClosedAndCanReOpen");
        return false;
    }

    @Override
    public void reOpenBug(Bug bug, String comment, UserAuthenticationStore credentials) {
        LOG.info("XXX OctaneBugTrackerPlugin::reOpenBug");
    }

    @Override
    public void addCommentToBug(Bug bug, String comment, UserAuthenticationStore credentials) {
        LOG.info("XXX OctaneBugTrackerPlugin::addCommentToBug");
    }
    
    private final OctaneApiClient getOctaneRestConnector(final UserAuthenticationStore authStore) {
		return octaneApiClientFactory.getOctaneRestApi(authStore);
	}

}
