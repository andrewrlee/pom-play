package uk.co.optimisticpanda.pom;

import java.util.function.Predicate;

import org.apache.maven.model.Dependency;

public class Predicates {

	public static Predicate<Dependency> hasGroup(String group){
		return dependency -> dependency.getGroupId().equals(group);
	}
	
}
