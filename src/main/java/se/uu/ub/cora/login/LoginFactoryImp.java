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

import se.uu.ub.cora.login.rest.AppTokenLogin;
import se.uu.ub.cora.login.rest.LoginFactory;
import se.uu.ub.cora.login.rest.PasswordLogin;
import se.uu.ub.cora.password.texthasher.TextHasher;
import se.uu.ub.cora.password.texthasher.TextHasherFactory;
import se.uu.ub.cora.password.texthasher.TextHasherFactoryImp;

public class LoginFactoryImp implements LoginFactory {

	private TextHasherFactory textHasherFactory;

	public LoginFactoryImp() {
		this.textHasherFactory = new TextHasherFactoryImp();
	}

	@Override
	public PasswordLogin factorPasswordLogin() {
		TextHasher textHasher = textHasherFactory.factor();
		return new PasswordLoginImp(textHasher);
	}

	@Override
	public AppTokenLogin factorAppTokenLogin() {
		return new AppTokenLoginImp();
	}

	public Object onlyForTestGetTextHasherFactory() {
		return textHasherFactory;
	}

	public void onlyForTestSetTextHasherFactory(TextHasherFactory textHasherFactory) {
		this.textHasherFactory = textHasherFactory;
	}
}
