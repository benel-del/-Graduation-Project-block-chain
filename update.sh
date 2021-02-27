#!/bin/bash

find /usr/local/lib/apache-tomcat-9.0.43/logs -name "catalina.2*" -mmin -1 -ls > /home/centos/eclipse-workspace/block/update.txt
find /usr/local/lib/apache-tomcat-9.0.43/logs -name "localhost_access_log.*" -mmin -1 -ls >> /home/centos/eclipse-workspace/block/update.txt

find /usr/local/lib/apache-tomcat-9.0.43/logs -name "catalina.2*" -ls > /home/centos/eclipse-workspace/block/files.txt
find /usr/local/lib/apache-tomcat-9.0.43/logs -name "localhost_access_log.*" -ls >> /home/centos/eclipse-workspace/block/files.txt
