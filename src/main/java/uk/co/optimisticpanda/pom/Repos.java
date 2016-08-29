package uk.co.optimisticpanda.pom;

import java.io.File;
import java.net.URI;

import org.eclipse.aether.repository.RemoteRepository;

public enum Repos {
	;
	
	public static RemoteRepository central() {
		return new RemoteRepository.Builder("central", "default", "http://central.maven.org/maven2/").build();
	}
	
	public static RemoteRepository home() {
		URI repo = new File(System.getProperty("user.home")).toPath().resolve(".m2").resolve("repository").toUri();
		return new RemoteRepository.Builder("central", "default", repo.toString()).build();
	}
}
