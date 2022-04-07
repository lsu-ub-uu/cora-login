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

/**
 * TextHasher is intended to provide functionality to hash a text and match a text with a previously
 * hashed text.
 * <p>
 * It is expected that implementations of this interface uses hashing algoritms that are sutible for
 * storing passwords and other sensitive information on a server in compliance with:
 * <a href="https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html">OWASP
 * Password Storage Cheat Sheet</a>
 * 
 */
public interface TextHasher {
	/**
	 * hashText hashes a plain text to a hashed String.
	 * <p>
	 * The returned hash contains the hashed plain text as well as all the settings used by the
	 * implementing hasher to hash the string in such way you can easily check using
	 * {@link TextHasher#matches(String, String)} if the original hashed plain text matches the
	 * another text.
	 * 
	 * @param plainText
	 *            text to be hashed
	 * @return a String with the hashed text
	 */
	String hashText(String plainText);

	/**
	 * matches return a boolean if the supplied plain text matches the supplied hashedText. The
	 * hashed text is expected to have earlier been created using
	 * {@link TextHasher#hashText(String)}.
	 * 
	 * @param plainText
	 *            A String with a plain text to see if it matches the text that was supplied to
	 *            create the hashed text.
	 * @param hashedText
	 *            A String with a privously hashed text
	 * 
	 * @return A boolean if the plainText matches the hashedText
	 */
	boolean matches(String plainText, String hashedText);

}
