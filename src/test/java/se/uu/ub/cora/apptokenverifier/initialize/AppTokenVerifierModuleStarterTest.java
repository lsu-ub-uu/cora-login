/*
 * Copyright 2019 Olov McKie
 * Copyright 2019 Uppsala University Library
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
package se.uu.ub.cora.apptokenverifier.initialize;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.apptokenstorage.AppTokenStorage;
import se.uu.ub.cora.apptokenstorage.AppTokenStorageProvider;

public class AppTokenVerifierModuleStarterTest {

	private Map<String, String> initInfo;
	private List<AppTokenStorageProvider> appTokenStorageProviders;

	@BeforeMethod
	public void beforeMethod() {
		initInfo = new HashMap<>();
		initInfo.put("guestUserId", "someGuestUserId");
		appTokenStorageProviders = new ArrayList<>();
		appTokenStorageProviders.add(new AppTokenStorageProviderSpy());

	}

	private void startAppTokenVerifierModuleStarter() {
		AppTokenVerifierModuleStarter starter = new AppTokenVerifierModuleStarterImp();
		starter.startUsingInitInfoAndAppTokenStorageProviders(initInfo, appTokenStorageProviders);
	}

	@Test(expectedExceptions = AppTokenVerifierInitializationException.class, expectedExceptionsMessageRegExp = ""
			+ "No implementations found for AppTokenStorageProvider")
	public void testStartModuleThrowsErrorIfNoUserStorageImplementations() throws Exception {
		appTokenStorageProviders.clear();
		startAppTokenVerifierModuleStarter();
	}

	@Test(expectedExceptions = AppTokenVerifierInitializationException.class, expectedExceptionsMessageRegExp = ""
			+ "More than one implementation found for AppTokenStorageProvider")
	public void testStartModuleThrowsErrorIfMoreThanOneUserStorageImplementations()
			throws Exception {
		appTokenStorageProviders.add(new AppTokenStorageProviderSpy());
		startAppTokenVerifierModuleStarter();
	}

	@Test()
	public void testStartModuleInitInfoSentToAppTokenStorageProviderImplementation()
			throws Exception {
		AppTokenStorageProviderSpy appTokenStorageProviderSpy = (AppTokenStorageProviderSpy) appTokenStorageProviders
				.get(0);
		startAppTokenVerifierModuleStarter();
		assertSame(appTokenStorageProviderSpy.initInfo, initInfo);
	}

	// @Test(expectedExceptions = GatekeeperInitializationException.class,
	// expectedExceptionsMessageRegExp = ""
	// + "No implementations found for UserPickerProvider")
	// public void testStartModuleThrowsErrorIfNoUserPickerProviderImplementations()
	// throws Exception {
	// userPickerProviders.clear();
	// startGatekeeperModuleStarter();
	// }

	// @Test(expectedExceptions = GatekeeperInitializationException.class,
	// expectedExceptionsMessageRegExp = ""
	// + "More than one implementation found for UserPickerProvider")
	// public void
	// testStartModuleThrowsErrorIfMoreThanOneUserPickerProviderImplementations()
	// throws Exception {
	// userPickerProviders.add(new UserPickerProviderSpy(null));
	// startGatekeeperModuleStarter();
	// }

	// @Test(expectedExceptions = AppTokenVerifierInitializationException.class,
	// expectedExceptionsMessageRegExp = ""
	// + "InitInfo must contain guestUserId")
	// public void testStartModuleThrowsErrorIfMissingGuestUserId() throws Exception
	// {
	// initInfo.clear();
	// startAppTokenVerifierModuleStarter();
	// }

	// @Test()
	// public void testStartModuleGuestUserIdSentToUserPickerImplementation() throws
	// Exception {
	// UserPickerProviderSpy userPickerProviderSpy = (UserPickerProviderSpy)
	// userPickerProviders
	// .get(0);
	// startGatekeeperModuleStarter();
	// assertEquals(userPickerProviderSpy.guestUserId(), "someGuestUserId");
	// }

	@Test
	public void testStartModuleAppTokenStorageSentToUserPickerImplementation() throws Exception {
		// UserPickerProviderSpy userPickerProviderSpy = (UserPickerProviderSpy)
		// userPickerProviders
		// .get(0);
		AppTokenStorageProviderSpy userStorageProviderSpy = (AppTokenStorageProviderSpy) appTokenStorageProviders
				.get(0);
		AppTokenStorage userStorage = userStorageProviderSpy.getAppTokenStorage();
		startAppTokenVerifierModuleStarter();
		assertEquals(userStorageProviderSpy.getAppTokenStorage(), userStorage);
	}

	@Test
	public void testGatekeeperInstanceProviderSetUpWithLocator() throws Exception {
		AppTokenStorageProviderSpy appTokenStorageProviderSpy = (AppTokenStorageProviderSpy) appTokenStorageProviders
				.get(0);
		AppTokenStorage appTokenStorage = appTokenStorageProviderSpy.getAppTokenStorage();
		startAppTokenVerifierModuleStarter();
		assertSame(AppTokenInstanceProvider.getApptokenStorage(), appTokenStorage);
	}
}
