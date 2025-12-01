/*
 * Copyright 2025 Olov McKie
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
package se.uu.ub.cora.login.json;

import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

import org.testng.annotations.Test;

public class AuthTokenToJsonConverterProviderTest {
	@Test
	public void testPrivateConstructor() throws Exception {
		Constructor<AuthTokenToJsonConverterProvider> constructor = AuthTokenToJsonConverterProvider.class
				.getDeclaredConstructor();
		assertTrue(Modifier.isPrivate(constructor.getModifiers()));

	}

	@Test(expectedExceptions = InvocationTargetException.class)
	public void testPrivateConstructorInvoke() throws Exception {
		Constructor<AuthTokenToJsonConverterProvider> constructor = AuthTokenToJsonConverterProvider.class
				.getDeclaredConstructor();
		assertTrue(Modifier.isPrivate(constructor.getModifiers()));
		constructor.setAccessible(true);
		constructor.newInstance();
	}

	@Test
	public void testGetStandardConverter() {
		AuthTokenToJsonConverter converter = AuthTokenToJsonConverterProvider.getConverter();
		assertTrue(converter instanceof AuthTokenToJsonConverterImp);
	}

	@Test
	public void testOnlyForTestSetConverterSupplier() {
		AuthTokenToJsonConverter authTokenToJsonConverter = (_, _) -> "{ \"fake\": \"json\" }";
		AuthTokenToJsonConverterProvider.onlyForTestSetConverterSupplier(() -> {
			return authTokenToJsonConverter;
		});

		assertSame(AuthTokenToJsonConverterProvider.getConverter(), authTokenToJsonConverter);
		AuthTokenToJsonConverterProvider.resetSupplier();
		assertTrue(AuthTokenToJsonConverterProvider
				.getConverter() instanceof AuthTokenToJsonConverterImp);

	}
}
