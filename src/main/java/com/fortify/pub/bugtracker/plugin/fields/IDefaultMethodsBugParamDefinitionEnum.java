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

/**
 * This interface extends {@link IDefaultMethodsBugParamDefinition} to allow access to
 * all of the default methods provided by that interface. In addition, this interface
 * provides a default implementation for the {@link #getIdentifier()} method based on
 * the {@link #name()} method provided by enumerations.
 * 
 * @author Ruud Senden
 *
 * @param <OnChangeHandler>
 */
public interface IDefaultMethodsBugParamDefinitionEnum<OnChangeHandler> extends IDefaultMethodsBugParamDefinition<OnChangeHandler> {
	/**
	 * Implementation for this method is (usually) automatically provided by enumeration entries.
	 * @return
	 */
	public String name();
	
	/**
	 * Default implementation for the {@link #IIdentifier.getIdentifier()} method, based on the
	 * value returned by the {@link #name()} method.
	 */
	@Override
	default String getIdentifier() {
		return String.format("enum.%s", name());
	}

}
