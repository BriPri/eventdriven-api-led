package com.bitsinglass;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.codehaus.jettison.json.JSONObject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mule.api.MuleEvent;
import org.mule.api.MuleMessage;
import org.mule.api.client.MuleClient;
import org.mule.api.processor.MessageProcessor;
import org.mule.tck.junit4.FunctionalTestCase;

public class TestDispatchEventForJMSQueue extends FunctionalTestCase {


	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}
	
	protected String getConfigResources() {
		System.out.println("just called getConfigResources()");
        return "main.xml";
    }
	
	
	@Test
	public void testRunVmFlow() throws Exception {
		
		/*
		 * The point or goal of this test is to show how a Mule flow can be activated by sending a message to a Queue.
		 * This example code uses the built in VM queue to reduce any external dependency.  
		 * Generally, queues are configured to be asynchronous and therefore will have receive or generate a response. 
		 * So the test below is only testing that it can successfully activate a flow based on a Queue. 
		 * 
		 */
		
		String inputPayload = "{ \"name\" : \"Santa Clause\", \"address\" : \"North Pole\" }";
		
		MuleEvent event = getTestEvent(inputPayload, muleContext);
		MuleClient client = muleContext.getClient();
		
		MuleMessage muleMessage = event.getMessage();

        MuleMessage reply = client.send("vm://test-queue-in", muleMessage);
        assertTrue("testMessageTransformerWithEmptyStringPayload", reply == null);
		
	}
		
	@Test
	public void testActualFlowCall() throws Exception {
		
		/*
		 * The point of this test is to run or call a Flow (not through the VM source) but directly and 
		 * be able to test the results returned.
		 * 
		 */
		
		String inputPayload = "{ \"name\" : \"Santa Clause\", \"address\" : \"North Pole\" }";
		
		MuleEvent event = getTestEvent(inputPayload, muleContext);
		
		MessageProcessor subFlow = muleContext.getRegistry().lookupObject("receiving-queue_Flow");
		MuleEvent result = subFlow.process(event);
		
		String jsonResponse = result.getMessage().getPayloadAsString();
		
		JSONObject jsonObject = new JSONObject(jsonResponse);
		String responseValue = (String) jsonObject.get("newMessage");
		
		System.out.println("payload response = " + jsonResponse);
		System.out.println("Parsed value = " + responseValue);
		
		assertEquals("Response from Flow call", responseValue, "Santa ClauseNorth Pole");
		
	}
	


}
