/*
 * Copyright 2017, 2018, 2021, 2022, 2024 Uppsala University Library
 *
 * This file is part of Cora.
 *
 *     Cora is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Cora is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Cora.  If not, see <http://www.gnu.org/licenses/>.
 */

package se.uu.ub.cora.login.rest;

import static org.testng.Assert.assertEquals;

import java.net.URISyntaxException;
import java.util.LinkedHashSet;
import java.util.Optional;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import se.uu.ub.cora.gatekeeper.user.User;
import se.uu.ub.cora.gatekeepertokenprovider.AuthToken;
import se.uu.ub.cora.initialize.SettingsProvider;
import se.uu.ub.cora.logger.LoggerProvider;
import se.uu.ub.cora.logger.spies.LoggerFactorySpy;
import se.uu.ub.cora.login.LoginFactoryImp;
import se.uu.ub.cora.login.initialize.GatekeeperInstanceProvider;
import se.uu.ub.cora.login.spies.AppTokenLoginSpy;
import se.uu.ub.cora.login.spies.GatekeeperTokenProviderErrorSpy;
import se.uu.ub.cora.login.spies.GatekeeperTokenProviderSpy;
import se.uu.ub.cora.login.spies.HttpServletRequestSpy;
import se.uu.ub.cora.login.spies.LoginFactorySpy;
import se.uu.ub.cora.login.spies.MapSpy;
import se.uu.ub.cora.login.spies.PasswordLoginSpy;
import se.uu.ub.cora.testutils.mcr.MethodCallRecorder;
import se.uu.ub.cora.testutils.mrv.MethodReturnValues;

public class LoginEndpointTest {
	private static final String SOME_LOGIN_ID = "someLoginId";
	private static final String SOME_PASSWORD = "somePassword";
	private static final String SOME_APP_TOKEN = "tokenStringFromSpy";
	private static final String SOME_USER_ID = "someUserId";
	private static final String TEXT_PLAIN_CHARSET_UTF_8 = "text/plain; charset=utf-8";
	private static final String APPLICATION_VND_UUB_RECORD_JSON = "application/vnd.uub.record+json";
	private LoginEndpoint loginEndpoint;
	private HttpServletRequestSpy request;
	private GatekeeperTokenProviderSpy gatekeeperTokenProvider;
	private MapSpy<String, String> settingsMapSpy;
	private User user;
	private LoginFactorySpy loginFactory;
	private PasswordLoginSpy passwordLoginSpy;
	private AppTokenLoginSpy appTokenLoginSpy;

	@BeforeMethod
	public void setup() {
		LoggerFactorySpy loggerFactory = new LoggerFactorySpy();
		LoggerProvider.setLoggerFactory(loggerFactory);

		settingsMapSpy = new MapSpy<>();
		settingsMapSpy.put("loginPublicPathToSystem", "/login/rest/");
		SettingsProvider.setSettings(settingsMapSpy);

		gatekeeperTokenProvider = new GatekeeperTokenProviderSpy();
		GatekeeperInstanceProvider.setGatekeeperTokenProvider(gatekeeperTokenProvider);

		setUpLoginDependencyProvider();

		request = new HttpServletRequestSpy();

		user = new User(SOME_USER_ID);
		configureUser(user, true, Optional.empty(), "someAppTokenId1", "someAppTokenId2");

		loginEndpoint = new LoginEndpoint(request);
	}

	private void setUpLoginDependencyProvider() {
		passwordLoginSpy = new PasswordLoginSpy();
		appTokenLoginSpy = new AppTokenLoginSpy();

		loginFactory = new LoginFactorySpy();
		loginFactory.MRV.setDefaultReturnValuesSupplier("factorPasswordLogin",
				() -> passwordLoginSpy);
		loginFactory.MRV.setDefaultReturnValuesSupplier("factorAppTokenLogin",
				() -> appTokenLoginSpy);

		LoginDependencyProvider.onlyForTestSetLoginFactory(loginFactory);
	}

	@AfterMethod
	private void afterMethod() {
		LoginDependencyProvider.onlyForTestSetLoginFactory(new LoginFactoryImp());
	}

	private User configureUser(User user, boolean active, Optional<String> passwordId,
			String... appTokens) {
		user.active = active;
		user.passwordId = passwordId;
		user.appTokenIds = new LinkedHashSet<>();
		for (String appToken : appTokens) {
			user.appTokenIds.add(appToken);
		}
		return user;
	}

	@Test
	public void testLoginEndpointPathAnnotation() throws Exception {
		AnnotationTestHelper annotationHelper = AnnotationTestHelper
				.createAnnotationTestHelperForClass(LoginEndpoint.class);
		annotationHelper.assertPathAnnotationForClass("/");
	}

	@Test
	public void testGetAuthTokenForAppToken_Annotations() throws Exception {
		AnnotationTestHelper annotationHelper = AnnotationTestHelper
				.createAnnotationTestHelperForClassMethodNameAndNumOfParameters(LoginEndpoint.class,
						"getAuthTokenForAppToken", 2);

		annotationHelper.assertHttpMethodAndPathAnnotation("POST", "apptoken/{loginId}");
		annotationHelper.assertConsumesAnnotation(TEXT_PLAIN_CHARSET_UTF_8);
		annotationHelper.assertProducesAnnotation(APPLICATION_VND_UUB_RECORD_JSON);
		annotationHelper.assertPathParamAnnotationByNameAndPosition("loginId", 0);
	}

	@Test
	public void testGetAuthTokenWithAppToken() throws Exception {
		loginEndpoint.getAuthTokenForAppToken(SOME_USER_ID, SOME_APP_TOKEN);

		loginFactory.MCR.assertMethodWasCalled("factorAppTokenLogin");
		appTokenLoginSpy.MCR.assertParameters("getAuthToken", 0, SOME_USER_ID, SOME_APP_TOKEN);

	}

	@Test
	public void testGetAuthTokenWithAppToken_BuildResponse() throws Exception {
		LoginEndpointOnlyForTest loginEndpoint = new LoginEndpointOnlyForTest(request);

		Response response = loginEndpoint.getAuthTokenForAppToken(SOME_LOGIN_ID, SOME_PASSWORD);

		var authToken = appTokenLoginSpy.MCR.getReturnValue("getAuthToken", 0);

		loginEndpoint.MCR.assertParameters("buildResponseUsingAuthToken", 0, authToken);
		loginEndpoint.MCR.assertReturn("buildResponseUsingAuthToken", 0, response);
	}

	@Test
	public void testGetAuthTokenWithAppToken_LoginException_ResponseWithUnauthorized()
			throws Exception {
		appTokenLoginSpy.MRV.setAlwaysThrowException("getAuthToken",
				LoginException.withMessage("aSpyException"));

		Response response = loginEndpoint.getAuthTokenForAppToken(SOME_USER_ID, SOME_PASSWORD);

		assertResponseStatusIs(response, Response.Status.UNAUTHORIZED);
	}

	@Test
	public void testGetAuthTokenWithAppToken_AnyException_ResponseWithInternalServerError()
			throws Exception {
		appTokenLoginSpy.MRV.setAlwaysThrowException("getAuthToken", new RuntimeException());

		Response response = loginEndpoint.getAuthTokenForAppToken(SOME_USER_ID, SOME_PASSWORD);

		assertResponseStatusIs(response, Response.Status.INTERNAL_SERVER_ERROR);
	}

	class LoginEndpointOnlyForTest extends LoginEndpoint {

		public MethodCallRecorder MCR = new MethodCallRecorder();
		public MethodReturnValues MRV = new MethodReturnValues();

		public LoginEndpointOnlyForTest(HttpServletRequest request) {
			super(request);
			MCR.useMRV(MRV);
			MRV.setDefaultReturnValuesSupplier("getUserAndMakeSureIsActive", () -> user);
			MRV.setDefaultReturnValuesSupplier("buildResponseUsingAuthToken",
					() -> Response.status(Status.OK).build());
		}

		@Override
		Response buildResponseUsingAuthToken(AuthToken authToken) throws URISyntaxException {
			return (Response) MCR.addCallAndReturnFromMRV("authToken", authToken);
		}
	}

	@Test
	public void testBuildResponseUsingAuthToken_WithName() throws Exception {
		AuthToken authToken = AuthToken.withIdAndValidForNoSecondsAndIdInUserStorageAndIdFromLogin(
				"someAuthToken", 278, "someIdInUserStorage", "someIdFromLogin");
		authToken.firstName = "someFirstName";
		authToken.lastName = "someLastName";

		Response response = loginEndpoint.buildResponseUsingAuthToken(authToken);

		assertResponseStatusIs(response, Response.Status.CREATED);
		String expectedJsonToken = "{\"data\":{\"children\":["
				+ "{\"name\":\"id\",\"value\":\"someAuthToken\"},"
				+ "{\"name\":\"validForNoSeconds\",\"value\":\"278\"},"
				+ "{\"name\":\"idInUserStorage\",\"value\":\"someIdInUserStorage\"},"
				+ "{\"name\":\"idFromLogin\",\"value\":\"someIdFromLogin\"},"
				+ "{\"name\":\"firstName\",\"value\":\"someFirstName\"},"
				+ "{\"name\":\"lastName\",\"value\":\"someLastName\"}]"
				+ ",\"name\":\"authToken\"},"
				+ "\"actionLinks\":{\"delete\":{\"requestMethod\":\"DELETE\","
				+ "\"rel\":\"delete\","
				+ "\"url\":\"http://localhost:8080/login/rest/authToken/someIdInUserStorage\"}}}";
		String entity = (String) response.getEntity();
		assertEquals(entity, expectedJsonToken);
	}

	private void assertResponseStatusIs(Response response, Status excpectedResponseStatus) {
		assertEquals(response.getStatusInfo(), excpectedResponseStatus);
	}

	@Test
	public void testGetAuthTokenWithPassword_Annotations() throws Exception {
		AnnotationTestHelper annotationHelper = AnnotationTestHelper
				.createAnnotationTestHelperForClassMethodNameAndNumOfParameters(LoginEndpoint.class,
						"getAuthTokenForPassword", 2);

		annotationHelper.assertHttpMethodAndPathAnnotation("POST", "password/{loginId}");
		annotationHelper.assertConsumesAnnotation(TEXT_PLAIN_CHARSET_UTF_8);
		annotationHelper.assertProducesAnnotation(APPLICATION_VND_UUB_RECORD_JSON);
		annotationHelper.assertPathParamAnnotationByNameAndPosition("loginId", 0);
	}

	@Test
	public void testGetAuthTokenWithPassword_PasswordLogin() throws Exception {
		loginEndpoint.getAuthTokenForPassword(SOME_LOGIN_ID, SOME_PASSWORD);

		loginFactory.MCR.assertParameters("factorPasswordLogin", 0);
		PasswordLoginSpy passwordLogin = (PasswordLoginSpy) loginFactory.MCR
				.getReturnValue("factorPasswordLogin", 0);

		passwordLogin.MCR.assertParameters("getAuthToken", 0, SOME_LOGIN_ID, SOME_PASSWORD);

	}

	@Test
	public void testGetAuthTokenWithPassword_BuildResponse() throws Exception {
		LoginEndpointOnlyForTest loginEndpoint = new LoginEndpointOnlyForTest(request);

		Response response = loginEndpoint.getAuthTokenForPassword(SOME_LOGIN_ID, SOME_PASSWORD);

		var authToken = passwordLoginSpy.MCR.getReturnValue("getAuthToken", 0);

		loginEndpoint.MCR.assertParameters("buildResponseUsingAuthToken", 0, authToken);
		loginEndpoint.MCR.assertReturn("buildResponseUsingAuthToken", 0, response);
	}

	@Test
	public void testGetAuthTokenWithPassword_LoginException_ResponseWithUnauthorized()
			throws Exception {
		passwordLoginSpy.MRV.setAlwaysThrowException("getAuthToken",
				LoginException.withMessage("aSpyException"));

		Response response = loginEndpoint.getAuthTokenForPassword(SOME_USER_ID, SOME_PASSWORD);

		assertResponseStatusIs(response, Response.Status.UNAUTHORIZED);
	}

	@Test
	public void testGetAuthTokenWithPassword_AnyException_ResponseWithInternalServerError()
			throws Exception {
		passwordLoginSpy.MRV.setAlwaysThrowException("getAuthToken", new RuntimeException());

		Response response = loginEndpoint.getAuthTokenForPassword(SOME_USER_ID, SOME_PASSWORD);

		assertResponseStatusIs(response, Response.Status.INTERNAL_SERVER_ERROR);
	}

	@Test
	public void testRemoveAuthTokenForUser() {
		Response response = loginEndpoint.removeAuthTokenForAppToken(SOME_USER_ID, "someAuthToken");

		assertResponseStatusIs(response, Response.Status.OK);
	}

	@Test
	public void testRemoveAuthTokenForUserWrongToken() {
		GatekeeperTokenProviderErrorSpy gatekeeperTokenProvider = new GatekeeperTokenProviderErrorSpy();
		GatekeeperInstanceProvider.setGatekeeperTokenProvider(gatekeeperTokenProvider);

		Response response = loginEndpoint.removeAuthTokenForAppToken(SOME_USER_ID,
				"someAuthTokenNotFound");

		assertResponseStatusIs(response, Response.Status.NOT_FOUND);
	}

	@Test
	public void testRemoveAuthTokenForAppToken_Annotations() throws Exception {
		AnnotationTestHelper annotationHelper = AnnotationTestHelper
				.createAnnotationTestHelperForClassMethodNameAndNumOfParameters(LoginEndpoint.class,
						"removeAuthTokenForAppToken", 2);

		annotationHelper.assertHttpMethodAndPathAnnotation("DELETE", "authToken/{userRecordId}");
		annotationHelper.assertConsumesAnnotation(TEXT_PLAIN_CHARSET_UTF_8);
		annotationHelper.assertPathParamAnnotationByNameAndPosition("userRecordId", 0);
	}

}
