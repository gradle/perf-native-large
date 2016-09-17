package org.gradle.generator.model

import groovy.transform.Canonical
import groovy.transform.CompileStatic

@CompileStatic
@Canonical
class Dependency {
    String projectName
    String componentName
    Library library
}
