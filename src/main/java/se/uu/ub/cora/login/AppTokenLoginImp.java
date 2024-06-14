/*
 * Copyright 2024 Uppsala University Library
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
package se.uu.ub.cora.login;

import se.uu.ub.cora.gatekeeper.storage.UserStorageProvider;
import se.uu.ub.cora.gatekeeper.storage.UserStorageView;
import se.uu.ub.cora.gatekeeper.user.User;
import se.uu.ub.cora.gatekeepertokenprovider.AuthToken;
import se.uu.ub.cora.login.rest.AppTokenLogin;
import se.uu.ub.cora.login.rest.LoginException;
import se.uu.ub.cora.password.texthasher.TextHasher;

public class AppTokenLoginImp implements AppTokenLogin {

	private static final String ERROR_MESSAGE = "Login failed.";
	private UserStorageView userStorageView = UserStorageProvider.getStorageView();

	public AppTokenLoginImp(TextHasher textHasher) {
		// TODO Auto-generated constructor stub
	}

	@Override
	public AuthToken getAuthToken(String loginId, String appToken) {
		try {
			return tryToGetAuthToken(loginId, appToken);
		} catch (Exception e) {
			throw LoginException.withMessage(ERROR_MESSAGE);
		}
	}

	private AuthToken tryToGetAuthToken(String loginId, String appToken) {
		User user = userStorageView.getUserByIdFromLogin(loginId);
		ifUserNotActiveThrowException(user);
		// ifPasswordDoNotMatchThrowException(password, user);
		// return getNewAuthTokenFromGatekeeper(user.id);
		return null;
	}

	private void ifUserNotActiveThrowException(User user) {
		if (!user.active) {
			throw LoginException.withMessage(ERROR_MESSAGE);
		}
	}

	// ------------------------------------------------------------------------

	// private Response tryToGetAuthTokenForAppToken(String loginId, String appToken)
	// throws URISyntaxException {
	//
	// User user = getUserAndMakeSureIsActive(userRecordId);
	// ensureMatchingAppTokenFromStorage(user.appTokenIds, appToken);
	// AuthToken authToken = getNewAuthTokenFromGatekeeper(userRecordId);
	// return buildResponseUsingAuthToken(authToken);
	// }

	// User getUserAndMakeSureIsActive(String userRecordId) {
	// User user = userStorageView.getUserById(userRecordId);
	// ensureUserIsActive(user);
	// return user;
	// }

	// private void ensureMatchingAppTokenFromStorage(Set<String> appTokenIds,
	// String userTokenString) {
	// boolean matchingTokenFound = tokenStringExistsInStorage(appTokenIds, userTokenString);
	// if (!matchingTokenFound) {
	// throw LoginException.withMessage("No matching token found");
	// }
	// }

	// private boolean tokenStringExistsInStorage(Set<String> appTokenIds, String userTokenString) {
	// for (String appTokenId : appTokenIds) {
	// AppToken appToken = userStorageView.getAppTokenById(appTokenId);
	// if (userTokenString.equals(appToken.tokenString)) {
	// return true;
	// }
	// }
	// return false;
	// }

	// private void ensureUserIsActive(User user) {
	// if (!user.active) {
	// throw LoginException.withMessage("User is not active");
	// }
	// }

	// private AuthToken getNewAuthTokenFromGatekeeper(String userRecordId) {
	// UserInfo userInfo = UserInfo.withIdInUserStorage(userRecordId);
	// GatekeeperTokenProvider gatekeeperTokenProvider = GatekeeperInstanceProvider
	// .getGatekeeperTokenProvider();
	// return gatekeeperTokenProvider.getAuthTokenForUserInfo(userInfo);
	// }

}
