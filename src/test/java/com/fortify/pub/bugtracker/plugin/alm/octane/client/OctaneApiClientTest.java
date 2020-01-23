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

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.UUID;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonValue;

import org.junit.jupiter.api.Test;

class OctaneApiClientTest {
	@Test
	void testValidateConnection() throws Exception {
		OctaneApiClient client = TestHelper.getOctaneApiClient();
		if ( client!=null ) {
			client.validateConnection();
		}
	}
	
	@Test
	void testGetWorkItemRootNames() throws Exception {
		OctaneApiClient client = TestHelper.getOctaneApiClient();
		if ( client!=null ) {
			List<String> result = client.getWorkItemRootNames();
			assertTrue(result.contains("Backlog"));
		}
	}
	
	@Test
	void testGetEpicNames() throws Exception {
		OctaneApiClient client = TestHelper.getOctaneApiClient();
		if ( client!=null ) {
			List<String> result = client.getEpicNames("Backlog");
			System.out.println(result);
		}
	}
	
	@Test
	void testGetFeatureNames() throws Exception {
		OctaneApiClient client = TestHelper.getOctaneApiClient();
		if ( client!=null ) {
			List<String> result = client.getFeatureNames("Backlog", "Billing");
			System.out.println(result);
		}
	}
	
	@Test
	void testGetIdForBacklog() throws Exception {
		OctaneApiClient client = TestHelper.getOctaneApiClient();
		if ( client!=null ) {
			String result = client.getWorkItemRootId("Backlog");
			System.out.println(result);
		}
	}
	
	@Test
	void testGetIdForBillingEpic() throws Exception {
		OctaneApiClient client = TestHelper.getOctaneApiClient();
		if ( client!=null ) {
			String result = client.getEpicId("Backlog", "Billing");
			System.out.println(result);
		}
	}
	
	@Test
	void testGetIdForInvoicesFeature() throws Exception {
		OctaneApiClient client = TestHelper.getOctaneApiClient();
		if ( client!=null ) {
			String result = client.getFeatureId("Backlog", "Billing", "Invoices");
			System.out.println(result);
		}
	}
	
	@Test
	void testFileBug() throws Exception {
		OctaneApiClient client = TestHelper.getOctaneApiClient();
		if ( client!=null ) {
			String result = client.fileBug(getBugContents());
			System.out.println(result);
		}
	}

	private JsonObject getBugContents() {
		return Json.createObjectBuilder()
				.add("name", "JUnitTest-"+UUID.randomUUID().toString())
				.add("description", "TestDescription")
				.add("parent", getReference("work_item", "2017"))
				.add("phase", getReference("phase", "phase.defect.new"))
				.build();
	}

	private JsonValue getReference(String type, String id) {
		return Json.createObjectBuilder()
			.add("type", type)
			.add("id", id)
			.build();
	}

	

}
