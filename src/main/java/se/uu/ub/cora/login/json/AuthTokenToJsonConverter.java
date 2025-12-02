/*
 * Copyright 2025 Uppsala University Library
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
package se.uu.ub.cora.login.json;

import se.uu.ub.cora.gatekeepertokenprovider.AuthToken;

public interface AuthTokenToJsonConverter {

	/**
	 * convertAuthTokenToJson converts an AuthToken to a json representation of it
	 * 
	 * @param authToken
	 *            An {@link AuthToken} to convert
	 * @param url
	 *            A {@link String} with the externally accessible logoutUrl (url to login module)
	 *            ending in slash
	 * @return A {@link String} representation of the AuthToken
	 */
	String convertAuthTokenToJson(AuthToken authToken, String url);

}