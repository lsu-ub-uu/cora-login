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

import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.gatekeeper.storage.UserStorageProvider;
import se.uu.ub.cora.logger.LoggerProvider;
import se.uu.ub.cora.logger.spies.LoggerFactorySpy;
import se.uu.ub.cora.login.rest.AppTokenLogin;
import se.uu.ub.cora.login.spies.TextHasherFactorySpy;
import se.uu.ub.cora.login.spies.UserStorageViewInstanceProviderSpy;
import se.uu.ub.cora.password.texthasher.TextHasherFactory;

public class LoginFactoryTest {

	private LoginFactoryImp loginFactory;
	private TextHasherFactorySpy textHasherFactory;

	@BeforeMethod
	public void setup() {
		textHasherFactory = new TextHasherFactorySpy();

		LoggerFactorySpy loggerFactory = new LoggerFactorySpy();
		LoggerProvider.setLoggerFactory(loggerFactory);
		UserStorageViewInstanceProviderSpy userStorageInstanceProvider = new UserStorageViewInstanceProviderSpy();
		UserStorageProvider
				.onlyForTestSetUserStorageViewInstanceProvider(userStorageInstanceProvider);

		loginFactory = new LoginFactoryImp();
	}

	@Test
	public void testFactorPasswordLogin() throws Exception {
		loginFactory.onlyForTestSetTextHasherFactory(textHasherFactory);

		PasswordLoginImp passwordLogin = (PasswordLoginImp) loginFactory.factorPasswordLogin();

		assertTrue(passwordLogin instanceof PasswordLoginImp);
		textHasherFactory.MCR.assertReturn("factor", 0, passwordLogin.onlyForTestGetTextHasher());
	}

	@Test
	public void testFactorAppTokenLogin() throws Exception {
		loginFactory.onlyForTestSetTextHasherFactory(textHasherFactory);

		AppTokenLogin passwordLogin = loginFactory.factorAppTokenLogin();

		assertTrue(passwordLogin instanceof AppTokenLoginImp);
	}

	@Test
	public void testOnlyForTestGetTextHasherFactory() throws Exception {
		loginFactory.onlyForTestSetTextHasherFactory(textHasherFactory);
		assertSame(loginFactory.onlyForTestGetTextHasherFactory(), textHasherFactory);
	}

	@Test
	public void testOnlyForTestSetTextHasherFactory() throws Exception {
		loginFactory.onlyForTestSetTextHasherFactory(textHasherFactory);
		assertSame(loginFactory.onlyForTestGetTextHasherFactory(), textHasherFactory);
	}

	@Test
	public void testGetImplementingTextHasherFactoryImp() throws Exception {
		assertTrue(loginFactory.onlyForTestGetTextHasherFactory() instanceof TextHasherFactory);
	}
}
