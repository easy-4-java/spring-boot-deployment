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

CI executes the four route regressions on Java 8, 17, and 21 and archives Maven
evidence. Boot 2.1.4, public production sources, JMX, and deployment settings are
unchanged. No claim is made that all other old dependencies are secure.
