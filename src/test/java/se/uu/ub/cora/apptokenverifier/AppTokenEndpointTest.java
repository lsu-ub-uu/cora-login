/*
 * Copyright 2017, 2018 Uppsala University Library
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

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.apptokenstorage.AppTokenStorage;
import se.uu.ub.cora.apptokenverifier.initialize.AppTokenInstanceProvider;
import se.uu.ub.cora.gatekeepertokenprovider.AuthToken;

public class AppTokenEndpointTest {
	private Response response;
	private AppTokenEndpoint appTokenEndpoint;
	private TestHttpServletRequest request;
	private GatekeeperTokenProviderSpy gatekeeperTokenProvider;
	private Map<String, String> initInfo = new HashMap<>();

	@BeforeMethod
	public void setup() {
		Map<String, String> initInfo = new HashMap<>();
		initInfo.put("appTokenVerifierPublicPathToSystem", "/apptokenverifier/rest/");
		initInfo.put("storageOnDiskBasePath", "/mnt/data/basicstorage");
		AppTokenStorage appTokenStorage = new AppTokenStorageSpy(initInfo);
		AppTokenInstanceProvider.setApptokenStorage(appTokenStorage);
		gatekeeperTokenProvider = new GatekeeperTokenProviderSpy();
		AppTokenInstanceProvider.setGatekeeperTokenProvider(gatekeeperTokenProvider);
		AppTokenInstanceProvider.setInitInfo(initInfo);

		request = new TestHttpServletRequest();
		appTokenEndpoint = new AppTokenEndpoint(request);
	}

	@Test
	public void testGetAuthTokenForAppToken() {
		String userId = "someUserId";
		String appToken = "someAppToken";

		response = appTokenEndpoint.getAuthTokenForAppToken(userId, appToken);
		assertResponseStatusIs(Response.Status.CREATED);
		String expectedJsonToken = "{\"data\":{\"children\":["
				+ "{\"name\":\"id\",\"value\":\"someAuthToken\"},"
				+ "{\"name\":\"validForNoSeconds\",\"value\":\"278\"},"
				+ "{\"name\":\"idInUserStorage\",\"value\":\"someIdInUserStorage\"},"
				+ "{\"name\":\"idFromLogin\",\"value\":\"someIdFromLogin\"}" + "]"
				+ ",\"name\":\"authToken\"},"
				+ "\"actionLinks\":{\"delete\":{\"requestMethod\":\"DELETE\","
				+ "\"rel\":\"delete\","
				+ "\"url\":\"http://localhost:8080/apptokenverifier/rest/apptoken/someUserId\"}}}";
		String entity = (String) response.getEntity();
		assertEquals(entity, expectedJsonToken);
	}

	@Test
	public void testGetAuthTokenForAppTokenWithName() {
		AuthToken authToken = AuthToken.withIdAndValidForNoSecondsAndIdInUserStorageAndIdFromLogin(
				"someAuthToken", 278, "someIdInUserStorage", "someIdFromLogin");
		authToken.firstName = "someFirstName";
		authToken.lastName = "someLastName";
		gatekeeperTokenProvider.authToken = authToken;
		AppTokenInstanceProvider.setGatekeeperTokenProvider(gatekeeperTokenProvider);

		String userId = "someUserId";
		String appToken = "someAppToken";

		response = appTokenEndpoint.getAuthTokenForAppToken(userId, appToken);
		assertResponseStatusIs(Response.Status.CREATED);
		String expectedJsonToken = "{\"data\":{\"children\":["
				+ "{\"name\":\"id\",\"value\":\"someAuthToken\"},"
				+ "{\"name\":\"validForNoSeconds\",\"value\":\"278\"},"
				+ "{\"name\":\"idInUserStorage\",\"value\":\"someIdInUserStorage\"},"
				+ "{\"name\":\"idFromLogin\",\"value\":\"someIdFromLogin\"},"
				+ "{\"name\":\"firstName\",\"value\":\"someFirstName\"},"
				+ "{\"name\":\"lastName\",\"value\":\"someLastName\"}" + "]"

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
		String userId = "someUserId";
		String appToken = "someAppToken";

		response = appTokenEndpoint.getAuthTokenForAppToken(userId, appToken);
		assertResponseStatusIs(Response.Status.CREATED);
		String expectedJsonToken = "{\"data\":{\"children\":["
				+ "{\"name\":\"id\",\"value\":\"someAuthToken\"},"
				+ "{\"name\":\"validForNoSeconds\",\"value\":\"278\"},"
				+ "{\"name\":\"idInUserStorage\",\"value\":\"someIdInUserStorage\"},"
				+ "{\"name\":\"idFromLogin\",\"value\":\"someIdFromLogin\"}" + "]"
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
		String userId = "someUserId";
		String appToken = "someAppToken";

		response = appTokenEndpoint.getAuthTokenForAppToken(userId, appToken);
		assertResponseStatusIs(Response.Status.CREATED);
		String expectedJsonToken = "{\"data\":{\"children\":["
				+ "{\"name\":\"id\",\"value\":\"someAuthToken\"},"
				+ "{\"name\":\"validForNoSeconds\",\"value\":\"278\"},"
				+ "{\"name\":\"idInUserStorage\",\"value\":\"someIdInUserStorage\"},"
				+ "{\"name\":\"idFromLogin\",\"value\":\"someIdFromLogin\"}" + "]"
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
		String userId = "someUserId";
		String appToken = "someAppToken";

		response = appTokenEndpoint.getAuthTokenForAppToken(userId, appToken);
		assertResponseStatusIs(Response.Status.CREATED);
		String expectedJsonToken = "{\"data\":{\"children\":["
				+ "{\"name\":\"id\",\"value\":\"someAuthToken\"},"
				+ "{\"name\":\"validForNoSeconds\",\"value\":\"278\"},"
				+ "{\"name\":\"idInUserStorage\",\"value\":\"someIdInUserStorage\"},"
				+ "{\"name\":\"idFromLogin\",\"value\":\"someIdFromLogin\"}" + "]"
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
		String userId = "someUserIdNotFound";
		String appToken = "someAppToken";

		response = appTokenEndpoint.getAuthTokenForAppToken(userId, appToken);
		assertResponseStatusIs(Response.Status.NOT_FOUND);
	}

	@Test
	public void testGetAuthTokenForAppTokenNotFound() {
		String userId = "someUserId";
		String appToken = "someAppTokenNotFound";

		response = appTokenEndpoint.getAuthTokenForAppToken(userId, appToken);
		assertResponseStatusIs(Response.Status.NOT_FOUND);
	}

	@Test
	public void testGetAuthTokenForAppTokenErrorFromGatekeeper() {
		GatekeeperTokenProviderErrorSpy gatekeeperTokenProvider = new GatekeeperTokenProviderErrorSpy();
		AppTokenInstanceProvider.setGatekeeperTokenProvider(gatekeeperTokenProvider);

		String userId = "someUserId";
		String appToken = "someAppToken";

		response = appTokenEndpoint.getAuthTokenForAppToken(userId, appToken);
		assertResponseStatusIs(Response.Status.INTERNAL_SERVER_ERROR);
	}

	@Test
	public void testRemoveAuthTokenForUser() {
		String userId = "someUserId";
		String authToken = "someAuthToken";

		response = appTokenEndpoint.removeAuthTokenForAppToken(userId, authToken);
		assertResponseStatusIs(Response.Status.OK);
	}

	@Test
	public void testRemoveAuthTokenForUserWrongToken() {
		GatekeeperTokenProviderErrorSpy gatekeeperTokenProvider = new GatekeeperTokenProviderErrorSpy();
		AppTokenInstanceProvider.setGatekeeperTokenProvider(gatekeeperTokenProvider);

		String userId = "someUserId";
		String authToken = "someAuthTokenNotFound";

		response = appTokenEndpoint.removeAuthTokenForAppToken(userId, authToken);
		assertResponseStatusIs(Response.Status.NOT_FOUND);
	}

}
