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
package com.fortify.pub.bugtracker.plugin.config;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.fortify.pub.bugtracker.plugin.BugTrackerPlugin;
import com.fortify.pub.bugtracker.support.BugTrackerConfig;

/**
 * <p>This interface is usually implemented by {@link Enum} instances that define
 * {@link BugTrackerConfig} fields; for each enum entry the the 
 * {@link #getBugTrackerConfig()} method should return a corresponding 
 * {@link BugTrackerConfig} instance.</p>
 * 
 * <p>The static {@link #addFields(List, IBugTrackerConfigField[])} method can be
 * used to easily add all the {@link BugTrackerConfig} instance for all enum entries
 * to the given {@link List}, using an invocation like 
 * <code>IBugTrackerConfigField.addFields(bugTrackerConfigList, MyEnum.values())</code>.
 * This allows for easy generation of a {@link List} of {@link BugTrackerConfig}
 * instances that can be returned by {@link BugTrackerPlugin#getConfiguration()}.</p>
 * 
 * <p>The {@link #getValue(Map)} can be used to retrieve the value for an enum entry
 * from the current configuration. Various alternative getter methods are available
 * to convert the configuration {@link String} value to commonly used other types.<p> 
 *  
 * @author Ruud Senden
 *
 */
public interface IBugTrackerConfigField {
	public BugTrackerConfig getBugTrackerConfig();

	public default String getValue(Map<String, String> bugTrackerConfig) {
		String result = bugTrackerConfig.get(getBugTrackerConfig().getIdentifier());
        return StringUtils.isBlank(result) ? null : result.trim();
	}
	
	public default Integer getIntValue(Map<String, String> bugTrackerConfig) {
		String value = getValue(bugTrackerConfig);
		return value==null ? null : new Integer(value);
	}
	
	public default URL getURLValue(Map<String, String> bugTrackerConfig) {
		String value = getValue(bugTrackerConfig);
		try {
			return value==null ? null : new URL(value);
		} catch (MalformedURLException e) {
			throw new RuntimeException("Error parsing URL "+value, e);
		}
	}
	
	public static void addFields(List<BugTrackerConfig> list, IBugTrackerConfigField[] fields) {
		for ( IBugTrackerConfigField field : fields ) { list.add(field.getBugTrackerConfig()); }
	}
}