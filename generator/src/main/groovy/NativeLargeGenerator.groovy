import groovy.transform.TupleConstructor

@TupleConstructor
class NativeLargeGenerator {
    File reportFile
    File outputDir

    void generate() {
        outputDir.mkdirs()
        List<Project> projects = ReportParser.parse(reportFile)

        ProjectClassifier.classify(projects)
        printProjects(projects)

        def generatedDependencies = DependencyGenerator.generateDependencies(projects)
        generatedDependencies.each { Project p, List<Project> dependencies ->
            println "project: ${p.name} dependencies: ${dependencies.collect{it.name}.join(',')}"
        }
    }

    static printProjects(List<Project> projects) {
        projects.each { project ->
            println project.name
            if (project.modules) {
                println "Modules:"
                project.modules.each { module ->
                    println "  " + module
                }
            } else {
                println "No Modules"
            }
            if (project.components) {
                println "Components:"
                project.components.each { component ->
                    println "  " + component
                }
            } else {
                println "No Components"
            }
        }
    }

    static void main(String[] args) {
        def generator = new NativeLargeGenerator(new File('report.txt'), new File('/tmp/native-large'))
        generator.generate()
    }
}