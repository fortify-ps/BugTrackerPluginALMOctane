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
package com.fortify.pub.bugtracker.plugin.valueaccessor;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

/**
 * Based on the identifier returned by the {@link #getIdentifier()} method,
 * this class provides various methods for retrieving a corresponding
 * value for that identifier from a given values {@link Map}.
 * 
 * @author Ruud Senden
 *
 */
public class ValueAccessor extends Identifiable {
	
	/**
	 * Constructor to configure the identifier
	 * @param identifier
	 */
	public ValueAccessor(String identifier) {
		super(identifier);
	}

	/**
	 * Get the value for the identifier returned by {@link #getIdentifier()}
	 * from the given {@link Map}
	 * @param values
	 * @return
	 */
	public final String getValue(Map<String, String> values) {
		return values.get(getIdentifier());
	}
	
	/**
	 * Get the normalized value for the identifier returned by {@link #getIdentifier()}
	 * from the given {@link Map}. If the value is null or blank, this method will return
	 * null. Otherwise, the value will be trimmed before being returned.
	 * @param values
	 * @return
	 */
	public final String getNormalizedValue(Map<String, String> values) {
		String value = getValue(values);
        return StringUtils.isBlank(value) ? null : value.trim();
	}
	
	/**
	 * Same as {@link #getNormalizedValue(Map)}, but with the return value
	 * abbreviated to the given maximum width if necessary.
	 * @param values
	 * @param maxWidth
	 * @return
	 */
	public final String getNormalizedValue(Map<String, String> values, int maxWidth) {
		return StringUtils.abbreviate(getNormalizedValue(values), maxWidth);
	}
	
	/**
	 * Convert the normalized value (see {@link #getNormalizedValue(Map)} to
	 * an {@link Integer}.
	 * @param values
	 * @return
	 */
	public final Integer getIntValue(Map<String, String> values) {
		String value = getNormalizedValue(values);
		return value==null ? null : new Integer(value);
	}
	
	/**
	 * Convert the normalized value (see {@link #getNormalizedValue(Map)} to
	 * a {@link URL}.
	 * @param values
	 * @return
	 */
	public final URL getNormalizedURLValue(Map<String, String> values) {
		String value = getNormalizedValue(values);
		if ( value == null ) {
			return null;
		} else {
			if (value.endsWith("/")) {
	            value = value.substring(0,value.length()-1);
	        }
			try {
				return new URL(value);
			} catch (MalformedURLException e) {
				throw new RuntimeException("Error parsing URL "+value, e);
			}
		}
	}
}
