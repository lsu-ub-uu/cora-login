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
package se.uu.ub.cora.apptokenverifier.texthasher;

import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * TextHasherArgon2 is an implementation of TextHasher using springframeworks Argon2PasswordEncoder.
 */
public class TextHasherArgon2 implements TextHasher {
	PasswordEncoder argonEncoder;

	public TextHasherArgon2() {

		int saltLength = 256 / 8;
		int hashLength = 512 / 8;
		int parallelism = 1;
		int memoryInKb = 400 * 1024;
		int iterations = 2;

		argonEncoder = createEncoder(saltLength, hashLength, parallelism, memoryInKb, iterations);
	}

	Argon2PasswordEncoder createEncoder(int saltLength, int hashLength, int parallelism,
			int memoryInKb, int iterations) {
		return new Argon2PasswordEncoder(saltLength, hashLength, parallelism, memoryInKb,
				iterations);
	}

	@Override
	public String hashText(String plainText) {
		return argonEncoder.encode(plainText);
	}

	@Override
	public boolean matches(String plainText, String hashedText) {
		return argonEncoder.matches(plainText, hashedText);
	}
}
