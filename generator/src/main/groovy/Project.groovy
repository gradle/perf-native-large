import groovy.transform.Canonical

@Canonical
class Project {
    int number
    List<Module> modules = []
    List<Component> components = []

    String getName() {
        "Project${String.format("%03d", number)}"
    }

    String toString() {
        def results = new StringBuilder(getName()).append("\n")
        results.append("Modules [${modules.size()}]\n")
        modules.each { module ->
            results.append("  $module\n")
        }
        results.append("Components [${components.size()}]\n")
        components.each { component ->
            results.append("  $component\n")
        }
        results.toString()
    }
}