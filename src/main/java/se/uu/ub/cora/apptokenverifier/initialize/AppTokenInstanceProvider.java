/*
 * Copyright 2017, 2018 Uppsala University Library
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
import se.uu.ub.cora.gatekeepertokenprovider.GatekeeperTokenProvider;

public final class AppTokenInstanceProvider {

	private static AppTokenStorage appTokenStorage;
	private static GatekeeperTokenProvider gatekeeperTokenProvider;
	private static Map<String, String> initInfo;

	private AppTokenInstanceProvider() {
		// not called
		throw new UnsupportedOperationException();
	}

	public static void setApptokenStorage(AppTokenStorage appTokenStorage) {
		AppTokenInstanceProvider.appTokenStorage = appTokenStorage;

	}

	public static AppTokenStorage getApptokenStorage() {
		return appTokenStorage;
	}

	public static void setGatekeeperTokenProvider(GatekeeperTokenProvider gatekeeperTokenProvider) {
		AppTokenInstanceProvider.gatekeeperTokenProvider = gatekeeperTokenProvider;

	}

	public static GatekeeperTokenProvider getGatekeeperTokenProvider() {
		return gatekeeperTokenProvider;
	}

	public static void setInitInfo(Map<String, String> initInfo) {
		AppTokenInstanceProvider.initInfo = initInfo;
	}

	public static Map<String, String> getInitInfo() {
		return initInfo;
	}

}
