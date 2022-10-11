module se.uu.ub.cora.apptokenverifier {
	requires se.uu.ub.cora.logger;

	requires se.uu.ub.cora.gatekeepertokenprovider;
	requires se.uu.ub.cora.json;
	requires se.uu.ub.cora.httphandler;
	requires jakarta.servlet;
	requires jakarta.ws.rs;
	requires se.uu.ub.cora.initialize;

	exports se.uu.ub.cora.apptokenverifier;

}