
set JAVA_OPTS=%JAVA_OPTS% -Xms256m -Xmx512m -XX:PermSize=512m -XX:MaxPermSize=512m

set GRAILS_OPTS=-server -Xmx1024M -Xms256M -XX:PermSize=512m -XX:MaxPermSize=512m

grails -Ddisable.auto.recompile=true run-app
