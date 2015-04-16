/*
 * Copyright © 2014 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package co.cask.cdap.proto;

import com.google.common.base.Objects;
import com.google.gson.annotations.SerializedName;

import javax.annotation.Nullable;

/**
 * This class records information for a particular run.
 */
public final class RunRecord {
  @SerializedName("runid")
  private final String pid;

  @SerializedName("start")
  private final long startTs;

  @SerializedName("end")
  private final Long stopTs;

  @SerializedName("status")
  private final ProgramRunStatus status;

  @SerializedName("adapter")
  private final String adapterName;

  public RunRecord(String pid, long startTs, Long stopTs, ProgramRunStatus status, @Nullable String adapterName) {
    this.pid = pid;
    this.startTs = startTs;
    this.stopTs = stopTs;
    this.status = status;
    this.adapterName = adapterName;
  }

  public RunRecord(String pid, long startTs, Long stopTs, ProgramRunStatus status) {
    this(pid, startTs, stopTs, status, null);
  }

  public RunRecord(RunRecord started, long stopTs, ProgramRunStatus status) {
    this(started.pid, started.startTs, stopTs, status, started.getAdapterName());
  }

  public String getPid() {
    return pid;
  }

  public long getStartTs() {
    return startTs;
  }

  public long getStopTs() {
    return stopTs;
  }

  public ProgramRunStatus getStatus() {
    return status;
  }

  @Nullable
  public String getAdapterName() {
    return adapterName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    RunRecord that = (RunRecord) o;

    return Objects.equal(this.pid, that.pid) &&
      Objects.equal(this.startTs, that.startTs) &&
      Objects.equal(this.stopTs, that.stopTs) &&
      Objects.equal(this.status, that.status) &&
      Objects.equal(this.adapterName, that.adapterName);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(pid, startTs, stopTs, status, adapterName);
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this)
      .add("pid", pid)
      .add("startTs", startTs)
      .add("stopTs", stopTs)
      .add("status", status)
      .add("adapter", adapterName)
      .toString();
  }
}
