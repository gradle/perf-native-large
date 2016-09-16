package org.gradle.generator.model

import groovy.transform.CompileStatic

@CompileStatic
enum ReportModuleType {
    API_LIBRARY("ApiLibrary"),
    SHARED_LIBRARY("SharedLibrary"),
    STATIC_LIBRARY("StaticLibrary"),
    CUDA_SHARED_LIBRARY("CudaSharedLibrary"),
    CUDA_STATIC_LIBRARY("CudaStaticLibrary"),
    CUDA_EXECUTABLE("CudaExecutable"),
    EXECUTABLE("Executable"),
    PREBUILT_API("PrebuiltApi"),
    PREBUILT_SHARED("PrebuiltShared"),
    PREBULIT_STATIC("PrebuiltStatic")

    final String name
    static final Map<String, ReportModuleType> reverseMap

    static {
        reverseMap = [:] as TreeMap
        values().each { ReportModuleType type ->
            reverseMap.put(type.name, type)
        }
    }

    private ReportModuleType(String name) {
        this.name = name
    }

    static ReportModuleType from(String type) {
        reverseMap[type]
    }
}
