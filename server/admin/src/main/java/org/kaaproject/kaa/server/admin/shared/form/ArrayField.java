package org.kaaproject.kaa.server.admin.shared.form;

import java.util.ArrayList;
import java.util.List;

public class ArrayField extends FormField {

    private static final long serialVersionUID = -1859402253654290694L;
    
    private RecordField elementMetadata;
    
    private List<RecordField> value;
    
    private int minRowCount = 0;

    public ArrayField() {
        super();
        value = new ArrayList<>();
    }
    
    public ArrayField(String fieldName, 
            String displayName, 
            boolean optional) {
        super(fieldName, displayName, optional);
        value = new ArrayList<>();
    }
    
    public RecordField getElementMetadata() {
        return elementMetadata;
    }

    public void setElementMetadata(RecordField elementMetadata) {
        this.elementMetadata = elementMetadata;
    }

    public List<RecordField> getValue() {
        return value;
    }
    
    public int getMinRowCount() {
        return minRowCount;
    }
    
    public void setMinRowCount(int minRowCount) {
        this.minRowCount = minRowCount;
    }

    @Override
    public FieldType getFieldType() {
        return FieldType.ARRAY;
    }
    
    @Override
    public boolean isNull() {
        return false;
    }
    
    public void addArrayData(RecordField data) {
        this.value.add(data);
    }

    @Override
    protected FormField createInstance() {
        return new ArrayField();
    }
    
    @Override
    protected void copyFields(FormField cloned) {
        super.copyFields(cloned);
        ArrayField clonedArrayField = (ArrayField)cloned;
        clonedArrayField.minRowCount = minRowCount;
        clonedArrayField.elementMetadata = (RecordField)elementMetadata.clone();
        for (RecordField field : value) {
            clonedArrayField.value.add((RecordField)field.clone());
        }
    }

    @Override
    protected boolean valid() {
        if (value.size() > 0 && value.size() >= minRowCount) {
            boolean valid = true;
            for (RecordField field : value) {
                valid &= field.isValid();
            }
            return valid;
        }
        return false;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result
                + ((elementMetadata == null) ? 0 : elementMetadata.hashCode());
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ArrayField other = (ArrayField) obj;
        if (elementMetadata == null) {
            if (other.elementMetadata != null) {
                return false;
            }
        } else if (!elementMetadata.equals(other.elementMetadata)) {
            return false;
        }
        if (value == null) {
            if (other.value != null) {
                return false;
            }
        } else if (!value.equals(other.value)) {
            return false;
        }
        return true;
    }

}
