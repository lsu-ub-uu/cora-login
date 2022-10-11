package se.uu.ub.cora.apptokenverifier.initialize;

import java.util.Map;

import se.uu.ub.cora.apptokenverifier.AppTokenStorageViewInstanceProvider;

public interface AppTokenVerifierModuleStarter {

	void startUsingInitInfoAndAppTokenStorageProviders(Map<String, String> initInfo,
			Iterable<AppTokenStorageViewInstanceProvider> appTokenStorageProviderImplementations);

}