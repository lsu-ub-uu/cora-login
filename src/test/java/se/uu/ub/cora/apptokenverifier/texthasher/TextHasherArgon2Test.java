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

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;

import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.testng.annotations.Test;

import se.uu.ub.cora.testutils.mcr.MethodCallRecorder;

public class TextHasherArgon2Test {
	// https://en.wikipedia.org/wiki/Argon2
	// https://en.wikipedia.org/wiki/Key_derivation_function

	@Test
	public void testDefaultCreateEncoder() throws Exception {
		TextHasherExtendedForTest hasher = new TextHasherExtendedForTest();
		hasher.MCR.assertParameters("createEncoder", 0, 32, 64, 1, 409600, 2);
	}

	@Test
	public void testDefaultCreateEncoderCreatesAnArcon2PasswordEncoder() throws Exception {
		TextHasherExtendedForTest hasher = new TextHasherExtendedForTest();
		Argon2PasswordEncoder encoder = (Argon2PasswordEncoder) hasher.getArgon2PasswordEncoder();

		hasher.MCR.assertReturn("createEncoder", 0, encoder);
	}

	@Test
	public void testSpyEncoderEncodeTextIsReturned() throws Exception {
		TextHasherExtendedForTest hasher = new TextHasherExtendedForTest();

		hasher.createPasswordEncoderSpy();
		PasswordEncoderSpy encoder = (PasswordEncoderSpy) hasher.getArgon2PasswordEncoder();

		String plainText = "someText";
		String hashedText = hasher.hashText(plainText);
		encoder.MCR.assertReturn("encode", 0, hashedText);
	}

	@Test
	public void testDefaultWithEncode() throws Exception {
		TextHasherArgon2 hasher = new TextHasherArgon2();
		String hashedText = hasher.hashText("someText");
		String hashedText2 = hasher.hashText("someText");
		assertTrue(hashedText.startsWith("$argon2id$v=19$m=409600,t=2,p=1$"));
		assertTrue(hashedText2.startsWith("$argon2id$v=19$m=409600,t=2,p=1$"));
		assertNotEquals(hashedText, hashedText2);
	}

	@Test
	public void testDefaultEncoderMatches() throws Exception {
		TextHasher hasher = new TextHasherArgon2();

		String hashedText = hasher.hashText("someText");

		boolean match = hasher.matches("someText", hashedText);
		assertTrue(match);
		boolean match2 = hasher.matches("otherText", hashedText);
		assertFalse(match2);
	}

	@Test
	public void testSpyMatchesIsSentToEncoder() throws Exception {
		TextHasherExtendedForTest hasher = new TextHasherExtendedForTest();
		hasher.createPasswordEncoderSpy();
		PasswordEncoderSpy encoder = (PasswordEncoderSpy) hasher.getArgon2PasswordEncoder();

		boolean match = hasher.matches("someText", "hashedText");

		encoder.MCR.assertParameters("matches", 0, "someText", "hashedText");
		encoder.MCR.assertReturn("matches", 0, match);
	}

	class TextHasherExtendedForTest extends TextHasherArgon2 {
		public MethodCallRecorder MCR;

		PasswordEncoder getArgon2PasswordEncoder() {
			return argonEncoder;
		}

		public void createPasswordEncoderSpy() {
			argonEncoder = new PasswordEncoderSpy();
		}

		@Override
		Argon2PasswordEncoder createEncoder(int saltLength, int hashLength, int parallelism,
				int memoryInKb, int iterations) {
			ensureMCRexists();
			MCR.addCall("saltLength", saltLength, "hashLength", hashLength, "parallelism",
					parallelism, "memoryInKb", memoryInKb, "iterations", iterations);
			Argon2PasswordEncoder argonEncoder = super.createEncoder(saltLength, hashLength,
					parallelism, memoryInKb, iterations);

			MCR.addReturned(argonEncoder);
			return argonEncoder;

		}

		private void ensureMCRexists() {
			if (MCR == null) {
				MCR = new MethodCallRecorder();
			}
		}
	}
}
