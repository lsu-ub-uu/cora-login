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

import static org.testng.Assert.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.gatekeeper.storage.UserStorageProvider;
import se.uu.ub.cora.gatekeeper.storage.UserStorageViewInstanceProvider;
import se.uu.ub.cora.logger.LoggerFactory;
import se.uu.ub.cora.logger.LoggerProvider;
import se.uu.ub.cora.logger.spies.LoggerFactorySpy;
import se.uu.ub.cora.login.AppTokenLoginImp;
import se.uu.ub.cora.login.LoginFactoryImp;
import se.uu.ub.cora.login.PasswordLoginImp;
import se.uu.ub.cora.login.spies.LoginFactorySpy;
import se.uu.ub.cora.login.spies.UserStorageViewInstanceProviderSpy;
import se.uu.ub.cora.password.texthasher.TextHasher;
import se.uu.ub.cora.password.texthasher.TextHasherFactory;

public class LoginDependencyProviderTest {

	@BeforeMethod
	private void beforeMethod() {
		LoggerFactory loggerFactory = new LoggerFactorySpy();
		LoggerProvider.setLoggerFactory(loggerFactory);

		UserStorageViewInstanceProvider instanceProvider = new UserStorageViewInstanceProviderSpy();
		UserStorageProvider.onlyForTestSetUserStorageViewInstanceProvider(instanceProvider);

		LoginDependencyProvider.onlyForTestSetLoginFactory(new LoginFactoryImp());
	}

	@Test(expectedExceptions = InvocationTargetException.class)
	public void testPrivateConstructorInvoke() throws Exception {
		Constructor<LoginDependencyProvider> constructor = LoginDependencyProvider.class
				.getDeclaredConstructor();
		constructor.setAccessible(true);
		constructor.newInstance();
	}

	@Test
	public void testOnlyForTestGetLoginFactory() throws Exception {
		assertTrue(LoginDependencyProvider.onlyForTestGetLoginFactory() instanceof LoginFactory);
	}

	@Test
	public void testLoginFactoryHasTextHasherFactory() throws Exception {
		LoginFactoryImp loginFactory = (LoginFactoryImp) LoginDependencyProvider
				.onlyForTestGetLoginFactory();

		assertTrue(loginFactory.onlyForTestGetTextHasherFactory() instanceof TextHasherFactory);
	}

	@Test
	public void testGetPasswordLoginReturnsPasswordLoginImp() throws Exception {
		PasswordLoginImp passwordLogin = (PasswordLoginImp) LoginDependencyProvider
				.getPasswordLogin();

		assertTrue(passwordLogin.onlyForTestGetTextHasher() instanceof TextHasher);
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
