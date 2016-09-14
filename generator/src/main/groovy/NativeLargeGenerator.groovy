import groovy.transform.TupleConstructor

@TupleConstructor
class NativeLargeGenerator {
    File reportFile
    File outputDir

    void generate() {
        outputDir.deleteDir()
        outputDir.mkdirs()
        List<Project> projects = ReportParser.parse(reportFile)

        projects.each { project ->
            File projectDir = new File(outputDir, project.name)
            projectDir.mkdir()
        }
    }

    static void main(String[] args) {
        def generator = new NativeLargeGenerator(new File('components.txt'), new File('/tmp/native-large'))
        generator.generate()
    }
}