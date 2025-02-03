/// *
// * Copyright 2017, 2024 Uppsala University Library
// *
// * This file is part of Cora.
// *
// * Cora is free software: you can redistribute it and/or modify
// * it under the terms of the GNU General Public License as published by
// * the Free Software Foundation, either version 3 of the License, or
// * (at your option) any later version.
// *
// * Cora is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// * GNU General Public License for more details.
// *
// * You should have received a copy of the GNU General Public License
// * along with Cora. If not, see <http://www.gnu.org/licenses/>.
// */
//
// package se.uu.ub.cora.login.spies;
//
// import se.uu.ub.cora.gatekeepertokenprovider.AuthToken;
// import se.uu.ub.cora.gatekeepertokenprovider.GatekeeperTokenProvider;
// import se.uu.ub.cora.gatekeepertokenprovider.UserInfo;
// import se.uu.ub.cora.gatekeepertokenprovider.authentication.AuthenticationException;
//
// public class GatekeeperTokenProviderErrorSpy implements GatekeeperTokenProvider {
//
// @Override
// public AuthToken getAuthTokenForUserInfo(UserInfo userInfo) {
// throw new AuthenticationException("authToken gives no authorization");
// }
//
// @Override
// public void removeAuthToken(String tokenId, String authToken) {
// throw new AuthenticationException("authToken could not be removed");
// }
//
// @Override
// public AuthToken renewAuthToken(String tokenId, String token) {
// // TODO Auto-generated method stub
// return null;
// }
//
// }
