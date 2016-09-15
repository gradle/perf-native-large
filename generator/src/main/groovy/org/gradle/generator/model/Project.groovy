package org.gradle.generator.model

import groovy.transform.Canonical
import groovy.transform.CompileStatic

@CompileStatic
@Canonical
class Project {
    int number
    List<Component> components = []
    List<Component> prebuiltLibraries = []

    String getName() {
        "project${String.format("%03d", number)}"
    }

    String toString() {
        def results = new StringBuilder(getName()).append("\n")
        results.append("Components [${components.size() + prebuiltLibraries.size()}]\n")
        components.each { component ->
            results.append("  $component\n")
        }
        prebuiltLibraries.each { component ->
            results.append("  $component (prebuilt)\n")
        }
        results.toString()
    }
}