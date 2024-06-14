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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.util.LinkedHashSet;
import java.util.Optional;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.gatekeeper.storage.UserStorageProvider;
import se.uu.ub.cora.gatekeeper.storage.UserStorageViewException;
import se.uu.ub.cora.gatekeeper.user.User;
import se.uu.ub.cora.logger.LoggerProvider;
import se.uu.ub.cora.logger.spies.LoggerFactorySpy;
import se.uu.ub.cora.login.rest.AppTokenLogin;
import se.uu.ub.cora.login.rest.LoginException;
import se.uu.ub.cora.login.spies.TextHasherSpy;
import se.uu.ub.cora.login.spies.UserStorageViewInstanceProviderSpy;
import se.uu.ub.cora.login.spies.UserStorageViewSpy;

public class AppTokenLoginTest {

	private AppTokenLogin apptokenLogin;
	private static final String SOME_LOGIN_ID = "someLoginId";
	private static final String SOME_APP_TOKEN = "someAppToken";
	private static final String SOME_SYSTEM_SECRET_ID = "someSystemSecretId";
	private User user;
	private TextHasherSpy textHasher;
	private UserStorageViewInstanceProviderSpy userStorageInstanceProvider;
	private UserStorageViewSpy userStorageView;

	@BeforeMethod
	private void beforeMethod() {
		LoggerFactorySpy loggerFactorySpy = new LoggerFactorySpy();
		LoggerProvider.setLoggerFactory(loggerFactorySpy);

		textHasher = new TextHasherSpy();
		user = new User("someRecordInfoId");
		configureUser(user, true, Optional.of(SOME_SYSTEM_SECRET_ID), "someAppTokenId1",
				"someAppTokenId2");
		setupBasicUserInStorage(user);

		apptokenLogin = new AppTokenLoginImp(textHasher);
	}

	private User configureUser(User user, boolean active, Optional<String> passwordId,
			String... appTokens) {
		user.active = active;
		user.passwordId = passwordId;
		user.appTokenIds = new LinkedHashSet<>();
		for (String appToken : appTokens) {
			user.appTokenIds.add(appToken);
		}
		return user;
	}

	private void setupBasicUserInStorage(User user) {
		userStorageView = new UserStorageViewSpy();
		userStorageInstanceProvider = new UserStorageViewInstanceProviderSpy();
		userStorageView.MRV.setDefaultReturnValuesSupplier("getUserByIdFromLogin", () -> user);
		userStorageInstanceProvider.MRV.setDefaultReturnValuesSupplier("getStorageView",
				() -> userStorageView);

		UserStorageProvider
				.onlyForTestSetUserStorageViewInstanceProvider(userStorageInstanceProvider);
	}

	@Test(expectedExceptions = LoginException.class, expectedExceptionsMessageRegExp = ""
			+ "Login failed.")
	public void testGetAuthToken_ExceptionWhileGettingAuthToken() throws Exception {
		userStorageView.MRV.setAlwaysThrowException("getUserByIdFromLogin",
				UserStorageViewException.usingMessage("someException"));

		apptokenLogin.getAuthToken(SOME_LOGIN_ID, SOME_APP_TOKEN);
	}

	@Test
	public void testGetAuthToken_CallsGetUser() throws Exception {
		textHasher.MRV.setDefaultReturnValuesSupplier("matches", () -> true);

		apptokenLogin.getAuthToken(SOME_LOGIN_ID, SOME_APP_TOKEN);

		userStorageView.MCR.assertParameters("getUserByIdFromLogin", 0, SOME_LOGIN_ID);
	}

	@Test
	public void testUserIsNotActive_ThrowLoginException() throws Exception {
		configureUser(user, false, Optional.of(SOME_SYSTEM_SECRET_ID));
		try {
			apptokenLogin.getAuthToken(SOME_LOGIN_ID, SOME_APP_TOKEN);
			fail("It should throw an exception");
		} catch (Exception e) {
			assertTrue(e instanceof LoginException);
			assertEquals(e.getMessage(), "Login failed.");
			userStorageView.MCR.assertMethodNotCalled("getSystemSecretById");
		}
	}

	// TODO: make sure we have set up correct data for appToken in configureUser()
	// @Test(expectedExceptions = LoginException.class, expectedExceptionsMessageRegExp = ""
	// + "Login failed.")
	// public void testNoSystemSecretInStorage() throws Exception {
	// configureUser(user, true, Optional.empty());
	//
	// apptokenLogin.getAuthToken(SOME_LOGIN_ID, SOME_APP_TOKEN);
	// }

}
