#!/bin/sh

selfDir="$( dirname -- "$( realpath -- "$0" )" )"
taskName='execLab'

if [ "$#" -ne 2 ]; then
    echo "Usage: ./exec-lab.sh <projectName> <mainClass>"
    exit 1
fi

projectName="$1"
mainClass="$2"

"$selfDir/gradlew" ":${projectName}:${taskName}" -Dlab.exec.main.class="$mainClass" -q --console=plain
