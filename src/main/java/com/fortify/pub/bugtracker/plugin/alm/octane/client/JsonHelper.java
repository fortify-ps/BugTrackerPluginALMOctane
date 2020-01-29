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
package com.fortify.pub.bugtracker.plugin.alm.octane.client;

import javax.json.Json;
import javax.json.JsonObject;

public class JsonHelper {

	public JsonHelper() {
		// TODO Auto-generated constructor stub
	}

	public static final JsonObject getReferenceObject(String type, String id) {
		return Json.createObjectBuilder()
			.add("type", type)
			.add("id", id)
			.build();
	}
	
	public static final JsonObject getReferenceObjectForDefectId(String defectId) {
		return getReferenceObject("defect", defectId);
	}

	public static final JsonObject getReferenceObjectForPhaseId(String phaseId) {
		return getReferenceObject("phase", phaseId);
	}
	
	public static final JsonObject getReferenceObjectForWorkItemRootId(String phaseId) {
		return getReferenceObject("work_item_root", phaseId);
	}
	
	public static final JsonObject getReferenceObjectForEpicId(String phaseId) {
		return getReferenceObject("epic", phaseId);
	}
	
	public static final JsonObject getReferenceObjectForFeatureId(String phaseId) {
		return getReferenceObject("feature", phaseId);
	}
	
	public static final JsonObject wrapAsDataArray(JsonObject json) {
		return Json.createObjectBuilder()
				.add("data", Json.createArrayBuilder().add(json).build())
				.build();
	}
}
