import groovy.transform.Canonical

@Canonical
class Project {
    String name
    List<Module> modules = []
}