import groovy.transform.TupleConstructor

@TupleConstructor
class NativeLargeGenerator {
    File reportFile
    File outputDir

    void generate() {
        outputDir.mkdirs()

        reportFile.eachLine { String line ->
            println line
        }
    }

    static void main(String[] args) {
        def generator = new NativeLargeGenerator(new File('report.txt'), new File('/tmp/native-large'))
        generator.generate()
    }
}
