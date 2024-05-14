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

package se.uu.ub.cora.login;

import static org.testng.Assert.assertEquals;

import java.net.URI;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import se.uu.ub.cora.gatekeeper.storage.UserStorageProvider;
import se.uu.ub.cora.gatekeeper.storage.UserStorageViewException;
import se.uu.ub.cora.gatekeeper.user.User;
import se.uu.ub.cora.gatekeepertokenprovider.AuthToken;
import se.uu.ub.cora.initialize.SettingsProvider;
import se.uu.ub.cora.logger.LoggerProvider;
import se.uu.ub.cora.logger.spies.LoggerFactorySpy;
import se.uu.ub.cora.login.initialize.GatekeeperInstanceProvider;
import se.uu.ub.cora.login.spies.GatekeeperTokenProviderErrorSpy;
import se.uu.ub.cora.login.spies.GatekeeperTokenProviderSpy;
import se.uu.ub.cora.login.spies.HttpServletRequestSpy;
import se.uu.ub.cora.login.spies.UserStorageViewInstanceProviderSpy;
import se.uu.ub.cora.login.spies.UserStorageViewSpy;
import se.uu.ub.cora.testutils.mcr.MethodCallRecorder;
import se.uu.ub.cora.testutils.mrv.MethodReturnValues;

public class LoginEndpointTest {
	private static final String SOME_PASSWORD = "somePassword";
	private static final String SOME_APP_TOKEN = "tokenStringFromSpy";
	private static final String SOME_USER_ID = "someUserId";
	private static final String TEXT_PLAIN_CHARSET_UTF_8 = "text/plain; charset=utf-8";
	private static final String APPLICATION_VND_UUB_RECORD_JSON = "application/vnd.uub.record+json";
	private LoginEndpoint loginEndpoint;
	private HttpServletRequestSpy request;
	private GatekeeperTokenProviderSpy gatekeeperTokenProvider;
	private UserStorageViewInstanceProviderSpy userStorageInstanceProvider;
	private MapSpy<String, String> settingsMapSpy;
	private User user;
	private UserStorageViewSpy userStorageView;
	private LoginFactorySpy loginFactory;

	@BeforeMethod
	public void setup() {
		LoggerFactorySpy loggerFactory = new LoggerFactorySpy();
		LoggerProvider.setLoggerFactory(loggerFactory);

		userStorageInstanceProvider = new UserStorageViewInstanceProviderSpy();
		UserStorageProvider
				.onlyForTestSetUserStorageViewInstanceProvider(userStorageInstanceProvider);

		settingsMapSpy = new MapSpy<>();
		settingsMapSpy.put("loginPublicPathToSystem", "/login/rest/");
		SettingsProvider.setSettings(settingsMapSpy);

		gatekeeperTokenProvider = new GatekeeperTokenProviderSpy();
		GatekeeperInstanceProvider.setGatekeeperTokenProvider(gatekeeperTokenProvider);

		loginFactory = new LoginFactorySpy();
		LoginDependencyProvider.onlyForTestSetLoginFactory(loginFactory);

		request = new HttpServletRequestSpy();

		user = new User(SOME_USER_ID);
		configureUser(user, true, Optional.empty(), "someAppTokenId1", "someAppTokenId2");
		setupBasicUserInStorage(user);

		loginEndpoint = new LoginEndpoint(request);
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

	private void setupBasicUserInStorage(User user) {
		userStorageView = new UserStorageViewSpy();
		userStorageView.MRV.setDefaultReturnValuesSupplier("getUserById", () -> user);
		userStorageInstanceProvider.MRV.setDefaultReturnValuesSupplier("getStorageView",
				() -> userStorageView);

		UserStorageProvider
				.onlyForTestSetUserStorageViewInstanceProvider(userStorageInstanceProvider);
	}

	@Test
	public void testUserStorageViewCreatedOnInitialization() throws Exception {
		userStorageInstanceProvider.MCR.assertMethodWasCalled("getStorageView");
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

		annotationHelper.assertHttpMethodAndPathAnnotation("POST", "apptoken/{userId}");
		annotationHelper.assertConsumesAnnotation(TEXT_PLAIN_CHARSET_UTF_8);
		annotationHelper.assertProducesAnnotation(APPLICATION_VND_UUB_RECORD_JSON);
		annotationHelper.assertPathParamAnnotationByNameAndPosition("userId", 0);
	}

	@Test
	public void testGetAuthTokenForAppToken() {
		Response response = loginEndpoint.getAuthTokenForAppToken(SOME_USER_ID, SOME_APP_TOKEN);

		assertResponseStatusIs(response, Response.Status.CREATED);
		String expectedJsonToken = "{\"data\":{\"children\":["
				+ "{\"name\":\"id\",\"value\":\"someAuthToken\"},"
				+ "{\"name\":\"validForNoSeconds\",\"value\":\"278\"},"
				+ "{\"name\":\"idInUserStorage\",\"value\":\"someIdInUserStorage\"},"
				+ "{\"name\":\"idFromLogin\",\"value\":\"someIdFromLogin\"}]"
				+ ",\"name\":\"authToken\"},"
				+ "\"actionLinks\":{\"delete\":{\"requestMethod\":\"DELETE\","
				+ "\"rel\":\"delete\","
				+ "\"url\":\"http://localhost:8080/login/rest/authToken/someUserId\"}}}";
		String entity = (String) response.getEntity();
		assertEquals(entity, expectedJsonToken);
		List<Object> locationHeaders = response.getHeaders().get("location");
		assertEquals(locationHeaders.size(), 1);
		URI firstLocationHeader = (URI) locationHeaders.get(0);
		assertEquals(firstLocationHeader.getPath(), "authToken/");
	}

	@Test
	public void testAssertGetUserAndMakeSureIsActive() throws Exception {
		configureUser(user, true, Optional.empty());

		loginEndpoint.getUserAndMakeSureIsActive(SOME_USER_ID);

		userStorageView.MCR.assertParameters("getUserById", 0, SOME_USER_ID);
	}

	@Test(expectedExceptions = LoginException.class, expectedExceptionsMessageRegExp = "User is not active")
	public void testGetAuthTokenForAppTokenNotActiveUser() {
		configureUser(user, false, Optional.empty());

		loginEndpoint.getUserAndMakeSureIsActive(SOME_USER_ID);

	}

	@Test
	public void testGetAuthTokenWithApptokenCallsGetUserAndMakeSureIsActive() throws Exception {
		LoginEndpointOnlyForTest loginTest = new LoginEndpointOnlyForTest(request);

		loginTest.getAuthTokenForAppToken(SOME_USER_ID, SOME_APP_TOKEN);

		loginTest.MCR.assertParameters("getUserAndMakeSureIsActive", 0, SOME_USER_ID);
	}

	class LoginEndpointOnlyForTest extends LoginEndpoint {

		public MethodCallRecorder MCR = new MethodCallRecorder();
		public MethodReturnValues MRV = new MethodReturnValues();

		public LoginEndpointOnlyForTest(HttpServletRequest request) {
			super(request);
			MCR.useMRV(MRV);
			MRV.setDefaultReturnValuesSupplier("getUserAndMakeSureIsActive", () -> user);
		}

		@Override
		User getUserAndMakeSureIsActive(String userId) {
			return (User) MCR.addCallAndReturnFromMRV("userId", userId);
		}

	}

	@Test
	public void testCallsAppTokenStorage() throws Exception {

		loginEndpoint.getAuthTokenForAppToken(SOME_USER_ID, SOME_APP_TOKEN);

		userStorageView.MCR.assertNumberOfCallsToMethod("getAppTokenById", 1);
		userStorageView.MCR.assertParameters("getAppTokenById", 0, "someAppTokenId1");
	}

	@Test
	public void testGetAuthTokenForAppTokenWithName() {
		AuthToken authToken = AuthToken.withIdAndValidForNoSecondsAndIdInUserStorageAndIdFromLogin(
				"someAuthToken", 278, "someIdInUserStorage", "someIdFromLogin");
		authToken.firstName = "someFirstName";
		authToken.lastName = "someLastName";
		gatekeeperTokenProvider.authToken = authToken;
		GatekeeperInstanceProvider.setGatekeeperTokenProvider(gatekeeperTokenProvider);

		Response response = loginEndpoint.getAuthTokenForAppToken(SOME_USER_ID, SOME_APP_TOKEN);

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
				+ "\"url\":\"http://localhost:8080/login/rest/authToken/someUserId\"}}}";
		String entity = (String) response.getEntity();
		assertEquals(entity, expectedJsonToken);
	}

	@Test
	public void testGetAuthTokenForAppTokenXForwardedProtoHttps() {
		request.headers.put("X-Forwarded-Proto", "https");
		loginEndpoint = new LoginEndpoint(request);

		Response response = loginEndpoint.getAuthTokenForAppToken(SOME_USER_ID, SOME_APP_TOKEN);

		assertResponseStatusIs(response, Response.Status.CREATED);
		String expectedJsonToken = "{\"data\":{\"children\":["
				+ "{\"name\":\"id\",\"value\":\"someAuthToken\"},"
				+ "{\"name\":\"validForNoSeconds\",\"value\":\"278\"},"
				+ "{\"name\":\"idInUserStorage\",\"value\":\"someIdInUserStorage\"},"
				+ "{\"name\":\"idFromLogin\",\"value\":\"someIdFromLogin\"}]"
				+ ",\"name\":\"authToken\"},"
				+ "\"actionLinks\":{\"delete\":{\"requestMethod\":\"DELETE\","
				+ "\"rel\":\"delete\","
				+ "\"url\":\"https://localhost:8080/login/rest/authToken/someUserId\"}}}";
		String entity = (String) response.getEntity();
		assertEquals(entity, expectedJsonToken);
	}

	@Test
	public void testGetAuthTokenForAppTokenXForwardedProtoHttpsWhenAlreadyHttpsInRequestUrl() {
		request.headers.put("X-Forwarded-Proto", "https");
		request.requestURL = new StringBuffer(
				"https://localhost:8080/apptoken/rest/apptoken/141414");
		loginEndpoint = new LoginEndpoint(request);

		Response response = loginEndpoint.getAuthTokenForAppToken(SOME_USER_ID, SOME_APP_TOKEN);

		assertResponseStatusIs(response, Response.Status.CREATED);
		String expectedJsonToken = "{\"data\":{\"children\":["
				+ "{\"name\":\"id\",\"value\":\"someAuthToken\"},"
				+ "{\"name\":\"validForNoSeconds\",\"value\":\"278\"},"
				+ "{\"name\":\"idInUserStorage\",\"value\":\"someIdInUserStorage\"},"
				+ "{\"name\":\"idFromLogin\",\"value\":\"someIdFromLogin\"}]"
				+ ",\"name\":\"authToken\"},"
				+ "\"actionLinks\":{\"delete\":{\"requestMethod\":\"DELETE\","
				+ "\"rel\":\"delete\","
				+ "\"url\":\"https://localhost:8080/login/rest/authToken/someUserId\"}}}";
		String entity = (String) response.getEntity();
		assertEquals(entity, expectedJsonToken);
	}

	@Test
	public void testGetAuthTokenForAppTokenXForwardedProtoEmpty() {
		request.headers.put("X-Forwarded-Proto", "");
		loginEndpoint = new LoginEndpoint(request);

		Response response = loginEndpoint.getAuthTokenForAppToken(SOME_USER_ID, SOME_APP_TOKEN);

		assertResponseStatusIs(response, Response.Status.CREATED);
		String expectedJsonToken = "{\"data\":{\"children\":["
				+ "{\"name\":\"id\",\"value\":\"someAuthToken\"},"
				+ "{\"name\":\"validForNoSeconds\",\"value\":\"278\"},"
				+ "{\"name\":\"idInUserStorage\",\"value\":\"someIdInUserStorage\"},"
				+ "{\"name\":\"idFromLogin\",\"value\":\"someIdFromLogin\"}]"
				+ ",\"name\":\"authToken\"},"
				+ "\"actionLinks\":{\"delete\":{\"requestMethod\":\"DELETE\","
				+ "\"rel\":\"delete\","
				+ "\"url\":\"http://localhost:8080/login/rest/authToken/someUserId\"}}}";
		String entity = (String) response.getEntity();
		assertEquals(entity, expectedJsonToken);
	}

	private void assertResponseStatusIs(Response response, Status excpectedResponseStatus) {
		assertEquals(response.getStatusInfo(), excpectedResponseStatus);
	}

	@Test
	public void testGetAuthTokenForAppTokenUserIdNotFound() {
		userStorageView.MRV.setAlwaysThrowException("getUserById",
				UserStorageViewException.usingMessage("error"));

		Response response = loginEndpoint.getAuthTokenForAppToken("someUserIdNotFound",
				SOME_APP_TOKEN);

		assertResponseStatusIs(response, Response.Status.NOT_FOUND);
	}

	@Test
	public void testGetAuthTokenForAppTokenNoAppTokens() {
		configureUser(user, true, Optional.empty());

		Response response = loginEndpoint.getAuthTokenForAppToken(SOME_USER_ID, SOME_APP_TOKEN);

		assertResponseStatusIs(response, Response.Status.NOT_FOUND);
	}

	@Test
	public void testUserIsNotActiveThrowException() throws Exception {
		configureUser(user, false, Optional.empty(), "appToken1");

		Response response = loginEndpoint.getAuthTokenForAppToken(SOME_USER_ID, SOME_APP_TOKEN);

		assertResponseStatusIs(response, Response.Status.NOT_FOUND);
	}

	@Test
	public void testGetAuthTokenForAppTokenNoCorrectTokenAllTokensAreChecked() {
		Response response = loginEndpoint.getAuthTokenForAppToken(SOME_USER_ID,
				"someAppTokenNotFound");

		assertResponseStatusIs(response, Response.Status.NOT_FOUND);
		UserStorageViewSpy userStorageView = (UserStorageViewSpy) userStorageInstanceProvider.MCR
				.getReturnValue("getStorageView", 0);
		userStorageView.MCR.assertNumberOfCallsToMethod("getAppTokenById", 2);
		userStorageView.MCR.assertParameters("getAppTokenById", 0, "someAppTokenId1");
		userStorageView.MCR.assertParameters("getAppTokenById", 1, "someAppTokenId2");
	}

	@Test
	public void testGetAuthTokenForAppTokenErrorFromGatekeeper() {
		GatekeeperTokenProviderErrorSpy gatekeeperTokenProvider = new GatekeeperTokenProviderErrorSpy();
		GatekeeperInstanceProvider.setGatekeeperTokenProvider(gatekeeperTokenProvider);

		Response response = loginEndpoint.getAuthTokenForAppToken(SOME_USER_ID, SOME_APP_TOKEN);

		assertResponseStatusIs(response, Response.Status.INTERNAL_SERVER_ERROR);
	}

	@Test
	public void testGetAuthTokenWithPassword_Annotations() throws Exception {
		AnnotationTestHelper annotationHelper = AnnotationTestHelper
				.createAnnotationTestHelperForClassMethodNameAndNumOfParameters(LoginEndpoint.class,
						"getAuthTokenForPassword", 2);

		annotationHelper.assertHttpMethodAndPathAnnotation("POST", "password/{userId}");
		// annotationHelper.assertConsumesAnnotation(TEXT_PLAIN_CHARSET_UTF_8);
		annotationHelper.assertProducesAnnotation(APPLICATION_VND_UUB_RECORD_JSON);
		annotationHelper.assertPathParamAnnotationByNameAndPosition("userId", 0);
	}

	@Test
	public void testGetAuthTokenWithPassword_PasswordLogin() throws Exception {

		loginEndpoint.getAuthTokenForPassword(SOME_USER_ID, SOME_PASSWORD);

		loginFactory.MCR.assertParameters("factorPasswordLogin", 0);
	}

	// @Test
	// public void testGetAuthTokenWithPassword_CallsGetUserAndMakeSureIsActive() throws Exception {
	// LoginEndpointOnlyForTest loginEndpoint = new LoginEndpointOnlyForTest(request);
	//
	// loginEndpoint.getAuthTokenForPassword(SOME_USER_ID, SOME_PASSWORD);
	//
	// loginEndpoint.MCR.assertParameters("getUserAndMakeSureIsActive", 0, SOME_USER_ID);
	// }
	//
	// @Test
	// public void testGetAuthTokenWithPassword_CallsPasswordMatchesForUser() throws Exception {
	// LoginEndpointOnlyForTest loginEndpoint = new LoginEndpointOnlyForTest(request);
	//
	// loginEndpoint.getAuthTokenForPassword(SOME_USER_ID, SOME_PASSWORD);
	//
	// User user = (User) loginEndpoint.MCR.getReturnValue("getUserAndMakeSureIsActive", 0);
	// userStorageView.MCR.assertParameters("doesPasswordMatchForUser", 0, user, SOME_PASSWORD);
	// }
	//
	// @Test
	// public void testGetAuthTokenWithPassword_PasswordDoNotMatch() throws Exception {
	//
	// Response response = loginEndpoint.getAuthTokenForPassword(SOME_USER_ID, SOME_PASSWORD);
	//
	// assertResponseStatusIs(response, Response.Status.NOT_FOUND);
	// }

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

		annotationHelper.assertHttpMethodAndPathAnnotation("DELETE", "authToken/{userId}");
		annotationHelper.assertConsumesAnnotation(TEXT_PLAIN_CHARSET_UTF_8);
		annotationHelper.assertPathParamAnnotationByNameAndPosition("userId", 0);
	}

}
