/*
 * Copyright 2018 Uppsala University Library
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

package se.uu.ub.cora.apptokenverifier.spies;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import se.uu.ub.cora.apptokenverifier.AppTokenStorageView;

public class AppTokenStorageThrowsInvocationExceptionSpy implements AppTokenStorageView {
	private Map<String, String> initInfo;

	public AppTokenStorageThrowsInvocationExceptionSpy(Map<String, String> initInfo)
			throws InvocationTargetException {
		throw new InvocationTargetException(new Throwable(),
				"Invocation exception from AppTokenStorageThrowsInvocationExceptionSpy");
	}

	public Map<String, String> getInitInfo() {
		return initInfo;
	}

	@Override
	public boolean userIdHasAppToken(String userId, String appToken) {
		return false;
	}
}
