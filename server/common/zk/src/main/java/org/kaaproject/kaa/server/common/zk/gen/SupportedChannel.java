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
public class SupportedChannel extends org.apache.avro.specific.SpecificRecordBase implements org.apache.avro.specific.SpecificRecord {
  public static final org.apache.avro.Schema SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"SupportedChannel\",\"namespace\":\"org.kaaproject.kaa.server.common.zk.gen\",\"fields\":[{\"name\":\"ZkChannel\",\"type\":{\"type\":\"record\",\"name\":\"ZkSupportedChannel\",\"fields\":[{\"name\":\"ChannelType\",\"type\":{\"type\":\"enum\",\"name\":\"ZkChannelType\",\"symbols\":[\"HTTP\",\"HTTP_LP\",\"KAATCP\"]}},{\"name\":\"RedirectionSupported\",\"type\":\"boolean\"},{\"name\":\"CommunicationParameters\",\"type\":[{\"type\":\"record\",\"name\":\"ZkHttpComunicationParameters\",\"fields\":[{\"name\":\"ZkComunicationParameters\",\"type\":{\"type\":\"record\",\"name\":\"IpComunicationParameters\",\"fields\":[{\"name\":\"HostName\",\"type\":\"string\"},{\"name\":\"Port\",\"type\":\"int\"}]}}]},{\"type\":\"record\",\"name\":\"ZkHttpLpComunicationParameters\",\"fields\":[{\"name\":\"ZkComunicationParameters\",\"type\":\"IpComunicationParameters\"}]},{\"type\":\"record\",\"name\":\"ZkKaaTcpComunicationParameters\",\"fields\":[{\"name\":\"ZkComunicationParameters\",\"type\":\"IpComunicationParameters\"}]}]},{\"name\":\"ChannelStatistics\",\"type\":[{\"type\":\"record\",\"name\":\"ZkHttpStatistics\",\"fields\":[{\"name\":\"ZkStatistics\",\"type\":{\"type\":\"record\",\"name\":\"BaseStatistics\",\"fields\":[{\"name\":\"processedRequestCount\",\"type\":\"int\"},{\"name\":\"registeredUsersCount\",\"type\":\"int\"},{\"name\":\"deltaCalculationCount\",\"type\":\"int\"},{\"name\":\"timeStarted\",\"type\":\"long\"}]}}]},{\"type\":\"record\",\"name\":\"ZkHttpLpStatistics\",\"fields\":[{\"name\":\"ZkStatistics\",\"type\":\"BaseStatistics\"}]},{\"type\":\"record\",\"name\":\"ZkKaaTcpStatistics\",\"fields\":[{\"name\":\"ZkStatistics\",\"type\":\"BaseStatistics\"}]}]}]}}]}");
  public static org.apache.avro.Schema getClassSchema() { return SCHEMA$; }
   private org.kaaproject.kaa.server.common.zk.gen.ZkSupportedChannel ZkChannel;

  /**
   * Default constructor.  Note that this does not initialize fields
   * to their default values from the schema.  If that is desired then
   * one should use {@link \#newBuilder()}.
   */
  public SupportedChannel() {}

  /**
   * All-args constructor.
   */
  public SupportedChannel(org.kaaproject.kaa.server.common.zk.gen.ZkSupportedChannel ZkChannel) {
    this.ZkChannel = ZkChannel;
  }

  @Override
public org.apache.avro.Schema getSchema() { return SCHEMA$; }
  // Used by DatumWriter.  Applications should not call.
  @Override
public java.lang.Object get(int field$) {
    switch (field$) {
    case 0: return ZkChannel;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }
  // Used by DatumReader.  Applications should not call.
  @Override
@SuppressWarnings(value="unchecked")
  public void put(int field$, java.lang.Object value$) {
    switch (field$) {
    case 0: ZkChannel = (org.kaaproject.kaa.server.common.zk.gen.ZkSupportedChannel)value$; break;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }

  /**
   * Gets the value of the 'ZkChannel' field.
   */
  public org.kaaproject.kaa.server.common.zk.gen.ZkSupportedChannel getZkChannel() {
    return ZkChannel;
  }

  /**
   * Sets the value of the 'ZkChannel' field.
   * @param value the value to set.
   */
  public void setZkChannel(org.kaaproject.kaa.server.common.zk.gen.ZkSupportedChannel value) {
    this.ZkChannel = value;
  }

  /** Creates a new SupportedChannel RecordBuilder */
  public static org.kaaproject.kaa.server.common.zk.gen.SupportedChannel.Builder newBuilder() {
    return new org.kaaproject.kaa.server.common.zk.gen.SupportedChannel.Builder();
  }

  /** Creates a new SupportedChannel RecordBuilder by copying an existing Builder */
  public static org.kaaproject.kaa.server.common.zk.gen.SupportedChannel.Builder newBuilder(org.kaaproject.kaa.server.common.zk.gen.SupportedChannel.Builder other) {
    return new org.kaaproject.kaa.server.common.zk.gen.SupportedChannel.Builder(other);
  }

  /** Creates a new SupportedChannel RecordBuilder by copying an existing SupportedChannel instance */
  public static org.kaaproject.kaa.server.common.zk.gen.SupportedChannel.Builder newBuilder(org.kaaproject.kaa.server.common.zk.gen.SupportedChannel other) {
    return new org.kaaproject.kaa.server.common.zk.gen.SupportedChannel.Builder(other);
  }

  /**
   * RecordBuilder for SupportedChannel instances.
   */
  public static class Builder extends org.apache.avro.specific.SpecificRecordBuilderBase<SupportedChannel>
    implements org.apache.avro.data.RecordBuilder<SupportedChannel> {

    private org.kaaproject.kaa.server.common.zk.gen.ZkSupportedChannel ZkChannel;

    /** Creates a new Builder */
    private Builder() {
      super(org.kaaproject.kaa.server.common.zk.gen.SupportedChannel.SCHEMA$);
    }

    /** Creates a Builder by copying an existing Builder */
    private Builder(org.kaaproject.kaa.server.common.zk.gen.SupportedChannel.Builder other) {
      super(other);
      if (isValidValue(fields()[0], other.ZkChannel)) {
        this.ZkChannel = data().deepCopy(fields()[0].schema(), other.ZkChannel);
        fieldSetFlags()[0] = true;
      }
    }

    /** Creates a Builder by copying an existing SupportedChannel instance */
    private Builder(org.kaaproject.kaa.server.common.zk.gen.SupportedChannel other) {
            super(org.kaaproject.kaa.server.common.zk.gen.SupportedChannel.SCHEMA$);
      if (isValidValue(fields()[0], other.ZkChannel)) {
        this.ZkChannel = data().deepCopy(fields()[0].schema(), other.ZkChannel);
        fieldSetFlags()[0] = true;
      }
    }

    /** Gets the value of the 'ZkChannel' field */
    public org.kaaproject.kaa.server.common.zk.gen.ZkSupportedChannel getZkChannel() {
      return ZkChannel;
    }

    /** Sets the value of the 'ZkChannel' field */
    public org.kaaproject.kaa.server.common.zk.gen.SupportedChannel.Builder setZkChannel(org.kaaproject.kaa.server.common.zk.gen.ZkSupportedChannel value) {
      validate(fields()[0], value);
      this.ZkChannel = value;
      fieldSetFlags()[0] = true;
      return this;
    }

    /** Checks whether the 'ZkChannel' field has been set */
    public boolean hasZkChannel() {
      return fieldSetFlags()[0];
    }

    /** Clears the value of the 'ZkChannel' field */
    public org.kaaproject.kaa.server.common.zk.gen.SupportedChannel.Builder clearZkChannel() {
      ZkChannel = null;
      fieldSetFlags()[0] = false;
      return this;
    }

    @Override
    public SupportedChannel build() {
      try {
        SupportedChannel record = new SupportedChannel();
        record.ZkChannel = fieldSetFlags()[0] ? this.ZkChannel : (org.kaaproject.kaa.server.common.zk.gen.ZkSupportedChannel) defaultValue(fields()[0]);
        return record;
      } catch (Exception e) {
        throw new org.apache.avro.AvroRuntimeException(e);
      }
    }
  }
}
