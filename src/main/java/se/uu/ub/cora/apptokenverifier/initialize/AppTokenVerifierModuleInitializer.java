/*
 * Copyright 2019, 2021 Uppsala University Library
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

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import se.uu.ub.cora.gatekeepertokenprovider.GatekeeperTokenProviderImp;
import se.uu.ub.cora.httphandler.HttpHandlerFactory;
import se.uu.ub.cora.httphandler.HttpHandlerFactoryImp;
import se.uu.ub.cora.initialize.SettingsProvider;
import se.uu.ub.cora.logger.Logger;
import se.uu.ub.cora.logger.LoggerProvider;

@WebListener
public class AppTokenVerifierModuleInitializer implements ServletContextListener {
	// private AppTokenVerifierModuleStarter starter = new AppTokenVerifierModuleStarterImp();
	private Logger log = LoggerProvider.getLoggerForClass(AppTokenVerifierModuleInitializer.class);
	private ServletContext servletContext;
	private HashMap<String, String> initInfo = new HashMap<>();
	// private Iterable<AppTokenStorageViewInstanceProvider> appTokenStorageProviderImplementations;
	private String simpleName = AppTokenVerifierModuleInitializer.class.getSimpleName();

	@Override
	public void contextInitialized(ServletContextEvent contextEvent) {
		servletContext = contextEvent.getServletContext();
		initializeAppTokenVerifier();
	}

	private void initializeAppTokenVerifier() {
		log.logInfoUsingMessage(simpleName + " starting...");
		collectInitInformation();
		createAndSetGatekeeperTokenProvider();
		// collectAppTokenStorageProviderImplementations();
		// startAppTokenVerifier();
		log.logInfoUsingMessage(simpleName + " started");
	}

	private void collectInitInformation() {
		Enumeration<String> initParameterNames = servletContext.getInitParameterNames();
		while (initParameterNames.hasMoreElements()) {
			String key = initParameterNames.nextElement();
			initInfo.put(key, servletContext.getInitParameter(key));
		}
		SettingsProvider.setSettings(initInfo);
	}

	private void createAndSetGatekeeperTokenProvider() {
		String baseUrl = SettingsProvider.getSetting("gatekeeperURL");
		HttpHandlerFactory httpHandlerFactory = new HttpHandlerFactoryImp();
		GatekepperInstanceProvider.setGatekeeperTokenProvider(GatekeeperTokenProviderImp
				.usingBaseUrlAndHttpHandlerFactory(baseUrl, httpHandlerFactory));
	}

	// private void ensureInitInfoIsSetInAppTokenInstanceProviderForEndpoint() {
	// GatekepperInstanceProvider.setInitInfo(initInfo);
	// log.logInfoUsingMessage("Using " + initInfo.get("apptokenVerifierPublicPathToSystem")
	// + " as apptokenVerifierPublicPathToSystem.");
	// }

	// private void collectAppTokenStorageProviderImplementations() {
	// appTokenStorageProviderImplementations = ServiceLoader
	// .load(AppTokenStorageViewInstanceProvider.class);
	// }
	//
	// private void startAppTokenVerifier() {
	// starter.startUsingInitInfoAndAppTokenStorageProviders(initInfo,
	// appTokenStorageProviderImplementations);
	// }
	//
	// void setStarter(AppTokenVerifierModuleStarter starter) {
	// // needed for test
	// this.starter = starter;
	// }
	//
	// AppTokenVerifierModuleStarter getStarter() {
	// // needed for test
	// return starter;
	// }

}
