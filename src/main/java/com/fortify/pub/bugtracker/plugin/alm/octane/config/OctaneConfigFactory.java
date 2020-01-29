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
package com.fortify.pub.bugtracker.plugin.alm.octane.config;

import java.util.List;
import java.util.Map;

import com.fortify.pub.bugtracker.plugin.config.BugTrackerConfigDefinition;
import com.fortify.pub.bugtracker.plugin.config.IBugTrackerConfigDefinitionProvider;
import com.fortify.pub.bugtracker.support.BugTrackerConfig;

/**
 * This class provides the following functionality:
 * 
 * <ul>
 *  <li>The {@link OctaneConfigField} enum provides {@link BugTrackerConfig} instances
 *      that allow for configuring Octane settings like URL, shared space id and workspace
 *      id.</li>
 *  <li>The {@link #addBugTrackerConfigs(List)} can be called to add all 
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
    private static enum OctaneConfigField implements IBugTrackerConfigDefinitionProvider {
    	URL("ALM Octane URL", "Server at which ALM Octane REST API is accessible. Example: http://w2k3r2sp2:8080", true),
    	SHARED_SPACE_ID("Shared Space ID", "ID of shared space. Either numeric or a UUID.", true),
        WORKSPACE_ID("Workspace ID", "ID of workspace. Numeric.", true)
        ;

        private final BugTrackerConfigDefinition bugTrackerConfigDefinition;
        OctaneConfigField(final String displayLabel, final String description, boolean required) {
        	this.bugTrackerConfigDefinition = new BugTrackerConfigDefinition(name(), ()->new BugTrackerConfig()
        			.setDisplayLabel(displayLabel)
        			.setDescription(description)
        			.setRequired(required));
        }
        @Override
        public BugTrackerConfigDefinition definition() {
        	return bugTrackerConfigDefinition;
        }
        
    }
	
    /**
	 * Add the {@link BugTrackerConfig} instances for the various ALM Octane
	 * configuration fields to the given {@link List}. 
	 * 
	 * @param list
	 */
    public static final void addBugTrackerConfigs(List<BugTrackerConfig> list) {
		IBugTrackerConfigDefinitionProvider.addBugTrackerConfigs(list, OctaneConfigField.values());
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
				OctaneConfigField.URL.definition().getNormalizedURLValue(bugTrackerConfig),
				OctaneConfigField.SHARED_SPACE_ID.definition().getNormalizedValue(bugTrackerConfig),
				OctaneConfigField.WORKSPACE_ID.definition().getNormalizedValue(bugTrackerConfig));
	}
}
