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

import java.util.List;

import org.glassfish.jersey.internal.util.Producer;

import com.fortify.pub.bugtracker.support.BugTrackerConfig;

/**
 * Based on a {@link Producer} as returned by the {@link #getBugTrackerConfigProducer()}
 * method, this interface provides various default methods. As this interface extends 
 * {@link IDefaultMethodsGetIdentifierValue}, all default methods provided by that interface 
 * are available as well.
 * 
 * @author Ruud Senden
 *
 * @param <OnChangeHandler>
 */
public interface IDefaultMethodsBugTrackerConfigDefinition extends IDefaultMethodsGetIdentifierValue {
	/**
	 * Implementations of this interface need to implement this method to return 
	 * a {@link Producer} instance that can produce {@link BugTrackerConfig} instances.
	 * @return
	 */
	public Producer<BugTrackerConfig> getBugTrackerConfigProducer();
	
	/**
	 * Create a new {@link BugTrackerConfig} instance based on the {@link Producer}
	 * returned by the {@link #getBugTrackerConfigProducer()} method.
	 * @return
	 */
	public default BugTrackerConfig createBugTrackerConfig() {
		return getBugTrackerConfigProducer().call().setIdentifier(getIdentifier());
	}
	
	/**
	 * Static method for adding a {@link BugTrackerConfig} instance as produced
	 * by each provided {@link IDefaultMethodsBugTrackerConfigDefinition} to the
	 * given {@link BugTrackerConfig} {@link List}.
	 * 
	 * TODO Does it make sense to have this method here, or should it be moved to 
	 * {@link IDefaultMethodsBugTrackerConfigDefinitionEnum}?
	 * 
	 * @param list
	 * @param configDefinitions
	 */
	public static void addFields(List<BugTrackerConfig> list, IDefaultMethodsBugTrackerConfigDefinition[] configDefinitions) {
		for ( IDefaultMethodsBugTrackerConfigDefinition configDefinition : configDefinitions ) { 
			list.add(configDefinition.createBugTrackerConfig()); 
		}
	}
}