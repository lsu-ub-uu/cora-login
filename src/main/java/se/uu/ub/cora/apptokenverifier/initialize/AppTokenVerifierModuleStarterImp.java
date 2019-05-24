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

import java.util.Map;

import se.uu.ub.cora.apptokenstorage.AppTokenStorage;
import se.uu.ub.cora.apptokenstorage.AppTokenStorageProvider;
import se.uu.ub.cora.apptokenstorage.SelectOrder;
import se.uu.ub.cora.gatekeepertokenprovider.GatekeeperTokenProviderImp;
import se.uu.ub.cora.httphandler.HttpHandlerFactory;
import se.uu.ub.cora.httphandler.HttpHandlerFactoryImp;
import se.uu.ub.cora.logger.Logger;
import se.uu.ub.cora.logger.LoggerProvider;

public class AppTokenVerifierModuleStarterImp implements AppTokenVerifierModuleStarter {
	private static final String FOUND = "Found ";
	private Map<String, String> initInfo;
	private Iterable<AppTokenStorageProvider> appTokenStorageProviders;
	private Logger log = LoggerProvider.getLoggerForClass(AppTokenVerifierModuleStarterImp.class);
	private String simpleName = AppTokenVerifierModuleStarterImp.class.getSimpleName();

	@Override
	public void startUsingInitInfoAndAppTokenStorageProviders(Map<String, String> initInfo,
			Iterable<AppTokenStorageProvider> appTokenStorageProviderImplementations) {
		this.initInfo = initInfo;
		this.appTokenStorageProviders = appTokenStorageProviderImplementations;
		start();
	}

	public void start() {
		startAppTokenStorage();
		startGatekeeperTokenProvider();
	}

	private void startAppTokenStorage() {
		AppTokenStorageProvider appTokenStorageProvider = getImplementationBasedOnPreferenceLevelThrowErrorIfNone(
				appTokenStorageProviders, "AppTokenStorageProvider");

		appTokenStorageProvider.startUsingInitInfo(initInfo);
		AppTokenStorage appTokenStorage = appTokenStorageProvider.getAppTokenStorage();

		AppTokenInstanceProvider.setApptokenStorage(appTokenStorage);
	}

	private <T extends SelectOrder> T getImplementationBasedOnPreferenceLevelThrowErrorIfNone(
			Iterable<T> implementations, String interfaceClassName) {
		T implementation = findAndLogPreferedImplementation(implementations, interfaceClassName);
		throwErrorIfNoImplementationFound(interfaceClassName, implementation);
		log.logInfoUsingMessage("Using " + implementation.getClass().getName() + " as "
				+ interfaceClassName + " implementation.");
		return implementation;
	}

	private <T extends SelectOrder> T findAndLogPreferedImplementation(Iterable<T> implementations,
			String interfaceClassName) {
		T implementation = null;
		int preferenceLevel = -99999;
		for (T currentImplementation : implementations) {
			if (preferenceLevel < currentImplementation.getOrderToSelectImplementionsBy()) {
				preferenceLevel = currentImplementation.getOrderToSelectImplementionsBy();
				implementation = currentImplementation;
			}
			log.logInfoUsingMessage(FOUND + currentImplementation.getClass().getName() + " as "
					+ interfaceClassName + " implementation with select order "
					+ currentImplementation.getOrderToSelectImplementionsBy() + ".");
		}
		return implementation;
	}

	private <T extends SelectOrder> void throwErrorIfNoImplementationFound(
			String interfaceClassName, T implementation) {
		if (null == implementation) {
			String errorMessage = "No implementations found for " + interfaceClassName;
			log.logFatalUsingMessage(errorMessage);
			throw new AppTokenVerifierInitializationException(errorMessage);
		}
	}

	private void startGatekeeperTokenProvider() {
		ensureKeyExistsInInitInfo("gatekeeperURL");
		ensureKeyExistsInInitInfo("apptokenVerifierPublicPathToSystem");
		createAndSetGatekeeperTokenProvider();
	}

	private void ensureKeyExistsInInitInfo(String keyName) {
		if (!initInfo.containsKey(keyName)) {
			String message = "Error starting " + simpleName + ", context must have a " + keyName
					+ " set.";
			log.logFatalUsingMessage(message);
			throw new AppTokenVerifierInitializationException(message);
		}
	}

	private void createAndSetGatekeeperTokenProvider() {
		String baseUrl = initInfo.get("gatekeeperURL");
		HttpHandlerFactory httpHandlerFactory = new HttpHandlerFactoryImp();
		AppTokenInstanceProvider.setGatekeeperTokenProvider(GatekeeperTokenProviderImp
				.usingBaseUrlAndHttpHandlerFactory(baseUrl, httpHandlerFactory));
	}
}
