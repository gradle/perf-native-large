import groovy.transform.Canonical

@Canonical
class Project {
    int number
    List<Component> components = []

    String getName() {
        "project${String.format("%03d", number)}"
    }

    String toString() {
        def results = new StringBuilder(getName()).append("\n")
        results.append("Components [${components.size()}]\n")
        components.each { component ->
            results.append("  $component\n")
        }
        results.toString()
    }
}