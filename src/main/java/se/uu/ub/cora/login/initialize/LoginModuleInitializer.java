/*
 * Copyright 2019, 2021, 2022 Uppsala University Library
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

import java.util.Enumeration;
import java.util.HashMap;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import se.uu.ub.cora.gatekeeper.storage.UserStorageProvider;
import se.uu.ub.cora.gatekeepertokenprovider.GatekeeperTokenProviderImp;
import se.uu.ub.cora.httphandler.HttpHandlerFactory;
import se.uu.ub.cora.httphandler.HttpHandlerFactoryImp;
import se.uu.ub.cora.initialize.SettingsProvider;
import se.uu.ub.cora.logger.Logger;
import se.uu.ub.cora.logger.LoggerProvider;

@WebListener
public class LoginModuleInitializer implements ServletContextListener {
	private Logger log = LoggerProvider.getLoggerForClass(LoginModuleInitializer.class);
	private ServletContext servletContext;
	private HashMap<String, String> initInfo = new HashMap<>();
	private String simpleName = LoginModuleInitializer.class.getSimpleName();

	@Override
	public void contextInitialized(ServletContextEvent contextEvent) {
		servletContext = contextEvent.getServletContext();
		initializeLogin();
	}

	private void initializeLogin() {
		log.logInfoUsingMessage(simpleName + " starting...");
		collectInitInformation();
		createAndSetGatekeeperTokenProvider();
		makeCallToKnownNeededProvidersToMakeSureTheyStartCorrectlyAtSystemStartup();
		log.logInfoUsingMessage(simpleName + " started");
	}

	private void makeCallToKnownNeededProvidersToMakeSureTheyStartCorrectlyAtSystemStartup() {
		UserStorageProvider.getStorageView();
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
		GatekeeperInstanceProvider.setGatekeeperTokenProvider(GatekeeperTokenProviderImp
				.usingBaseUrlAndHttpHandlerFactory(baseUrl, httpHandlerFactory));
	}
}
