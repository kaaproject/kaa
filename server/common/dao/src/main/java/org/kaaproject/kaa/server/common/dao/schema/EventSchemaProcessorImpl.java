/*
 * Copyright 2014-2016 CyberVision, Inc.
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

package org.kaaproject.kaa.server.common.dao.schema;

import org.apache.avro.Schema;
import org.apache.avro.SchemaParseException;
import org.kaaproject.kaa.common.dto.event.EventClassType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class EventSchemaProcessorImpl implements EventSchemaProcessor {

  /**
   * The Constant LOG.
   */
  private static final Logger LOG = LoggerFactory.getLogger(EventSchemaProcessorImpl.class);

  private static final String CLASS_TYPE = "classType";
  private static final String CTL_SCHEMA_ID = "ctlSchemaId";

  @Override
  public List<EventSchemaMetadata> processSchema(String schema)
      throws EventSchemaException {
    Schema parsedSchema = null;
    List<EventSchemaMetadata> eventClassSchemas = null;
    try {
      parsedSchema = new Schema.Parser().parse(schema);
    } catch (SchemaParseException spe) {
      LOG.error("Can't parse schema.", spe);
      throw new EventSchemaException("Can't parse provided event class family schema.", spe);
    }

    try {
      List<Schema> parsedEventClassSchemas = parsedSchema.getTypes();
      eventClassSchemas = new ArrayList<>(parsedEventClassSchemas.size());
      for (Schema parsedEventClassSchema : parsedEventClassSchemas) {
        EventSchemaMetadata eventClassSchema = new EventSchemaMetadata();
        eventClassSchema.setFqn(parsedEventClassSchema.getFullName());
        String strClassType = parsedEventClassSchema.getProp(CLASS_TYPE);
        EventClassType classType = null;
        try { //NOSONAR
          classType = EventClassType.valueOf(strClassType.toUpperCase());
        } catch (Exception ex) {
          LOG.error("Can't process provided event class family schema. Invalid classType [{}]. "
                    + "Exception catched: {}", strClassType, ex);
          throw new EventSchemaException("Can't process provided event class family schema. "
                                         + "Invalid classType: " + strClassType);
        }
        String ctlSchemaId = parsedEventClassSchema.getProp(CTL_SCHEMA_ID);
        eventClassSchema.setCtlSchemaId(ctlSchemaId);
        eventClassSchema.setType(classType);
        eventClassSchemas.add(eventClassSchema);
      }
    } catch (Exception ex) {
      LOG.error("Invalid event class family schema.", ex);
      throw new EventSchemaException("Can't process provided event class family schema.", ex);
    }
    return eventClassSchemas;
  }

}
