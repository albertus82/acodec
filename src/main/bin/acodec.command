#!/bin/sh
java -Xms@console.vm.initialHeapSize@m -Xmx@console.vm.maxHeapSize@m -D@mainClass@.main.mode=console -jar "`dirname $0`/@macos.jarFileName@" "$@"
