#!/bin/bash

# Run tests for project as part of git hooks
# Use profile to fail fast (at first failure) and output dots
lein kaocha/v1 {} --profile :check