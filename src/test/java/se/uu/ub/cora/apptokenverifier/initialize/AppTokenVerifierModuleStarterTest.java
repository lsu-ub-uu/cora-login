/*
 * Copyright 2019 Olov McKie
 * Copyright 2019, 2022 Uppsala University Library
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
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.apptokenstorage.AppTokenStorageProvider;
import se.uu.ub.cora.apptokenverifier.spy.AppTokenStorageProviderSpy;
import se.uu.ub.cora.gatekeepertokenprovider.GatekeeperTokenProviderImp;
import se.uu.ub.cora.httphandler.HttpHandlerFactory;
import se.uu.ub.cora.httphandler.HttpHandlerFactoryImp;
import se.uu.ub.cora.logger.LoggerProvider;
import se.uu.ub.cora.testspies.logger.LoggerFactorySpy;
import se.uu.ub.cora.testspies.logger.LoggerSpy;

public class AppTokenVerifierModuleStarterTest {

	private Map<String, String> initInfo;
	private List<AppTokenStorageProvider> appTokenStorageProviders;
	private LoggerFactorySpy loggerFactorySpy;
	private Class<AppTokenVerifierModuleStarterImp> testedClass = AppTokenVerifierModuleStarterImp.class;
	private LoggerSpy loggerSpy;

	@BeforeMethod
	public void beforeMethod() {
		loggerFactorySpy = new LoggerFactorySpy();
		LoggerProvider.setLoggerFactory(loggerFactorySpy);
		loggerSpy = new LoggerSpy();
		loggerFactorySpy.MRV.setReturnValues("factorForClass", List.of(loggerSpy), testedClass);
		initInfo = new HashMap<>();
		initInfo.put("guestUserId", "someGuestUserId");
		appTokenStorageProviders = new ArrayList<>();
		appTokenStorageProviders.add(new AppTokenStorageProviderSpy());
		initInfo.put("apptokenVerifierPublicPathToSystem", "/systemone/idplogin/rest/");
		initInfo.put("gatekeeperURL", "http://localhost:8080/gatekeeper/");

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

	@Test
	public void testStartModuleLogsErrorIfNoUserStorageProviderImplementations() throws Exception {
		appTokenStorageProviders.clear();
		startAppTokenVerifierMakeSureAnExceptionIsThrown();
		loggerSpy.MCR.assertParameters("logFatalUsingMessage", 0,
				"No implementations found for AppTokenStorageProvider");
	}

	private Exception startAppTokenVerifierMakeSureAnExceptionIsThrown() {
		Exception caughtException = null;
		try {
			startAppTokenVerifierModuleStarter();
		} catch (Exception e) {
			caughtException = e;
		}
		assertTrue(caughtException instanceof AppTokenVerifierInitializationException);
		assertNotNull(caughtException);
		return caughtException;
	}

	@Test
	public void testStartModuleLogsInfoIfMoreThanOneUserStorageProviderImplementations()
			throws Exception {
		AppTokenStorageProviderSpy spy2 = new AppTokenStorageProviderSpy();
		spy2.MRV.setDefaultReturnValuesSupplier("getOrderToSelectImplementionsBy",
				(Supplier<Integer>) () -> 2);
		appTokenStorageProviders.add(spy2);
		appTokenStorageProviders.add(new AppTokenStorageProviderSpy());

		startAppTokenVerifierModuleStarter();

		loggerSpy.MCR.assertParameters("logInfoUsingMessage", 0,
				"Found se.uu.ub.cora.apptokenverifier.spy.AppTokenStorageProviderSpy as "
						+ "AppTokenStorageProvider implementation with select order 0.");
		loggerSpy.MCR.assertParameters("logInfoUsingMessage", 1,
				"Found se.uu.ub.cora.apptokenverifier.spy.AppTokenStorageProviderSpy as "
						+ "AppTokenStorageProvider implementation with select order 2.");
		loggerSpy.MCR.assertParameters("logInfoUsingMessage", 2,
				"Found se.uu.ub.cora.apptokenverifier.spy.AppTokenStorageProviderSpy as "
						+ "AppTokenStorageProvider implementation with select order 0.");
		loggerSpy.MCR.assertParameters("logInfoUsingMessage", 3,
				"Using se.uu.ub.cora.apptokenverifier.spy.AppTokenStorageProviderSpy as "
						+ "AppTokenStorageProvider implementation.");
	}

	@Test()
	public void testStartModuleInitInfoSentToAppTokenStorageProviderImplementation()
			throws Exception {
		AppTokenStorageProviderSpy appTokenStorageProviderSpy = (AppTokenStorageProviderSpy) appTokenStorageProviders
				.get(0);
		startAppTokenVerifierModuleStarter();
		appTokenStorageProviderSpy.MCR.assertParameters("startUsingInitInfo", 0, initInfo);
	}

	@Test
	public void testGatekeeperInstanceProviderSetUpWithLocator() throws Exception {
		AppTokenStorageProviderSpy appTokenStorageProviderSpy = (AppTokenStorageProviderSpy) appTokenStorageProviders
				.get(0);

		startAppTokenVerifierModuleStarter();

		var appTokenStorageFromProvider = appTokenStorageProviderSpy.MCR
				.getReturnValue("getAppTokenStorage", 0);
		assertSame(AppTokenInstanceProvider.getApptokenStorage(), appTokenStorageFromProvider);
	}

	@Test
	public void testInitializeSystemWithoutGatekeeperURLThrowsErrorAndLogsMessage() {
		initInfo.remove("gatekeeperURL");
		Exception caughtException = startAppTokenVerifierMakeSureAnExceptionIsThrown();
		assertEquals(caughtException.getMessage(),
				"Error starting AppTokenVerifierModuleStarterImp, context must have a gatekeeperURL set.");
		loggerSpy.MCR.assertParameters("logFatalUsingMessage", 0,
				"Error starting AppTokenVerifierModuleStarterImp, context must have a gatekeeperURL set.");
	}

	@Test
	public void testGatekeeperTokenProviderIsSet() {
		GatekeeperTokenProviderImp gatekeeperTokenProvider = (GatekeeperTokenProviderImp) AppTokenInstanceProvider
				.getGatekeeperTokenProvider();
		assertTrue(gatekeeperTokenProvider instanceof GatekeeperTokenProviderImp);
		String gatekeeperUrl = gatekeeperTokenProvider.getGatekeeperUrl();
		assertEquals(gatekeeperUrl, initInfo.get("gatekeeperURL"));

		HttpHandlerFactory httpHandlerFactory = gatekeeperTokenProvider.getHttpHandlerFactory();
		assertTrue(httpHandlerFactory instanceof HttpHandlerFactoryImp);
	}

	@Test
	public void testInitializeSystemWithoutapptokenVerifierPublicPathToSystemThrowsErrorAndLogsMessage() {
		initInfo.remove("apptokenVerifierPublicPathToSystem");
		Exception caughtException = startAppTokenVerifierMakeSureAnExceptionIsThrown();
		assertEquals(caughtException.getMessage(),
				"Error starting AppTokenVerifierModuleStarterImp, "
						+ "context must have a apptokenVerifierPublicPathToSystem set.");
		loggerSpy.MCR.assertParameters("logFatalUsingMessage", 0,
				"Error starting AppTokenVerifierModuleStarterImp, "
						+ "context must have a apptokenVerifierPublicPathToSystem set.");
	}

	@Test
	public void testAppTokenInstanceProviderHasInitInfoSetForEndpoint() throws Exception {
		startAppTokenVerifierModuleStarter();
		assertSame(AppTokenInstanceProvider.getInitInfo(), initInfo);
	}

	@Test
	public void testAppTokenInstanceProviderLogsInitInfoForEndpoint() throws Exception {
		startAppTokenVerifierModuleStarter();

		loggerSpy.MCR.assertParameters("logInfoUsingMessage", 2,
				"Using /systemone/idplogin/rest/ as " + "apptokenVerifierPublicPathToSystem.");
	}
}
