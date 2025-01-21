/*
 * Copyright 2017, 2018, 2021, 2022, 2024, 2025 Uppsala University Library
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

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import se.uu.ub.cora.gatekeepertokenprovider.AuthToken;
import se.uu.ub.cora.gatekeepertokenprovider.GatekeeperTokenProvider;
import se.uu.ub.cora.initialize.SettingsProvider;
import se.uu.ub.cora.login.initialize.GatekeeperInstanceProvider;
import se.uu.ub.cora.login.json.AuthTokenToJsonConverter;

@Path("/")
public class LoginEndpoint {
	public static final String PATH_TO_SYSTEM = SettingsProvider
			.getSetting("loginPublicPathToSystem");
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
	@Path("apptoken")
	@Consumes("application/vnd.uub.login")
	@Produces("application/vnd.uub.authentication+json")
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
		return appTokenLogin.getAuthToken(credentials.loginId(), credentials.secret());
	}

	private Credentials credentialsAsRecord(String credentials) {
		String[] credentialsArray = credentials.split("\n");
		return new Credentials(credentialsArray[0], credentialsArray[1]);
	}

	private record Credentials(String loginId, String secret) {
	}

	Response buildResponseUsingAuthToken(AuthToken authToken) throws URISyntaxException {
		String json = convertAuthTokenToJson(authToken, url + authToken.tokenId());
		URI uri = new URI("authToken/" + authToken.tokenId());
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
	@Path("password")
	@Consumes("application/vnd.uub.login")
	@Produces("application/vnd.uub.authentication+json")
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
		return passwordLogin.getAuthToken(credentials.loginId(), credentials.secret());
	}

	@POST
	@Path("authToken/{tokenId}")
	@Produces("application/vnd.uub.authentication+json")
	public Response renewAuthToken(@HeaderParam("authToken") String token,
			@PathParam("tokenId") String tokenId) {
		try {
			return tryToRenewAuthToken(tokenId, token);
		} catch (Exception error) {
			return buildResponseUsingStatus(Response.Status.UNAUTHORIZED);
		}
	}

	private Response tryToRenewAuthToken(String tokenId, String token) {
		GatekeeperTokenProvider gatekeeperTokenProvider = GatekeeperInstanceProvider
				.getGatekeeperTokenProvider();
		AuthToken renewedAuthToken = gatekeeperTokenProvider.renewAuthToken(tokenId, token);
		return buildResponseOKUsingAuthToken(renewedAuthToken);
	}

	Response buildResponseOKUsingAuthToken(AuthToken authToken) {
		String json = convertAuthTokenToJson(authToken, url + authToken.tokenId());
		return Response.ok().entity(json).build();
	}

	@DELETE
	@Path("authToken/{tokenId}")
	public Response removeAuthTokenForAppToken(@HeaderParam("authToken") String token,
			@PathParam("tokenId") String tokenId) {
		try {
			return tryToRemoveAuthToken(tokenId, token);
		} catch (Exception error) {
			return buildResponseUsingStatus(Response.Status.NOT_FOUND);
		}
	}

	private Response tryToRemoveAuthToken(String tokenId, String token) {
		GatekeeperTokenProvider gatekeeperTokenProvider = GatekeeperInstanceProvider
				.getGatekeeperTokenProvider();
		gatekeeperTokenProvider.removeAuthToken(tokenId, token);
		return buildResponseUsingStatus(Status.OK);
	}
}
