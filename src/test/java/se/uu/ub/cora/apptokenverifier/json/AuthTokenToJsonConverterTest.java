/*
 * Copyright 2016, 2017 Uppsala University Library
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

package se.uu.ub.cora.apptokenverifier.json;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import se.uu.ub.cora.gatekeepertokenprovider.AuthToken;

public class AuthTokenToJsonConverterTest {
	@Test
	public void testAuthTokenToJsonConverter() {
		String url = "http://epc.ub.uu.se/apptokenverifier/rest/apptoken/131313";
		AuthToken authToken = AuthToken.withIdAndValidForNoSecondsAndIdInUserStorage("someId", 599,
				"someIdInUserStorage");
		AuthTokenToJsonConverter converter = new AuthTokenToJsonConverter(authToken, url);
		String json = converter.convertAuthTokenToJson();
		String expected = "{\"data\":{\"children\":[" + "{\"name\":\"id\",\"value\":\"someId\"},"
				+ "{\"name\":\"validForNoSeconds\",\"value\":\"599\"}]"
				+ ",\"name\":\"authToken\"},"
				+ "\"actionLinks\":{\"delete\":{\"requestMethod\":\"DELETE\","
				+ "\"rel\":\"delete\","
				+ "\"url\":\"http://epc.ub.uu.se/apptokenverifier/rest/apptoken/131313\"}}}";
		assertEquals(json, expected);
	}
}
