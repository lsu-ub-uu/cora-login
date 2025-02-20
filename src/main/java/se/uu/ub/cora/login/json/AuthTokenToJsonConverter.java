/*
 * Copyright 2024, 2025 Uppsala University Library
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
		JsonObjectBuilder everyThingBuilder = orgJsonBuilderFactoryAdapter.createObjectBuilder();
		createAuthetication(everyThingBuilder);
		return everyThingBuilder.toJsonFormattedString();
	}

	private void createAuthetication(JsonObjectBuilder everyThingBuilder) {
		JsonObjectBuilder authenticationBuilder = orgJsonBuilderFactoryAdapter
				.createObjectBuilder();

		createData(authenticationBuilder);
		createActionLinksBuilder(authenticationBuilder);
		everyThingBuilder.addKeyJsonObjectBuilder("authentication", authenticationBuilder);
	}

	private void createData(JsonObjectBuilder everythingBuilder) {
		JsonObjectBuilder authTokenBuilder = createAuthToken();
		everythingBuilder.addKeyJsonObjectBuilder("data", authTokenBuilder);
	}

	private JsonObjectBuilder createAuthToken() {
		JsonObjectBuilder authTokenBuilder = createObjectBuilderWithName("authToken");
		JsonArrayBuilder userChildren = createChildrenArrayBuilder(authTokenBuilder);
		addChildren(userChildren);
		return authTokenBuilder;
	}

	private void addChildren(JsonArrayBuilder userChildren) {
		addTokenToJson(userChildren);
		addValidUntilToJson(userChildren);
		addRenewUntilToJson(userChildren);
		addUserIdToJson(userChildren);
		addLoginIdToJson(userChildren);
		possiblyAddFirstnameAndLastnameToJson(userChildren);
		possiblyAddPermissionUnitsToJson(userChildren);
	}

	private void createActionLinksBuilder(JsonObjectBuilder everythingBuilder) {
		JsonObjectBuilder actionLinksBuilder = orgJsonBuilderFactoryAdapter.createObjectBuilder();
		everythingBuilder.addKeyJsonObjectBuilder("actionLinks", actionLinksBuilder);

		createRenewBuilder(actionLinksBuilder);
		createDeleteBuilder(actionLinksBuilder);
	}

	private void createRenewBuilder(JsonObjectBuilder actionLinksBuilder) {
		JsonObjectBuilder builder = orgJsonBuilderFactoryAdapter.createObjectBuilder();
		actionLinksBuilder.addKeyJsonObjectBuilder("renew", builder);
		builder.addKeyString("requestMethod", "POST");
		builder.addKeyString("accept", "application/vnd.uub.authentication+json");
		builder.addKeyString("rel", "renew");
		builder.addKeyString("url", url);
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

	private void addTokenToJson(JsonArrayBuilder userChildren) {
		JsonObjectBuilder id = createObjectBuilderWithName("token");
		id.addKeyString(VALUE, authToken.token());
		userChildren.addJsonObjectBuilder(id);
	}

	private void addValidUntilToJson(JsonArrayBuilder userChildren) {
		JsonObjectBuilder validForNoSeconds = createObjectBuilderWithName("validUntil");
		validForNoSeconds.addKeyString(VALUE, String.valueOf(authToken.validUntil()));
		userChildren.addJsonObjectBuilder(validForNoSeconds);
	}

	private void addRenewUntilToJson(JsonArrayBuilder userChildren) {
		JsonObjectBuilder validForNoSeconds = createObjectBuilderWithName("renewUntil");
		validForNoSeconds.addKeyString(VALUE, String.valueOf(authToken.renewUntil()));
		userChildren.addJsonObjectBuilder(validForNoSeconds);
	}

	private JsonArrayBuilder createChildrenArrayBuilder(JsonObjectBuilder authTokenBuilder) {
		JsonArrayBuilder userChildren = orgJsonBuilderFactoryAdapter.createArrayBuilder();
		authTokenBuilder.addKeyJsonArrayBuilder(CHILDREN, userChildren);
		return userChildren;
	}

	private void addUserIdToJson(JsonArrayBuilder userChildren) {
		JsonObjectBuilder userId = createObjectBuilderWithName("userId");
		userId.addKeyString(VALUE, String.valueOf(authToken.idInUserStorage()));
		userChildren.addJsonObjectBuilder(userId);
	}

	private void addLoginIdToJson(JsonArrayBuilder userChildren) {
		JsonObjectBuilder loginId = createObjectBuilderWithName("loginId");
		loginId.addKeyString(VALUE, String.valueOf(authToken.loginId()));
		userChildren.addJsonObjectBuilder(loginId);
	}

	private void possiblyAddFirstnameAndLastnameToJson(JsonArrayBuilder userChildren) {
		possiblyAddFirstNameToJson(userChildren);
		possiblyAddLastNameToJson(userChildren);
	}

	private void possiblyAddFirstNameToJson(JsonArrayBuilder userChildren) {
		if (authToken.firstName().isPresent()) {
			JsonObjectBuilder firstName = createObjectBuilderWithName("firstName");
			firstName.addKeyString(VALUE, String.valueOf(authToken.firstName().get()));
			userChildren.addJsonObjectBuilder(firstName);
		}
	}

	private void possiblyAddLastNameToJson(JsonArrayBuilder userChildren) {
		if (authToken.lastName().isPresent()) {
			JsonObjectBuilder lastName = createObjectBuilderWithName("lastName");
			lastName.addKeyString(VALUE, String.valueOf(authToken.lastName().get()));
			userChildren.addJsonObjectBuilder(lastName);
		}
	}

	private void possiblyAddPermissionUnitsToJson(JsonArrayBuilder userChildren) {
		if (!authToken.permissionUnits().isEmpty()) {
			int repeatId = 0;
			for (String permissionUnit : authToken.permissionUnits()) {
				repeatId++;
				addPermissionUnitToJson(userChildren, permissionUnit, String.valueOf(repeatId));
			}
		}
	}

	private void addPermissionUnitToJson(JsonArrayBuilder userChildren, String linkedRecordId,
			String repeatId) {
		JsonObjectBuilder permissionUnit = createObjectBuilderWithName("permissionUnit");
		JsonArrayBuilder permissionUnitChildren = orgJsonBuilderFactoryAdapter.createArrayBuilder();
		permissionUnit.addKeyJsonArrayBuilder(CHILDREN, permissionUnitChildren);
		permissionUnit.addKeyString("repeatId", repeatId);

		JsonObjectBuilder type = createObjectBuilderWithName("linkedRecordType");
		type.addKeyString(VALUE, "permissionUnit");
		permissionUnitChildren.addJsonObjectBuilder(type);

		JsonObjectBuilder id = createObjectBuilderWithName("linkedRecordId");
		id.addKeyString(VALUE, linkedRecordId);
		permissionUnitChildren.addJsonObjectBuilder(id);

		userChildren.addJsonObjectBuilder(permissionUnit);
	}
}
