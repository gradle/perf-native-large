package org.gradle.generator

import groovy.transform.CompileStatic
import groovy.transform.TupleConstructor
import org.gradle.generator.model.Component
import org.gradle.generator.model.Dependency
import org.gradle.generator.model.Library
import org.gradle.generator.model.Linkage
import org.gradle.generator.model.Project
import org.gradle.generator.parser.ReportParser
import org.gradle.generator.populator.Root

@CompileStatic
@TupleConstructor
class NativeLargeGenerator {
    File reportFile
    File outputDir

    void generate() {
        Map<String, Project> depsProviderMap = [:]
        List<Project> projects = ReportParser.parse(reportFile, depsProviderMap)

        Map<String, Dependency> dependencyMap = [:]
        populateDependencyMap(depsProviderMap, dependencyMap)

        // pruneTransitiveDeps(projects, depsProviderMap, dependencyMap)

        new Root(outputDir).populateWith(projects, dependencyMap)
    }

    static pruneTransitiveDeps(
            List<Project> projects, Map<String, Project> depsProviderMap, Map<String, Dependency> dependencyMap) {
        projects.each { project ->
            project.components.each { component ->
                Set<String> allDeps = component.dependencies as Set
                Set<String> transitiveDeps = [] as Set
                for(String dep : component.dependencies) {
                    def depProject = depsProviderMap[dep]
                    def dependency = dependencyMap[dep]
                    depProject.components.each { depComponent ->
                        if (depComponent.name == dependency.componentName) {
                            transitiveDeps.addAll(depComponent.dependencies)
                        }
                    }
                }
                component.dependencies = (allDeps - transitiveDeps) as List
                component.transitiveDeps = transitiveDeps as List
            }
        }
    }

    static populateDependencyMap(Map<String, Project> depsProviderMap, Map<String, Dependency> dependencyMap) {
        depsProviderMap.each { depName, project ->
            boolean found = false
            List<Component> allComponents = []
            allComponents.addAll(project.components)
            allComponents.addAll(project.testSuites)
            allComponents.addAll(project.prebuiltLibraries)
            allComponents.each { component ->
                for (Library library : component.libraries) {
                    if (library == null) {
                        println "Project: $project.name Component: $component.name Dep: $depName"
                    }
                    if (library.name == depName) {
                        dependencyMap.put(
                                depName,
                                new Dependency(
                                        projectName: project.name, componentName: component.name, library: library))
                        found = true
                    }
                }
                if (!found && component.name == depName) {
                    Linkage linkage = Linkage.API
                    if (component.hasStaticLibrary) {
                        linkage = Linkage.STATIC
                    }
                    if (component.hasSharedLibrary) {
                        linkage = Linkage.SHARED
                    }
                    dependencyMap.put(
                            depName,
                            new Dependency(
                                    projectName: project.name,
                                    componentName: component.name,
                                    library: new Library(name: depName, linkage: linkage)))
                    found = true
                }
            }
            assert found : "Didn't find : $depName"
        }
    }

    static void main(String[] args) {
        def generator = new NativeLargeGenerator(new File('components.txt'), new File('../'))
        generator.generate()
    }
}
