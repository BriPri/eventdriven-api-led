package samples.components;

import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;

public class SampleJavaComponent implements Callable {

	@Override
	public Object onCall(MuleEventContext eventContext) throws Exception {
		MuleMessage message = eventContext.getMessage();
		Object origMessagePayload = message.getOriginalPayload();
		
		String firstName = message.getProperty("FirstName", PropertyScope.INVOCATION);
		String lastName = message.getProperty("LastName", PropertyScope.INVOCATION);
		
		if (firstName != null && lastName != null) {
			System.out.println("FirstName from flowVar: " + firstName);
			System.out.println("LastName from flowVar: " + lastName);
			
			/*
			 * Compute the response by calling a private method
			 */
			String response = computeResponse(firstName, lastName);
			System.out.println("in SampleJavaComponent: response from computeResponse = " + response);
			
			/*
			 * Hold or store the value in a Mule flowVar so it will be tested 
			 * in the Unit test by pulling out the value from the Mule context.
			 */
			message.setProperty("ComputedName", response, PropertyScope.INVOCATION);
			System.out.println("Just set ComputedName flow var to return it to the caller");			
		} else {
			System.out.println("firstName and lastName are both null so skipping method call");
		}
		
		
		/*
		 * I am returning the original payload so the existing flow will
		 * work as expected.
		 *  
		 */
		return origMessagePayload;
		
	}
	
	
	// Perform the business logic in a testable method.
	protected String computeResponse(String firstName, String lastName) {
		System.out.println("About to compute firstname and lastname.  Inbound source for firstname = " + firstName + ", inbound lastname = " + lastName);
		return firstName + " " + lastName;
	}
	
	
	

}
