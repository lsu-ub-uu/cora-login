module se.uu.ub.cora.apptokenverifier {
	requires se.uu.ub.cora.logger;

	requires se.uu.ub.cora.gatekeepertokenprovider;
	requires se.uu.ub.cora.json;
	requires se.uu.ub.cora.apptokenstorage;
	requires jakarta.activation;
	requires se.uu.ub.cora.httphandler;
	requires java.ws.rs;
	requires javax.servlet.api;

	uses se.uu.ub.cora.apptokenstorage.AppTokenStorageProvider;
}