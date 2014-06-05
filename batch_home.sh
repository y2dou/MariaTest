#!/bin/bash
CP=$HOME/MariaPrototype/bin:$HOME/MariaPrototype/lib/'*':$HOME/MariaPrototype/lib/generic/'*':$HOME/repastS/bin:$HOME/repastS/lib/'*'
cd $HOME/MariaPrototype
java -classpath $CP -Xss10M -Xms1000M -Xmx1000M repast.simphony.batch.BatchMain -params ./$1 ~/MariaPrototype/mariaprototype.rs

