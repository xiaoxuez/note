#!/bin/bash

storm jar $@
./storm_jar_add.rb $@
