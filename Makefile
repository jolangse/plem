CLASSPATH = .:lib/converter-2.2.jar:lib/rrd4j.jar:lib/commons-configuration-1.10.jar:lib/commons-lang-2.6.jar:lib/web/jetty-continuation-7.6.12.v20130726.jar:lib/web/jetty-http-7.6.12.v20130726.jar:lib/web/jetty-io-7.6.12.v20130726.jar:lib/web/jetty-security-7.6.12.v20130726.jar:lib/web/jetty-server-7.6.12.v20130726.jar:lib/web/jetty-servlet-7.6.12.v20130726.jar:lib/web/jetty-util-7.6.12.v20130726.jar:lib/web/jetty-webapp-7.6.12.v20130726.jar:lib/web/jetty-xml-7.6.12.v20130726.jar:lib/web/servlet-api.jar:lib/web/jsp/javax.el-2.1.0.v201105211819.jar:lib/web/jsp/javax.servlet.jsp-2.1.0.v201105211820.jar:lib/web/jsp/org.apache.jasper.glassfish-2.1.0.v201110031002.jar:lib/slf4j-api-1.7.5.jar:lib/slf4j-simple-1.7.5.jar:lib/jcl-over-slf4j-1.7.5.jar

JCFLAGS = -Xlint:unchecked -cp $(CLASSPATH)

JFLAGS = -cp $(CLASSPATH)

all:
	javac $(JCFLAGS) no/defcon/plem/Core.java

tool:
	javac no/defcon/plem/tool/Command.java

run:
	java $(JFLAGS) no.defcon.plem.Core

clean:
	$(RM) no/defcon/plem/*.class no/defcon/plem/jetty/*.class no/defcon/plem/tool/*.class

var-clean:
	$(RM) var/*jrrd
