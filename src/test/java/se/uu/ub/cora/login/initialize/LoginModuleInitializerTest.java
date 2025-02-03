/*
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
package se.uu.ub.cora.login.initialize;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import se.uu.ub.cora.gatekeeper.storage.UserStorageProvider;
import se.uu.ub.cora.gatekeepertokenprovider.GatekeeperTokenProviderImp;
import se.uu.ub.cora.httphandler.HttpHandlerFactory;
import se.uu.ub.cora.httphandler.HttpHandlerFactoryImp;
import se.uu.ub.cora.initialize.SettingsProvider;
import se.uu.ub.cora.logger.LoggerProvider;
import se.uu.ub.cora.logger.spies.LoggerFactorySpy;
import se.uu.ub.cora.logger.spies.LoggerSpy;
import se.uu.ub.cora.login.spies.ServletContextSpy;
import se.uu.ub.cora.login.spies.UserStorageViewInstanceProviderSpy;

public class LoginModuleInitializerTest {
	private ServletContext source;
	private ServletContextEvent context;
	private LoginModuleInitializer initializer;
	private LoggerFactorySpy loggerFactorySpy;
	private UserStorageViewInstanceProviderSpy userStorageInstanceProvider;

	@BeforeMethod
	public void beforeMethod() {
		loggerFactorySpy = new LoggerFactorySpy();
		LoggerProvider.setLoggerFactory(loggerFactorySpy);
		resetSettingsProviderAndGatekeeperInstanceToEmptyState();
		userStorageInstanceProvider = new UserStorageViewInstanceProviderSpy();
		UserStorageProvider
				.onlyForTestSetUserStorageViewInstanceProvider(userStorageInstanceProvider);
		source = new ServletContextSpy();
		context = new ServletContextEvent(source);
		setNeededInitParameters();
		initializer = new LoginModuleInitializer();
	}

	@AfterMethod
	public void afterMethod() {
		resetSettingsProviderAndGatekeeperInstanceToEmptyState();
	}

	private void resetSettingsProviderAndGatekeeperInstanceToEmptyState() {
		SettingsProvider.setSettings(new HashMap<>());
		GatekeeperInstanceProvider.setGatekeeperTokenProvider(null);
	}

	private void setNeededInitParameters() {
		source.setInitParameter("initParam1", "initValue1");
		source.setInitParameter("initParam2", "initValue2");
		source.setInitParameter("gatekeeperURL", "/some/gatekeeper/url");
	}

	@Test
	public void testLogMessagesOnStartup() throws Exception {
		initializer.contextInitialized(context);
		loggerFactorySpy.MCR.assertParameters("factorForClass", 0, LoginModuleInitializer.class);
		LoggerSpy loggerSpy = (LoggerSpy) loggerFactorySpy.MCR.getReturnValue("factorForClass", 0);
		loggerSpy.MCR.assertParameters("logInfoUsingMessage", 0,
				"LoginModuleInitializer starting...");
		loggerSpy.MCR.assertParameters("logInfoUsingMessage", 1, "LoginModuleInitializer started");
	}

	@Test
	public void testMakeCallToKnownNeededProvidersToMakeSureTheyStartCorrectlyAtSystemStartup()
			throws Exception {
		initializer.contextInitialized(context);
		userStorageInstanceProvider.MCR.assertMethodWasCalled("getStorageView");
	}

	@Test
	public void testInitParametersArePassedOnToStarter() {
		initializer.contextInitialized(context);
		assertEquals(SettingsProvider.getSetting("initParam1"), "initValue1");
		assertEquals(SettingsProvider.getSetting("initParam2"), "initValue2");
	}

	@Test
	public void testGatekeeperTokenProviderIsSet() throws Exception {
		initializer.contextInitialized(context);
		GatekeeperTokenProviderImp gatekeeperTokenProvider = (GatekeeperTokenProviderImp) GatekeeperInstanceProvider
				.getGatekeeperTokenProvider();
		assertTrue(gatekeeperTokenProvider instanceof GatekeeperTokenProviderImp);
		String gatekeeperUrl = gatekeeperTokenProvider.onlyForTestGetGatekeeperUrl();
		assertEquals(gatekeeperUrl, SettingsProvider.getSetting("gatekeeperURL"));

		HttpHandlerFactory httpHandlerFactory = gatekeeperTokenProvider.onlyForTersGetHttpHandlerFactory();
		assertTrue(httpHandlerFactory instanceof HttpHandlerFactoryImp);
	}
}
