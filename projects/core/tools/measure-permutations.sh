#!/bin/sh
# shellcheck disable=SC2016

startDir="$( pwd )"

selfDir="$( dirname -- "$( realpath -- "$0" )" )"
projectDir="$( realpath -- "${selfDir}/.." )"
rootDir="$( realpath -- "${projectDir}/../.." )"
buildDir="${projectDir}/build"
outDir="${buildDir}/measure-permutations"
dataDir="${outDir}/data"
compressedDir="${outDir}/compressed"

cd "$rootDir" || {
    echo "Failed to cd to rootDir=${rootDir}"
    exit 1
}


rm -rf "$outDir"
mkdir -p "$outDir"

mkdir "$dataDir"
echo "$dataDir" | ./exec-lab.sh core 'hu.webarticum.holodb.core.lab.permutation.PermutationDumpMain'

mkdir "$compressedDir"

originalSize="$( stat -c '%s' "$( find "$dataDir" -name "*" -type f -print -quit )" )"
echo "Original size: ${originalSize}"

printf '%s' '
bzip2 -c "<SSS>" > "<TTT>.bz2"
gzip -c "<SSS>" > "<TTT>.gz"
zstd -19 -o "<TTT>.zstd" "{}"
brotli --quality=11 -o "<TTT>.brotli" "<SSS>"
xz -9 -c "<SSS>" > "<TTT>.xz"
zpaq a "<TTT>.zpaq" "<SSS>"
paq8px -4 "<SSS>" "<TTT>.paq8"
' | while IFS=' ' read -r command optionsPattern; do
    if [ -n "$( printf '%s\n' "$command" | sed -E 's/^\s*#.*$//' )" ]; then
        specificCompressedDir="${compressedDir}/${command}"
        echo "$specificCompressedDir"
        mkdir "$specificCompressedDir"
        sourcePath="{}"
        targetPath="$specificCompressedDir"'/$(basename "{}")'
        sourcePathReplacement="$( printf '%s\n' "$sourcePath" | sed -e 's/[\/&]/\\&/g' )"
        targetPathReplacement="$( printf '%s\n' "$targetPath" | sed -e 's/[\/&]/\\&/g' )"
        shCommand="${command} $( printf '%s' "$optionsPattern" | sed -E "s/<SSS>/${sourcePathReplacement}/g" | sed -E "s/<TTT>/${targetPathReplacement}/g" )"
        find "$dataDir" -maxdepth 1 -type f -print0 | xargs -0 -I {} sh -c "$shCommand"
    fi
done


cd "$startDir" || {
    echo "Failed to cd to startDir=${startDir}"
    exit 1
}
