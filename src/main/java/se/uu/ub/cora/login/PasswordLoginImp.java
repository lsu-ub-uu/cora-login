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
import se.uu.ub.cora.gatekeepertokenprovider.GatekeeperTokenProvider;
import se.uu.ub.cora.gatekeepertokenprovider.UserInfo;
import se.uu.ub.cora.login.initialize.GatekeeperInstanceProvider;
import se.uu.ub.cora.login.rest.LoginException;
import se.uu.ub.cora.login.rest.PasswordLogin;
import se.uu.ub.cora.password.texthasher.TextHasher;

public class PasswordLoginImp implements PasswordLogin {

	private static final String ERROR_MESSAGE = "Login failed.";
	private UserStorageView userStorageView = UserStorageProvider.getStorageView();
	private TextHasher textHasher;

	public PasswordLoginImp(TextHasher textHasher) {
		this.textHasher = textHasher;
	}

	@Override
	public AuthToken getAuthToken(String loginId, String password) {
		try {
			return tryToGetAuthToken(loginId, password);
		} catch (Exception e) {
			throw LoginException.withMessage(ERROR_MESSAGE);
		}
	}

	private AuthToken tryToGetAuthToken(String loginId, String password) {
		User user = userStorageView.getUserByLoginId(loginId);
		ifUserNotActiveThrowException(user);
		ifPasswordDoNotMatchThrowException(password, user);
		return getNewAuthTokenFromGatekeeper(user.id);
	}

	private void ifUserNotActiveThrowException(User user) {
		if (!user.active) {
			throw LoginException.withMessage(ERROR_MESSAGE);
		}
	}

	private void ifPasswordDoNotMatchThrowException(String password, User user) {
		String secret = userStorageView.getSystemSecretById(user.passwordId.get());
		if (!textHasher.matches(password, secret)) {
			throw LoginException.withMessage(ERROR_MESSAGE);
		}
	}

	private AuthToken getNewAuthTokenFromGatekeeper(String userRecordInfoId) {
		GatekeeperTokenProvider gatekeeperTokenProvider = GatekeeperInstanceProvider
				.getGatekeeperTokenProvider();

		return getAUthTokenUsingUserInfo(userRecordInfoId, gatekeeperTokenProvider);
	}

	private AuthToken getAUthTokenUsingUserInfo(String userRecordInfoId,
			GatekeeperTokenProvider gatekeeperTokenProvider) {
		UserInfo userInfo = UserInfo.withIdInUserStorage(userRecordInfoId);
		return gatekeeperTokenProvider.getAuthTokenForUserInfo(userInfo);
	}

	public TextHasher onlyForTestGetTextHasher() {
		return textHasher;
	}
}
