/*
 * Copyright 2017 Uppsala University Library
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
import static org.testng.Assert.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.Test;

import se.uu.ub.cora.apptokenstorage.AppTokenStorage;
import se.uu.ub.cora.apptokenverifier.AppTokenStorageSpy;
import se.uu.ub.cora.apptokenverifier.GatekeeperTokenProviderSpy;
import se.uu.ub.cora.gatekeepertokenprovider.GatekeeperTokenProvider;

public class AppTokenInstanceProviderTest {
	@Test
	public void testPrivateConstructor() throws Exception {
		Constructor<AppTokenInstanceProvider> constructor = AppTokenInstanceProvider.class
				.getDeclaredConstructor();
		assertTrue(Modifier.isPrivate(constructor.getModifiers()));
	}

	@Test(expectedExceptions = InvocationTargetException.class)
	public void testPrivateConstructorInvoke() throws Exception {
		Constructor<AppTokenInstanceProvider> constructor = AppTokenInstanceProvider.class
				.getDeclaredConstructor();
		assertTrue(Modifier.isPrivate(constructor.getModifiers()));
		constructor.setAccessible(true);
		constructor.newInstance();
	}

	@Test
	public void testApptokenStorage() {
		Map<String, String> initInfo = new HashMap<>();
		initInfo.put("storageOnDiskBasePath", "/mnt/data/basicstorage");
		AppTokenStorage apptokenStorageSpy = new AppTokenStorageSpy(initInfo);
		AppTokenInstanceProvider.setApptokenStorage(apptokenStorageSpy);
		assertEquals(AppTokenInstanceProvider.getApptokenStorage(), apptokenStorageSpy);
	}

	@Test
	public void testGatekeeperTokenProvider() {
		GatekeeperTokenProvider gatekeeperTokenProvider = new GatekeeperTokenProviderSpy();
		AppTokenInstanceProvider.setGatekeeperTokenProvider(gatekeeperTokenProvider);
		assertEquals(AppTokenInstanceProvider.getGatekeeperTokenProvider(),
				gatekeeperTokenProvider);
	}

}
