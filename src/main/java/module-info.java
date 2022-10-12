import se.uu.ub.cora.apptokenverifier.AppTokenStorageViewInstanceProvider;

module se.uu.ub.cora.apptokenverifier {
	requires se.uu.ub.cora.logger;

	requires se.uu.ub.cora.gatekeepertokenprovider;
	requires se.uu.ub.cora.json;
	requires se.uu.ub.cora.httphandler;
	requires jakarta.servlet;
	requires jakarta.ws.rs;
	requires transitive se.uu.ub.cora.initialize;

	uses AppTokenStorageViewInstanceProvider;

	exports se.uu.ub.cora.apptokenverifier;

}