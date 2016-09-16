package org.gradle.generator.parser

import org.gradle.generator.model.Component
import org.gradle.generator.model.Library
import org.gradle.generator.model.Linkage
import org.gradle.generator.model.ReportModuleType
/**
 * Keeps track of how many components of some type have been
 * attempted to be added to a project and helps the parser
 * decide if it is time to add a new one.
 */
class ComponentTracker {
    List<Component> seenComponents

    ComponentTracker() {
        reset()
    }

    /**
     * Takes a look at the state of the libraries already seen by the
     * parser for this project, and decides if the current one should
     * be added.
     */
    public boolean shouldAdd(Component component, ReportModuleType type) {
        def library
        switch (type) {
            case ReportModuleType.SHARED_LIBRARY:
            case ReportModuleType.CUDA_SHARED_LIBRARY:
                library = new Library(linkage: Linkage.SHARED, name: component.name)
                break
            case ReportModuleType.STATIC_LIBRARY:
            case ReportModuleType.CUDA_STATIC_LIBRARY:
                // Only add a static library if we haven't seen a corresponding
                // shared library
                library = new Library(linkage: Linkage.STATIC, name: component.name)
                break
            case ReportModuleType.API_LIBRARY:
                // Only add an api library if we haven't seen a corresponding
                // static library
                library = new Library(linkage: Linkage.API, name: component.name)
                break
            default:
                // All other types get their own component for now.
                return true
        }
        // Can we add the library to a component we've already seen?
        for (Component seen : seenComponents) {
            // Will only return true if there was an open spot to add it.
            if (seen.add(library)) {
                return false
            }
        }
        // Nope, so we'll add it to the new component and say it should be added to the project.
        component.add(library)
        seenComponents << component
        return true

    }

    public void reset() {
        seenComponents = []
    }
}
