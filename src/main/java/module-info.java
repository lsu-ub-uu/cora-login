module se.uu.ub.cora.apptokenverifier {
	requires se.uu.ub.cora.logger;

	requires se.uu.ub.cora.gatekeepertokenprovider;
	requires se.uu.ub.cora.json;
	requires se.uu.ub.cora.httphandler;
	requires transitive jakarta.servlet;
	requires transitive jakarta.ws.rs;
	requires transitive se.uu.ub.cora.initialize;
	requires se.uu.ub.cora.gatekeeper;

	exports se.uu.ub.cora.apptokenverifier;

}