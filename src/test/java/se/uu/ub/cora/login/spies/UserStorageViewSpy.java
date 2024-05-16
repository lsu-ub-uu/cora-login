/*
 * Copyright 2022, 2024 Uppsala University Library
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
package se.uu.ub.cora.login.spies;

import se.uu.ub.cora.gatekeeper.storage.UserStorageView;
import se.uu.ub.cora.gatekeeper.user.AppToken;
import se.uu.ub.cora.gatekeeper.user.User;
import se.uu.ub.cora.testutils.mcr.MethodCallRecorder;
import se.uu.ub.cora.testutils.mrv.MethodReturnValues;

public class UserStorageViewSpy implements UserStorageView {
	public MethodCallRecorder MCR = new MethodCallRecorder();
	public MethodReturnValues MRV = new MethodReturnValues();

	User user = new User("idFromSpy");
	AppToken apptoken = new AppToken("idFromSpy", "tokenStringFromSpy");

	public UserStorageViewSpy() {
		MCR.useMRV(MRV);
		MRV.setDefaultReturnValuesSupplier("getUserById", () -> user);
		MRV.setDefaultReturnValuesSupplier("getUserByIdFromLogin", () -> user);
		MRV.setDefaultReturnValuesSupplier("getAppTokenById", () -> apptoken);
		MRV.setDefaultReturnValuesSupplier("getSystemSecretById", () -> "someSystemSecret");
	}

	@Override
	public User getUserById(String recordId) {
		return (User) MCR.addCallAndReturnFromMRV("recordId", recordId);
	}

	@Override
	public User getUserByIdFromLogin(String idFromLogin) {
		return (User) MCR.addCallAndReturnFromMRV("idFromLogin", idFromLogin);
	}

	@Override
	public AppToken getAppTokenById(String tokenId) {
		return (AppToken) MCR.addCallAndReturnFromMRV("tokenId", tokenId);
	}

	@Override
	public String getSystemSecretById(String systemSecretId) {
		return (String) MCR.addCallAndReturnFromMRV("systemSecretId", systemSecretId);
	}
}