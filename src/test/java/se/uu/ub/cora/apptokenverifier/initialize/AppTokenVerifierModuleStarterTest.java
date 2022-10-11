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
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.apptokenstorage.AppTokenStorageViewInstanceProvider;
import se.uu.ub.cora.apptokenverifier.AppTokenStorageView;
import se.uu.ub.cora.apptokenverifier.spies.AppTokenStorageProviderSpy;
import se.uu.ub.cora.apptokenverifier.spies.AppTokenStorageProviderSpy2;
import se.uu.ub.cora.gatekeepertokenprovider.GatekeeperTokenProviderImp;
import se.uu.ub.cora.httphandler.HttpHandlerFactory;
import se.uu.ub.cora.httphandler.HttpHandlerFactoryImp;
import se.uu.ub.cora.logger.LoggerProvider;
import se.uu.ub.cora.logger.spies.LoggerFactorySpy;
import se.uu.ub.cora.logger.spies.LoggerSpy;

public class AppTokenVerifierModuleStarterTest {

	private Map<String, String> initInfo;
	private List<AppTokenStorageViewInstanceProvider> appTokenStorageProviders;
	private LoggerFactorySpy loggerFactorySpy;

	@BeforeMethod
	public void beforeMethod() {
		loggerFactorySpy = new LoggerFactorySpy();
		LoggerProvider.setLoggerFactory(loggerFactorySpy);
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

		LoggerSpy loggerSpy = getLoggerSpy();
		loggerSpy.MCR.assertParameters("logFatalUsingMessage", 0,
				"No implementations found for AppTokenStorageProvider");
	}

	private LoggerSpy getLoggerSpy() {
		loggerFactorySpy.MCR.assertParameters("factorForClass", 0,
				AppTokenVerifierModuleStarterImp.class);
		LoggerSpy loggerSpy = (LoggerSpy) loggerFactorySpy.MCR.getReturnValue("factorForClass", 0);
		return loggerSpy;
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
		appTokenStorageProviders.add(new AppTokenStorageProviderSpy2());
		appTokenStorageProviders.add(new AppTokenStorageProviderSpy());
		startAppTokenVerifierModuleStarter();

		LoggerSpy loggerSpy = getLoggerSpy();
		loggerSpy.MCR.assertParameters("logInfoUsingMessage", 0,
				"Found se.uu.ub.cora.apptokenverifier.spies.AppTokenStorageProviderSpy as "
						+ "AppTokenStorageProvider implementation with select order 0.");
		loggerSpy.MCR.assertParameters("logInfoUsingMessage", 1,
				"Found se.uu.ub.cora.apptokenverifier.spies.AppTokenStorageProviderSpy2 as "
						+ "AppTokenStorageProvider implementation with select order 2.");
		loggerSpy.MCR.assertParameters("logInfoUsingMessage", 2,
				"Found se.uu.ub.cora.apptokenverifier.spies.AppTokenStorageProviderSpy as "
						+ "AppTokenStorageProvider implementation with select order 0.");
		loggerSpy.MCR.assertParameters("logInfoUsingMessage", 3,
				"Using se.uu.ub.cora.apptokenverifier.spies.AppTokenStorageProviderSpy2 as "
						+ "AppTokenStorageProvider implementation.");
	}

	@Test()
	public void testStartModuleInitInfoSentToAppTokenStorageProviderImplementation()
			throws Exception {
		AppTokenStorageProviderSpy appTokenStorageProviderSpy = (AppTokenStorageProviderSpy) appTokenStorageProviders
				.get(0);
		startAppTokenVerifierModuleStarter();
		assertSame(appTokenStorageProviderSpy.initInfo, initInfo);
	}

	@Test
	public void testStartModuleAppTokenStorageSentToUserPickerImplementation() throws Exception {
		AppTokenStorageProviderSpy userStorageProviderSpy = (AppTokenStorageProviderSpy) appTokenStorageProviders
				.get(0);
		AppTokenStorageView userStorage = userStorageProviderSpy.getAppTokenStorage();
		startAppTokenVerifierModuleStarter();
		assertEquals(userStorageProviderSpy.getAppTokenStorage(), userStorage);
	}

	@Test
	public void testGatekeeperInstanceProviderSetUpWithLocator() throws Exception {
		AppTokenStorageProviderSpy appTokenStorageProviderSpy = (AppTokenStorageProviderSpy) appTokenStorageProviders
				.get(0);
		AppTokenStorageView appTokenStorage = appTokenStorageProviderSpy.getAppTokenStorage();
		startAppTokenVerifierModuleStarter();
		assertSame(AppTokenInstanceProvider.getApptokenStorage(), appTokenStorage);
	}

	@Test
	public void testInitializeSystemWithoutGatekeeperURLThrowsErrorAndLogsMessage() {
		initInfo.remove("gatekeeperURL");
		Exception caughtException = startAppTokenVerifierMakeSureAnExceptionIsThrown();
		assertEquals(caughtException.getMessage(),
				"Error starting AppTokenVerifierModuleStarterImp, context must have a gatekeeperURL set.");
		LoggerSpy loggerSpy = getLoggerSpy();
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
				"Error starting AppTokenVerifierModuleStarterImp, context must have a apptokenVerifierPublicPathToSystem set.");
		LoggerSpy loggerSpy = getLoggerSpy();
		loggerSpy.MCR.assertParameters("logFatalUsingMessage", 0,
				"Error starting AppTokenVerifierModuleStarterImp, context must have a apptokenVerifierPublicPathToSystem set.");
	}

	@Test
	public void testAppTokenInstanceProviderHasInitInfoSetForEndpoint() throws Exception {
		startAppTokenVerifierModuleStarter();
		assertSame(AppTokenInstanceProvider.getInitInfo(), initInfo);
	}

	@Test
	public void testAppTokenInstanceProviderLogsInitInfoForEndpoint() throws Exception {
		startAppTokenVerifierModuleStarter();
		LoggerSpy loggerSpy = getLoggerSpy();
		loggerSpy.MCR.assertParameters("logInfoUsingMessage", 2,
				"Using /systemone/idplogin/rest/ as apptokenVerifierPublicPathToSystem.");
	}
}
