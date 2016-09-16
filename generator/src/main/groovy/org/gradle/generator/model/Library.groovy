package org.gradle.generator.model

import groovy.transform.Canonical
import groovy.transform.CompileStatic

@CompileStatic
@Canonical
class Library {
    String name
    Linkage linkage
}
