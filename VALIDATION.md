# Non-deployment validation

The parent manages Commons IO 2.22.0, which Apache documents as Java 8 compatible:
https://commons.apache.org/proper/commons-io/changes.html
https://commons.apache.org/proper/commons-io/security.html

The effective POM previously managed 2.5. The actual dependency trees contain no
Commons IO dependency in any of the three reactor projects. This is preventive
version-management maintenance, not evidence of runtime exploitability.

Run from the repository root:

```sh
mvn -B --no-transfer-progress clean test
mvn -B --no-transfer-progress help:effective-pom -Doutput=target/effective-pom.xml
mvn -B --no-transfer-progress dependency:tree -DoutputFile=target/dependency-tree.txt
python3 scripts/check-dependency-management.py
```

Do not substitute `package`, `verify`, `install`, or `deploy`: the Docker module
binds image build/tag to package and push to deploy. The test lifecycle stops
before those phases. The controller tests use standalone MockMvc and do not
start the application, connect to a Docker daemon, or deploy services.

CI executes the four route regressions and two logging compatibility tests on Java 8, 17, and 21 and archives Maven
evidence. Boot 2.1.4, public production sources, JMX, and deployment settings are
unchanged. No claim is made that all other old dependencies are secure.

## Log4j follow-up

Both applications previously resolved log4j-core 2.11.2 at compile scope, in the
Apache CVE-2021-44228 affected range. This dependency finding does not prove an
exploitable input path in a deployment. The Boot parent `log4j2.version` override
aligns API, Core, JUL, Web and the SLF4J 1.x bridge to 2.26.1. SLF4J remains 1.7.25;
no SLF4J 2.x or Jakarta bridge migration is required.

Official release, requirements and security references:
https://logging.apache.org/log4j/2.x/release-notes.html
https://logging.apache.org/log4j/2.x/manual/installation.html
https://logging.apache.org/security.html

The logging tests initialize Boot 2.1's actual Log4j2LoggingSystem and capture
messages through Log4j, SLF4J, Spring JCL and JUL. They verify message lookup
syntax remains literal and the default JNDI lookup is disabled. No network
lookup, application server or container is started. Dependency checks enforce
aligned modules without a competing Logback backend or reverse SLF4J bridge.
