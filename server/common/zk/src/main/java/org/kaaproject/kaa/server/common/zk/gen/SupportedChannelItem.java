/*
 * Copyright 2014 CyberVision, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kaaproject.kaa.server.common.zk.gen;
@SuppressWarnings("all")
@org.apache.avro.specific.AvroGenerated
public class SupportedChannelItem extends org.apache.avro.specific.SpecificRecordBase implements org.apache.avro.specific.SpecificRecord {
  public static final org.apache.avro.Schema SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"SupportedChannelItem\",\"namespace\":\"org.kaaproject.kaa.server.common.zk.gen\",\"fields\":[{\"name\":\"SupportedChannel\",\"type\":{\"type\":\"record\",\"name\":\"ZkSupportedChannel\",\"fields\":[{\"name\":\"ChannelType\",\"type\":{\"type\":\"enum\",\"name\":\"ZkChannelType\",\"symbols\":[\"HTTP\",\"HTTP_LP\"]}},{\"name\":\"CommunicationParameters\",\"type\":[{\"type\":\"record\",\"name\":\"ZkHttpComunicationParameters\",\"fields\":[{\"name\":\"ZkComunicationParameters\",\"type\":{\"type\":\"record\",\"name\":\"IpComunicationParameters\",\"fields\":[{\"name\":\"HostName\",\"type\":\"string\"},{\"name\":\"Port\",\"type\":\"int\"}]}}]},{\"type\":\"record\",\"name\":\"ZkHttpLpComunicationParameters\",\"fields\":[{\"name\":\"ZkComunicationParameters\",\"type\":\"IpComunicationParameters\"}]}]},{\"name\":\"ChannelStatistics\",\"type\":[{\"type\":\"record\",\"name\":\"ZkHttpStatistics\",\"fields\":[{\"name\":\"ZkStatistics\",\"type\":{\"type\":\"record\",\"name\":\"BaseStatistics\",\"fields\":[{\"name\":\"processedRequestCount\",\"type\":\"int\"},{\"name\":\"registeredUsersCount\",\"type\":\"int\"},{\"name\":\"deltaCalculationCount\",\"type\":\"int\"},{\"name\":\"timeStarted\",\"type\":\"long\"}]}}]},{\"type\":\"record\",\"name\":\"ZkHttpLpStatistics\",\"fields\":[{\"name\":\"ZkStatistics\",\"type\":\"BaseStatistics\"}]}]}]}}]}");
  public static org.apache.avro.Schema getClassSchema() { return SCHEMA$; }
   private org.kaaproject.kaa.server.common.zk.gen.ZkSupportedChannel SupportedChannel;

  /**
   * Default constructor.  Note that this does not initialize fields
   * to their default values from the schema.  If that is desired then
   * one should use {@link \#newBuilder()}.
   */
  public SupportedChannelItem() {}

  /**
   * All-args constructor.
   */
  public SupportedChannelItem(org.kaaproject.kaa.server.common.zk.gen.ZkSupportedChannel SupportedChannel) {
    this.SupportedChannel = SupportedChannel;
  }

  @Override
public org.apache.avro.Schema getSchema() { return SCHEMA$; }
  // Used by DatumWriter.  Applications should not call.
  @Override
public java.lang.Object get(int field$) {
    switch (field$) {
    case 0: return SupportedChannel;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }
  // Used by DatumReader.  Applications should not call.
  @Override
@SuppressWarnings(value="unchecked")
  public void put(int field$, java.lang.Object value$) {
    switch (field$) {
    case 0: SupportedChannel = (org.kaaproject.kaa.server.common.zk.gen.ZkSupportedChannel)value$; break;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }

  /**
   * Gets the value of the 'SupportedChannel' field.
   */
  public org.kaaproject.kaa.server.common.zk.gen.ZkSupportedChannel getSupportedChannel() {
    return SupportedChannel;
  }

  /**
   * Sets the value of the 'SupportedChannel' field.
   * @param value the value to set.
   */
  public void setSupportedChannel(org.kaaproject.kaa.server.common.zk.gen.ZkSupportedChannel value) {
    this.SupportedChannel = value;
  }

  /** Creates a new SupportedChannelItem RecordBuilder */
  public static org.kaaproject.kaa.server.common.zk.gen.SupportedChannelItem.Builder newBuilder() {
    return new org.kaaproject.kaa.server.common.zk.gen.SupportedChannelItem.Builder();
  }

  /** Creates a new SupportedChannelItem RecordBuilder by copying an existing Builder */
  public static org.kaaproject.kaa.server.common.zk.gen.SupportedChannelItem.Builder newBuilder(org.kaaproject.kaa.server.common.zk.gen.SupportedChannelItem.Builder other) {
    return new org.kaaproject.kaa.server.common.zk.gen.SupportedChannelItem.Builder(other);
  }

  /** Creates a new SupportedChannelItem RecordBuilder by copying an existing SupportedChannelItem instance */
  public static org.kaaproject.kaa.server.common.zk.gen.SupportedChannelItem.Builder newBuilder(org.kaaproject.kaa.server.common.zk.gen.SupportedChannelItem other) {
    return new org.kaaproject.kaa.server.common.zk.gen.SupportedChannelItem.Builder(other);
  }

  /**
   * RecordBuilder for SupportedChannelItem instances.
   */
  public static class Builder extends org.apache.avro.specific.SpecificRecordBuilderBase<SupportedChannelItem>
    implements org.apache.avro.data.RecordBuilder<SupportedChannelItem> {

    private org.kaaproject.kaa.server.common.zk.gen.ZkSupportedChannel SupportedChannel;

    /** Creates a new Builder */
    private Builder() {
      super(org.kaaproject.kaa.server.common.zk.gen.SupportedChannelItem.SCHEMA$);
    }

    /** Creates a Builder by copying an existing Builder */
    private Builder(org.kaaproject.kaa.server.common.zk.gen.SupportedChannelItem.Builder other) {
      super(other);
      if (isValidValue(fields()[0], other.SupportedChannel)) {
        this.SupportedChannel = data().deepCopy(fields()[0].schema(), other.SupportedChannel);
        fieldSetFlags()[0] = true;
      }
    }

    /** Creates a Builder by copying an existing SupportedChannelItem instance */
    private Builder(org.kaaproject.kaa.server.common.zk.gen.SupportedChannelItem other) {
            super(org.kaaproject.kaa.server.common.zk.gen.SupportedChannelItem.SCHEMA$);
      if (isValidValue(fields()[0], other.SupportedChannel)) {
        this.SupportedChannel = data().deepCopy(fields()[0].schema(), other.SupportedChannel);
        fieldSetFlags()[0] = true;
      }
    }

    /** Gets the value of the 'SupportedChannel' field */
    public org.kaaproject.kaa.server.common.zk.gen.ZkSupportedChannel getSupportedChannel() {
      return SupportedChannel;
    }

    /** Sets the value of the 'SupportedChannel' field */
    public org.kaaproject.kaa.server.common.zk.gen.SupportedChannelItem.Builder setSupportedChannel(org.kaaproject.kaa.server.common.zk.gen.ZkSupportedChannel value) {
      validate(fields()[0], value);
      this.SupportedChannel = value;
      fieldSetFlags()[0] = true;
      return this;
    }

    /** Checks whether the 'SupportedChannel' field has been set */
    public boolean hasSupportedChannel() {
      return fieldSetFlags()[0];
    }

    /** Clears the value of the 'SupportedChannel' field */
    public org.kaaproject.kaa.server.common.zk.gen.SupportedChannelItem.Builder clearSupportedChannel() {
      SupportedChannel = null;
      fieldSetFlags()[0] = false;
      return this;
    }

    @Override
    public SupportedChannelItem build() {
      try {
        SupportedChannelItem record = new SupportedChannelItem();
        record.SupportedChannel = fieldSetFlags()[0] ? this.SupportedChannel : (org.kaaproject.kaa.server.common.zk.gen.ZkSupportedChannel) defaultValue(fields()[0]);
        return record;
      } catch (Exception e) {
        throw new org.apache.avro.AvroRuntimeException(e);
      }
    }
  }
}
