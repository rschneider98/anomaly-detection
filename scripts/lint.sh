#!/bin/bash

# This runs the linter for the project. 
# This file should be run from the root of the project directory.

# NOTE: Calling the linter requires the files
# to be compiliable; AND any side-effects (notably tests)
# can occur: database connections, file editing, etc.
# See https://github.com/jonase/eastwood#usage-1 for more info.

mkdir -p debug

# check syntax of project files (does not check tests) to
# ensure project can compile
lein check > debug/syntax_check.txt

if [$? != 0] ; then
    echo "Compiling: Leiningen syntax check found errors"
    exit 1
fi

# lint the project and test files
# Clojure linter
lein eastwood > debug/lint_eastwood.txt

# if eastwood created output/warnings, there is a non-zero exit code
if [$? != 0] ; then
    echo "Linting: Eastwood produced warnings"
    exit 1
fi

# lint the project and test files 
# file formatting linter
lein bikeshed > debug/lint_bikeshed.txt

# if all files pass inspection, the last line is "Success"
if [$(awk '/^Success$/' debug/lint_bikeshed.txt)] ; then
    echo "Linting: Bikeshed produced warnings"
    exit 1
fi