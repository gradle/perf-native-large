import groovy.transform.CompileStatic

@CompileStatic
class DependencyGenerator {
    static Map<Project, List<Project>> generateDependencies(List<Project> projects) {
        def countDependencies = { Project project ->
            int dependencyCount = (project.modules.max { it.dependencies }?.dependencies) ?: 0
            dependencyCount
        }

        List<Project> sortedByDependencies = projects.sort(false) { Project project ->
            // sort by the maximum number of dependencies in any module
            countDependencies(project)
        }.reverse()

        Map<Project, List<Project>> generatedDependencies = [:]
        sortedByDependencies.eachWithIndex { Project project, int i ->
            int numberOfDependencies = countDependencies(project)
            List<Project> dependenciesForProject = []
            if (numberOfDependencies > 0) {
                def possibleDependencies = [] + sortedByDependencies.subList(i + 1, sortedByDependencies.size() - 1)
                Collections.shuffle(possibleDependencies, new Random(project.number as long))
                dependenciesForProject = possibleDependencies.take(numberOfDependencies).sort(false) {
                    project.number
                }
            }
            if (numberOfDependencies != dependenciesForProject.size()) {
                println "WARN: unable to generate enough dependencies for ${project} , required ${numberOfDependencies}, actual ${dependenciesForProject.size()}"
            }
            generatedDependencies.put(project, dependenciesForProject)
        }

        Map<Project, List<Project>> orderedGeneratedDependencies = [:]
        projects.each { Project project ->
            orderedGeneratedDependencies.put(project, generatedDependencies.get(project))
        }
        orderedGeneratedDependencies
    }
}
