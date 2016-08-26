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

package org.kaaproject.kaa.server.admin.client.mvp.place;

import com.google.gwt.place.shared.Prefix;
import org.kaaproject.kaa.common.dto.event.EventClassDto;
import org.kaaproject.kaa.server.admin.client.mvp.view.EventClassView;
import org.kaaproject.kaa.server.admin.client.util.Utils;
import org.kaaproject.kaa.server.admin.shared.schema.CtlSchemaFormDto;
import org.kaaproject.kaa.server.admin.shared.schema.EventClassViewDto;

import java.util.ArrayList;
import java.util.List;

public class EventClassPlace extends AbstractSchemaPlaceEvent {

    private String ctlSchemaId;
    private String nameEC;

    public EventClassPlace(String ecfId, String ecfVersionId, int ecfVersion, String schemaId, List<EventClassViewDto> eventClassDtoList) {
        super(ecfId, ecfVersionId, ecfVersion, schemaId);
        this.eventClassDtoList = eventClassDtoList;
    }

    public EventClassPlace(String ecfId, String ecfVersionId, int ecfVersion, String schemaId) {
        super(ecfId, ecfVersionId, ecfVersion, schemaId);
    }

    public String getCtlSchemaId() {
        return ctlSchemaId;
    }

    public void setCtlSchemaId(String ctlSchemaId) {
        this.ctlSchemaId = ctlSchemaId;
    }

    public String getNameEC() {
        return nameEC;
    }

    public void setNameEC(String nameEC) {
        this.nameEC = nameEC;
    }

    @Override
    public String getName() {
        return Utils.constants.versions();
    }

    @Prefix(value = "eventClass")
    public static class Tokenizer extends AbstractSchemaPlaceEvent.Tokenizer<EventClassPlace> {

        @Override
        protected EventClassPlace getPlaceImpl(String ecfId, String ecfVersionId, int ecfVersion, String schemaId) {
            return new EventClassPlace(ecfId, ecfVersionId, ecfVersion, schemaId);
        }

    }

    @Override
    public TreePlace createDefaultPreviousPlace() {
        return new EcfVersionPlace(ecfId, ecfVersionId, ecfVersion, eventClassDtoList);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EventClassPlace)) return false;
        if (!super.equals(o)) return false;

        EventClassPlace that = (EventClassPlace) o;

        return getEcfVersionId() != null ? getEcfVersionId().equals(that.getEcfVersionId()) : that.getEcfVersionId() == null;

    }

    @Override
    public int hashCode() {
        return getEcfVersionId() != null ? getEcfVersionId().hashCode() : 0;
    }
}
