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
    List<Component> components

    ComponentTracker() {
        reset()
    }

    /**
     * Takes a look at the state of the libraries already seen by the
     * parser for this project, and decides if the current one should
     * be added.
     */
    public boolean shouldAdd(Component component, ReportModuleType type) {
        switch (type) {
            case ReportModuleType.SHARED_LIBRARY:
            case ReportModuleType.CUDA_SHARED_LIBRARY:
                components << component
                // Always add shared libraries as they always come first.
                return true
            case ReportModuleType.STATIC_LIBRARY:
            case ReportModuleType.CUDA_STATIC_LIBRARY:
                // Only add a static library if we haven't seen a corresponding
                // shared library
                for (Component seen : components) {
                    if (!seen.hasStaticLibrary) {
                        seen.libraries << new Library(linkage: Linkage.STATIC, name: component.name)
                        seen.hasStaticLibrary = true
                        return false
                    }
                }
                components << component
                return true
            case ReportModuleType.API_LIBRARY:
                // Only add an api library if we haven't seen a corresponding
                // static library
                for (Component seen : components) {
                    if (!seen.hasApiLibrary) {
                        seen.libraries << new Library(linkage: Linkage.API, name: component.name)
                        seen.hasApiLibrary = true
                        return false
                    }
                }
                components << component
                return true
            default:
                // All other types get their own component for now.
                return true
        }
    }

    public void reset() {
        components = []
    }
}
