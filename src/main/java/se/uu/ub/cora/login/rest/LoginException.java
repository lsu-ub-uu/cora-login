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

public class LoginException extends RuntimeException {

	private static final long serialVersionUID = 2241064467145940402L;

	public static LoginException withMessage(String message) {
		return new LoginException(message);
	}

	public static LoginException withMessageAndException(String message, Exception exception) {
		return new LoginException(message, exception);
	}

	private LoginException(String message) {
		super(message);
	}

	private LoginException(String message, Exception exception) {
		super(message, exception);
	}
}
