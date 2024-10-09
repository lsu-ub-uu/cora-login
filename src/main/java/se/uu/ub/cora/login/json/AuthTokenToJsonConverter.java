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

import se.uu.ub.cora.gatekeepertokenprovider.AuthToken;
import se.uu.ub.cora.json.builder.JsonArrayBuilder;
import se.uu.ub.cora.json.builder.JsonObjectBuilder;
import se.uu.ub.cora.json.builder.org.OrgJsonBuilderFactoryAdapter;

public final class AuthTokenToJsonConverter {

	private static final String VALUE = "value";
	private static final String NAME = "name";
	private static final String CHILDREN = "children";
	private AuthToken authToken;
	private OrgJsonBuilderFactoryAdapter orgJsonBuilderFactoryAdapter = new OrgJsonBuilderFactoryAdapter();
	private String url;

	public AuthTokenToJsonConverter(AuthToken authToken, String url) {
		this.authToken = authToken;
		this.url = url;
	}

	public String convertAuthTokenToJson() {
		JsonObjectBuilder everythingBuilder = orgJsonBuilderFactoryAdapter.createObjectBuilder();
		createAuthTokenBuilder(everythingBuilder);
		createActionLinksBuilder(everythingBuilder);
		return everythingBuilder.toJsonFormattedString();
	}

	private void createAuthTokenBuilder(JsonObjectBuilder everythingBuilder) {
		JsonObjectBuilder authTokenBuilder = createObjectBuilderWithName("authToken");
		everythingBuilder.addKeyJsonObjectBuilder("data", authTokenBuilder);
		JsonArrayBuilder userChildren = returnAndAddChildrenToBuilder(authTokenBuilder);

		addTokenToJson(authToken, userChildren);
		addValidForNoSecondsToJson(authToken, userChildren);
		addIdInUserStorageToJson(userChildren);
		addLoginIdToJson(authToken, userChildren);
		possiblyAddFirstnameAndLastnameToJson(authToken, userChildren);
	}

	private void createActionLinksBuilder(JsonObjectBuilder everythingBuilder) {
		JsonObjectBuilder actionLinksBuilder = orgJsonBuilderFactoryAdapter.createObjectBuilder();
		everythingBuilder.addKeyJsonObjectBuilder("actionLinks", actionLinksBuilder);

		createDeleteBuilder(actionLinksBuilder);
	}

	private void createDeleteBuilder(JsonObjectBuilder actionLinksBuilder) {
		JsonObjectBuilder deleteBuilder = orgJsonBuilderFactoryAdapter.createObjectBuilder();
		actionLinksBuilder.addKeyJsonObjectBuilder("delete", deleteBuilder);
		deleteBuilder.addKeyString("requestMethod", "DELETE");
		deleteBuilder.addKeyString("rel", "delete");
		deleteBuilder.addKeyString("url", url);
	}

	private JsonObjectBuilder createObjectBuilderWithName(String name) {
		JsonObjectBuilder roleBuilder = orgJsonBuilderFactoryAdapter.createObjectBuilder();
		roleBuilder.addKeyString(NAME, name);
		return roleBuilder;
	}

	private void addTokenToJson(AuthToken authToken, JsonArrayBuilder userChildren) {
		JsonObjectBuilder id = createObjectBuilderWithName("token");
		id.addKeyString(VALUE, authToken.token());
		userChildren.addJsonObjectBuilder(id);
	}

	private void addValidForNoSecondsToJson(AuthToken authToken, JsonArrayBuilder userChildren) {
		JsonObjectBuilder validForNoSeconds = createObjectBuilderWithName("validForNoSeconds");
		validForNoSeconds.addKeyString(VALUE, String.valueOf(authToken.validForNoSeconds()));
		userChildren.addJsonObjectBuilder(validForNoSeconds);
	}

	private JsonArrayBuilder returnAndAddChildrenToBuilder(JsonObjectBuilder userBuilder) {
		JsonArrayBuilder userChildren = orgJsonBuilderFactoryAdapter.createArrayBuilder();
		userBuilder.addKeyJsonArrayBuilder(CHILDREN, userChildren);
		return userChildren;
	}

	private void addIdInUserStorageToJson(JsonArrayBuilder userChildren) {
		JsonObjectBuilder idInUserStorage = createObjectBuilderWithName("idInUserStorage");
		idInUserStorage.addKeyString(VALUE, String.valueOf(authToken.idInUserStorage()));
		userChildren.addJsonObjectBuilder(idInUserStorage);
	}

	private void addLoginIdToJson(AuthToken authToken, JsonArrayBuilder userChildren) {
		JsonObjectBuilder loginId = createObjectBuilderWithName("loginId");
		loginId.addKeyString(VALUE, String.valueOf(authToken.loginId()));
		userChildren.addJsonObjectBuilder(loginId);
	}

	private void possiblyAddFirstnameAndLastnameToJson(AuthToken authToken,
			JsonArrayBuilder userChildren) {
		possiblyAddFirstNameToJson(authToken, userChildren);
		possiblyAddLastNameToJson(authToken, userChildren);
	}

	private void possiblyAddFirstNameToJson(AuthToken authToken, JsonArrayBuilder userChildren) {
		if (authToken.firstName().isPresent()) {
			JsonObjectBuilder firstName = createObjectBuilderWithName("firstName");
			firstName.addKeyString(VALUE, String.valueOf(authToken.firstName().get()));
			userChildren.addJsonObjectBuilder(firstName);
		}
	}

	private void possiblyAddLastNameToJson(AuthToken authToken, JsonArrayBuilder userChildren) {
		if (authToken.lastName().isPresent()) {
			JsonObjectBuilder lastName = createObjectBuilderWithName("lastName");
			lastName.addKeyString(VALUE, String.valueOf(authToken.lastName().get()));
			userChildren.addJsonObjectBuilder(lastName);
		}
	}
}
