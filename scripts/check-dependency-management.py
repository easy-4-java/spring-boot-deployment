"""Check Maven evidence without running packaging or deploying anything."""
from pathlib import Path
import xml.etree.ElementTree as ET

ns = {"m": "http://maven.apache.org/POM/4.0.0"}
root = ET.parse("target/effective-pom.xml").getroot()
# Aggregator help output wraps all three projects in <projects>.
projects = root.findall("m:project", ns)
assert len(projects) == 3, "Missing reactor effective POMs"
for project in projects:
    dependencies = project.findall("m:dependencyManagement/m:dependencies/m:dependency", ns)
    versions = [d.findtext("m:version", namespaces=ns) for d in dependencies
                if d.findtext("m:groupId", namespaces=ns) == "commons-io"
                and d.findtext("m:artifactId", namespaces=ns) == "commons-io"]
    assert versions == ["2.22.0"], versions
for directory in (Path("."), Path("spring-boot-deployment-app"), Path("spring-boot-deployment-docker")):
    tree = (directory / "target/dependency-tree.txt").read_text()
    assert "commons-io:commons-io:" not in tree, tree
    print(str(directory) + ": manages Commons IO 2.22.0; no actual Commons IO dependency")

# Both application modules must use one aligned Log4j backend and the SLF4J 1.x bridge.
for module in ("spring-boot-deployment-app", "spring-boot-deployment-docker"):
    tree = (Path(module) / "target/dependency-tree.txt").read_text()
    artifacts = ("log4j-api", "log4j-core", "log4j-jul", "log4j-web", "log4j-slf4j-impl")
    for artifact in artifacts:
        assert "org.apache.logging.log4j:" + artifact + ":jar:2.26.1:compile" in tree, tree
    assert "log4j-to-slf4j:" not in tree, "Circular logging bridge"
    assert "logback-classic:" not in tree, "Competing logging backend"
    print(module + ": aligned Log4j 2.26.1 with SLF4J 1.x bridge")
