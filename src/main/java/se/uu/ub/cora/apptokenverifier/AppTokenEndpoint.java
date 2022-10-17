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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import se.uu.ub.cora.apptokenverifier.initialize.GatekepperInstanceProvider;
import se.uu.ub.cora.apptokenverifier.json.AuthTokenToJsonConverter;
import se.uu.ub.cora.gatekeeper.storage.UserStorageProvider;
import se.uu.ub.cora.gatekeeper.storage.UserStorageView;
import se.uu.ub.cora.gatekeeper.storage.UserStorageViewException;
import se.uu.ub.cora.gatekeeper.user.AppToken;
import se.uu.ub.cora.gatekeeper.user.User;
import se.uu.ub.cora.gatekeepertokenprovider.AuthToken;
import se.uu.ub.cora.gatekeepertokenprovider.GatekeeperTokenProvider;
import se.uu.ub.cora.gatekeepertokenprovider.UserInfo;
import se.uu.ub.cora.initialize.SettingsProvider;

@Path("apptoken")
public class AppTokenEndpoint {
	public static final String PATH_TO_SYSTEM = SettingsProvider
			.getSetting("apptokenVerifierPublicPathToSystem");
	private static final int AFTERHTTP = 10;
	private String url;
	private HttpServletRequest request;

	public AppTokenEndpoint(@Context HttpServletRequest request) {
		this.request = request;
		url = getBaseURLFromURI();
	}

	private final String getBaseURLFromURI() {
		String baseURL = getBaseURLFromRequest();

		baseURL = changeHttpToHttpsIfHeaderSaysSo(baseURL);

		return baseURL;
	}

	private String getBaseURLFromRequest() {
		String tempUrl = request.getRequestURL().toString();
		String baseURL = tempUrl.substring(0, tempUrl.indexOf('/', AFTERHTTP));
		baseURL += PATH_TO_SYSTEM;

		baseURL += "apptoken/";
		return baseURL;
	}

	private String changeHttpToHttpsIfHeaderSaysSo(String baseURI) {
		String forwardedProtocol = request.getHeader("X-Forwarded-Proto");

		if (ifForwardedProtocolExists(forwardedProtocol)) {
			return baseURI.replace("http:", forwardedProtocol + ":");
		}
		return baseURI;
	}

	private boolean ifForwardedProtocolExists(String forwardedProtocol) {
		return null != forwardedProtocol && !"".equals(forwardedProtocol);
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
		UserStorageView storageView = UserStorageProvider.getStorageView();
		User user = storageView.getUserById(userId);
		ensureUserIsActiveAndHasAtLeastOneAppToken(user);
		ensureMatchingAppTokenFromStorage(storageView, user.appTokenIds, appToken);

	}

	private void ensureMatchingAppTokenFromStorage(UserStorageView storageView,
			Set<String> appTokenIds, String userTokenString) {
		boolean matchingTokenFound = tokenStringExistsInStorage(storageView, appTokenIds,
				userTokenString);
		if (!matchingTokenFound) {
			throw UserStorageViewException.usingMessage("No matching token found");
		}
	}

	private boolean tokenStringExistsInStorage(UserStorageView storageView, Set<String> appTokenIds,
			String userTokenString) {
		for (String appTokenId : appTokenIds) {
			AppToken appToken = storageView.getAppTokenById(appTokenId);
			if (userTokenString.equals(appToken.tokenString)) {
				return true;
			}
		}
		return false;
	}

	private void ensureUserIsActiveAndHasAtLeastOneAppToken(User user) {
		if (!user.active || user.appTokenIds.isEmpty()) {
			throw UserStorageViewException.usingMessage("User is not active");
		}
	}

	private Response getNewAuthTokenFromGatekeeper(String userId) throws URISyntaxException {
		GatekeeperTokenProvider gatekeeperTokenProvider = GatekepperInstanceProvider
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
		if (error instanceof UserStorageViewException) {
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
		GatekeeperTokenProvider gatekeeperTokenProvider = GatekepperInstanceProvider
				.getGatekeeperTokenProvider();
		gatekeeperTokenProvider.removeAuthTokenForUser(userId, authToken);
		return buildResponse(Status.OK);
	}
}
