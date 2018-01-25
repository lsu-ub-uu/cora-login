/*
 * Copyright 2016, 2018 Uppsala University Library
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
	private AppTokenInitializer apptokenInitializer;
	private ServletContext source;
	private ServletContextEvent context;

	@BeforeMethod
	public void setUp() {
		apptokenInitializer = new AppTokenInitializer();
		source = new ServletContextSpy();
		context = new ServletContextEvent(source);

	}

	@Test
	public void testInitializeSystem() {
		setNeededInitParameters();
		assertTrue(AppTokenInstanceProvider.getApptokenStorage() instanceof AppTokenStorageSpy);
	}

	private void setNeededInitParameters() {
		source.setInitParameter("appTokenStorageClassName",
				"se.uu.ub.cora.apptokenverifier.AppTokenStorageSpy");
		source.setInitParameter("gatekeeperURL", "http://localhost:8080/gatekeeper/");
		source.setInitParameter("storageOnDiskBasePath", "/mnt/data/basicstorage");
		source.setInitParameter("apptokenVerifierPublicPathToSystem", "/systemone/idplogin/rest/");
		apptokenInitializer.contextInitialized(context);
	}

	@Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = "Error "
			+ "starting AppTokenVerifier: Context must have a appTokenStorageClassName set.")
	public void testInitializeSystemWithoutUserPickerFactoryClassName() {
		source.setInitParameter("gatekeeperURL", "http://localhost:8080/gatekeeper/");
		source.setInitParameter("storageOnDiskBasePath", "/mnt/data/basicstorage");
		source.setInitParameter("apptokenVerifierPublicPathToSystem", "/systemone/idplogin/rest/");
		apptokenInitializer.contextInitialized(context);
	}

	@Test
	public void testInitializeSystemInitInfoSetInDependencyProvider() {
		setNeededInitParameters();
		AppTokenStorageSpy appTokenStorageSpy = (AppTokenStorageSpy) AppTokenInstanceProvider
				.getApptokenStorage();
		assertEquals(appTokenStorageSpy.getInitInfo().get("storageOnDiskBasePath"),
				"/mnt/data/basicstorage");
	}

	@Test
	public void testGatekeeperTokenProviderIsSet() {
		setNeededInitParameters();
		assertNotNull(AppTokenInstanceProvider.getGatekeeperTokenProvider());
	}

	@Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = "Error "
			+ "starting AppTokenVerifier: Context must have a gatekeeperURL set.")
	public void testInitializeSystemWithoutGatekeeperURL() {
		source.setInitParameter("appTokenStorageClassName",
				"se.uu.ub.cora.apptokenverifier.AppTokenStorageSpy");
		source.setInitParameter("storageOnDiskBasePath", "/mnt/data/basicstorage");
		source.setInitParameter("apptokenVerifierPublicPathToSystem", "/systemone/idplogin/rest/");
		apptokenInitializer.contextInitialized(context);
	}

	@Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = "Error "
			+ "starting AppTokenVerifier: Context must have a storageOnDiskBasePath set.")
	public void testInitializeSystemWithoutStorageOnDiskBasePath() {
		source.setInitParameter("appTokenStorageClassName",
				"se.uu.ub.cora.apptokenverifier.AppTokenStorageSpy");
		source.setInitParameter("gatekeeperURL", "http://localhost:8080/gatekeeper/");
		source.setInitParameter("apptokenVerifierPublicPathToSystem", "/systemone/idplogin/rest/");
		apptokenInitializer.contextInitialized(context);
	}

	@Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = "Error "
			+ "starting AppTokenVerifier: Context must have a apptokenVerifierPublicPathToSystem set.")
	public void testInitializeSystemWithoutapptokenVerifierPublicPathToSystem() {
		source.setInitParameter("appTokenStorageClassName",
				"se.uu.ub.cora.apptokenverifier.AppTokenStorageSpy");
		source.setInitParameter("gatekeeperURL", "http://localhost:8080/gatekeeper/");
		source.setInitParameter("storageOnDiskBasePath", "/mnt/data/basicstorage");
		apptokenInitializer.contextInitialized(context);
	}

	@Test
	public void testInitInfoSetApptokenVerifierInstanceProvider() throws Exception {
		setNeededInitParameters();
		assertEquals(AppTokenInstanceProvider.getInitInfo().get("apptokenVerifierPublicPathToSystem"),
				"/systemone/idplogin/rest/");
	}

	@Test
	public void testDestroySystem() {
		AppTokenInitializer ApptokenInitializer = new AppTokenInitializer();
		ApptokenInitializer.contextDestroyed(null);
		// TODO: should we do something on destroy?
	}
}
