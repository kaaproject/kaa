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

package org.kaaproject.kaa.server.operations.service.filter.el;

import org.apache.avro.generic.GenericRecord;
import org.springframework.expression.AccessException;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.PropertyAccessor;
import org.springframework.expression.TypedValue;


/**
 * The Class GenericRecordPropertyAccessor.
 */
public class GenericRecordPropertyAccessor implements PropertyAccessor {

    /* (non-Javadoc)
     * @see org.springframework.expression.PropertyAccessor#getSpecificTargetClasses()
     */
    @Override
    public Class<?>[] getSpecificTargetClasses() {
        return new Class<?>[] { GenericRecord.class };
    }

    /* (non-Javadoc)
     * @see org.springframework.expression.PropertyAccessor#canRead(org.springframework.expression.EvaluationContext, java.lang.Object, java.lang.String)
     */
    @Override
    public boolean canRead(EvaluationContext context, Object target, String name)
            throws AccessException {
        GenericRecord record = (GenericRecord) target;
        return record.getSchema().getField(name) != null;
    }

    /* (non-Javadoc)
     * @see org.springframework.expression.PropertyAccessor#read(org.springframework.expression.EvaluationContext, java.lang.Object, java.lang.String)
     */
    @Override
    public TypedValue read(EvaluationContext context, Object target, String name)
            throws AccessException {
        GenericRecord record = (GenericRecord) target;
        return new TypedValue(record.get(name));
    }

    /* (non-Javadoc)
     * @see org.springframework.expression.PropertyAccessor#canWrite(org.springframework.expression.EvaluationContext, java.lang.Object, java.lang.String)
     */
    @Override
    public boolean canWrite(EvaluationContext context, Object target,
            String name) throws AccessException {
        return false;
    }

    /* (non-Javadoc)
     * @see org.springframework.expression.PropertyAccessor#write(org.springframework.expression.EvaluationContext, java.lang.Object, java.lang.String, java.lang.Object)
     */
    @Override
    public void write(EvaluationContext context, Object target, String name,
            Object newValue) throws AccessException {
        throw new UnsupportedOperationException("Write is not supported");
    }
}