# Developer Resources for Project

## Guide to Environment Setup and Tooling
This project uses `Leiningen` for handling the project and dependencies. Additionally, `Eastwood` and `Bikeshed` are used for linting as part of pre-commits. The documentation is managed by `cljdoc`, and the `clojure.test` functions are run through the testing suite `Kaocha`. 

Prequistes:
- Git
- Bash (emulator, if on Windows)
- Java 8 or 11 
- Clojure 1.10.3 (see https://clojure.org/guides/getting_started for installation instructions)
- Leiningen 2.4 or 2.6.1 (see https://leiningen.org/ for installation instructions)

### Eastwood
Eastwood can be used with Leiningen to run linting as needed. The `project.clj` file already references Eastwood so that Leiningen knows what to use. Eastwood linting is done as part of the pre-commit hooks, but can also be run separately by executing either `sh scripts/lint.sh` or `lein eastwood` from the project's root directory. 

### Bikeshed
Bikeshed is referenced in the project file and is called by the pre-commit hook. It can be run separately in the project root with either `sh scripts/lint.sh` or `lein bikeshed`.


## Documentation
TODO: This project uses the docstrings and static files to automatically create documentation.

## Testing

### Profiles for Different Versions of Clojure
TODO: Set up testing for multiple versions of clojure and dependencies. Requires Clojure >= 1.10 due to dependency on Ubergraph. See: https://github.com/technomancy/leiningen/blob/master/doc/PROFILES.md

### Coverage Testing for Project
TODO: https://github.com/cloverage/cloverage

### Docstring Checks for All Public Variables
TODO: https://github.com/camsaul/lein-docstring-checker