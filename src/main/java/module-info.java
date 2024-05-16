module se.uu.ub.cora.login {
	requires se.uu.ub.cora.logger;

	requires transitive se.uu.ub.cora.gatekeeper;
	requires transitive se.uu.ub.cora.gatekeepertokenprovider;
	requires transitive se.uu.ub.cora.password;
	requires se.uu.ub.cora.json;
	requires se.uu.ub.cora.httphandler;
	requires transitive jakarta.servlet;
	requires transitive jakarta.ws.rs;
	requires transitive se.uu.ub.cora.initialize;
	requires jersey.media.multipart;

	exports se.uu.ub.cora.login.rest;
}