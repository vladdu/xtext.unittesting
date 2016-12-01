# Developer Guide

## Release Process

* Run launch config `xtext-testing - set version - tycho.launch`
* Enter current version (will set to release version)
* Commit changes
* Run launch config `xtext-testing - release - clean package.launch`
* Run launch config `xtext-testing - set version - tycho.launch`
* Enter next version
* Commit changes

## Build

http://www.lorenzobettini.it/2015/01/creating-p2-composite-repositories-during-the-build/
