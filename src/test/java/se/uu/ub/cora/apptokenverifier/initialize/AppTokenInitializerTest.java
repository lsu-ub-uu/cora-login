/*
 * Copyright 2016 Uppsala University Library
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
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.apptokenverifier.AppTokenStorageSpy;

public class AppTokenInitializerTest {
	private AppTokenInitializer ApptokenInitializer;
	private ServletContext source;
	private ServletContextEvent context;

	@BeforeMethod
	public void setUp() {
		ApptokenInitializer = new AppTokenInitializer();
		source = new ServletContextSpy();
		context = new ServletContextEvent(source);

	}

	@Test
	public void testInitializeSystem() {
		source.setInitParameter("appTokenStorageClassName",
				"se.uu.ub.cora.apptokenverifier.AppTokenStorageSpy");
		source.setInitParameter("gatekeeperURL", "http://localhost:8080/gatekeeper/");
		ApptokenInitializer.contextInitialized(context);
		assertTrue(AppTokenInstanceProvider.getApptokenStorage() instanceof AppTokenStorageSpy);
	}

	@Test(expectedExceptions = RuntimeException.class)
	public void testInitializeSystemWithoutUserPickerFactoryClassName() {
		source.setInitParameter("gatekeeperURL", "http://localhost:8080/gatekeeper/");
		ApptokenInitializer.contextInitialized(context);
	}

	@Test
	public void testInitializeSystemInitInfoSetInDependencyProvider() {
		source.setInitParameter("appTokenStorageClassName",
				"se.uu.ub.cora.apptokenverifier.AppTokenStorageSpy");
		source.setInitParameter("gatekeeperURL", "http://localhost:8080/gatekeeper/");
		source.setInitParameter("storageOnDiskBasePath", "/mnt/data/basicstorage");
		ApptokenInitializer.contextInitialized(context);

		AppTokenStorageSpy appTokenStorageSpy = (AppTokenStorageSpy) AppTokenInstanceProvider
				.getApptokenStorage();
		assertEquals(appTokenStorageSpy.getInitInfo().get("storageOnDiskBasePath"),
				"/mnt/data/basicstorage");
	}

	@Test
	public void testGatekeeperTokenProviderIsSet() {
		source.setInitParameter("appTokenStorageClassName",
				"se.uu.ub.cora.apptokenverifier.AppTokenStorageSpy");
		source.setInitParameter("gatekeeperURL", "http://localhost:8080/gatekeeper/");
		ApptokenInitializer.contextInitialized(context);
		assertNotNull(AppTokenInstanceProvider.getGatekeeperTokenProvider());
	}

	@Test(expectedExceptions = RuntimeException.class)
	public void testInitializeSystemWithoutGatekeeperURL() {
		source.setInitParameter("appTokenStorageClassName",
				"se.uu.ub.cora.apptokenverifier.AppTokenStorageSpy");
		ApptokenInitializer.contextInitialized(context);
	}

	@Test
	public void testDestroySystem() {
		AppTokenInitializer ApptokenInitializer = new AppTokenInitializer();
		ApptokenInitializer.contextDestroyed(null);
		// TODO: should we do something on destroy?
	}
}
