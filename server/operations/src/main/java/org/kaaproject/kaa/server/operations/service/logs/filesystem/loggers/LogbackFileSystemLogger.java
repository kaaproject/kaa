package org.kaaproject.kaa.server.operations.service.logs.filesystem.loggers;

import java.io.IOException;
import java.nio.file.Path;

import org.kaaproject.kaa.common.dto.logs.LogAppenderDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;

@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Component
public class LogbackFileSystemLogger implements FileSystemLogger {
    private static final Logger LOG = LoggerFactory.getLogger(LogbackFileSystemLogger.class);

    private static final String DEFAULT_ROLLING_FILE_NAME_PATTERN = "logFile.%d{yyyy-MM-dd}.log";
    private static final Integer DEFAULT_ROLLING_MAX_HOSTORY = 30;
    private static final String DEFAULT_TRIGGER_MAX_FILE_SIZE = "1GB";
    private static final String DEFAULT_ENCODER_PATTERN = "%-4relative [%thread] %-5level %logger{35} - %msg%n";

    @Value("#{properties[rolling_filename_pattern]}")
    private String rollingFileNamePatern = DEFAULT_ROLLING_FILE_NAME_PATTERN;
    @Value("#{properties[rolling_max_history]}")
    private Integer rollingMaxHistory = DEFAULT_ROLLING_MAX_HOSTORY;
    @Value("#{properties[trigger_max_file_size]}")
    private String triggerMaxFileSize = DEFAULT_TRIGGER_MAX_FILE_SIZE;
    @Value("#{properties[encoder_pattern]}")
    private String encoderPattern = DEFAULT_ENCODER_PATTERN;

    private ch.qos.logback.classic.Logger logger;
    private RollingFileAppender rfAppender;

    @Override
    public void close() throws IOException {
        rfAppender.stop();
    }

    @Override
    public void init(LogAppenderDto appenderDto, Path filePath) {
        LOG.info(
                "[{}][{}][{}] Initializing with rollingFileNamePatern: {}, rollingMaxHistory: {}, triggerMaxFileSize: {}, encoderPattern: {}",
                appenderDto.getTenantId(), appenderDto.getApplicationId(), appenderDto.getSchemaVersion(),
                rollingFileNamePatern, rollingMaxHistory, triggerMaxFileSize, encoderPattern);
        
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        rfAppender = new RollingFileAppender();
        rfAppender.setContext(loggerContext);
        rfAppender.setFile(filePath.toAbsolutePath().toString());
        TimeBasedRollingPolicy rollingPolicy = new TimeBasedRollingPolicy<>();
        rollingPolicy.setFileNamePattern(rollingFileNamePatern);
        rollingPolicy.setMaxHistory(rollingMaxHistory);
        rollingPolicy.setContext(loggerContext);
        rollingPolicy.start();

        SizeBasedTriggeringPolicy triggeringPolicy = new ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy();
        triggeringPolicy.setMaxFileSize(triggerMaxFileSize);
        triggeringPolicy.start();

        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(loggerContext);
        encoder.setPattern(encoderPattern);
        encoder.start();

        rfAppender.setEncoder(encoder);
        rfAppender.setRollingPolicy(rollingPolicy);
        rfAppender.setTriggeringPolicy(triggeringPolicy);
        rfAppender.start();

        logger = loggerContext.getLogger(appenderDto.getTenantId() + "." + appenderDto.getApplicationToken());
        logger.setLevel(Level.ALL);
        logger.addAppender(rfAppender);
        LOG.debug("[{}][{}][{}] Initialized with context {}", appenderDto.getTenantId(),
                appenderDto.getApplicationId(), appenderDto.getSchemaVersion(), loggerContext);
    }

    @Override
    public void append(String event) {
        logger.info(event);
    }
}
