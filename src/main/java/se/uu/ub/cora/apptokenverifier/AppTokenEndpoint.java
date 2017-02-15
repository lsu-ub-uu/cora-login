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

import java.net.URI;
import java.net.URISyntaxException;

import javax.ws.rs.DELETE;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import se.uu.ub.cora.apptokenstorage.AppTokenStorage;
import se.uu.ub.cora.apptokenverifier.initialize.AppTokenInstanceProvider;
import se.uu.ub.cora.apptokenverifier.json.AuthTokenToJsonConverter;
import se.uu.ub.cora.gatekeepertokenprovider.AuthToken;
import se.uu.ub.cora.gatekeepertokenprovider.GatekeeperTokenProvider;
import se.uu.ub.cora.gatekeepertokenprovider.UserInfo;

@Path("apptoken")
public class AppTokenEndpoint {

	private UriInfo uriInfo;
	private String url;

	public AppTokenEndpoint(@Context UriInfo uriInfo) {
		this.uriInfo = uriInfo;
		url = getBaseURLFromURI();
	}

	private String getBaseURLFromURI() {
		String baseURI = uriInfo.getBaseUri().toString();
		return baseURI + "apptoken/";
	}

	@POST
	@Path("{userid}")
	public Response getAuthTokenForAppToken(@PathParam("userid") String userId, String appToken) {
		try {
			return tryToGetAuthTokenForAppToken(userId, appToken);
		} catch (Exception error) {
			return handleError(error);
		}
	}

	private Response tryToGetAuthTokenForAppToken(String userId, String appToken)
			throws URISyntaxException {
		checkAppTokenIsValid(userId, appToken);
		return getNewAuthTokenFromGatekeeper(userId);
	}

	private void checkAppTokenIsValid(String userId, String appToken) {
		AppTokenStorage appTokenStorage = AppTokenInstanceProvider.getApptokenStorage();
		if (!appTokenStorage.userIdHasAppToken(userId, appToken)) {
			throw new NotFoundException();
		}
	}

	private Response getNewAuthTokenFromGatekeeper(String userId) throws URISyntaxException {
		GatekeeperTokenProvider gatekeeperTokenProvider = AppTokenInstanceProvider
				.getGatekeeperTokenProvider();

		UserInfo userInfo = UserInfo.withIdInUserStorage(userId);
		AuthToken authTokenForUserInfo = gatekeeperTokenProvider.getAuthTokenForUserInfo(userInfo);
		String json = convertAuthTokenToJson(authTokenForUserInfo, url + userId);
		URI uri = new URI("apptoken/");
		return Response.created(uri).entity(json).build();
	}

	private String convertAuthTokenToJson(AuthToken authTokenForUserInfo, String url) {
		return new AuthTokenToJsonConverter(authTokenForUserInfo, url).convertAuthTokenToJson();
	}

	private Response handleError(Exception error) {
		if (error instanceof NotFoundException) {
			return buildResponse(Response.Status.NOT_FOUND);
		}
		return buildResponse(Status.INTERNAL_SERVER_ERROR);
	}

	private Response buildResponse(Status status) {
		return Response.status(status).build();
	}

	@DELETE
	@Path("{userid}")
	public Response removeAuthTokenForAppToken(@PathParam("userid") String userId,
			String authToken) {
		try {
			return tryToRemoveAuthTokenForUser(userId, authToken);
		} catch (Exception error) {
			return buildResponse(Response.Status.NOT_FOUND);
		}
	}

	private Response tryToRemoveAuthTokenForUser(String userId, String authToken) {
		GatekeeperTokenProvider gatekeeperTokenProvider = AppTokenInstanceProvider
				.getGatekeeperTokenProvider();
		gatekeeperTokenProvider.removeAuthTokenForUser(userId, authToken);
		return buildResponse(Status.OK);
	}
}
