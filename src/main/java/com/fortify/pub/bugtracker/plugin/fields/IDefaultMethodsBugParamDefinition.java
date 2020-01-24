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
 * Based on a {@link BugParamDefinition} as returned by the {@link #getBugParamDefinition()}
 * method, this interface provides various default methods. As this interface extends 
 * {@link IDefaultMethodsGetIdentifierValue}, all default methods provided by that interface 
 * are available as well.
 * 
 * @author Ruud Senden
 *
 * @param <OnChangeHandler>
 */
public interface IDefaultMethodsBugParamDefinition<OnChangeHandler> extends IDefaultMethodsGetIdentifierValue {
	/**
	 * This class defines a {@link BugParam} definition, consisting of a {@link Producer} for producing 
	 * {@link BugParam} instances, and an optional onChange handler for these {@link BugParam} instances.
	 * @author Ruud Senden
	 *
	 * @param <OnChangeHandler>
	 */
	static final class BugParamDefinition<OnChangeHandler> {
		private final Producer<? extends BugParam> bugParamProducer;
        private final OnChangeHandler onChangeHandler;
        public BugParamDefinition(Producer<? extends BugParam> bugParamProducer, OnChangeHandler onChangeHandler) {
        	this.bugParamProducer = bugParamProducer;
        	this.onChangeHandler = onChangeHandler;
        }
        public BugParamDefinition(Producer<? extends BugParam> bugParamProducer) {
        	this.bugParamProducer = bugParamProducer;
        	this.onChangeHandler = null;
        }
		public Producer<? extends BugParam> getBugParamProducer() {
			return bugParamProducer;
		}
		public OnChangeHandler getOnChangeHandler() {
			return onChangeHandler;
		}
	}
	
	/**
	 * Implementations of this interface need to implement this method to return 
	 * a {@link BugParamDefinition} instance.
	 * @return
	 */
	BugParamDefinition<OnChangeHandler> getBugParamDefinition();
	
	/**
	 * This method returns the onChange handler as provided by the {@link BugParamDefinition} 
	 * returned by the {@link #getBugParamDefinition()} method.
	 * @return
	 */
	public default OnChangeHandler getOnChangeHandler() {
		return getBugParamDefinition().getOnChangeHandler();
	}
	
	/**
	 * This method returns a {@link BugParam} instance as produced by the {@link Producer}
	 * provided by the {@link BugParamDefinition} returned by the {@link #getBugParamDefinition()}
	 * method. The produced {@link BugParam} instance will be updated with the identifier
	 * returned by {@link #getIdentifier()}. If the {@link BugParamDefinition} provides
	 * an onChange handler, the {@link BugParamChoice#setHasDependentParams(boolean)} method
	 * will be called to indicate that this {@link BugParamChoice} instance has dependent
	 * parameters.
	 
	 * @return
	 */
	public default BugParam createBugParam() {
    	BugParamDefinition<OnChangeHandler> bugParamDefinition = getBugParamDefinition();
		BugParam result = bugParamDefinition.getBugParamProducer().call();
    	result.setIdentifier(getIdentifier());
		if ( getOnChangeHandler()!=null ) {
			Validate.isInstanceOf(BugParamChoice.class, result, "OnChange handler is not supported for "+result.getClass().getSimpleName());
			((BugParamChoice)result).setHasDependentParams(true);
		}
		return result;
    }
	
	/**
	 * Get the {@link BugParam} instance from the given {@link List} of {@link BugParam}
	 * instances that corresponds to the identifier returned by {@link #getIdentifier()}.
	 * 
	 * @param currentValues
	 * @return
	 */
    public default BugParam getCurrentBugParam(List<BugParam> currentValues) {
    	return currentValues.stream().filter(this::hasSameId).findFirst().get();
    }
    
    /**
     * This method returns true if the identifier for the given {@link BugParam} instance 
     * matches the identifier returned by {@link #getIdentifier()}.
     * @param bugParam
     * @return
     */
    public default boolean hasSameId(BugParam bugParam) {
    	return getIdentifier().equals(bugParam.getIdentifier());
    }
}
