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
package com.fortify.pub.bugtracker.plugin.bugparam;

import java.util.ArrayList;
import java.util.List;

import com.fortify.pub.bugtracker.support.BugParam;

/**
 * This interface provides access to a {@link BugParamDefinition} instance
 * through the {@link #definition()} method.
 * 
 * @author Ruud Senden
 *
 * @param <OnChangeHandler>
 */
public interface IBugParamDefinitionProvider<OnChangeHandler> {
	public BugParamDefinition<OnChangeHandler> definition();
	
	/**
	 * Static method for adding a {@link BugParam} instance as produced
	 * by each given {@link IBugParamDefinitionProvider} to the given 
	 * {@link BugParam} {@link List}.
	 * 
	 * @param list
	 * @param providers
	 */
	public static void addBugParams(List<BugParam> list, IBugParamDefinitionProvider<?>[] providers) {
		for ( IBugParamDefinitionProvider<?> provider : providers ) { 
			list.add(provider.definition().createBugParam()); 
		}
	}
	
	/**
	 * Static method for retrieving a {@link List} of {@link BugParam} instances 
	 * as produced by each given {@link IBugParamDefinitionProvider}.
	 * 
	 * @param list
	 * @param providers
	 */
	public static List<BugParam> getBugParams(IBugParamDefinitionProvider<?>[] providers) {
		List<BugParam> result = new ArrayList<>(providers.length);
		addBugParams(result, providers);
		return result;
	}
}
