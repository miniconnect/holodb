#!/bin/sh

DIRECTORY="$( dirname "$0" )"
TASK_NAME='execLab'


if [ "$#" -ne 2 ]; then
    echo "Usage: ./exec-lab.sh <projectName> <mainClass>"
    exit 1
fi

projectName="$1"
mainClass="$2"

"$DIRECTORY/gradlew" ":${1}:${TASK_NAME}" -Dlab.exec.main.class="$2" -q --console=plain
