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
package com.fortify.pub.bugtracker.plugin.fields;

import java.util.ArrayList;
import java.util.List;

import com.fortify.pub.bugtracker.support.BugTrackerConfig;

/**
 * This interface provides access to a {@link BugTrackerConfigDefinition} instance
 * through the {@link #definition()} method.
 * 
 * @author Ruud Senden
 */
public interface IBugTrackerConfigDefinitionProvider {
	public BugTrackerConfigDefinition definition();
	
	/**
	 * Static method for adding a {@link BugTrackerConfig} instance as produced
	 * by each given {@link IBugTrackerConfigDefinitionProvider} to the given 
	 * {@link BugTrackerConfig} {@link List}.
	 * 
	 * @param list
	 * @param providers
	 */
	public static void addBugTrackerConfigs(List<BugTrackerConfig> list, IBugTrackerConfigDefinitionProvider[] providers) {
		for ( IBugTrackerConfigDefinitionProvider provider : providers ) { 
			list.add(provider.definition().createBugTrackerConfig()); 
		}
	}
	
	/**
	 * Static method for retrieving a {@link List} of {@link BugTrackerConfig} instances 
	 * as produced by each given {@link IBugTrackerConfigDefinitionProvider}.
	 * 
	 * @param list
	 * @param providers
	 */
	public static List<BugTrackerConfig> getBugTrackerConfigs(IBugTrackerConfigDefinitionProvider[] providers) {
		List<BugTrackerConfig> result = new ArrayList<>(providers.length);
		addBugTrackerConfigs(result, providers);
		return result;
	}
}
