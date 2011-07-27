#!/bin/bash

export GRAILS_OPTS="-server -Xmx1024M -Xms256M -XX:MaxPermSize=256m"

grails -Ddisable.auto.recompile=true run-app
