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

package se.uu.ub.cora.login.json;

import static org.testng.Assert.assertEquals;

import java.util.Optional;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.gatekeepertokenprovider.AuthToken;

public class AuthTokenToJsonConverterTest {
	private String url;

	@BeforeMethod
	public void beforeMethod() {
		url = "someUrl";
	}

	@Test
	public void testAuthTokenToJsonConverter() {
		AuthToken authToken = new AuthToken("someToken", "someTokenId", 100L, 200L,
				"someIdInUserStorage", "someLoginId", Optional.empty(), Optional.empty());
		AuthTokenToJsonConverter converter = new AuthTokenToJsonConverter(authToken, url);

		String json = converter.convertAuthTokenToJson();

		String expected = """
				{
				  "data": {
				    "children": [
				      {
				        "name": "token",
				        "value": "someToken"
				      },
				      {
				        "name": "validUntil",
				        "value": "100"
				      },
				      {
				        "name": "renewUntil",
				        "value": "200"
				      },
				      {
				        "name": "userId",
				        "value": "someIdInUserStorage"
				      },
				      {
				        "name": "loginId",
				        "value": "someLoginId"
				      }
				    ],
				    "name": "authToken"
				  },
				  "actionLinks": {
				    "renew": {
				      "requestMethod": "POST",
				      "rel": "renew",
				      "url": "someUrl",
				      "accept": "application/vnd.uub.authToken+json"
				    },
				    "delete": {
				      "requestMethod": "DELETE",
				      "rel": "delete",
				      "url": "someUrl"
				    }
				  }
				}""";
		;
		assertEquals(json, compactString(expected));
	}

	private String compactString(String string) {
		return string.trim().replace("\n", "").replace("\s", "");
	}

	@Test
	public void testAuthTokenToJsonConverterWithName() {

		AuthToken authToken = new AuthToken("someToken", "someTokenId", 100L, 200L,
				"someIdInUserStorage", "someLoginId", Optional.of("someFirstName"),
				Optional.of("someLastName"));
		AuthTokenToJsonConverter converter = new AuthTokenToJsonConverter(authToken, url);

		String json = converter.convertAuthTokenToJson();

		String expected = """
								{
				  "data": {
				    "children": [
				      {
				        "name": "token",
				        "value": "someToken"
				      },
				      {
				        "name": "validUntil",
				        "value": "100"
				      },
				      {
				        "name": "renewUntil",
				        "value": "200"
				      },
				      {
				        "name": "userId",
				        "value": "someIdInUserStorage"
				      },
				      {
				        "name": "loginId",
				        "value": "someLoginId"
				      },
				      {
				        "name": "firstName",
				        "value": "someFirstName"
				      },
				      {
				        "name": "lastName",
				        "value": "someLastName"
				      }
				    ],
				    "name": "authToken"
				  },
				  "actionLinks": {
				    "renew": {
				      "requestMethod": "POST",
				      "rel": "renew",
				      "url": "someUrl",
				      "accept": "application/vnd.uub.authToken+json"
				    },
				    "delete": {
				      "requestMethod": "DELETE",
				      "rel": "delete",
				      "url": "someUrl"
				    }
				  }
				}
				""";
		assertEquals(json, compactString(expected));
	}
}
