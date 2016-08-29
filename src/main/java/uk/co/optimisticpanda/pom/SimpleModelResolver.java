package uk.co.optimisticpanda.pom;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.model.Parent;
import org.apache.maven.model.Repository;
import org.apache.maven.model.building.FileModelSource;
import org.apache.maven.model.building.ModelSource;
import org.apache.maven.model.resolution.InvalidRepositoryException;
import org.apache.maven.model.resolution.ModelResolver;
import org.apache.maven.model.resolution.UnresolvableModelException;
import org.apache.maven.repository.internal.ArtifactDescriptorUtils;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
 
class SimpleModelResolver implements ModelResolver { 
 
    private final RepositorySystem system; 
    private final RepositorySystemSession session; 
    private final Set<String> repositoryIds; 
    private final List<RemoteRepository> repositories; 
 
    public SimpleModelResolver(RepositorySystem system, RepositorySystemSession session,  
            List<RemoteRepository> remoteRepositories) { 
        this.system = system; 
        this.session = session; 
        this.repositories = new ArrayList<RemoteRepository>(remoteRepositories); 
        this.repositoryIds = new HashSet<String>(); 
        remoteRepositories.forEach(repo -> repositoryIds.add(repo.getId()));
    } 
 
    private SimpleModelResolver(SimpleModelResolver original) { 
        this.session = original.session; 
        this.system = original.system; 
        this.repositoryIds = new HashSet<String>(original.repositoryIds);
        this.repositories = new ArrayList<RemoteRepository>(original.repositories);
    } 
 
    @Override 
    public void addRepository(Repository repository) throws InvalidRepositoryException { 
    	addRepository(repository, false);    
    } 
 
    @Override 
    public ModelResolver newCopy() { 
        return new SimpleModelResolver(this); 
    } 
 
    @Override 
    public ModelSource resolveModel(String groupId, String artifactId, String version) 
            throws UnresolvableModelException { 
        try { 
        	Artifact pomArtifact = new DefaultArtifact(groupId, artifactId, "", "pom", version); 
            ArtifactRequest request = new ArtifactRequest(pomArtifact, repositories, null); 
            pomArtifact = system.resolveArtifact(session, request).getArtifact(); 
            return new FileModelSource(pomArtifact.getFile()); 
        } catch (ArtifactResolutionException ex) { 
            throw new UnresolvableModelException(ex.getMessage(), groupId, artifactId, version, ex); 
        } 
    }

	@Override
	public ModelSource resolveModel(Parent parent) throws UnresolvableModelException {
		return resolveModel( parent.getGroupId(), parent.getArtifactId(), parent.getVersion() );
	}

	@Override
	public void addRepository(Repository repository, boolean replace) throws InvalidRepositoryException {
        if (!repositoryIds.add(repository.getId()) && !replace) { 
            return; 
        } 
        this.repositories.add(ArtifactDescriptorUtils.toRemoteRepository(repository)); 
	} 
}