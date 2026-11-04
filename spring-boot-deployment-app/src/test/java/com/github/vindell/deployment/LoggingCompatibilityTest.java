package com.github.vindell.deployment;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.springframework.boot.logging.LoggingInitializationContext;
import org.springframework.boot.logging.LoggingSystem;
import org.springframework.boot.logging.log4j2.Log4J2LoggingSystem;
import org.springframework.core.env.StandardEnvironment;
import static org.junit.Assert.*;

public class LoggingCompatibilityTest {
    @Test
    public void bootLoggingAndLegacyFacadesReachLog4jCore() {
        LoggingSystem system = LoggingSystem.get(getClass().getClassLoader());
        assertTrue(system instanceof Log4J2LoggingSystem);
        system.beforeInitialize();
        system.initialize(new LoggingInitializationContext(new StandardEnvironment()), null, null);
        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        final List<String> messages = new ArrayList<>();
        AbstractAppender appender = new AbstractAppender("capture", null, null, false, Property.EMPTY_ARRAY) {
            @Override public void append(LogEvent event) {
                messages.add(org.apache.logging.log4j.core.layout.PatternLayout.createDefaultLayout()
                        .toSerializable(event).trim());
            }
        };
        appender.start();
        context.getRootLogger().addAppender(appender);
        try {
            assertEquals("org.apache.logging.slf4j.Log4jLoggerFactory",
                    org.slf4j.LoggerFactory.getILoggerFactory().getClass().getName());
            LogManager.getLogger(getClass()).info("direct-log4j");
            org.slf4j.LoggerFactory.getLogger(getClass()).info("legacy-slf4j");
            org.apache.commons.logging.LogFactory.getLog(getClass()).info("spring-jcl");
            java.util.logging.Logger.getLogger(getClass().getName()).info("jul-bridge");
            assertTrue(messages.toString(), messages.contains("direct-log4j"));
            assertTrue(messages.toString(), messages.contains("legacy-slf4j"));
            assertTrue(messages.toString(), messages.contains("spring-jcl"));
            assertTrue(messages.toString(), messages.contains("jul-bridge"));
            assertFalse(org.apache.logging.log4j.core.net.JndiManager.isJndiLookupEnabled());
            String literal = "${java:version}";
            LogManager.getLogger(getClass()).info(literal);
            assertTrue(messages.contains(literal));
        } finally {
            context.getRootLogger().removeAppender(appender);
            appender.stop();
            system.cleanUp();
        }
    }
}
