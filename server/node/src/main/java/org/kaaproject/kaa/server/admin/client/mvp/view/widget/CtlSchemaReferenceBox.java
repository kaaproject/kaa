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

package org.kaaproject.kaa.server.admin.client.mvp.view.widget;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kaaproject.avro.ui.gwt.client.widget.FqnReferenceBox;
import org.kaaproject.avro.ui.shared.Fqn;
import org.kaaproject.avro.ui.shared.FqnKey;
import org.kaaproject.avro.ui.shared.FqnVersion;
import org.kaaproject.kaa.server.admin.client.util.Utils;
import org.kaaproject.kaa.server.admin.shared.schema.CtlSchemaReferenceDto;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.HasConstrainedValue;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.RequiresResize;

public class CtlSchemaReferenceBox extends HorizontalPanel implements HasConstrainedValue<CtlSchemaReferenceDto>, RequiresResize {

    private CtlSchemaReferenceDto value;
    
    private FqnReferenceBox fqnReferenceBox;
    private IntegerListBox versionsBox;
    
    private Map<Fqn, List<Integer>> fqnVersionsMap = new HashMap<>();
    private Map<FqnVersion, CtlSchemaReferenceDto> fqnVersionToReferenceMap = new HashMap<>();
    
    public CtlSchemaReferenceBox() {
        fqnReferenceBox = new FqnReferenceBox(Utils.constants.schemaReferencePrompt());
        fqnReferenceBox.setWidth("100%");
        add(fqnReferenceBox);
        this.setCellWidth(fqnReferenceBox, "100%");
        versionsBox = new IntegerListBox();
        versionsBox.setWidth("60px");
        versionsBox.setHeight("100%");
        versionsBox.getElement().getStyle().setPaddingTop(2, Unit.PX);
        versionsBox.getElement().getStyle().setPaddingBottom(1, Unit.PX);
        versionsBox.getElement().getStyle().setMarginLeft(6, Unit.PX);
        add(versionsBox);
        
        fqnReferenceBox.addValueChangeHandler(new ValueChangeHandler<FqnKey>() {
            @Override
            public void onValueChange(ValueChangeEvent<FqnKey> event) {
                updateValueByFqnKeyAndVersion(event.getValue(), null);
            }
        });
        
        versionsBox.addValueChangeHandler(new ValueChangeHandler<Integer>() {
            @Override
            public void onValueChange(ValueChangeEvent<Integer> event) {
                updateValueByFqnKeyAndVersion(fqnReferenceBox.getValue(), event.getValue());
            }
        });
    }
    
    private void updateValueByFqnKeyAndVersion(FqnKey fqnKey, Integer version) {
        CtlSchemaReferenceDto newValue = null;
        if (fqnKey != null) {
            Fqn fqn = fqnKey.getFqn();
            if (version == null) {
                List<Integer> versions = fqnVersionsMap.get(fqn);
                version = Collections.max(versions);
            }
            FqnVersion fqnVersion = new FqnVersion(fqn, version);
            newValue = fqnVersionToReferenceMap.get(fqnVersion);
        } 
        setValue(newValue, true);
    }
    
    @Override
    public CtlSchemaReferenceDto getValue() {
        return value;
    }

    @Override
    public void setValue(CtlSchemaReferenceDto value) {
        setValue(value, false);
    }

    @Override
    public void setValue(CtlSchemaReferenceDto value, boolean fireEvents) {
        if (value == this.value
                || (this.value != null && this.value.equals(value))) {
            return;
        }
        CtlSchemaReferenceDto before = this.value;
        this.value = value;
        
        updateBox();
        
        if (fireEvents) {
            ValueChangeEvent.fireIfNotEqual(this, before, value);
        }
    }

    @Override
    public HandlerRegistration addValueChangeHandler(
            ValueChangeHandler<CtlSchemaReferenceDto> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }

    @Override
    public void setAcceptableValues(Collection<CtlSchemaReferenceDto> values) {
        fqnVersionsMap.clear();
        fqnVersionToReferenceMap.clear();
        Map<FqnKey, Fqn> declaredFqns = new HashMap<>();
        
        for (CtlSchemaReferenceDto ctlReference : values) {
            Fqn fqn = new Fqn(ctlReference.getMetaInfo().getFqn());
            FqnKey fqnKey = new FqnKey(fqn);
            declaredFqns.put(fqnKey, fqn);
            
            List<Integer> versions = fqnVersionsMap.get(fqn);
            if (versions == null) {
                versions = new ArrayList<>();
                fqnVersionsMap.put(fqn, versions);
            }
            versions.add(ctlReference.getVersion());
            
            fqnVersionToReferenceMap.put(new FqnVersion(fqn, ctlReference.getVersion()), ctlReference);
        }
        fqnReferenceBox.updateDeclaredFqns(declaredFqns);
        
        updateBox();
    }
    
    public void reset() {
        value = null;
        fqnVersionsMap.clear();
        fqnVersionToReferenceMap.clear();
        fqnReferenceBox.reset();
        versionsBox.reset();
    }
    
    private void updateBox() {
        if (value != null) {
            Fqn fqn = new Fqn(value.getMetaInfo().getFqn());
            fqnReferenceBox.setValue(new FqnKey(fqn));
            List<Integer> versions = fqnVersionsMap.get(fqn);
            Collections.sort(versions);
            versionsBox.setValue(value.getVersion());
            versionsBox.setAcceptableValues(versions);
        } else {
            fqnReferenceBox.setValue(null);
            versionsBox.reset();
        }
    }
    
    @Override
    public void onResize() {
        fqnReferenceBox.onShown();
    }

}
