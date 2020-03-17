/*******************************************************************************
 * (c) Copyright 2020 Micro Focus or one of its affiliates
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
import java.util.Map;
import java.util.Set;

import com.fortify.pub.bugtracker.plugin.alm.octane.client.api.OctaneApiClient;
import com.fortify.pub.bugtracker.support.Bug;

/**
 * This helper class provides various methods related to ALM Octane bug state management.
 * The {@link #getBug(OctaneApiClient, String)} and {@link #getBugStatus(OctaneApiClient, String)}
 * return information about the current status of a given ALM Octane defect; the other methods
 * in this class interpret this status information to determine whether a bug is open, closed
 * or re-openable.
 * 
 * @author Ruud Senden
 *
 */
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

    /**
     * Get a {@link Bug} instance for the given bug id, calling 
     * {@link #getBugStatus(OctaneApiClient, String)} to get the 
     * corresponding bug status. 
     * @param client
     * @param bugId
     * @return
     */
	public final Bug getBug(OctaneApiClient client, String bugId) {
		return new Bug(bugId, getBugStatus(client, bugId));
	}

	/**
	 * Return the bug status for the given bug id. In our case, we return
	 * the current phase id for the ALM Octane defect identified by the
	 * given bug id.
	 *   
	 * @param client
	 * @param bugId
	 * @return
	 */
	public final String getBugStatus(OctaneApiClient client, String bugId) {
		return client.getPhaseIdForDefectId(bugId);
	}

	/**
	 * Determine whether the bug status contained in the given {@link Bug}
	 * instance corresponds to one of the phase id's listed in the
	 * {@link #OPEN_PHASE_IDS} set.
	 * @param bug
	 * @return
	 */
	public boolean isBugOpen(Bug bug) {
		return OPEN_PHASE_IDS.contains(bug.getBugStatus());
	}

	/**
	 * Determine whether the bug status contained in the given {@link Bug}
	 * instance corresponds to one of the phase id's listed in the
	 * {@link #CLOSED_PHASE_IDS} set.
	 * @param bug
	 * @return
	 */
	public boolean isBugClosed(Bug bug) {
		return CLOSED_PHASE_IDS.contains(bug.getBugStatus());
	}

	/**
	 * Determine whether the bug status contained in the given {@link Bug}
	 * instance corresponds to one of the phase id's listed in the
	 * {@link #CLOSED_PHASE_IDS} set (as returned by {@link #isBugClosed(Bug)}),
	 * and whether a transition for re-opening the defect exists in the 
	 * {@link #TRANSITIONS_TO_OPEN} {@link Map}.
	 * @param bug
	 * @return
	 */
	public boolean isBugClosedAndCanReOpen(Bug bug) {
		return isBugClosed(bug) && TRANSITIONS_TO_OPEN.containsKey(bug.getBugStatus());
	}

	/**
	 * Re-open the defect identified by the bug id as specified in the given {@link Bug}
	 * instance.
	 * 
	 * @param client
	 * @param bug
	 * @param comment
	 */
	public void reOpenBug(OctaneApiClient client, Bug bug, String comment) {
		String defectId = bug.getBugId();
		client.transitionToPhaseId(defectId, TRANSITIONS_TO_OPEN.get(bug.getBugStatus()));
		client.addCommentToBug(defectId, comment);
	}
}
