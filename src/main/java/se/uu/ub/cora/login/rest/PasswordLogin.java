/*
 * Copyright 2024 Uppsala University Library
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

import se.uu.ub.cora.gatekeepertokenprovider.AuthToken;

public interface PasswordLogin {
	/**
	 * getAuthToken method returns an {@link AuthToken} given a username and a password. This method
	 * checks if the user is active and if the password matches the already stored one.
	 * 
	 * @throws LoginException
	 *             if any exception while trying to perform the operations inside this method.
	 * 
	 * @param loginId
	 *            A String conatining the loginId.
	 * @param password
	 *            A String containg the password to be matched
	 * @return If the user is active and the password matches, a valid AuthToken is sent back.
	 */
	AuthToken getAuthToken(String loginId, String password);

}
