#!/bin/bash

mvn -f test-project/ROOT/parent clean install
mvn -f test-project/TEST/test-1 clean install
mvn -f test-project/TEST/test-2 clean install
mvn -f test-project/OTHER/test-3 clean install
