import groovy.transform.Canonical

@Canonical
class Project {
    int number
    List<Module> modules = []
}