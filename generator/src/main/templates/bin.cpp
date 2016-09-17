#include <stdio.h>

<% deps.each { dep -> %>
#include <${dependencyMap[dep].componentName}/lib1.h><% } %>

int main() {
  printf("Hello from Main\\n");
}
