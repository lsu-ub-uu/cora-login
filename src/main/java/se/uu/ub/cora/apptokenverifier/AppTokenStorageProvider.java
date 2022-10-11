/*
 * Copyright 2022 Uppsala University Library
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
package se.uu.ub.cora.apptokenverifier;

/**
 * AppTokenStorageViewInstanceProvider provides view access to search data stored in storage.
 */
public class AppTokenStorageProvider {

	private static AppTokenStorageViewInstanceProvider instanceProvider = new AppTokenStorageViewInstanceProviderImp();

	private AppTokenStorageProvider() {
		// prevent call to constructor
		throw new UnsupportedOperationException();
	}

	/**
	 * getStorageView returns a new AppTokenStorageView that can be used by anything that needs
	 * access search data.
	 * <p>
	 * <i>Code using the returned AppTokenStorageView instance MUST consider the returned instance
	 * as NOT thread safe.</i>
	 * 
	 * @return A AppTokenStorageView that gives access to search data.
	 */
	public static AppTokenStorageView getStorageView() {
		return instanceProvider.getStorageView();
	}

	public static void onlyForTestSetSearchStorageViewInstanceProvider(
			AppTokenStorageViewInstanceProvider instanceProvider) {
		AppTokenStorageProvider.instanceProvider = instanceProvider;

	}

	static AppTokenStorageViewInstanceProvider onlyForTestGetSearchStorageViewInstanceProvider() {
		return instanceProvider;
	}

}
