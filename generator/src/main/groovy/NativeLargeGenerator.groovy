import groovy.transform.TupleConstructor

@TupleConstructor
class NativeLargeGenerator {
    File reportFile
    File outputDir

    void generate() {
        outputDir.mkdirs()
        List<Project> projects = ReportParser.parse(reportFile)

        ProjectClassifier.classify(projects)
        projects.each {
            println it
        }

        def generatedDependencies = DependencyGenerator.generateDependencies(projects)
        generatedDependencies.each { Project p, List<Project> dependencies ->
            println "project: ${p.name} dependencies: ${dependencies.collect{it.name}.join(',')}"
        }
    }

    static void main(String[] args) {
        def generator = new NativeLargeGenerator(new File('report.txt'), new File('/tmp/native-large'))
        generator.generate()
    }
}