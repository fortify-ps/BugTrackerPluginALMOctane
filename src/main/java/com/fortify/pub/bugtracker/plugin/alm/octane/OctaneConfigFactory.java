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

import java.util.List;
import java.util.Map;

import org.glassfish.jersey.internal.util.Producer;

import com.fortify.pub.bugtracker.plugin.alm.octane.client.OctaneConfig;
import com.fortify.pub.bugtracker.plugin.fields.IDefaultMethodsBugTrackerConfigDefinition;
import com.fortify.pub.bugtracker.plugin.fields.IDefaultMethodsBugTrackerConfigDefinitionEnum;
import com.fortify.pub.bugtracker.support.BugTrackerConfig;

/**
 * This class provides the following functionality:
 * 
 * <ul>
 *  <li>The {@link OctaneConfigField} enum provides {@link BugTrackerConfig} instances
 *      that allow for configuring Octane settings like URL, shared space id and workspace
 *      id.</li>
 *  <li>The {@link #addBugTrackerConfigFields(List)} can be called to add all 
 *      {@link BugTrackerConfig} instances to the given {@link List}.</li>
 *  <li>The {@link #createOctaneConfig(Map)} method can be used to get an {@link OctaneConfig} 
 *      instance based on the given bug tracker configuration {@link Map}.</li> 
 * </ul>
 *  
 * @author Ruud Senden
 *
 */
public class OctaneConfigFactory {
	
	/**
	 * Define the various ALM Octane configuration fields.
	 */
    private static enum OctaneConfigField implements IDefaultMethodsBugTrackerConfigDefinitionEnum {
    	URL("url", "ALM Octane URL", "Server at which ALM Octane REST API is accessible. Example: http://w2k3r2sp2:8080", true),
    	SHARED_SPACE_ID("sharedspaceId", "Shared Space ID", "ID of shared space. Either numeric or a UUID.", true),
        WORKSPACE_ID("workspaceId", "Workspace ID", "ID of workspace. Numeric.", true)
        ;

        private final Producer<BugTrackerConfig> bugTrackerConfigProducer;
        OctaneConfigField(final String id, final String displayLabel, final String description, boolean required) {
        	this.bugTrackerConfigProducer = ()->new BugTrackerConfig()
        			.setIdentifier(id)
        			.setDisplayLabel(displayLabel)
        			.setDescription(description)
        			.setRequired(required);
        }
        @Override
		public Producer<BugTrackerConfig> getBugTrackerConfigProducer() {
			return bugTrackerConfigProducer;
		}
        
    }
	
    /**
	 * Add the {@link BugTrackerConfig} instances for the various ALM Octane
	 * configuration fields to the given {@link List}. 
	 * 
	 * @param list
	 */
    public static final void addBugTrackerConfigFields(List<BugTrackerConfig> list) {
		IDefaultMethodsBugTrackerConfigDefinition.addFields(list, OctaneConfigField.values());
	}

    /**
	 * Get the {@link OctaneConfig} instance based on the given bug tracker 
	 * configuration {@link Map}.
	 * 
	 * @param bugTrackerConfig
	 * @return
	 */
	public static final OctaneConfig createOctaneConfig(Map<String, String> bugTrackerConfig) {
		return new OctaneConfig(
				OctaneConfigField.URL.getNormalizedURLValue(bugTrackerConfig),
				OctaneConfigField.SHARED_SPACE_ID.getValue(bugTrackerConfig),
				OctaneConfigField.WORKSPACE_ID.getValue(bugTrackerConfig));
	}
}
