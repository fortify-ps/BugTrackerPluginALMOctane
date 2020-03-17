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
package com.fortify.pub.bugtracker.plugin.alm.octane.client.api;

import javax.json.Json;
import javax.json.JsonObject;

/**
 * This enumeration lists various ALM Octane entities as used by
 * this SSC bug tracker plugin (and as such is not meant to list 
 * every existing Octane entity).
 * 
 * @author Ruud Senden
 *
 */
public enum OctaneEntity {
	WORK_ITEM_ROOT, EPIC, FEATURE, DEFECT, PHASE, WORK_ITEM, COMMENT;
	
	/**
	 * Get the singular entity name for an enumeration entry. This just
	 * returns the enumeration name in lower case.
	 * @return
	 */
	public final String singular() {
		return name().toLowerCase();
	}
	
	/**
	 * Get the plural entity name for an enumeration entry. This just
	 * returns the singular name with an 's' appended at the end.
	 * @return
	 */
	public final String plural() {
		return singular()+"s";
	}
	
	/**
	 * This method returns a JsonObject that represents a reference to
	 * the entity identified by the given id of the entity type 
	 * represented by the current enumeration entry.
	 * 
	 * @param id
	 * @return
	 */
	public final JsonObject getReferenceObjectForId(String id) {
		return Json.createObjectBuilder()
			.add("type", singular())
			.add("id", id)
			.build();
	}
}
