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
package se.uu.ub.cora.apptokenverifier.initialize;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import se.uu.ub.cora.apptokenverifier.spy.AppTokenVerifierModuleStarterSpy;
import se.uu.ub.cora.apptokenverifier.spy.ServletContextSpy;
import se.uu.ub.cora.logger.LoggerProvider;
import se.uu.ub.cora.testspies.logger.LoggerFactorySpy;
import se.uu.ub.cora.testspies.logger.LoggerSpy;

public class AppTokenVerifierModuleInitializerTest {
	private ServletContext source;
	private ServletContextEvent context;
	private AppTokenVerifierModuleInitializer initializer;
	private LoggerFactorySpy loggerFactorySpy;
	private Class<AppTokenVerifierModuleInitializer> testedClass = AppTokenVerifierModuleInitializer.class;
	private LoggerSpy loggerSpy;

	@BeforeMethod
	public void beforeMethod() {
		loggerFactorySpy = new LoggerFactorySpy();
		LoggerProvider.setLoggerFactory(loggerFactorySpy);
		loggerSpy = new LoggerSpy();
		loggerFactorySpy.MRV.setReturnValues("factorForClass", List.of(loggerSpy), testedClass);

		source = new ServletContextSpy();
		context = new ServletContextEvent(source);
		initializer = new AppTokenVerifierModuleInitializer();
	}

	@Test
	public void testNonExceptionThrowingStartup() throws Exception {
		setNeededInitParameters();
		AppTokenVerifierModuleStarterSpy starter = startAppTokenVerifierModuleInitializerWithStarterSpy();
		starter.MCR.assertMethodWasCalled("startUsingInitInfoAndAppTokenStorageProviders");
	}

	private AppTokenVerifierModuleStarterSpy startAppTokenVerifierModuleInitializerWithStarterSpy() {
		AppTokenVerifierModuleStarterSpy starter = new AppTokenVerifierModuleStarterSpy();
		initializer.setStarter(starter);
		initializer.contextInitialized(context);
		return starter;
	}

	private void setNeededInitParameters() {
		source.setInitParameter("initParam1", "initValue1");
		source.setInitParameter("initParam2", "initValue2");

	}

	@Test
	public void testLogMessagesOnStartup() throws Exception {
		setNeededInitParameters();
		startAppTokenVerifierModuleInitializerWithStarterSpy();
		loggerSpy.MCR.assertParameters("logInfoUsingMessage", 0,
				"AppTokenVerifierModuleInitializer starting...");
		loggerSpy.MCR.assertParameters("logInfoUsingMessage", 1,
				"AppTokenVerifierModuleInitializer started");
	}

	@Test
	public void testInitParametersArePassedOnToStarter() {
		setNeededInitParameters();
		AppTokenVerifierModuleStarterSpy starter = startAppTokenVerifierModuleInitializerWithStarterSpy();
		Map<String, String> initInfo = (Map<String, String>) starter.MCR
				.getValueForMethodNameAndCallNumberAndParameterName(
						"startUsingInitInfoAndAppTokenStorageProviders", 0, "initInfo");
		assertEquals(initInfo.size(), 2);
	}

	@Test
	public void testUserPickerProviderImplementationsArePassedOnToStarter() {
		setNeededInitParameters();
		AppTokenVerifierModuleStarterSpy starter = startAppTokenVerifierModuleInitializerWithStarterSpy();

		var implementations = starter.MCR.getValueForMethodNameAndCallNumberAndParameterName(
				"startUsingInitInfoAndAppTokenStorageProviders", 0, "implementations");
		assertTrue(implementations instanceof ServiceLoader);
	}

	@Test
	public void testInitUsesDefaultAppTokenVerifierModuleStarter() throws Exception {
		setNeededInitParameters();
		startAndMakeSureErrorIsThrownAsNoImplementationsExistInThisModule();
		AppTokenVerifierModuleStarter starter = initializer.getStarter();
		assertStarterIsGatekeeperModuleStarter(starter);
	}

	private void startAndMakeSureErrorIsThrownAsNoImplementationsExistInThisModule() {
		Exception caughtException = startAndMakeSureErrorIsThrown();
		assertEquals(caughtException.getMessage(),
				"No implementations found for AppTokenStorageProvider");
	}

	private Exception startAndMakeSureErrorIsThrown() {
		Exception caughtException = null;
		try {
			initializer.contextInitialized(context);
		} catch (Exception e) {
			caughtException = e;
		}
		assertTrue(caughtException instanceof AppTokenVerifierInitializationException);
		return caughtException;
	}

	private void assertStarterIsGatekeeperModuleStarter(AppTokenVerifierModuleStarter starter) {
		assertTrue(starter instanceof AppTokenVerifierModuleStarterImp);
	}
}
