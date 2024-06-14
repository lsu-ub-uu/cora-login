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
package se.uu.ub.cora.login.spies;

import se.uu.ub.cora.login.rest.AppTokenLogin;
import se.uu.ub.cora.login.rest.LoginFactory;
import se.uu.ub.cora.login.rest.PasswordLogin;
import se.uu.ub.cora.testutils.mcr.MethodCallRecorder;
import se.uu.ub.cora.testutils.mrv.MethodReturnValues;

public class LoginFactorySpy implements LoginFactory {

	public MethodCallRecorder MCR = new MethodCallRecorder();
	public MethodReturnValues MRV = new MethodReturnValues();

	public LoginFactorySpy() {
		MCR.useMRV(MRV);
		MRV.setDefaultReturnValuesSupplier("factorPasswordLogin", PasswordLoginSpy::new);
		MRV.setDefaultReturnValuesSupplier("factorAppTokenLogin", AppTokenLoginSpy::new);
	}

	@Override
	public PasswordLogin factorPasswordLogin() {
		return (PasswordLogin) MCR.addCallAndReturnFromMRV();
	}

	@Override
	public AppTokenLogin factorAppTokenLogin() {
		return (AppTokenLogin) MCR.addCallAndReturnFromMRV();
	}

}
