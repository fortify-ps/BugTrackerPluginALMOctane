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
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fortify.pub.bugtracker.plugin.AbstractBatchBugTrackerPlugin;
import com.fortify.pub.bugtracker.plugin.BugTrackerPluginImplementation;
import com.fortify.pub.bugtracker.plugin.alm.octane.client.OctaneApiClient;
import com.fortify.pub.bugtracker.support.Bug;
import com.fortify.pub.bugtracker.support.BugParam;
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

    private BugTrackerOctaneApiClientFactory octaneApiClientFactory;
    private OctaneBugParamHelper octaneBugParamHelper;

    public OctaneBugTrackerPlugin() {}

    @Override
    public Bug fetchBugDetails(String bugId, UserAuthenticationStore authStore) {
        LOG.info("XXX OctaneBugTrackerPlugin::fetchBugDetails");
        return null;
    }

    @Override
    public Bug fileBug(BugSubmission bugSubmission, UserAuthenticationStore authStore) {
        LOG.info("XXX OctaneBugTrackerPlugin::fileBug");
        return null;
    }

    @Override
    public List<BugParam> getBugParameters(IssueDetail issueDetail, UserAuthenticationStore authStore) {
        LOG.info("XXX OctaneBugTrackerPlugin::getBugParameters");
        try (OctaneApiClient client = getOctaneRestApi(authStore)) {
        	return octaneBugParamHelper.getBugParameters(client, issueDetail);
        }
    }

    @Override
    public List<BugTrackerConfig> getConfiguration() {
        List<BugTrackerConfig> result = new ArrayList<>();
        BugTrackerOctaneApiClientFactory.addBugTrackerConfigFields(result);
        OctaneBugParamHelper.addBugTrackerConfigFields(result);
        pluginHelper.populateWithDefaultsIfAvailable(result);
        return result;
    }

    @Override
    public void setConfiguration(Map<String, String> config) {
        this.octaneApiClientFactory = new BugTrackerOctaneApiClientFactory(config);
        this.octaneBugParamHelper = new OctaneBugParamHelper(config);
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
        return "ALM Octane";
    }

    @Override
    public List<BugParam> onParameterChange(IssueDetail issueDetail, String changedParamIdentifier, List<BugParam> currentValues, UserAuthenticationStore authStore) {
        LOG.info("XXX OctaneBugTrackerPlugin::onParameterChange");
        try (OctaneApiClient client = getOctaneRestApi(authStore)) {
        	return octaneBugParamHelper.onParameterChange(client, issueDetail, changedParamIdentifier, currentValues);
        }
    }

    @Override
    public void testConfiguration(UserAuthenticationStore authStore) {
        LOG.info("XXX OctaneBugTrackerPlugin::testConfiguration");
        validateOctaneConnection(authStore);
    }

    @Override
    public void validateCredentials(UserAuthenticationStore authStore) throws RuntimeException {
        LOG.info("XXX OctaneBugTrackerPlugin::validateCredentials");
        validateOctaneConnection(authStore);
    }

    @Override
    public String getBugDeepLink(String bugId) {
        LOG.info("XXX OctaneBugTrackerPlugin::getBugDeepLink");
        return "http://dummy";
    }

    @Override
    public List<BugParam> getBatchBugParameters(UserAuthenticationStore authStore) {
        LOG.info("XXX OctaneBugTrackerPlugin::getBatchBugParameters");
        return getBugParameters(null, authStore);
    }

    @Override
    public List<BugParam> onBatchBugParameterChange(String changedParamIdentifier, List<BugParam> currentValues, UserAuthenticationStore authStore) {
        LOG.info("XXX OctaneBugTrackerPlugin::onBatchBugParameterChange");
        return onParameterChange(null, changedParamIdentifier, currentValues, authStore);
    }

    @Override
    public Bug fileMultiIssueBug(MultiIssueBugSubmission bugSubmission, UserAuthenticationStore authStore) {
        LOG.info("XXX OctaneBugTrackerPlugin::fileMultiIssueBug");
        try ( OctaneApiClient conn = getOctaneRestApi(authStore) ) {
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
    public boolean isBugOpen(Bug bug, UserAuthenticationStore authStore) {
        LOG.info("XXX OctaneBugTrackerPlugin::isBugOpen");
        return true;
    }

    @Override
    public boolean isBugClosed(Bug bug, UserAuthenticationStore authStore) {
        LOG.info("XXX OctaneBugTrackerPlugin::isBugClosed");
        return false;
    }

    @Override
    public boolean isBugClosedAndCanReOpen(Bug bug, UserAuthenticationStore authStore) {
        LOG.info("XXX OctaneBugTrackerPlugin::isBugClosedAndCanReOpen");
        return false;
    }

    @Override
    public void reOpenBug(Bug bug, String comment, UserAuthenticationStore authStore) {
        LOG.info("XXX OctaneBugTrackerPlugin::reOpenBug");
    }

    @Override
    public void addCommentToBug(Bug bug, String comment, UserAuthenticationStore authStore) {
        LOG.info("XXX OctaneBugTrackerPlugin::addCommentToBug");
    }
    
    
    
    private final OctaneApiClient getOctaneRestApi(final UserAuthenticationStore authStore) {
		return octaneApiClientFactory.getOctaneRestApi(authStore);
	}
    
    private final void validateOctaneConnection(final UserAuthenticationStore authStore) {
    	try (OctaneApiClient client = getOctaneRestApi(authStore)) {
            client.validateConnection();
        }
    }

}
