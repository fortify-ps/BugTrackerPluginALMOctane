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
package com.fortify.pub.bugtracker.plugin.alm.octane.bugstate;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import com.fortify.pub.bugtracker.plugin.alm.octane.client.OctaneApiClient;
import com.fortify.pub.bugtracker.support.Bug;

public class OctaneBugStateHelper {
	public OctaneBugStateHelper() {}
	
	/* Defect phase id's in default Octane installation, as returned by 
	   /api/shared_spaces/<id>/workspaces/<id>/phases?fields=id&query=%22entity%20EQ%20'defect'%22 
	 	phase.defect.new
		phase.defect.deferred
      	phase.defect.opened
      	phase.defect.fixed
      	phase.defect.proposeclose
      	phase.defect.closed
      	phase.defect.duplicate
      	phase.defect.rejected
	 */
	
	
    @SuppressWarnings("serial")
	private static final Set<String> OPEN_PHASE_IDS = new HashSet<String>() {{
        add("phase.defect.new");
        add("phase.defect.opened");
        add("phase.defect.deferred");
    }};

    @SuppressWarnings("serial")
	private static final Set<String> CLOSED_PHASE_IDS = new HashSet<String>() {{
        add("phase.defect.rejected");
        add("phase.defect.fixed");
        add("phase.defect.proposeclose");
        add("phase.defect.closed");
    }};

    @SuppressWarnings("serial")
	private static final HashMap<String, String> TRANSITIONS_TO_OPEN = new HashMap<String, String>() {{
        put("phase.defect.fixed", "phase.defect.opened");
    }};

	public final Bug getBug(OctaneApiClient client, String bugId) {
		return new Bug(bugId, getBugStatus(client, bugId));
	}

	public final String getBugStatus(OctaneApiClient client, String bugId) {
		return client.getPhaseIdForDefectId(bugId);
	}

	public boolean isBugOpen(Bug bug) {
		return OPEN_PHASE_IDS.contains(bug.getBugStatus());
	}

	public boolean isBugClosed(Bug bug) {
		return CLOSED_PHASE_IDS.contains(bug.getBugStatus());
	}

	public boolean isBugClosedAndCanReOpen(Bug bug) {
		return isBugClosed(bug) && TRANSITIONS_TO_OPEN.containsKey(bug.getBugStatus());
	}

	public void reOpenBug(OctaneApiClient client, Bug bug, String comment) {
		String defectId = bug.getBugId();
		client.transitionToPhaseId(defectId, TRANSITIONS_TO_OPEN.get(bug.getBugStatus()));
		client.addCommentToBug(defectId, comment);
	}
}
