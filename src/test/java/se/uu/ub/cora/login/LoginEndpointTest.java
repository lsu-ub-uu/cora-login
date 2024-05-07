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
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import se.uu.ub.cora.gatekeeper.storage.UserStorageProvider;
import se.uu.ub.cora.gatekeeper.storage.UserStorageView;
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

public class LoginEndpointTest {
	private static final String SOME_APP_TOKEN = "tokenStringFromSpy";
	private static final String SOME_USER_ID = "someUserId";
	private static final String TEXT_PLAIN_CHARSET_UTF_8 = "text/plain; charset=utf-8";
	private static final String APPLICATION_VND_UUB_RECORD_JSON = "application/vnd.uub.record+json";
	private Response response;
	private LoginEndpoint loginEndpoint;
	private HttpServletRequestSpy request;
	private GatekeeperTokenProviderSpy gatekeeperTokenProvider;
	private UserStorageViewInstanceProviderSpy userStorageInstanceProvider;
	private MapSpy<String, String> settingsMapSpy;

	@BeforeMethod
	public void setup() {
		LoggerFactorySpy loggerFactory = new LoggerFactorySpy();
		LoggerProvider.setLoggerFactory(loggerFactory);

		userStorageInstanceProvider = new UserStorageViewInstanceProviderSpy();
		UserStorageProvider
				.onlyForTestSetUserStorageViewInstanceProvider(userStorageInstanceProvider);

		settingsMapSpy = new MapSpy<>();
		settingsMapSpy.put("loginPublicPathToSystem", "/login/rest/");
		settingsMapSpy.put("storageOnDiskBasePath", "/mnt/data/basicstorage");
		SettingsProvider.setSettings(settingsMapSpy);

		gatekeeperTokenProvider = new GatekeeperTokenProviderSpy();
		GatekeeperInstanceProvider.setGatekeeperTokenProvider(gatekeeperTokenProvider);

		request = new HttpServletRequestSpy();
		loginEndpoint = new LoginEndpoint(request);

		User user = createUserUserWithStatusAndApptokens(true, "someAppTokenId1",
				"someAppTokenId2");

		setUserForUserIdInStorage(user);
	}

	private User createUserUserWithStatusAndApptokens(boolean active, String... appTokens) {
		User user = new User(SOME_USER_ID);
		user.active = active;
		for (String appToken : appTokens) {
			user.appTokenIds.add(appToken);
		}
		return user;
	}

	private void setUserForUserIdInStorage(User user) {
		UserStorageViewSpy userStorageView = new UserStorageViewSpy();
		userStorageView.MRV.setDefaultReturnValuesSupplier("getUserById", () -> user);
		userStorageInstanceProvider.MRV.setDefaultReturnValuesSupplier("getStorageView",
				() -> userStorageView);

		UserStorageProvider
				.onlyForTestSetUserStorageViewInstanceProvider(userStorageInstanceProvider);
	}

	@Test
	public void testLoginEndpointPathAnnotation() throws Exception {
		AnnotationTestHelper annotationHelper = AnnotationTestHelper
				.createAnnotationTestHelperForClass(LoginEndpoint.class);
		annotationHelper.assertPathAnnotationForClass("/");
	}

	@Test
	public void testAnnotationsetAuthTokenForAppToken_Annotations() throws Exception {
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
		response = loginEndpoint.getAuthTokenForAppToken(SOME_USER_ID, SOME_APP_TOKEN);

		assertResponseStatusIs(Response.Status.CREATED);
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
	public void testCallsAppTokenStorage() throws Exception {
		response = loginEndpoint.getAuthTokenForAppToken(SOME_USER_ID, SOME_APP_TOKEN);

		UserStorageViewSpy userStorageView = (UserStorageViewSpy) userStorageInstanceProvider.MCR
				.getReturnValue("getStorageView", 0);
		userStorageView.MCR.assertParameters("getUserById", 0, SOME_USER_ID);
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

		response = loginEndpoint.getAuthTokenForAppToken(SOME_USER_ID, SOME_APP_TOKEN);

		assertResponseStatusIs(Response.Status.CREATED);
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

		response = loginEndpoint.getAuthTokenForAppToken(SOME_USER_ID, SOME_APP_TOKEN);

		assertResponseStatusIs(Response.Status.CREATED);
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

		response = loginEndpoint.getAuthTokenForAppToken(SOME_USER_ID, SOME_APP_TOKEN);

		assertResponseStatusIs(Response.Status.CREATED);
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

		response = loginEndpoint.getAuthTokenForAppToken(SOME_USER_ID, SOME_APP_TOKEN);

		assertResponseStatusIs(Response.Status.CREATED);
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

	private void assertResponseStatusIs(Status responseStatus) {
		assertEquals(response.getStatusInfo(), responseStatus);
	}

	@Test
	public void testGetAuthTokenForAppTokenUserIdNotFound() {
		setNoUserForUserIdInStorage();

		response = loginEndpoint.getAuthTokenForAppToken("someUserIdNotFound", SOME_APP_TOKEN);

		assertResponseStatusIs(Response.Status.NOT_FOUND);
	}

	private void setNoUserForUserIdInStorage() {
		UserStorageViewSpy userStorageView = new UserStorageViewSpy();
		userStorageView.MRV.setAlwaysThrowException("getUserById",
				UserStorageViewException.usingMessage("error"));
		UserStorageViewInstanceProviderSpy instanceProvider = new UserStorageViewInstanceProviderSpy();
		instanceProvider.MRV.setDefaultReturnValuesSupplier("getStorageView",
				(Supplier<UserStorageView>) () -> userStorageView);

		UserStorageProvider.onlyForTestSetUserStorageViewInstanceProvider(instanceProvider);
	}

	@Test
	public void testGetAuthTokenForAppTokenNotActiveUser() {
		User user = new User(SOME_USER_ID);
		user.active = false;
		setUserForUserIdInStorage(user);

		response = loginEndpoint.getAuthTokenForAppToken(SOME_USER_ID, SOME_APP_TOKEN);

		assertResponseStatusIs(Response.Status.NOT_FOUND);
	}

	@Test
	public void testGetAuthTokenForAppTokenNoAppTokens() {
		User user = new User(SOME_USER_ID);
		user.active = true;
		user.appTokenIds = Collections.emptySet();
		setUserForUserIdInStorage(user);

		response = loginEndpoint.getAuthTokenForAppToken(SOME_USER_ID, SOME_APP_TOKEN);

		assertResponseStatusIs(Response.Status.NOT_FOUND);
	}

	@Test
	public void testGetAuthTokenForAppTokenNoCorrectTokenAllTokensAreChecked() {
		response = loginEndpoint.getAuthTokenForAppToken(SOME_USER_ID, "someAppTokenNotFound");

		assertResponseStatusIs(Response.Status.NOT_FOUND);
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

		response = loginEndpoint.getAuthTokenForAppToken(SOME_USER_ID, SOME_APP_TOKEN);

		assertResponseStatusIs(Response.Status.INTERNAL_SERVER_ERROR);
	}

	@Test
	public void testRemoveAuthTokenForUser() {
		response = loginEndpoint.removeAuthTokenForAppToken(SOME_USER_ID, "someAuthToken");

		assertResponseStatusIs(Response.Status.OK);
	}

	@Test
	public void testRemoveAuthTokenForUserWrongToken() {
		GatekeeperTokenProviderErrorSpy gatekeeperTokenProvider = new GatekeeperTokenProviderErrorSpy();
		GatekeeperInstanceProvider.setGatekeeperTokenProvider(gatekeeperTokenProvider);

		response = loginEndpoint.removeAuthTokenForAppToken(SOME_USER_ID, "someAuthTokenNotFound");

		assertResponseStatusIs(Response.Status.NOT_FOUND);
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

	@Test()
	public void testUserIsNotActiveThrowException() throws Exception {
		User user = createUserUserWithStatusAndApptokens(false, "appToken1");
		setUserForUserIdInStorage(user);

		response = loginEndpoint.getAuthTokenForAppToken(SOME_USER_ID, SOME_APP_TOKEN);

		assertResponseStatusIs(Response.Status.NOT_FOUND);
	}

	@Test()
	public void testUserIsActiveButHasNoAppTokenThrowException() throws Exception {
		User user = createUserUserWithStatusAndApptokens(true);
		setUserForUserIdInStorage(user);

		response = loginEndpoint.getAuthTokenForAppToken(SOME_USER_ID, SOME_APP_TOKEN);

		assertResponseStatusIs(Response.Status.NOT_FOUND);
	}
}
