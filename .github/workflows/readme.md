# Workflow Description

Workflows for software quality check, build, packaging, and release creation were established:

## build-publish-latest.yml

Build and publish latest image to test docker builds and pushes on your own branch. Includes a build number that can be used to force Helm installation with specific build

## createRelease.yml

Creates a new release for jar (pom), github release documentation, docker image.

* You can choose a major, minor or patch update (default is patch)
* The following steps are taken
  * Set release version
  * Tag commit
  * Create github release
  * Build and push docker image

## pr-build.yml

Minimal checks and build that are run for any pull request
