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

import org.glassfish.jersey.internal.util.Producer;

import com.fortify.pub.bugtracker.support.BugTrackerConfig;

/**
 * This class describes a {@link BugTrackerConfig} definition, consisting of a
 * {@link Producer} for producing {@link BugTrackerConfig} instances.
 * 
 * @author Ruud Senden
 */
public class BugTrackerConfigDefinition extends ValueAccessor {
	private final Producer<BugTrackerConfig> bugTrackerConfigProducer;
	
	public BugTrackerConfigDefinition(String identifier, Producer<BugTrackerConfig> bugTrackerConfigProducer) {
		super(identifier);
		this.bugTrackerConfigProducer = bugTrackerConfigProducer;
	}
	
	private final Producer<BugTrackerConfig> getBugTrackerConfigProducer() {
		return bugTrackerConfigProducer;
	}
	
	/**
	 * This method returns a {@link BugTrackerConfig} instance as produced by the
	 * configured {@link Producer}. The produced {@link BugTrackerConfig}
	 * instance will be updated with the identifier returned by
	 * {@link #getIdentifier()}.
	 * 
	 * @return
	 */
	public final BugTrackerConfig createBugTrackerConfig() {
		return getBugTrackerConfigProducer().call().setIdentifier(getIdentifier());
	}
}