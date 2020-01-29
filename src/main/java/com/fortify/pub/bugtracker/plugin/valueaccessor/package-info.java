/*******************************************************************************
 * (c) Copyright 2017 EntIT Software LLC, a Micro Focus company
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
/**
 * This package contains two generic base classes: 
 * <ul>
 *  <li>{@link com.fortify.pub.bugtracker.plugin.valueaccessor.Identifiable}
 *      is a very generic base class that provides access to some identifier
 *      that was passed to the constructor.</li>
 *  <li>{@link com.fortify.pub.bugtracker.plugin.valueaccessor.ValueAccessor}
 *      extends {@link com.fortify.pub.bugtracker.plugin.valueaccessor.Identifiable};
 *      this class provides various getter methods for retrieving values from
 *      identifier-to-value maps, based on the configured identifier.</li>
 * <ul> 
 * 
 * These base classes are used by {@link com.fortify.pub.bugtracker.plugin.bugparam.BugParamDefinition}
 * and {@link com.fortify.pub.bugtracker.plugin.config.BugTrackerConfigDefinition} to provide
 * access to bug parameter and bug tracker configuration values. 
 * 
 * @author Ruud Senden
 *
 */
package com.fortify.pub.bugtracker.plugin.valueaccessor;