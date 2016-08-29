package uk.co.optimisticpanda.pom;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.apache.maven.model.Model;
import org.apache.maven.model.building.DefaultModelBuilderFactory;
import org.apache.maven.model.building.DefaultModelBuildingRequest;
import org.apache.maven.model.building.ModelBuilder;
import org.apache.maven.model.building.ModelBuildingException;
import org.apache.maven.model.building.ModelBuildingRequest;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.VersionRangeRequest;
import org.eclipse.aether.resolution.VersionRangeResolutionException;
import org.eclipse.aether.resolution.VersionRangeResult;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transport.file.FileTransporterFactory;
import org.eclipse.aether.transport.http.HttpTransporterFactory;

import com.google.common.base.Throwables;

public class Repository {

	private final RepositorySystem service;
	private final RepositorySystemSession session;
	private final List<RemoteRepository> repositories;

	public Repository(RemoteRepository repository) {
		this.repositories = Arrays.asList(repository);
		DefaultServiceLocator locator = MavenRepositorySystemUtils.newServiceLocator();
		locator.addService(RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class);
		locator.addService(TransporterFactory.class, FileTransporterFactory.class);
		locator.addService(TransporterFactory.class, HttpTransporterFactory.class);

		this.service = locator.getService(RepositorySystem.class);
		DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();
		LocalRepository localRepo = new LocalRepository("target/local-repo");
		session.setLocalRepositoryManager(service.newLocalRepositoryManager(session, localRepo));
		this.session = session;
	}
	
	public Optional<Artifact> getLatestArtifact(String groupId, String artifactId, String type) {
		return getArtifact(groupId, artifactId, type, "(,]");
	}	

	public Optional<Artifact> getArtifact(String groupId, String artifactId, String type, String version) {
		VersionRangeRequest request = new VersionRangeRequest();
		request.setArtifact(new DefaultArtifact(groupId, artifactId, type, version));
		request.setRepositories(repositories);
		try {
			VersionRangeResult result = service.resolveVersionRange(session, request);
			return Optional.ofNullable(result.getHighestVersion()).map(v -> 
				new DefaultArtifact(groupId, artifactId, type, v.toString()));
		} catch (VersionRangeResolutionException e) {
			throw Throwables.propagate(e);
		}
	}	
	
	public Model resolvePom(Artifact artifact) {
		try {
			ArtifactResult result = service.resolveArtifact(session, new ArtifactRequest(artifact, repositories, ""));
			return parsePom(result.getArtifact().getFile());
		} catch (ArtifactResolutionException e) {
			throw Throwables.propagate(e);
		}
	}

	public Model parsePom(File file) {
		ModelBuildingRequest req = new DefaultModelBuildingRequest();
		req.setProcessPlugins(false);
		req.setPomFile(file);
		req.setModelResolver(new SimpleModelResolver(service, session, repositories));
		req.setValidationLevel(ModelBuildingRequest.VALIDATION_LEVEL_MINIMAL);
		ModelBuilder builder = new DefaultModelBuilderFactory().newInstance();
		try {
			return builder.build(req).getEffectiveModel();
		} catch (ModelBuildingException e) {
			throw Throwables.propagate(e);
		}
	}
}
