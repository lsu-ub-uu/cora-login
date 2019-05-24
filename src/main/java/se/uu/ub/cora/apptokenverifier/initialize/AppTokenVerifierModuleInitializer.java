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

import java.util.Enumeration;
import java.util.HashMap;
import java.util.ServiceLoader;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import se.uu.ub.cora.apptokenstorage.AppTokenStorageProvider;
import se.uu.ub.cora.logger.Logger;
import se.uu.ub.cora.logger.LoggerProvider;

public class AppTokenVerifierModuleInitializer implements ServletContextListener {
	private AppTokenVerifierModuleStarter starter = new AppTokenVerifierModuleStarterImp();
	private Logger log = LoggerProvider.getLoggerForClass(AppTokenVerifierModuleInitializer.class);
	private ServletContext servletContext;
	private HashMap<String, String> initInfo = new HashMap<>();
	private Iterable<AppTokenStorageProvider> appTokenStorageProviderImplementations;
	private String simpleName = AppTokenVerifierModuleInitializer.class.getSimpleName();

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		servletContext = arg0.getServletContext();
		initializeAppTokenVerifier();
	}

	private void initializeAppTokenVerifier() {
		log.logInfoUsingMessage(simpleName + " starting...");
		collectInitInformation();
		// ensureKeyExistsInInitInfo("gatekeeperURL");
		// ensureKeyExistsInInitInfo("apptokenVerifierPublicPathToSystem");
		collectUserPickerProviderImplementations();
		startAppTokenVerifier();
		log.logInfoUsingMessage(simpleName + " started");
	}

	private void collectInitInformation() {
		Enumeration<String> initParameterNames = servletContext.getInitParameterNames();
		while (initParameterNames.hasMoreElements()) {
			String key = initParameterNames.nextElement();
			initInfo.put(key, servletContext.getInitParameter(key));
		}
	}

	// private void ensureKeyExistsInInitInfo(String keyName) {
	// if (!initInfo.containsKey(keyName)) {
	// String message = "Error starting " + simpleName + ", context must have a " + keyName
	// + " set.";
	// log.logFatalUsingMessage(message);
	// throw new AppTokenVerifierInitializationException(message);
	// }
	// }

	private void collectUserPickerProviderImplementations() {
		appTokenStorageProviderImplementations = ServiceLoader.load(AppTokenStorageProvider.class);
	}

	private void startAppTokenVerifier() {
		starter.startUsingInitInfoAndAppTokenStorageProviders(initInfo,
				appTokenStorageProviderImplementations);
	}

	void setStarter(AppTokenVerifierModuleStarter starter) {
		// needed for test
		this.starter = starter;
	}

	AppTokenVerifierModuleStarter getStarter() {
		// needed for test
		return starter;
	}

}
