package se.uu.ub.cora.apptokenverifier.initialize;

import java.util.Map;

import se.uu.ub.cora.apptokenstorage.AppTokenStorageProvider;

public interface AppTokenVerifierModuleStarter {

	void startUsingInitInfoAndAppTokenStorageProviders(
			Map<String, String> initInfo,
			Iterable<AppTokenStorageProvider> appTokenStorageProviderImplementations);

}