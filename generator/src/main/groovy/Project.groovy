import groovy.transform.Canonical

@Canonical
class Project {
    int number
    List<Module> modules = []
    List<Component> components = []

    String getName() {
        "Project${String.format("%03d", number)}"
    }
}