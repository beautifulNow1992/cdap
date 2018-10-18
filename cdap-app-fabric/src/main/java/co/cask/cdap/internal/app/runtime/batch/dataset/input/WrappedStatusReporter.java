/*
 * Copyright © 2018 Cask Data, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License. You may obtain a copy of
 *  the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *  License for the specific language governing permissions and limitations under
 *  the License.
 */

package co.cask.cdap.internal.app.runtime.batch.dataset.input;

import org.apache.hadoop.mapreduce.Counter;
import org.apache.hadoop.mapreduce.StatusReporter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

/**
 * Wrapper status reporter
 */
public final class WrappedStatusReporter extends StatusReporter {

  private TaskAttemptContext context;

  WrappedStatusReporter(TaskAttemptContext context) {
    this.context = context;
  }

  @Override
  public Counter getCounter(Enum<?> name) {
    return context.getCounter(name);
  }

  @Override
  public Counter getCounter(String group, String name) {
    return context.getCounter(group, name);
  }

  @Override
  public void progress() {
    context.progress();
  }

  @Override
  public float getProgress() {
    return context.getProgress();
  }

  @Override
  public void setStatus(String status) {
    context.setStatus(status);
  }
}
