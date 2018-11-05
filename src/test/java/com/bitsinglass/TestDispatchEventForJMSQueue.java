package com.bitsinglass;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.codehaus.jettison.json.JSONObject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mule.DefaultMuleEventContext;
import org.mule.api.MuleContext;
import org.mule.api.MuleEvent;
import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.client.MuleClient;
import org.mule.api.processor.MessageProcessor;
import org.mule.api.transformer.DataType;
import org.mule.api.transport.PropertyScope;
import org.mule.tck.MuleTestUtils;
import org.mule.tck.junit4.FunctionalTestCase;

import com.mulesoft.mmc.agent.v3.dto.MessageExchangePattern;

import samples.components.SampleJavaComponent;

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
	
	
	@Test
	public void testJavaComponent() throws Exception {
		
		/*
		 * The goal of this test is to demonstrate how to test a Java component that relies on some
		 * data from Mule, in this case, two flow vars.  This is tested by creating the mule event contexttest a Java component by invoking the onCall() method 
		 * with the Mule event with two flow vars set.
		 */
		
		String inputPayload = "NoPayload";
		SampleJavaComponent comp = new SampleJavaComponent();

		MuleEvent event = getTestEvent(inputPayload, muleContext);
		
		MuleMessage msg = getTestMuleMessage();
		msg.setProperty("FirstName", "Santa", PropertyScope.INVOCATION);
		msg.setProperty("LastName", "Clause", PropertyScope.INVOCATION);
		
		event.setMessage(msg);
		MuleEventContext eventContext = new DefaultMuleEventContext(event);

		System.out.println("about to test flow var in unit test");
		String fn = eventContext.getMessage().getProperty("FirstName", PropertyScope.INVOCATION);
		assertTrue("ensure firstname could be set on the Mule Event Context", fn != null);
		
		System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> About to call Java component's onCall() <<<<<<<<<<<<<<<<<<<<<<<<<<<<");
		comp.onCall(eventContext);
		
		MuleMessage message = eventContext.getMessage();
		String computedResponse = message.getProperty("ComputedName", PropertyScope.INVOCATION);
		System.out.println("");
		
		assertEquals("Response from Flow call", "Santa Clause", computedResponse);
	}
	
	
	@Test
	public void testJavaComponent_directCall() throws Exception {
		assertTrue(true);
	}

}
