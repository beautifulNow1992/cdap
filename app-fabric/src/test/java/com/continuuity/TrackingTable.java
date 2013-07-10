package com.continuuity;

import com.continuuity.api.data.DataSet;
import com.continuuity.api.data.DataSetSpecification;
import com.continuuity.api.data.OperationException;
import com.continuuity.api.data.batch.BatchReadable;
import com.continuuity.api.data.batch.BatchWritable;
import com.continuuity.api.data.batch.Split;
import com.continuuity.api.data.batch.SplitReader;
import com.continuuity.api.data.dataset.KeyValueTable;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

/**
 * A key value table that tracks how often it is opened and closed, read and written.
 */
public class TrackingTable extends DataSet implements BatchReadable<byte[], byte[]>, BatchWritable<byte[], byte[]> {

  // some counters that are incremented by the table's operations and verified by the unit test.
  // the following is ugly. but there is no way to share a static counter or similar between the unit test and the
  // flowlets/procedures/etc, because those are loaded in a different class loader. So we use a global thing like the
  // system properties to count.

  public synchronized static int getTracker(String table, String op) {
    String key = table + "-" + op;
    String value = System.getProperty(key, "0");
    return Integer.valueOf(value);
  }
  private synchronized static void track(String table, String op) {
    String key = table + "-" + op;
    String value = System.getProperty(key, "0");
    String newValue = Integer.toString(Integer.valueOf(value) + 1);
    System.setProperty(key, newValue);
  }
  public synchronized static void resetTracker() {
    for (String table : Arrays.asList("foo", "bar")) {
      for (String op : Arrays.asList("open", "close", "read", "write", "split")) {
        String key = table + "-" + op;
        System.clearProperty(key);
      }
    }
  }

  KeyValueTable t;

  public TrackingTable(String name) {
    super(name);
    t = new KeyValueTable(name);
  }

  public TrackingTable(DataSetSpecification spec) {
    super(spec);
    t = new KeyValueTable(spec.getSpecificationFor(getName()));
    track(getName(), "open");
  }

  @Override
  public DataSetSpecification configure() {
    return new DataSetSpecification.Builder(this).dataset(t.configure()).create();
  }

  @Override
  public void close() {
    track(getName(), "close");
  }

  @Nullable
  public byte[] read(byte[] key) throws OperationException {
    track(getName(), "read");
    return t.read(key);
  }

  @Override
  public void write(byte[] key, byte[] value) throws OperationException {
    track(getName(), "write");
    t.write(key, value);
  }

  @Override
  public SplitReader<byte[], byte[]> createSplitReader(Split split) {
    return t.createSplitReader(split);
  }

  @Override
  public List<Split> getSplits() throws OperationException {
    track(getName(), "split");
    return t.getSplits(1, null, null); // return a single split for testing
  }
}
