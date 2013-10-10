/*
 * Copyright 2012-2013 Continuuity,Inc. All Rights Reserved.
 */

package com.continuuity.logging.appender.file;

import ch.qos.logback.classic.spi.ILoggingEvent;
import com.continuuity.common.conf.CConfiguration;
import com.continuuity.common.logging.LoggingContext;
import com.continuuity.common.logging.LoggingContextAccessor;
import com.continuuity.logging.LoggingConfiguration;
import com.continuuity.logging.appender.LogAppender;
import com.continuuity.logging.serialize.LogSchema;
import com.continuuity.weave.filesystem.Location;
import com.continuuity.weave.filesystem.LocationFactory;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Logback appender that writes log events to files.
 */
public class FileLogAppender extends LogAppender {
  private static final Logger LOG = LoggerFactory.getLogger(FileLogAppender.class);

  public static final String APPENDER_NAME = "FileLogAppender";

  private final Location logBaseDir;
  private final long logFileRotationIntervalMs;
  private final int syncIntervalBytes;
  private final long retentionDurationMs;

  private LogFileWriter logFileWriter;

  @Inject
  public FileLogAppender(CConfiguration cConfig, LocationFactory locationFactory) {
    setName(APPENDER_NAME);

    String baseDir = cConfig.get(LoggingConfiguration.LOG_BASE_DIR);
    Preconditions.checkNotNull(baseDir, "Log base dir cannot be null");
    this.logBaseDir = locationFactory.create(baseDir);

    float rotationMins = cConfig.getFloat(LoggingConfiguration.LOG_FILE_ROTATION_INTERVAL_MINS,
                                      TimeUnit.MINUTES.convert(1, TimeUnit.DAYS));
    Preconditions.checkArgument(rotationMins > 0, "Log file rotation interval is invalid: %s", rotationMins);
    this.logFileRotationIntervalMs = (long) (rotationMins * 60 * 1000);

    this.syncIntervalBytes = cConfig.getInt(LoggingConfiguration.LOG_FILE_SYNC_INTERVAL_BYTES, 5 * 1024 * 1024);
    Preconditions.checkArgument(this.syncIntervalBytes > 0,
                                "Log file sync interval is invalid: %s", this.syncIntervalBytes);

    long retentionDurationDays = cConfig.getLong(LoggingConfiguration.LOG_RETENTION_DURATION_DAYS, -1);
    Preconditions.checkArgument(retentionDurationDays > 0,
                                "Log file retention duration is invalid: %s", retentionDurationDays);
    this.retentionDurationMs = TimeUnit.MILLISECONDS.convert(retentionDurationDays, TimeUnit.DAYS);
  }

  @Override
  public void start() {
    super.start();
    try {
      logFileWriter = new LogFileWriter(logBaseDir, new LogSchema().getAvroSchema(),
                                        syncIntervalBytes, logFileRotationIntervalMs, retentionDurationMs);
    } catch (IOException e) {
      close();
      throw Throwables.propagate(e);
    }
  }

  @Override
  protected void append(ILoggingEvent eventObject) {
    LoggingContext loggingContext = LoggingContextAccessor.getLoggingContext();
    if (loggingContext == null) {
      return;
    }

    try {
      logFileWriter.append(eventObject);
    } catch (Throwable t) {
      LOG.error("Got exception while serializing log event {}.", eventObject, t);
    }
  }

  private void close() {
    try {
      if (logFileWriter != null) {
        logFileWriter.close();
      }
    } catch (IOException e) {
      throw Throwables.propagate(e);
    }
  }

  @Override
  public void stop() {
    super.stop();
    close();
  }

  LogFileWriter getLogFileWriter() {
    return logFileWriter;
  }
}
