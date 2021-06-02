CLASSPATH =.
# Data/RRD libraries:
CLASSPATH := $(CLASSPATH):lib/converter-2.2.jar
CLASSPATH := $(CLASSPATH):lib/rrd4j.jar
CLASSPATH := $(CLASSPATH):lib/commons-configuration-1.10.jar
CLASSPATH := $(CLASSPATH):lib/commons-lang-2.6.jar
# Logging:
CLASSPATH := $(CLASSPATH):lib/slf4j-api-1.7.5.jar
CLASSPATH := $(CLASSPATH):lib/slf4j-simple-1.7.5.jar
CLASSPATH := $(CLASSPATH):lib/jcl-over-slf4j-1.7.5.jar
# Jetty Web/Servlet server
CLASSPATH := $(CLASSPATH):lib/web/jetty-continuation-7.6.12.v20130726.jar
CLASSPATH := $(CLASSPATH):lib/web/jetty-http-7.6.12.v20130726.jar
CLASSPATH := $(CLASSPATH):lib/web/jetty-io-7.6.12.v20130726.jar
CLASSPATH := $(CLASSPATH):lib/web/jetty-security-7.6.12.v20130726.jar
CLASSPATH := $(CLASSPATH):lib/web/jetty-server-7.6.12.v20130726.jar
CLASSPATH := $(CLASSPATH):lib/web/jetty-servlet-7.6.12.v20130726.jar
CLASSPATH := $(CLASSPATH):lib/web/jetty-util-7.6.12.v20130726.jar
CLASSPATH := $(CLASSPATH):lib/web/jetty-webapp-7.6.12.v20130726.jar
CLASSPATH := $(CLASSPATH):lib/web/jetty-xml-7.6.12.v20130726.jar
CLASSPATH := $(CLASSPATH):lib/web/servlet-api.jar
CLASSPATH := $(CLASSPATH):lib/web/jsp/javax.el-2.1.0.v201105211819.jar
CLASSPATH := $(CLASSPATH):lib/web/jsp/javax.servlet.jsp-2.1.0.v201105211820.jar
CLASSPATH := $(CLASSPATH):lib/web/jsp/org.apache.jasper.glassfish-2.1.0.v201110031002.jar
# Jersey RESTfulness:
CLASSPATH := $(CLASSPATH):lib/web/jersey/jersey-container-servlet-core.jar
CLASSPATH := $(CLASSPATH):lib/web/jersey/jersey-container-servlet.jar
CLASSPATH := $(CLASSPATH):lib/web/jersey/jersey-common.jar
CLASSPATH := $(CLASSPATH):lib/web/jersey/jersey-server.jar
CLASSPATH := $(CLASSPATH):lib/web/jersey/jersey-client.jar
CLASSPATH := $(CLASSPATH):lib/web/jersey/javax.ws.rs-api-2.0.1.jar
CLASSPATH := $(CLASSPATH):lib/web/jersey/hk2-utils-2.5.0-b32.jar
CLASSPATH := $(CLASSPATH):lib/web/jersey/hk2-api-2.5.0-b32.jar
CLASSPATH := $(CLASSPATH):lib/web/jersey/hk2-locator-2.5.0-b32.jar
CLASSPATH := $(CLASSPATH):lib/web/jersey/hk2-utils-2.5.0-b32.jar
CLASSPATH := $(CLASSPATH):lib/web/jersey/jersey-guava-2.25.1.jar
CLASSPATH := $(CLASSPATH):lib/web/jersey/javax.inject-2.5.0-b32.jar
CLASSPATH := $(CLASSPATH):lib/web/jersey/javax.annotation-api-1.2.jar
CLASSPATH := $(CLASSPATH):lib/web/jersey/validation-api-1.1.0.Final.jar
# Jackson JSON data serialiser for Jersey
CLASSPATH := $(CLASSPATH):lib/web/jersey/jersey-media-json-jackson-2.25.jar
CLASSPATH := $(CLASSPATH):lib/web/jersey/jaxb-api-2.2.7.jar
CLASSPATH := $(CLASSPATH):lib/web/jersey/jackson-core-2.12.3.jar
CLASSPATH := $(CLASSPATH):lib/web/jersey/jackson-jaxrs-json-provider-2.12.3.jar
CLASSPATH := $(CLASSPATH):lib/web/jersey/jackson-jaxrs-base-2.12.3.jar
CLASSPATH := $(CLASSPATH):lib/web/jersey/jersey-entity-filtering-2.25.jar
CLASSPATH := $(CLASSPATH):lib/web/jersey/jackson-databind-2.7.4.jar
CLASSPATH := $(CLASSPATH):lib/web/jersey/jackson-annotations-2.7.0.jar
CLASSPATH := $(CLASSPATH):lib/web/jersey/jackson-module-jaxb-annotations-2.12.3.jar
# XML data serialising for Jersey
CLASSPATH := $(CLASSPATH):lib/web/jersey/jersey-media-jaxb.jar


JCFLAGS = -Xlint:unchecked -cp $(CLASSPATH)

JFLAGS = -cp $(CLASSPATH)

all:
	javac $(JCFLAGS) no/defcon/plem/Core.java

api:
	javac $(JCFLAGS) no/defcon/plem/rest/ApiTest.java no/defcon/plem/rest/ApiCore.java

tool:
	javac no/defcon/plem/tool/Command.java

run:
	java $(JFLAGS) no.defcon.plem.Core

clean:
	$(RM) no/defcon/plem/*.class no/defcon/plem/jetty/*.class no/defcon/plem/tool/*.class

api-clean:
	$(RM) no/defcon/plem/rest/*class

var-clean:
	$(RM) var/*jrrd
