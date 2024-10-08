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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import se.uu.ub.cora.gatekeepertokenprovider.GatekeeperTokenProvider;
import se.uu.ub.cora.initialize.SettingsProvider;
import se.uu.ub.cora.jsonconverter.converter.AuthToken;
import se.uu.ub.cora.jsonconverter.converter.AuthTokenToJsonConverter;
import se.uu.ub.cora.login.initialize.GatekeeperInstanceProvider;

@Path("/")
public class LoginEndpoint {
	public static final String PATH_TO_SYSTEM = SettingsProvider
			.getSetting("loginPublicPathToSystem");
	private static final String APPLICATION_VND_UUB_RECORD_JSON = "application/vnd.uub.record+json";
	private static final int AFTERHTTP = 10;
	private String url;
	private HttpServletRequest request;

	public LoginEndpoint(@Context HttpServletRequest request) {
		this.request = request;
		url = getBaseURLFromURI();
	}

	private final String getBaseURLFromURI() {
		String baseURL = getBaseURLFromRequest();
		return changeHttpToHttpsIfHeaderSaysSo(baseURL);
	}

	private String getBaseURLFromRequest() {
		String tempUrl = request.getRequestURL().toString();
		String baseURL = tempUrl.substring(0, tempUrl.indexOf('/', AFTERHTTP));
		return baseURL + PATH_TO_SYSTEM + "authToken";
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
	@Consumes("application/vnd.uub.login")
	@Produces(APPLICATION_VND_UUB_RECORD_JSON)
	@Path("apptoken")
	public Response getAuthTokenForAppToken(String credentialsAsString) {
		try {
			return tryToGetAuthTokenForAppToken(credentialsAsString);
		} catch (Exception error) {
			return handleError(error);
		}
	}

	private Response tryToGetAuthTokenForAppToken(String credentialsAsString)
			throws URISyntaxException {
		AppTokenLogin appTokenLogin = LoginDependencyProvider.getAppTokenLogin();
		Credentials credentials = credentialsAsRecord(credentialsAsString);
		AuthToken authToken = getAuthTokenForAppToken(appTokenLogin, credentials);
		return buildResponseUsingAuthToken(authToken);
	}

	private AuthToken getAuthTokenForAppToken(AppTokenLogin appTokenLogin,
			Credentials credentials) {
		se.uu.ub.cora.gatekeepertokenprovider.AuthToken authToken = appTokenLogin
				.getAuthToken(credentials.loginId(), credentials.secret());
		return parseToAuthTokenFromConverter(authToken);
	}

	private AuthToken parseToAuthTokenFromConverter(
			se.uu.ub.cora.gatekeepertokenprovider.AuthToken authToken) {

		return new AuthToken(authToken.token, authToken.validForNoSeconds,
				authToken.idInUserStorage, authToken.loginId,
				Optional.ofNullable(authToken.firstName), Optional.ofNullable(authToken.lastName));
	}

	private Credentials credentialsAsRecord(String credentials) {
		String[] credentialsArray = credentials.split("\n");
		return new Credentials(credentialsArray[0], credentialsArray[1]);
	}

	private record Credentials(String loginId, String secret) {
	}

	Response buildResponseUsingAuthToken(AuthToken authToken) throws URISyntaxException {
		String json = convertAuthTokenToJson(authToken, url);
		URI uri = new URI("authToken/");
		return Response.created(uri).entity(json).build();
	}

	private String convertAuthTokenToJson(AuthToken authToken, String url) {

		AuthTokenToJsonConverter authTokenToJsonConverter = new AuthTokenToJsonConverter(authToken,
				url);
		return authTokenToJsonConverter.convertAuthTokenToJson();
	}

	private Response handleError(Exception error) {
		if (isLoginException(error)) {
			return buildResponseUsingStatus(Response.Status.UNAUTHORIZED);
		}
		return buildResponseUsingStatus(Status.INTERNAL_SERVER_ERROR);
	}

	private boolean isLoginException(Exception error) {
		return error instanceof LoginException;
	}

	private Response buildResponseUsingStatus(Status status) {
		return Response.status(status).build();
	}

	@POST
	@Consumes("application/vnd.uub.login")
	@Produces(APPLICATION_VND_UUB_RECORD_JSON)
	@Path("password")
	public Response getAuthTokenForPassword(String credentialsAsString) {
		try {
			return tryToGetAuthTokenForPassword(credentialsAsString);
		} catch (Exception error) {
			return handleError(error);
		}
	}

	private Response tryToGetAuthTokenForPassword(String credentialsAsString)
			throws URISyntaxException {
		PasswordLogin passwordLogin = LoginDependencyProvider.getPasswordLogin();
		Credentials credentials = credentialsAsRecord(credentialsAsString);
		AuthToken authToken = getAuthTokenForPassword(passwordLogin, credentials);
		return buildResponseUsingAuthToken(authToken);
	}

	private AuthToken getAuthTokenForPassword(PasswordLogin passwordLogin,
			Credentials credentials) {
		se.uu.ub.cora.gatekeepertokenprovider.AuthToken authToken = passwordLogin
				.getAuthToken(credentials.loginId(), credentials.secret());
		return parseToAuthTokenFromConverter(authToken);
	}

	@DELETE
	@Consumes("application/vnd.uub.logout")
	@Path("authToken")
	public Response removeAuthTokenForAppToken(String authToken) {
		try {
			return tryToRemoveAuthTokenForUser(authToken);
		} catch (Exception error) {
			return buildResponseUsingStatus(Response.Status.NOT_FOUND);
		}
	}

	private Response tryToRemoveAuthTokenForUser(String credentialsAsString) {
		GatekeeperTokenProvider gatekeeperTokenProvider = GatekeeperInstanceProvider
				.getGatekeeperTokenProvider();
		Credentials credentials = credentialsAsRecord(credentialsAsString);
		gatekeeperTokenProvider.removeAuthTokenForUser(credentials.loginId(), credentials.secret());
		return buildResponseUsingStatus(Status.OK);
	}
}
