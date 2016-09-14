import groovy.transform.Canonical

@Canonical
class Module {
    ReportModuleType type
    int sources
    int headers
    int includePaths
    int dependencies
}
