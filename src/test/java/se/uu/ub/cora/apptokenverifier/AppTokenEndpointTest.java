/*
 * Copyright 2017, 2018, 2021 Uppsala University Library
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

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import se.uu.ub.cora.apptokenverifier.initialize.GatekepperInstanceProvider;
import se.uu.ub.cora.apptokenverifier.spies.AppTokenStorageViewInstanceProviderSpy;
import se.uu.ub.cora.apptokenverifier.spies.AppTokenStorageViewSpy;
import se.uu.ub.cora.apptokenverifier.spies.GatekeeperTokenProviderErrorSpy;
import se.uu.ub.cora.apptokenverifier.spies.GatekeeperTokenProviderSpy;
import se.uu.ub.cora.apptokenverifier.spies.HttpServletRequestSpy;
import se.uu.ub.cora.gatekeepertokenprovider.AuthToken;
import se.uu.ub.cora.initialize.SettingsProvider;
import se.uu.ub.cora.logger.LoggerProvider;
import se.uu.ub.cora.logger.spies.LoggerFactorySpy;

public class AppTokenEndpointTest {
	private static final String SOME_APP_TOKEN = "someAppToken";
	private static final String SOME_USER_ID = "someUserId";
	private Response response;
	private AppTokenEndpoint appTokenEndpoint;
	private HttpServletRequestSpy request;
	private GatekeeperTokenProviderSpy gatekeeperTokenProvider;
	private AppTokenStorageViewInstanceProviderSpy instanceProvider;

	@BeforeMethod
	public void setup() {
		LoggerFactorySpy loggerFactory = new LoggerFactorySpy();
		LoggerProvider.setLoggerFactory(loggerFactory);

		Map<String, String> settings = new HashMap<>();
		settings.put("apptokenVerifierPublicPathToSystem", "/apptokenverifier/rest/");
		settings.put("storageOnDiskBasePath", "/mnt/data/basicstorage");

		instanceProvider = new AppTokenStorageViewInstanceProviderSpy();
		AppTokenStorageProvider.onlyForTestSetAppTokenViewInstanceProvider(instanceProvider);
		gatekeeperTokenProvider = new GatekeeperTokenProviderSpy();
		GatekepperInstanceProvider.setGatekeeperTokenProvider(gatekeeperTokenProvider);
		SettingsProvider.setSettings(settings);

		request = new HttpServletRequestSpy();
		appTokenEndpoint = new AppTokenEndpoint(request);
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
		AppTokenStorageViewSpy appTokenStorage = (AppTokenStorageViewSpy) instanceProvider.MCR
				.getReturnValue("getStorageView", 0);
		appTokenStorage.MCR.assertParameters("userIdHasAppToken", 0, SOME_USER_ID, SOME_APP_TOKEN);

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
		setNoUserForAppTokenInStorage();

		response = appTokenEndpoint.getAuthTokenForAppToken("someUserIdNotFound", SOME_APP_TOKEN);

		assertResponseStatusIs(Response.Status.NOT_FOUND);
	}

	@Test
	public void testGetAuthTokenForAppTokenNotFound() {
		setNoUserForAppTokenInStorage();

		response = appTokenEndpoint.getAuthTokenForAppToken(SOME_USER_ID, "someAppTokenNotFound");

		assertResponseStatusIs(Response.Status.NOT_FOUND);
	}

	private void setNoUserForAppTokenInStorage() {
		AppTokenStorageViewSpy appTokenStorageView = new AppTokenStorageViewSpy();
		appTokenStorageView.MRV.setDefaultReturnValuesSupplier("userIdHasAppToken",
				(Supplier<Boolean>) () -> false);
		AppTokenStorageViewInstanceProviderSpy instanceProvider = new AppTokenStorageViewInstanceProviderSpy();
		instanceProvider.MRV.setDefaultReturnValuesSupplier("getStorageView",
				(Supplier<AppTokenStorageView>) () -> appTokenStorageView);

		AppTokenStorageProvider.onlyForTestSetAppTokenViewInstanceProvider(instanceProvider);
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
