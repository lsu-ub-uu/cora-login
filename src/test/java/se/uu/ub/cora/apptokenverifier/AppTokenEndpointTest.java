/*
 * Copyright 2017, 2018, 2021, 2022 Uppsala University Library
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

package se.uu.ub.cora.apptokenverifier;

import static org.testng.Assert.assertEquals;

import java.util.Collections;
import java.util.function.Supplier;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import se.uu.ub.cora.apptokenverifier.initialize.GatekepperInstanceProvider;
import se.uu.ub.cora.apptokenverifier.spies.GatekeeperTokenProviderErrorSpy;
import se.uu.ub.cora.apptokenverifier.spies.GatekeeperTokenProviderSpy;
import se.uu.ub.cora.apptokenverifier.spies.HttpServletRequestSpy;
import se.uu.ub.cora.apptokenverifier.spies.UserStorageViewInstanceProviderSpy;
import se.uu.ub.cora.apptokenverifier.spies.UserStorageViewSpy;
import se.uu.ub.cora.gatekeeper.storage.UserStorageProvider;
import se.uu.ub.cora.gatekeeper.storage.UserStorageView;
import se.uu.ub.cora.gatekeeper.storage.UserStorageViewException;
import se.uu.ub.cora.gatekeeper.user.User;
import se.uu.ub.cora.gatekeepertokenprovider.AuthToken;
import se.uu.ub.cora.initialize.SettingsProvider;
import se.uu.ub.cora.logger.LoggerProvider;
import se.uu.ub.cora.logger.spies.LoggerFactorySpy;

public class AppTokenEndpointTest {
	private static final String SOME_APP_TOKEN = "tokenStringFromSpy";
	private static final String SOME_USER_ID = "someUserId";
	private Response response;
	private AppTokenEndpoint appTokenEndpoint;
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
		settingsMapSpy.put("apptokenVerifierPublicPathToSystem", "/apptokenverifier/rest/");
		settingsMapSpy.put("storageOnDiskBasePath", "/mnt/data/basicstorage");
		SettingsProvider.setSettings(settingsMapSpy);

		gatekeeperTokenProvider = new GatekeeperTokenProviderSpy();
		GatekepperInstanceProvider.setGatekeeperTokenProvider(gatekeeperTokenProvider);

		request = new HttpServletRequestSpy();
		appTokenEndpoint = new AppTokenEndpoint(request);

		User user = new User(SOME_USER_ID);
		user.active = true;
		user.appTokenIds.add("someAppTokenId1");
		user.appTokenIds.add("someAppTokenId2");
		setUserForUserIdInStorage(user);
	}

	private void setUserForUserIdInStorage(User user) {
		UserStorageViewSpy userStorageView = new UserStorageViewSpy();
		userStorageView.MRV.setDefaultReturnValuesSupplier("getUserById",
				(Supplier<User>) () -> user);
		userStorageInstanceProvider.MRV.setDefaultReturnValuesSupplier("getStorageView",
				(Supplier<UserStorageView>) () -> userStorageView);

		UserStorageProvider
				.onlyForTestSetUserStorageViewInstanceProvider(userStorageInstanceProvider);
	}

	@Test
	public void testGetAuthTokenForAppToken() {
		response = appTokenEndpoint.getAuthTokenForAppToken(SOME_USER_ID, SOME_APP_TOKEN);

		assertResponseStatusIs(Response.Status.CREATED);
		String expectedJsonToken = "{\"data\":{\"children\":["
				+ "{\"name\":\"id\",\"value\":\"someAuthToken\"},"
				+ "{\"name\":\"validForNoSeconds\",\"value\":\"278\"},"
				+ "{\"name\":\"idInUserStorage\",\"value\":\"someIdInUserStorage\"},"
				+ "{\"name\":\"idFromLogin\",\"value\":\"someIdFromLogin\"}]"
				+ ",\"name\":\"authToken\"},"
				+ "\"actionLinks\":{\"delete\":{\"requestMethod\":\"DELETE\","
				+ "\"rel\":\"delete\","
				+ "\"url\":\"http://localhost:8080/apptokenverifier/rest/apptoken/someUserId\"}}}";
		String entity = (String) response.getEntity();
		assertEquals(entity, expectedJsonToken);
	}

	@Test
	public void testCallsAppTokenStorage() throws Exception {
		response = appTokenEndpoint.getAuthTokenForAppToken(SOME_USER_ID, SOME_APP_TOKEN);

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
		GatekepperInstanceProvider.setGatekeeperTokenProvider(gatekeeperTokenProvider);

		response = appTokenEndpoint.getAuthTokenForAppToken(SOME_USER_ID, SOME_APP_TOKEN);

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
				+ "\"url\":\"http://localhost:8080/apptokenverifier/rest/apptoken/someUserId\"}}}";
		String entity = (String) response.getEntity();
		assertEquals(entity, expectedJsonToken);
	}

	@Test
	public void testGetAuthTokenForAppTokenXForwardedProtoHttps() {
		request.headers.put("X-Forwarded-Proto", "https");
		appTokenEndpoint = new AppTokenEndpoint(request);

		response = appTokenEndpoint.getAuthTokenForAppToken(SOME_USER_ID, SOME_APP_TOKEN);

		assertResponseStatusIs(Response.Status.CREATED);
		String expectedJsonToken = "{\"data\":{\"children\":["
				+ "{\"name\":\"id\",\"value\":\"someAuthToken\"},"
				+ "{\"name\":\"validForNoSeconds\",\"value\":\"278\"},"
				+ "{\"name\":\"idInUserStorage\",\"value\":\"someIdInUserStorage\"},"
				+ "{\"name\":\"idFromLogin\",\"value\":\"someIdFromLogin\"}]"
				+ ",\"name\":\"authToken\"},"
				+ "\"actionLinks\":{\"delete\":{\"requestMethod\":\"DELETE\","
				+ "\"rel\":\"delete\","
				+ "\"url\":\"https://localhost:8080/apptokenverifier/rest/apptoken/someUserId\"}}}";
		String entity = (String) response.getEntity();
		assertEquals(entity, expectedJsonToken);
	}

	@Test
	public void testGetAuthTokenForAppTokenXForwardedProtoHttpsWhenAlreadyHttpsInRequestUrl() {
		request.headers.put("X-Forwarded-Proto", "https");
		request.requestURL = new StringBuffer(
				"https://localhost:8080/apptoken/rest/apptoken/141414");
		appTokenEndpoint = new AppTokenEndpoint(request);

		response = appTokenEndpoint.getAuthTokenForAppToken(SOME_USER_ID, SOME_APP_TOKEN);

		assertResponseStatusIs(Response.Status.CREATED);
		String expectedJsonToken = "{\"data\":{\"children\":["
				+ "{\"name\":\"id\",\"value\":\"someAuthToken\"},"
				+ "{\"name\":\"validForNoSeconds\",\"value\":\"278\"},"
				+ "{\"name\":\"idInUserStorage\",\"value\":\"someIdInUserStorage\"},"
				+ "{\"name\":\"idFromLogin\",\"value\":\"someIdFromLogin\"}]"
				+ ",\"name\":\"authToken\"},"
				+ "\"actionLinks\":{\"delete\":{\"requestMethod\":\"DELETE\","
				+ "\"rel\":\"delete\","
				+ "\"url\":\"https://localhost:8080/apptokenverifier/rest/apptoken/someUserId\"}}}";
		String entity = (String) response.getEntity();
		assertEquals(entity, expectedJsonToken);
	}

	@Test
	public void testGetAuthTokenForAppTokenXForwardedProtoEmpty() {
		request.headers.put("X-Forwarded-Proto", "");
		appTokenEndpoint = new AppTokenEndpoint(request);

		response = appTokenEndpoint.getAuthTokenForAppToken(SOME_USER_ID, SOME_APP_TOKEN);

		assertResponseStatusIs(Response.Status.CREATED);
		String expectedJsonToken = "{\"data\":{\"children\":["
				+ "{\"name\":\"id\",\"value\":\"someAuthToken\"},"
				+ "{\"name\":\"validForNoSeconds\",\"value\":\"278\"},"
				+ "{\"name\":\"idInUserStorage\",\"value\":\"someIdInUserStorage\"},"
				+ "{\"name\":\"idFromLogin\",\"value\":\"someIdFromLogin\"}]"
				+ ",\"name\":\"authToken\"},"
				+ "\"actionLinks\":{\"delete\":{\"requestMethod\":\"DELETE\","
				+ "\"rel\":\"delete\","
				+ "\"url\":\"http://localhost:8080/apptokenverifier/rest/apptoken/someUserId\"}}}";
		String entity = (String) response.getEntity();
		assertEquals(entity, expectedJsonToken);
	}

	private void assertResponseStatusIs(Status responseStatus) {
		assertEquals(response.getStatusInfo(), responseStatus);
	}

	@Test
	public void testGetAuthTokenForAppTokenUserIdNotFound() {
		setNoUserForUserIdInStorage();

		response = appTokenEndpoint.getAuthTokenForAppToken("someUserIdNotFound", SOME_APP_TOKEN);

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

		response = appTokenEndpoint.getAuthTokenForAppToken(SOME_USER_ID, SOME_APP_TOKEN);

		assertResponseStatusIs(Response.Status.NOT_FOUND);
	}

	@Test
	public void testGetAuthTokenForAppTokenNoAppTokens() {
		User user = new User(SOME_USER_ID);
		user.active = true;
		user.appTokenIds = Collections.emptySet();
		setUserForUserIdInStorage(user);

		response = appTokenEndpoint.getAuthTokenForAppToken(SOME_USER_ID, SOME_APP_TOKEN);

		assertResponseStatusIs(Response.Status.NOT_FOUND);
	}

	@Test
	public void testGetAuthTokenForAppTokenNoCorrectTokenAllTokensAreChecked() {
		response = appTokenEndpoint.getAuthTokenForAppToken(SOME_USER_ID, "someAppTokenNotFound");

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
		GatekepperInstanceProvider.setGatekeeperTokenProvider(gatekeeperTokenProvider);

		response = appTokenEndpoint.getAuthTokenForAppToken(SOME_USER_ID, SOME_APP_TOKEN);

		assertResponseStatusIs(Response.Status.INTERNAL_SERVER_ERROR);
	}

	@Test
	public void testRemoveAuthTokenForUser() {
		response = appTokenEndpoint.removeAuthTokenForAppToken(SOME_USER_ID, "someAuthToken");

		assertResponseStatusIs(Response.Status.OK);
	}

	@Test
	public void testRemoveAuthTokenForUserWrongToken() {
		GatekeeperTokenProviderErrorSpy gatekeeperTokenProvider = new GatekeeperTokenProviderErrorSpy();
		GatekepperInstanceProvider.setGatekeeperTokenProvider(gatekeeperTokenProvider);

		response = appTokenEndpoint.removeAuthTokenForAppToken(SOME_USER_ID,
				"someAuthTokenNotFound");

		assertResponseStatusIs(Response.Status.NOT_FOUND);
	}

}
