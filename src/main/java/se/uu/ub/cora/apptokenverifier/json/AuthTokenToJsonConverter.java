/*
 * Copyright 2016 Uppsala University Library
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

import se.uu.ub.cora.gatekeepertokenprovider.AuthToken;
import se.uu.ub.cora.json.builder.JsonArrayBuilder;
import se.uu.ub.cora.json.builder.JsonObjectBuilder;
import se.uu.ub.cora.json.builder.org.OrgJsonBuilderFactoryAdapter;

public final class AuthTokenToJsonConverter {

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

		addIdToJson(authToken, userChildren);

		addValidForNoSecondsToJson(authToken, userChildren);
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

	private void addIdToJson(AuthToken authToken, JsonArrayBuilder userChildren) {
		JsonObjectBuilder id = createObjectBuilderWithName("id");
		id.addKeyString("value", authToken.id);
		userChildren.addJsonObjectBuilder(id);
	}

	private void addValidForNoSecondsToJson(AuthToken authToken, JsonArrayBuilder userChildren) {
		JsonObjectBuilder validForNoSeconds = createObjectBuilderWithName("validForNoSeconds");
		validForNoSeconds.addKeyString("value", String.valueOf(authToken.validForNoSeconds));
		userChildren.addJsonObjectBuilder(validForNoSeconds);
	}

	private JsonArrayBuilder returnAndAddChildrenToBuilder(JsonObjectBuilder userBuilder) {
		JsonArrayBuilder userChildren = orgJsonBuilderFactoryAdapter.createArrayBuilder();
		userBuilder.addKeyJsonArrayBuilder(CHILDREN, userChildren);
		return userChildren;
	}

}
