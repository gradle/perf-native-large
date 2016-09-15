#!/usr/bin/env groovy
import groovy.transform.CompileStatic

@CompileStatic
static void sanitize(String input, String out) {
    def flames = new File(input)
    // CONFIGURE THIS IF YOUR WANT TO FILTER SOME FRAMES
    // TAG THEM, ...
    def repl = [
            'build_([a-z0-9]+)': 'build_',
            'settings_([a-z0-9]+)': 'settings_',
            'org[.]gradle[.]': '',
            //'internal[.]service[.][^ ]+': 'service_registry',
            //'org[.]codehaus[.]groovy[.][^ ]+' : 'groovy',
            //'sun[.]reflect[.][^ ]+' : 'reflection_call',
            //'groovy[.]lang[.](Meta|Closure)[^ ]+': 'groovy',
            'sun[.]reflect[.]GeneratedMethodAccessor[0-9]+': 'GeneratedMethodAccessor',
            'java[.]lang[.]reflect[.][^ ]+': 'reflection_call',
            //'model[.]internal[.][^ ]+': 'new_model',
            //'api[.](internal[.])?plugins[.][^ ]+': 'plugins',
            //'.+?Dynamic[^ ]+': 'DynamicObject',
            //'.+?[cC]anonical[^ ]+': 'canonicalization',
            //'.+?[sS]napshot[^ ]+': 'snapshotting',
            //'java[.]io[.][^ ]+': 'I/O',
            //'cache[.]internal[.][^ ]+': 'cache_access',

    ]

    new File(out).withWriter { wrt ->
        flames.eachLine { String line ->

            repl.each { p, r ->
                line = line.split(';').collect { String it -> it.replaceAll(p, r) }.join(';')
                StringBuilder sb = new StringBuilder()
                String prev
                def elems = line.split(' ')
                String prefix = elems[0]
                String suffix = elems[1]
                prefix.split(';').each {
                    if (it != prev) {
                        if (sb.length() > 0) {
                            sb.append(';')
                        }
                        sb.append(it)
                        prev = it
                    }
                }
                line = "${sb} $suffix".toString()
            }
            wrt.writeLine(line)

        }
    }


}

if(args.size() < 2) {
    System.err.println("Pass input and output files as parameter.")
    System.exit(1)
}

sanitize(args[0], args[1])