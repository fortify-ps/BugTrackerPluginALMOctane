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
package com.fortify.pub.bugtracker.plugin.alm.octane.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.UUID;

import javax.json.Json;
import javax.json.JsonObject;

import org.junit.jupiter.api.Test;

import com.fortify.pub.bugtracker.plugin.alm.octane.client.api.OctaneApiClient;
import com.fortify.pub.bugtracker.plugin.alm.octane.client.api.OctaneEntity;

class OctaneApiClientTest {
	@Test
	void testValidateConnection() throws Exception {
		try (OctaneApiClient client = TestHelper.getOctaneApiClient()) {
			if ( client!=null ) {
				client.validateConnection();
			}
		}
	}
	
	@Test
	void testGetWorkItemRootNames() throws Exception {
		try (OctaneApiClient client = TestHelper.getOctaneApiClient()) {
			if ( client!=null ) {
				List<String> result = client.getWorkItemRootNames();
				assertTrue(result.contains("Backlog"));
			}
		}
	}
	
	@Test
	void testGetEpicNames() throws Exception {
		try (OctaneApiClient client = TestHelper.getOctaneApiClient()) {
			if ( client!=null ) {
				List<String> result = client.getEpicNames("Backlog");
				System.out.println(result);
			}
		}
	}
	
	@Test
	void testGetFeatureNames() throws Exception {
		try (OctaneApiClient client = TestHelper.getOctaneApiClient()) {
			if ( client!=null ) {
				List<String> result = client.getFeatureNames("Backlog", "Billing");
				System.out.println(result);
			}
		}
	}
	
	@Test
	void testGetIdForBacklog() throws Exception {
		try (OctaneApiClient client = TestHelper.getOctaneApiClient()) {
			if ( client!=null ) {
				String result = client.getIdForWorkItemRootName("Backlog");
				System.out.println(result);
			}
		}
	}
	
	@Test
	void testGetIdForBillingEpic() throws Exception {
		try (OctaneApiClient client = TestHelper.getOctaneApiClient()) {
			if ( client!=null ) {
				String result = client.getIdForEpicName("Backlog", "Billing");
				System.out.println(result);
			}
		}
	}
	
	@Test
	void testGetIdForInvoicesFeature() throws Exception {
		try (OctaneApiClient client = TestHelper.getOctaneApiClient()) {
			if ( client!=null ) {
				String result = client.getIdForFeatureName("Backlog", "Billing", "Invoices");
				System.out.println(result);
			}
		}
	}
	
	@Test
	void testFileBug() throws Exception {
		try (OctaneApiClient client = TestHelper.getOctaneApiClient()) {
			if ( client!=null ) {
				String result = client.fileBug(getBugContents("testFileBug"));
				System.out.println(result);
			}
		}
	}
	
	@Test
	void testAddCommentToBug() throws Exception {
		try (OctaneApiClient client = TestHelper.getOctaneApiClient()) {
			if ( client!=null ) {
				String defectId = client.fileBug(getBugContents("testAddComment"));
				client.addCommentToBug(defectId, "Test Comment");
				System.out.println("testAddCommentBug defect id: "+defectId);
			}
		}
	}
	
	@Test
	void testTransitionToPhaseId() throws Exception {
		try (OctaneApiClient client = TestHelper.getOctaneApiClient()) {
			if ( client!=null ) {
				String defectId = client.fileBug(getBugContents("testTransitionToPhaseId"));
				client.transitionToPhaseId(defectId, "phase.defect.opened");
				System.out.println("testTransitionToPhaseId defect id: "+defectId);
				assertEquals("phase.defect.opened", client.getPhaseIdForDefectId(defectId), "");
			}
		}
	}

	private JsonObject getBugContents(String info) {
		return Json.createObjectBuilder()
				.add("name", info+"-"+UUID.randomUUID().toString())
				.add("description", "TestDescription")
				.add("parent", OctaneEntity.WORK_ITEM.getReferenceObjectForId("2017"))
				.add("phase", OctaneEntity.PHASE.getReferenceObjectForId("phase.defect.new"))
				.build();
	}
}
