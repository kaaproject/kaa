package org.kaaproject.kaa.server.appenders.hbase.appender;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.avro.generic.GenericRecord;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.NamespaceDescriptor;
import org.apache.hadoop.hbase.NamespaceNotFoundException;
import org.apache.hadoop.hbase.TableExistsException;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HConnection;
import org.apache.hadoop.hbase.client.HConnectionManager;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.io.compress.Compression.Algorithm;
import org.apache.hadoop.hbase.io.encoding.DataBlockEncoding;
import org.apache.hadoop.hbase.regionserver.BloomType;
import org.apache.hadoop.hbase.util.Bytes;
import org.kaaproject.kaa.common.avro.GenericAvroConverter;
import org.kaaproject.kaa.server.appenders.hbase.config.gen.BloomFilterType;
import org.kaaproject.kaa.server.appenders.hbase.config.gen.ColumnFamily;
import org.kaaproject.kaa.server.appenders.hbase.config.gen.ColumnMappingElement;
import org.kaaproject.kaa.server.appenders.hbase.config.gen.Encoding;
import org.kaaproject.kaa.server.appenders.hbase.config.gen.HBaseAppenderConfiguration;
import org.kaaproject.kaa.server.appenders.hbase.config.gen.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HBaseLogEventDao implements LogEventDao  {

  private static final Logger LOG = LoggerFactory.getLogger(HBaseLogEventDao.class);

  private HBaseAdmin hadmin;
  private HBaseAppenderConfiguration configuration;
  private String namespacedTable;
  private HConnection connection;
  private List<ColumnMappingElement> rowKeyMap;


  /**
   * Instantiates a new HBaseLogEventDao.
   */

  public HBaseLogEventDao(HBaseAppenderConfiguration config)  {
    if (config == null) {
      throw new IllegalArgumentException("Configuration shouldn't be null");
    }
    this.configuration  = config;

    LOG.info("Init HBase Log Event Dao");

    //HBase Initialization parameters

    //Get Zookeeper parameters
    String zk = configuration.getServer().getZkQuorum().toString();
    String zkPort = configuration.getServer().getPort().toString();

    LOG.info("Connecting to Zookeeper Quorum -" + zk  + ":" + zkPort);

    try {
      //Create HBase configuration
      Configuration hbconf = HBaseConfiguration.create();
      hbconf.set("hbase.zookeeper.quorum",zk);
      hbconf.set("hbase.zookeeper.property.clientPort", zkPort);

      //Check the availability of the configuration
      HBaseAdmin.checkHBaseAvailable(hbconf);

      //create a connection and instantiate an HBase Administrator object
      connection = HConnectionManager.createConnection(hbconf);
      hadmin = new HBaseAdmin(hbconf);


      LOG.info("HBase Admin Started");
    } catch (Exception e) {
      LOG.error("Ops!", e);
    }
  }


  @Override
  public String createHbTable() {

    //Get table and keyspace names
    String table  = configuration.getTableName().toString().toLowerCase().trim();
    String keyspace = configuration.getKeyspace().toString().toLowerCase().trim();
    LOG.info("Starting creation of table:  {}",keyspace + ":" + table);

    // Creates the namespace and/or table in case of not existing.

    try {
      NamespaceDescriptor namespace;
      try {
        namespace = hadmin.getNamespaceDescriptor(keyspace);
        LOG.info("Namespace Found -  "  + keyspace);
      } catch (NamespaceNotFoundException ne) {
        namespace = NamespaceDescriptor.create(keyspace).build();
        hadmin.createNamespace(namespace);
        LOG.info("Created Namespace -  " + keyspace);
      }

      try {
        TableName tableN = TableName.valueOf(keyspace,table);
        HTableDescriptor tableDescriptor = new HTableDescriptor(tableN);

        //adding Column families to table.
        addColumnFamilies(configuration.getColumnFamilies(),tableDescriptor);

        /* instantiate a list to store the column elements of which data that will be part of 
         * the row Key.  */
        rowKeyMap = new ArrayList<ColumnMappingElement>();
        getKeyMap(configuration.getColumnMapping());

        //Create table
        hadmin.createTable(tableDescriptor);
        LOG.info("Created Table -   " +  keyspace  + ":" + table);
      } catch (TableExistsException nt) {
        LOG.info("Table Found -   " + keyspace  + ":" + table);
      }
      //Format of hbase tables -  <namespace>:<table_name>
      namespacedTable = keyspace  + ":" + table;

    } catch (Exception e) {
      LOG.error("Ops",e);
    }
    return namespacedTable;
  }

  /**
   * Get the column elements which data will be a part of the Row Key,
   * for example: Id, timestamp, etc.
   * 
   * @param columnMapping
   **/

  private void getKeyMap(List<ColumnMappingElement> columnMapping) {
    for (ColumnMappingElement element : columnMapping) {
      if (element.getRowKey()) {
        rowKeyMap.add(element);
      }
    }
  }

  /**
   * Add all the column families with the settings specified at the schema creation.
   * 
   **/
  
  private void addColumnFamilies(List<ColumnFamily> columnFamilies, 
      HTableDescriptor tableDescriptor) {
    for (ColumnFamily columnf: columnFamilies) {
      HColumnDescriptor cf = new HColumnDescriptor(
          columnf.getCfName().toString().toLowerCase().trim());
      cf.setBlockCacheEnabled(columnf.getBlockCache());
      cf.setBlocksize(columnf.getBlockSize());
      cf.setBloomFilterType(getBloomFilter(columnf.getBloomFilter())); 
      cf.setCompressionType(getAlgorithmCompression(columnf.getCompression()));
      cf.setDataBlockEncoding(getEncoding(columnf.getDataBlockEncoding()));
      cf.setInMemory(columnf.getInMemory());
      cf.setKeepDeletedCells(columnf.getKeepDeletedCells());
      cf.setMaxVersions(columnf.getMaxVersion());
      cf.setMinVersions(columnf.getMinVersion());
      cf.setScope(columnf.getScope());
      if (columnf.getTtl()  !=  null) {
        cf.setTimeToLive(columnf.getTtl());
      }
      tableDescriptor.addFamily(cf);
    }


  }

  /**
   * Some methods to set column families properties.
   * 
   */


  private DataBlockEncoding getEncoding(Encoding dataBlockEncoding) {
    switch  (dataBlockEncoding) {
      case DIFF:
        return DataBlockEncoding.DIFF;
      case FAST_DIFF: 
        return DataBlockEncoding.FAST_DIFF;
      case PREFIX:
        return DataBlockEncoding.PREFIX;
      case PREFIX_TREE:
        return DataBlockEncoding.PREFIX_TREE;
      default:
        return DataBlockEncoding.NONE;
    }
  }


  private Algorithm getAlgorithmCompression(
      org.kaaproject.kaa.server.appenders.hbase.config.gen.Algorithm compression) {
    switch  (compression) {
      case GZ:
        return Algorithm.GZ;
      case LZ4: 
        return Algorithm.LZ4;
      case LZO:
        return Algorithm.LZO;
      case SNAPPY:
        return Algorithm.SNAPPY;
      default:
        return Algorithm.NONE;
    }
  }

  private BloomType getBloomFilter(BloomFilterType bloomFilter) {
    switch  (bloomFilter) {
      case NONE:
        return BloomType.NONE;
      case ROWCOL: 
        return BloomType.ROWCOL;
      default:
        return BloomType.ROW;
    }


  }






  @Override
  public void save(List<HBaseLogEventDto> logEventDtoList, String collectionName,
      GenericAvroConverter<GenericRecord> eventConverter,
      GenericAvroConverter<GenericRecord> headerConverter) throws IOException {

    LOG.debug("Saving {} log events", logEventDtoList.size());
    HTableInterface htable = connection.getTable(namespacedTable);
    List<Put> insertArray = new ArrayList<Put>();

    for (int i = 0; i < logEventDtoList.size(); i++) {
      HBaseLogEventDto dto = logEventDtoList.get(i);
      String row  = "";
      for (ColumnMappingElement element : rowKeyMap) {
        String rowElement = stringConverter(element.getValueType(), 
            dto.getEvent().get(element.getValue().toString()));
        if  (row.length() > 0) {
          row = row + "+";
        }
        row = row + rowElement;

      }
      Put p = new Put(Bytes.toBytes(row));
      for (ColumnMappingElement element : configuration.getColumnMapping()) {

        p.add(Bytes.toBytes(element.getCf().toString()), 
            Bytes.toBytes(element.getColumnName().toString()),
            Bytes.toBytes(stringConverter(
                element.getValueType(),dto.getEvent().get(element.getValue().toString()))));


      }
      insertArray.add(p);
    }
    htable.put(insertArray);
    htable.close();
  }

  /**
   * HBase stores all data as an array of bytes. In order to do that, some conversion is made below
   */

  private String stringConverter(Type datatype, Object object) throws IOException {
    String result = "";
    if (datatype  ==  Type.TEXT) {
      result = (String) object;
      return result;
    }
    if (datatype  ==  Type.INT) {
      int aux = (int) object;
      result = Integer.toString(aux);
      return result;
    }
    if (datatype  ==  Type.BIGINT) {
      long aux2 = (long) object;
      result = Long.toString(aux2);
      return result;
    }
    if (datatype  ==  Type.DOUBLE) {
      double aux3 = (double) object;
      result = Double.toString(aux3);
      return result;
    }
    if (datatype  ==  Type.FLOAT) {
      float aux4 = (float) object;
      result = Float.toString(aux4);
      return result;
    }
    if (datatype  ==  Type.BOOLEAN) {
      boolean aux5 = (boolean) object;
      result = Boolean.toString(aux5);
      return result;
    }
    if (datatype  ==  Type.BLOB) {
      result = objectToByte(object).toString();
      return result;
    }
    if (datatype  ==  Type.ARRAY) {
      result = arrayMapString(object);
      return result;
    }

    return "ERROR CONVERTING OBJECT TO DATA TYPE";
  }


  private String arrayMapString(Object object) {
    @SuppressWarnings({ "unchecked", "rawtypes" })
    String listString =  (String) ((Collection) object).stream().map(Object::toString)
        .collect(Collectors.joining(", "));
    String result = "[" + listString  + "]";
    return result;
  }


  private byte[] objectToByte(Object object) throws IOException {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    ObjectOutput out = new ObjectOutputStream(bos);
    out.writeObject(object);
    out.flush();
    byte[] resultBytes = bos.toByteArray();
    bos.close();

    return resultBytes;
  }


  @Override
  public void removeAll(String collectionName) {

    //Truncate HBase Table

    HTableDescriptor desciptor;
    try {
      desciptor = hadmin.getTableDescriptor(collectionName.getBytes());
      hadmin.disableTable(collectionName.getBytes());
      hadmin.deleteTable(collectionName.getBytes());
      hadmin.createTable(desciptor);
    } catch (IOException e) {
      e.printStackTrace();
    }

  }

  @Override
  public void close() {
    LOG.info("Closing Down LogEventCustomDAO");
    try {
      hadmin.close();
      connection.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }




}
