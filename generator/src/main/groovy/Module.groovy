import groovy.transform.Canonical

@Canonical
class Module {
    String type
    int sources
    int headers
    int includePaths
    int dependencies
}
