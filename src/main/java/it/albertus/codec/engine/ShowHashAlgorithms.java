package it.albertus.codec.engine;

import java.security.MessageDigest;
import java.security.Provider;
import java.security.Provider.Service;
import java.security.Security;

public class ShowHashAlgorithms {

	public static void main(String[] args) {
		for (Provider provider : Security.getProviders()) {
			for (Service service : provider.getServices()) {
				if (service.getType().equalsIgnoreCase(MessageDigest.class.getSimpleName())) {
					System.out.println(service.getAlgorithm());
				}
			}
		}
	}
}
