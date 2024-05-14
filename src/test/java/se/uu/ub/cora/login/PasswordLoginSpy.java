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
package se.uu.ub.cora.login;

import se.uu.ub.cora.gatekeepertokenprovider.AuthToken;
import se.uu.ub.cora.testutils.mcr.MethodCallRecorder;
import se.uu.ub.cora.testutils.mrv.MethodReturnValues;

public class PasswordLoginSpy implements PasswordLogin {
	public MethodCallRecorder MCR = new MethodCallRecorder();
	public MethodReturnValues MRV = new MethodReturnValues();

	public AuthToken authToken = AuthToken
			.withIdAndValidForNoSecondsAndIdInUserStorageAndIdFromLogin("someAuthToken", 278,
					"someIdInUserStorage", "someIdFromLogin");

	public PasswordLoginSpy() {
		MCR.useMRV(MRV);
		MRV.setDefaultReturnValuesSupplier("getAuthToken", () -> authToken);
	}

	@Override
	public AuthToken getAuthToken(String idFromLogin, String password) {
		return (AuthToken) MCR.addCallAndReturnFromMRV("idFromLogin", idFromLogin, "password",
				password);
	}

}
