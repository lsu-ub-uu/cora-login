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
import se.uu.ub.cora.gatekeepertokenprovider.AuthToken;
import se.uu.ub.cora.gatekeepertokenprovider.GatekeeperTokenProvider;
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
	@Consumes(TEXT_PLAIN_CHARSET_UTF_8)
	@Produces(APPLICATION_VND_UUB_RECORD_JSON)
	@Path("apptoken/{loginId}")
	public Response getAuthTokenForAppToken(@PathParam("loginId") String userRecordId,
			String appToken) {
		try {
			return tryToGetAuthTokenForAppToken(userRecordId, appToken);
		} catch (Exception error) {
			return handleError(error);
		}
	}

	private Response tryToGetAuthTokenForAppToken(String loginId, String appToken)
			throws URISyntaxException {
		AppTokenLogin appTokenLogin = LoginDependencyProvider.getAppTokenLogin();
		AuthToken authToken = appTokenLogin.getAuthToken(loginId, appToken);
		return buildResponseUsingAuthToken(authToken);
	}

	Response buildResponseUsingAuthToken(AuthToken authToken) throws URISyntaxException {
		String json = convertAuthTokenToJson(authToken, url + authToken.idInUserStorage);
		URI uri = new URI("authToken/");
		return Response.created(uri).entity(json).build();
	}

	private String convertAuthTokenToJson(AuthToken authTokenForUserInfo, String url) {
		AuthTokenToJsonConverter authTokenToJsonConverter = new AuthTokenToJsonConverter(
				authTokenForUserInfo, url);
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
	@Consumes(TEXT_PLAIN_CHARSET_UTF_8)
	@Produces(APPLICATION_VND_UUB_RECORD_JSON)
	@Path("password/{loginId}")
	public Response getAuthTokenForPassword(@PathParam("loginId") String loginId, String password) {
		try {
			return tryToGetAuthTokenForPassword(loginId, password);
		} catch (Exception error) {
			return handleError(error);
		}
	}

	private Response tryToGetAuthTokenForPassword(String loginId, String password)
			throws URISyntaxException {
		PasswordLogin passwordLogin = LoginDependencyProvider.getPasswordLogin();
		AuthToken authToken = passwordLogin.getAuthToken(loginId, password);
		return buildResponseUsingAuthToken(authToken);
	}

	@DELETE
	@Consumes(TEXT_PLAIN_CHARSET_UTF_8)
	@Path("authToken/{loginId}")
	public Response removeAuthTokenForAppToken(@PathParam("loginId") String loginId,
			String authToken) {
		try {
			return tryToRemoveAuthTokenForUser(loginId, authToken);
		} catch (Exception error) {
			return buildResponseUsingStatus(Response.Status.NOT_FOUND);
		}
	}

	private Response tryToRemoveAuthTokenForUser(String loginId, String authToken) {
		GatekeeperTokenProvider gatekeeperTokenProvider = GatekeeperInstanceProvider
				.getGatekeeperTokenProvider();
		gatekeeperTokenProvider.removeAuthTokenForUser(loginId, authToken);
		return buildResponseUsingStatus(Status.OK);
	}
}
