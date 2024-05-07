package se.uu.ub.cora.login;

import static org.testng.Assert.assertEquals;

import org.testng.Assert;
import org.testng.annotations.Test;

public class LoginExceptionTest {

	@Test
	public void testInit() {
		LoginException conflict = LoginException.withMessage("message");

		Assert.assertEquals(conflict.getMessage(), "message");
	}

	@Test
	public void testInitWithException() {
		Exception exception = new Exception();
		LoginException conflict = LoginException.withMessageAndException("message", exception);

		assertEquals(conflict.getMessage(), "message");
		assertEquals(conflict.getCause(), exception);
	}
}
