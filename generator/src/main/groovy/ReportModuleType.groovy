import groovy.transform.CompileStatic

@CompileStatic
enum ReportModuleType {
    SHARED_LIBRARY("SharedLibrary"),
    STATIC_LIBRARY("StaticLibrary"),
    CUDA_SHARED_LIBRARY("CudaSharedLibrary"),
    CUDA_STATIC_LIBRARY("CudaStaticLibrary"),
    CUDA_EXECUTABLE("CudaExecutable"),
    EXECUTABLE("Executable"),
    PREBUILT_LIBRARY("PrebuiltLibrary")

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
