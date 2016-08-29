import static com.google.common.base.Preconditions.checkState;
import static java.util.Collections.singletonMap;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static uk.co.optimisticpanda.pom.Predicates.hasGroup;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.eclipse.aether.artifact.Artifact;

import com.google.common.base.Preconditions;

import uk.co.optimisticpanda.pom.Repos;
import uk.co.optimisticpanda.pom.Repository;
import uk.co.optimisticpanda.pom.TemplateManager;

public class Main {

	public static void main(String[] args) throws IOException {
			
		Repository repo = new Repository(Repos.home());
		Optional<Artifact> artifact = repo.getArtifact("uk.co.optimisticpanda", "parent", "pom", "1.0-SNAPSHOT");
		checkState(artifact.isPresent(), "artifact not found!");
		
		Map<String, Project>  projects = findMavenProjects(repo, new File("src/test/resources/test-project"));
		
		Project parent = projects.get("parent");
		
		List<String> modules = repo.resolvePom(artifact.get()).getDependencyManagement().getDependencies().stream()
			.filter(hasGroup("uk.co.optimisticpanda"))
			.map(Dependency::getArtifactId)
			.map(projects::get)
			.map(p -> p.pathRelativeTo(parent))
			.collect(toList());
		
		TemplateManager.print(singletonMap("modules", modules));
	}

	private static Map<String, Project> findMavenProjects(Repository repo, File root) throws IOException {
		return Files.walk(root.toPath())
		    .filter(p -> p.getFileName().endsWith("pom.xml"))
			.map(path -> new Project(path, repo.parsePom(path.toFile())))
			.collect(toMap(Project::getArtifactId, identity()));
	}
	
	private static class Project {
		private final Path path;
		private final String artifactId;
		private Project(Path path, Model model) {
			this.path = path;
			this.artifactId = model.getArtifactId();
		}
		
		private String getArtifactId() {
			return artifactId;
		}
		
		private String pathRelativeTo(Project source) {
			return source.path.getParent().relativize(path.getParent()).toString();
		}
	}
}