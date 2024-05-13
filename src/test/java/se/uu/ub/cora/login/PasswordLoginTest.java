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

import java.util.LinkedHashSet;
import java.util.Optional;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.gatekeeper.storage.UserStorageProvider;
import se.uu.ub.cora.gatekeeper.storage.UserStorageViewException;
import se.uu.ub.cora.gatekeeper.user.User;
import se.uu.ub.cora.logger.LoggerProvider;
import se.uu.ub.cora.logger.spies.LoggerFactorySpy;
import se.uu.ub.cora.login.spies.UserStorageViewInstanceProviderSpy;
import se.uu.ub.cora.login.spies.UserStorageViewSpy;

public class PasswordLoginTest {

	private static final String SOME_PASSWORD = "somePassword";
	private static final String SOME_USER_ID = "someUserId";
	private PasswordLogin passwordLogin;
	private UserStorageViewInstanceProviderSpy userStorageInstanceProvider;
	private UserStorageViewSpy userStorageView;
	private User user;

	@BeforeMethod
	private void beforeMethod() {
		LoggerFactorySpy loggerFactorySpy = new LoggerFactorySpy();
		LoggerProvider.setLoggerFactory(loggerFactorySpy);

		user = new User(SOME_USER_ID);
		configureUser(user, true, Optional.empty(), "someAppTokenId1", "someAppTokenId2");
		setupBasicUserInStorage(user);

		passwordLogin = new PasswordLoginImp();
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

	@Test
	public void testUserStorageViewCreatedOnInitialization() throws Exception {
		userStorageInstanceProvider.MCR.assertMethodWasCalled("getStorageView");
	}

	@Test(expectedExceptions = LoginException.class, expectedExceptionsMessageRegExp = ""
			+ "Login failed.")
	public void testGetAuthToken_ExceptionWhileGettingAuthToken() throws Exception {
		userStorageView.MRV.setAlwaysThrowException("getUserByIdFromLogin",
				UserStorageViewException.usingMessage("someException"));

		passwordLogin.getAuthToken(SOME_USER_ID, SOME_PASSWORD);
	}

	@Test
	public void testGetAuthToken_CallsGetUser() throws Exception {
		configureUser(user, true, Optional.empty());

		passwordLogin.getAuthToken(SOME_USER_ID, SOME_PASSWORD);

		userStorageView.MCR.assertParameters("getUserByIdFromLogin", 0, SOME_USER_ID);
	}

	@Test(expectedExceptions = LoginException.class, expectedExceptionsMessageRegExp = ""
			+ "Login failed.")
	public void testUserIsNotActive_ThrowLoginException() throws Exception {
		configureUser(user, false, Optional.empty());

		passwordLogin.getAuthToken(SOME_USER_ID, SOME_PASSWORD);
	}

}
