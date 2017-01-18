/*
 * Copyright 2017 Uppsala University Library
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
import javax.ws.rs.core.UriInfo;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.apptokenverifier.AppTokenEndpoint;
import se.uu.ub.cora.apptokenverifier.AppTokenStorage;
import se.uu.ub.cora.apptokenverifier.initialize.AppTokenInstanceProvider;

public class AppTokenEndpointTest {
	private Response response;

	@BeforeMethod
	public void setup() {
		// UserPickerFactorySpy userPickerFactory = new UserPickerFactorySpy();
		// gatekeeper = new AuthenticatorImp(userPicker);
		// GatekeeperImp.INSTANCE.setUserPickerFactory(userPickerFactory);
		Map<String, String> initInfo = new HashMap<>();
		initInfo.put("storageOnDiskBasePath", "/mnt/data/basicstorage");
		AppTokenStorage appTokenStorage = new AppTokenStorageSpy(initInfo);
		AppTokenInstanceProvider.setApptokenStorage(appTokenStorage);
		GatekeeperTokenProviderSpy gatekeeperTokenProvider = new GatekeeperTokenProviderSpy();
		AppTokenInstanceProvider.setGatekeeperTokenProvider(gatekeeperTokenProvider);
	}

	@Test
	public void testGetAuthTokenForAppToken() {
		UriInfo uriInfo = new TestUri();
		AppTokenEndpoint appTokenEndpoint = new AppTokenEndpoint(uriInfo);

		String userId = "someUserId";
		String appToken = "someAppToken";

		response = appTokenEndpoint.getAuthTokenForAppToken(userId, appToken);
		assertResponseStatusIs(Response.Status.CREATED);
		String expectedJsonToken = "{\"children\":["
				+ "{\"name\":\"id\",\"value\":\"someAuthToken\"},"
				+ "{\"name\":\"validForNoSeconds\",\"value\":\"278\"}"
				+ "],\"name\":\"authToken\"}";
		String entity = (String) response.getEntity();
		assertEquals(entity, expectedJsonToken);
	}

	private void assertResponseStatusIs(Status responseStatus) {
		assertEquals(response.getStatusInfo(), responseStatus);
	}

	@Test
	public void testGetAuthTokenForAppTokenUserIdNotFound() {
		UriInfo uriInfo = new TestUri();
		AppTokenEndpoint appTokenEndpoint = new AppTokenEndpoint(uriInfo);

		String userId = "someUserIdNotFound";
		String appToken = "someAppToken";

		response = appTokenEndpoint.getAuthTokenForAppToken(userId, appToken);
		assertResponseStatusIs(Response.Status.NOT_FOUND);
	}

	@Test
	public void testGetAuthTokenForAppTokenNotFound() {
		UriInfo uriInfo = new TestUri();
		AppTokenEndpoint appTokenEndpoint = new AppTokenEndpoint(uriInfo);

		String userId = "someUserId";
		String appToken = "someAppTokenNotFound";

		response = appTokenEndpoint.getAuthTokenForAppToken(userId, appToken);
		assertResponseStatusIs(Response.Status.NOT_FOUND);
	}

	@Test
	public void testGetAuthTokenForAppTokenErrorFromGatekeeper() {
		GatekeeperTokenProviderErrorSpy gatekeeperTokenProvider = new GatekeeperTokenProviderErrorSpy();
		AppTokenInstanceProvider.setGatekeeperTokenProvider(gatekeeperTokenProvider);
		UriInfo uriInfo = new TestUri();
		AppTokenEndpoint appTokenEndpoint = new AppTokenEndpoint(uriInfo);

		String userId = "someUserId";
		String appToken = "someAppToken";

		response = appTokenEndpoint.getAuthTokenForAppToken(userId, appToken);
		assertResponseStatusIs(Response.Status.INTERNAL_SERVER_ERROR);
	}
	// {
	// "data": {
	// "children": [
	// {
	// "name": "id",
	// "value": "someId"
	// },
	// {
	// "name": "validForNoSeconds",
	// "value": "400"
	// }
	// ],
	// "name": "authToken"
	// },
	// "actionLinks": {
	// "delete": {
	// "requestMethod": "DELETE",
	// "rel": "delete",
	// "url":
	// "http://epc.ub.uu.se/apptoken/rest/apptoken/textSystemOne/idTextTextVarText"
	// }
	// }
	// }
}
