package org.gradle.generator.model

import groovy.transform.CompileStatic

@CompileStatic
class Component {
    String name
    GradleComponentType type
    int sources
    int headers
    int includePaths
    List<String> dependencies
    List<String> transitiveDeps
    // Relevant for NATIVE_LIBRARY_SPEC and PREBUILT_LIBRARY
    boolean hasApiLibrary
    boolean hasSharedLibrary
    boolean hasStaticLibrary
    // Only relevant for org.gradle.generator.model.GradleComponentType.NATIVE_LIBRARY_SPEC
    private Library apiLibrary
    private Library sharedLibrary
    private Library staticLibrary

    Component(String name) {
        this.name = name
        dependencies = []
        transitiveDeps = []
        apiLibrary = null
        sharedLibrary = null
        staticLibrary = null
        hasApiLibrary = false
        hasSharedLibrary = false
        hasStaticLibrary = false
    }

    def boolean add(Library library) {
        if (canAdd(library)) {
            switch (library.linkage) {
                case Linkage.API:
                    apiLibrary = library
                    hasApiLibrary = true
                    break
                case Linkage.STATIC:
                    staticLibrary = library
                    hasStaticLibrary = true
                    break
                case Linkage.SHARED:
                    sharedLibrary = library
                    hasSharedLibrary = true
                    break
                default:
                    break
            }
            return true
        } else {
            return false
        }
    }

    private boolean canAdd(Library library) {
        (Linkage.API == library.linkage && !hasApiLibrary) ||
                (Linkage.SHARED == library.linkage && !hasSharedLibrary) ||
                (Linkage.STATIC == library.linkage && !hasStaticLibrary)
    }

    def List<Library> getLibraries() {
        List<Library> libraries = []
        if (hasApiLibrary && apiLibrary != null) {
            libraries << apiLibrary
        }
        if (hasSharedLibrary && sharedLibrary != null) {
            libraries << sharedLibrary
        }
        if (hasStaticLibrary && staticLibrary != null) {
            libraries << staticLibrary
        }
        return libraries
    }

    def boolean hasExtraExportedHeaders() {
        return expectedPathCount < includePaths
    }

    def List<String> getExportedHeaders() {
        List<String> exportedHeaders = ["src/$name/headers".toString()]
        if (expectedPathCount < includePaths) {
            exportedHeaders << "src/shared/headers"
        }
        if (expectedPathCount + 1 < includePaths) {
            exportedHeaders << "src/$name/generated_headers".toString()
        }
        return exportedHeaders
    }

    private int getExpectedPathCount() {
        (type == GradleComponentType.GOOGLE_TEST_TEST_SUITE_SPEC) ? 2 : 1
    }
}
