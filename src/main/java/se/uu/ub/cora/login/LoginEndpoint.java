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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import se.uu.ub.cora.gatekeeper.storage.UserStorageProvider;
import se.uu.ub.cora.gatekeeper.storage.UserStorageView;
import se.uu.ub.cora.gatekeeper.storage.UserStorageViewException;
import se.uu.ub.cora.gatekeeper.user.AppToken;
import se.uu.ub.cora.gatekeeper.user.User;
import se.uu.ub.cora.gatekeepertokenprovider.AuthToken;
import se.uu.ub.cora.gatekeepertokenprovider.GatekeeperTokenProvider;
import se.uu.ub.cora.gatekeepertokenprovider.UserInfo;
import se.uu.ub.cora.initialize.SettingsProvider;
import se.uu.ub.cora.login.initialize.GatekeeperInstanceProvider;
import se.uu.ub.cora.login.json.AuthTokenToJsonConverter;

@Path("/")
public class LoginEndpoint {
	public static final String PATH_TO_SYSTEM = SettingsProvider
			.getSetting("loginPublicPathToSystem");
	private static final String TEXT_PLAIN_CHARSET_UTF_8 = "text/plain; charset=utf-8";
	private static final String APPLICATION_VND_UUB_RECORD_JSON = "application/vnd.uub.record+json";
	private static final int AFTERHTTP = 10;
	private String url;
	private HttpServletRequest request;
	private UserStorageView userStorageView;

	public LoginEndpoint(@Context HttpServletRequest request) {
		this.request = request;
		url = getBaseURLFromURI();
		userStorageView = UserStorageProvider.getStorageView();
	}

	private final String getBaseURLFromURI() {
		String baseURL = getBaseURLFromRequest();

		baseURL = changeHttpToHttpsIfHeaderSaysSo(baseURL);

		return baseURL;
	}

	private String getBaseURLFromRequest() {
		String tempUrl = request.getRequestURL().toString();
		String baseURL = tempUrl.substring(0, tempUrl.indexOf('/', AFTERHTTP));
		return baseURL + PATH_TO_SYSTEM + "authToken/";
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
	@Consumes(TEXT_PLAIN_CHARSET_UTF_8)
	@Produces(APPLICATION_VND_UUB_RECORD_JSON)
	@Path("apptoken/{userId}")
	public Response getAuthTokenForAppToken(@PathParam("userId") String userId, String appToken) {
		try {
			return tryToGetAuthTokenForAppToken(userId, appToken);
		} catch (Exception error) {
			return handleError(error);
		}
	}

	private Response tryToGetAuthTokenForAppToken(String userId, String appToken)
			throws URISyntaxException {
		User user = getUserAndMakeSureIsActive(userId);
		ensureMatchingAppTokenFromStorage(user.appTokenIds, appToken);
		return getNewAuthTokenFromGatekeeper(userId);
	}

	User getUserAndMakeSureIsActive(String userId) {
		User user = userStorageView.getUserById(userId);
		ensureUserIsActive(user);
		return user;
	}

	private void ensureMatchingAppTokenFromStorage(Set<String> appTokenIds,
			String userTokenString) {
		boolean matchingTokenFound = tokenStringExistsInStorage(appTokenIds, userTokenString);
		if (!matchingTokenFound) {
			throw LoginException.withMessage("No matching token found");
		}
	}

	private boolean tokenStringExistsInStorage(Set<String> appTokenIds, String userTokenString) {
		for (String appTokenId : appTokenIds) {
			AppToken appToken = userStorageView.getAppTokenById(appTokenId);
			if (userTokenString.equals(appToken.tokenString)) {
				return true;
			}
		}
		return false;
	}

	private void ensureUserIsActive(User user) {
		if (!user.active) {
			throw LoginException.withMessage("User is not active");
		}
	}

	private Response getNewAuthTokenFromGatekeeper(String userId) throws URISyntaxException {
		GatekeeperTokenProvider gatekeeperTokenProvider = GatekeeperInstanceProvider
				.getGatekeeperTokenProvider();

		UserInfo userInfo = UserInfo.withIdInUserStorage(userId);
		AuthToken authTokenForUserInfo = gatekeeperTokenProvider.getAuthTokenForUserInfo(userInfo);
		String json = convertAuthTokenToJson(authTokenForUserInfo, url + userId);
		URI uri = new URI("authToken/");
		return Response.created(uri).entity(json).build();
	}

	private String convertAuthTokenToJson(AuthToken authTokenForUserInfo, String url) {
		return new AuthTokenToJsonConverter(authTokenForUserInfo, url).convertAuthTokenToJson();
	}

	private Response handleError(Exception error) {
		if (isNotFoundError(error)) {
			return buildResponse(Response.Status.NOT_FOUND);
		}
		return buildResponse(Status.INTERNAL_SERVER_ERROR);
	}

	private boolean isNotFoundError(Exception error) {
		return error instanceof UserStorageViewException || error instanceof LoginException;
	}

	private Response buildResponse(Status status) {
		return Response.status(status).build();
	}

	@POST
	// @Consumes(TEXT_PLAIN_CHARSET_UTF_8)
	@Produces(APPLICATION_VND_UUB_RECORD_JSON)
	@Path("password/{userId}")
	public Response getAuthTokenForPassword(@PathParam("userId") String userId, String password) {
		User user = getUserAndMakeSureIsActive(userId);
		try {
			throwExcpetionIfNoPasswordMatch(password, user);
			// createNewAuthToken
			return null;
		} catch (Exception error) {
			return handleError(error);
		}
	}

	private void throwExcpetionIfNoPasswordMatch(String password, User user) {
		if (!userStorageView.doesPasswordMatchForUser(user, password)) {
			throw LoginException.withMessage("Password do not match");
		}
	}

	@DELETE
	@Consumes(TEXT_PLAIN_CHARSET_UTF_8)
	@Path("authToken/{userId}")
	public Response removeAuthTokenForAppToken(@PathParam("userId") String userId,
			String authToken) {
		try {
			return tryToRemoveAuthTokenForUser(userId, authToken);
		} catch (Exception error) {
			return buildResponse(Response.Status.NOT_FOUND);
		}
	}

	private Response tryToRemoveAuthTokenForUser(String userId, String authToken) {
		GatekeeperTokenProvider gatekeeperTokenProvider = GatekeeperInstanceProvider
				.getGatekeeperTokenProvider();
		gatekeeperTokenProvider.removeAuthTokenForUser(userId, authToken);
		return buildResponse(Status.OK);
	}
}
