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

import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.testng.annotations.AfterMethod;
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

public class LoginDependencyProviderTest {

	@BeforeMethod
	private void beforeMethod() {
		LoggerFactory loggerFactory = new LoggerFactorySpy();
		LoggerProvider.setLoggerFactory(loggerFactory);

		UserStorageViewInstanceProvider instanceProvider = new UserStorageViewInstanceProviderSpy();
		UserStorageProvider.onlyForTestSetUserStorageViewInstanceProvider(instanceProvider);
	}

	@AfterMethod
	private void afterMethod() {
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
	public void testLoginFactoryIsImpAsDefault() throws Exception {
		assertTrue(LoginDependencyProvider.onlyForTestGetLoginFactory() instanceof LoginFactoryImp);
	}

	@Test
	public void testGetPasswordLoginReturnsPasswordLoginImp() throws Exception {
		assertTrue(LoginDependencyProvider.getPasswordLogin() instanceof PasswordLoginImp);
	}

	@Test
	public void testGetAppTokenLoginReturnsPasswordLoginImp() throws Exception {
		assertTrue(LoginDependencyProvider.getAppTokenLogin() instanceof AppTokenLoginImp);
	}

	@Test
	public void testOnlyForTestSetLoginFactoryAndOnlyForTestGetLoginFactory() throws Exception {
		LoginFactorySpy loginFactory = new LoginFactorySpy();
		LoginDependencyProvider.onlyForTestSetLoginFactory(loginFactory);

		assertSame(loginFactory, LoginDependencyProvider.onlyForTestGetLoginFactory());
	}
}
