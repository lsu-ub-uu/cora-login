/*
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
import static org.testng.Assert.assertTrue;

import java.util.Map;
import java.util.ServiceLoader;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.apptokenstorage.AppTokenStorageProvider;
import se.uu.ub.cora.apptokenverifier.log.LoggerFactorySpy;
import se.uu.ub.cora.logger.LoggerProvider;

public class AppTokenVerifierModuleInitializerTest {
	private ServletContext source;
	private ServletContextEvent context;
	private AppTokenVerifierModuleInitializer initializer;
	private LoggerFactorySpy loggerFactorySpy;
	private String testedClassName = "AppTokenVerifierModuleInitializer";

	@BeforeMethod
	public void beforeMethod() {
		loggerFactorySpy = new LoggerFactorySpy();
		LoggerProvider.setLoggerFactory(loggerFactorySpy);
		source = new ServletContextSpy();
		// source.setInitParameter("initParam1", "initValue1");
		// source.setInitParameter("initParam2", "initValue2");
		context = new ServletContextEvent(source);
		initializer = new AppTokenVerifierModuleInitializer();
	}

	@Test
	public void testNonExceptionThrowingStartup() throws Exception {
		setNeededInitParameters();
		AppTokenVerifierModuleStarterSpy starter = startAppTokenVerifierModuleInitializerWithStarterSpy();
		assertTrue(starter.startWasCalled);
	}

	private AppTokenVerifierModuleStarterSpy startAppTokenVerifierModuleInitializerWithStarterSpy() {
		AppTokenVerifierModuleStarterSpy starter = new AppTokenVerifierModuleStarterSpy();
		initializer.setStarter(starter);
		initializer.contextInitialized(context);
		return starter;
	}

	private void setNeededInitParameters() {
		// source.setInitParameter("gatekeeperURL", "http://localhost:8080/gatekeeper/");
		// source.setInitParameter("apptokenVerifierPublicPathToSystem",
		// "/systemone/idplogin/rest/");
		source.setInitParameter("initParam1", "initValue1");
		source.setInitParameter("initParam2", "initValue2");

	}

	@Test
	public void testLogMessagesOnStartup() throws Exception {
		setNeededInitParameters();
		startAppTokenVerifierModuleInitializerWithStarterSpy();
		assertEquals(loggerFactorySpy.getInfoLogMessageUsingClassNameAndNo(testedClassName, 0),
				"AppTokenVerifierModuleInitializer starting...");
		assertEquals(loggerFactorySpy.getInfoLogMessageUsingClassNameAndNo(testedClassName, 1),
				"AppTokenVerifierModuleInitializer started");
	}

	@Test
	public void testInitParametersArePassedOnToStarter() {
		setNeededInitParameters();
		AppTokenVerifierModuleStarterSpy starter = startAppTokenVerifierModuleInitializerWithStarterSpy();
		Map<String, String> initInfo = starter.initInfo;
		assertEquals(initInfo.size(), 2);
		// assertEquals(initInfo.get("gatekeeperURL"), "http://localhost:8080/gatekeeper/");
		// assertEquals(initInfo.get("apptokenVerifierPublicPathToSystem"),
		// "/systemone/idplogin/rest/");
		source.setInitParameter("initParam1", "initValue1");
		source.setInitParameter("initParam2", "initValue2");
	}

	// @Test
	// public void testInitializeSystemWithoutGatekeeperURLThrowsErrorAndLogsMessage() {
	// source.setInitParameter("apptokenVerifierPublicPathToSystem", "/systemone/idplogin/rest/");
	// Exception caughtException = startAndMakeSureErrorIsThrown();
	// assertEquals(caughtException.getMessage(),
	// "Error starting AppTokenVerifierModuleInitializer, context must have a gatekeeperURL set.");
	// assertEquals(loggerFactorySpy.getFatalLogMessageUsingClassNameAndNo(testedClassName, 0),
	// "Error starting AppTokenVerifierModuleInitializer, context must have a gatekeeperURL set.");
	// }
	//
	// @Test
	// public void
	// testInitializeSystemWithoutapptokenVerifierPublicPathToSystemThrowsErrorAndLogsMessage() {
	// source.setInitParameter("gatekeeperURL", "http://localhost:8080/gatekeeper/");
	// Exception caughtException = startAndMakeSureErrorIsThrown();
	// assertEquals(caughtException.getMessage(),
	// "Error starting AppTokenVerifierModuleInitializer, context must have a
	// apptokenVerifierPublicPathToSystem set.");
	// assertEquals(loggerFactorySpy.getFatalLogMessageUsingClassNameAndNo(testedClassName, 0),
	// "Error starting AppTokenVerifierModuleInitializer, context must have a
	// apptokenVerifierPublicPathToSystem set.");
	// }

	@Test
	public void testUserPickerProviderImplementationsArePassedOnToStarter() {
		setNeededInitParameters();
		AppTokenVerifierModuleStarterSpy starter = startAppTokenVerifierModuleInitializerWithStarterSpy();

		Iterable<AppTokenStorageProvider> iterable = starter.appTokenStorageProviderImplementations;
		assertTrue(iterable instanceof ServiceLoader);
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
