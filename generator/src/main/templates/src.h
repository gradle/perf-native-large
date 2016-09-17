#ifndef PROJECT_HEADER_${component}_${it + 1}_H
#define PROJECT_HEADER_${component}_${it + 1}_H

<% deps.each { dep -> %>
#include <${dependencyMap[dep].componentName}/lib1.h><% } %>

int ${component}_${it + 1}();

#endif // PROJECT_HEADER_${component}_${it + 1}_H