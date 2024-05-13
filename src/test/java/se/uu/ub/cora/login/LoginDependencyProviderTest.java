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

import static org.testng.Assert.assertTrue;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class LoginDependencyProviderTest {

	@BeforeMethod
	private void beforeMethod() {
	}

	@Test
	public void testGetPasswordLoginReturnsPasswordLoginImp() throws Exception {
		PasswordLogin passwordLogin = LoginDependencyProvider.getPasswordLogin();

		assertTrue(passwordLogin instanceof PasswordLoginImp);
	}

	@Test
	public void testOnlyForTestSetLoginFactoryReturnsLoginsFromSetFactory() throws Exception {
		LoginFactorySpy loginFactorySpy = new LoginFactorySpy();
		LoginDependencyProvider.onlyForTestSetLoginFactory(loginFactorySpy);

		PasswordLogin passwordLogin = LoginDependencyProvider.getPasswordLogin();

		loginFactorySpy.MCR.assertReturn("factorPasswordLogin", 0, passwordLogin);
	}

	@Test
	public void testGetAppTokenLoginReturnsPasswordLoginImp() throws Exception {
		AppTokenLogin appTokenLogin = LoginDependencyProvider.getAppTokenLogin();

		assertTrue(appTokenLogin instanceof AppTokenLoginImp);
	}

	@Test
	public void testOnlyForTestSetLoginFactoryReturnsLoginsFromSetFactoryAppToken()
			throws Exception {
		LoginFactorySpy loginFactorySpy = new LoginFactorySpy();
		LoginDependencyProvider.onlyForTestSetLoginFactory(loginFactorySpy);

		AppTokenLogin appTokenLogin = LoginDependencyProvider.getAppTokenLogin();

		loginFactorySpy.MCR.assertReturn("factorAppTokenLogin", 0, appTokenLogin);
	}
}
