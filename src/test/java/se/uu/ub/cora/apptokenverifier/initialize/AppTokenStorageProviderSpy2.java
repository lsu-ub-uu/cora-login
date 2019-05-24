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

import java.util.Map;

import se.uu.ub.cora.apptokenstorage.AppTokenStorage;
import se.uu.ub.cora.apptokenstorage.AppTokenStorageProvider;
import se.uu.ub.cora.apptokenverifier.AppTokenStorageSpy;

public class AppTokenStorageProviderSpy2 implements AppTokenStorageProvider {

	Map<String, String> initInfo;
	private AppTokenStorageSpy appTokenStorageSpy = new AppTokenStorageSpy(initInfo);

	@Override
	public AppTokenStorage getAppTokenStorage() {
		return appTokenStorageSpy;
	}

	@Override
	public void startUsingInitInfo(Map<String, String> initInfo) {
		this.initInfo = initInfo;
	}

	@Override
	public int getOrderToSelectImplementionsBy() {
		return 2;
	}

}
