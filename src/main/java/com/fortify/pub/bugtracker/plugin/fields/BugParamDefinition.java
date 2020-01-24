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

import org.apache.commons.lang3.Validate;
import org.glassfish.jersey.internal.util.Producer;

import com.fortify.pub.bugtracker.support.BugParam;
import com.fortify.pub.bugtracker.support.BugParamChoice;

/**
 * This class describes a {@link BugParam} definition, consisting of a
 * {@link Producer} for producing {@link BugParam} instances, and an optional
 * onChange handler for these {@link BugParam} instances.
 * 
 * @author Ruud Senden
 *
 * @param <OnChangeHandler>
 */
public class BugParamDefinition<OnChangeHandler> extends ValueAccessor {
	private final Producer<? extends BugParam> bugParamProducer;
	private final OnChangeHandler onChangeHandler;

	public BugParamDefinition(String identifier, Producer<? extends BugParam> bugParamProducer, OnChangeHandler onChangeHandler) {
		super(identifier);
    	this.bugParamProducer = bugParamProducer;
    	this.onChangeHandler = onChangeHandler;
    }

	public BugParamDefinition(String identifier, Producer<? extends BugParam> bugParamProducer) {
		super(identifier);
    	this.bugParamProducer = bugParamProducer;
    	this.onChangeHandler = null;
    }

	/**
	 * Get the configured {@link BugParam} {@link Producer}
	 * @return
	 */
	private final Producer<? extends BugParam> getBugParamProducer() {
		return bugParamProducer;
	}

	/**
	 * Get the configured onChange handler
	 * @return
	 */
	public final OnChangeHandler getOnChangeHandler() {
		return onChangeHandler;
	}

	/**
	 * This method returns a {@link BugParam} instance as produced by the
	 * configured {@link Producer}. The produced {@link BugParam}
	 * instance will be updated with the identifier returned by
	 * {@link #getIdentifier()}. If the {@link BugParamDefinition} provides an
	 * onChange handler, the {@link BugParamChoice#setHasDependentParams(boolean)}
	 * method will be called to indicate that this {@link BugParamChoice} instance
	 * has dependent parameters.
	 * 
	 * @return
	 */
	public final BugParam createBugParam() {
		BugParam result = getBugParamProducer().call();
		result.setIdentifier(getIdentifier());
		if (getOnChangeHandler() != null) {
			Validate.isInstanceOf(BugParamChoice.class, result,
					"OnChange handler is not supported for " + result.getClass().getSimpleName());
			((BugParamChoice) result).setHasDependentParams(true);
		}
		return result;
	}

	/**
	 * Get the {@link BugParam} instance from the given {@link List} of
	 * {@link BugParam} instances that corresponds to the identifier returned by
	 * {@link #getIdentifier()}.
	 * 
	 * @param currentValues
	 * @return
	 */
	public BugParam getCurrentBugParam(List<BugParam> currentValues) {
		return currentValues.stream().filter(this::hasSameId).findFirst().get();
	}

	/**
	 * This method returns true if the identifier for the given {@link BugParam}
	 * instance matches the identifier returned by {@link #getIdentifier()}.
	 * 
	 * @param bugParam
	 * @return
	 */
	private final boolean hasSameId(BugParam bugParam) {
		return getIdentifier().equals(bugParam.getIdentifier());
	}
}
