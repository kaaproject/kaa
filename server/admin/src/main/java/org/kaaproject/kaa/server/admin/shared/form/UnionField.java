package org.kaaproject.kaa.server.admin.shared.form;

import java.util.ArrayList;
import java.util.List;

public class UnionField extends FormField {

    private static final long serialVersionUID = -7020719983305986557L;

    private List<RecordField> acceptableValues;
    
    private RecordField value;
    
    public UnionField() {
        super();
        acceptableValues = new ArrayList<>();
    }
    
    public UnionField(String fieldName, 
            String displayName, 
            boolean optional) {
        super(fieldName, displayName, optional);
        acceptableValues = new ArrayList<>();
    }
    
    public RecordField getValue() {
        return value;
    }

    public void setValue(RecordField value) {
        if (value != null) {
            int index = -1;
            for (int i=0;i<acceptableValues.size();i++) {
                if (acceptableValues.get(i).isSameType(value)) {
                    index = i;
                    break;
                }
            }
            if (index > -1) {
                this.value = value;
                this.acceptableValues.set(index, value);
            }
            else {
                throw new IllegalArgumentException("Value type not in list of union types!");
            }
        }
        else {
            this.value = null;
        }
    }
    
    public List<RecordField> getAcceptableValues() {
        return acceptableValues;
    }
    
    public void setAcceptableValues(List<RecordField> acceptableValues) {
        this.acceptableValues = acceptableValues;
    }

    @Override
    public FieldType getFieldType() {
        return FieldType.UNION;
    }

    @Override
    public boolean isNull() {
        return value == null;
    }

    @Override
    protected boolean valid() {
        return value != null && value.isValid();
    }

    @Override
    protected FormField createInstance() {
        return new UnionField();
    }
    
    @Override
    protected void copyFields(FormField cloned) {
        super.copyFields(cloned);
        UnionField clonedUnionField = (UnionField)cloned;
        for (RecordField acceptableValue : acceptableValues) {
            clonedUnionField.acceptableValues.add((RecordField) acceptableValue.clone());
        }
        clonedUnionField.setValue(value != null ? (RecordField) value.clone() : null);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
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
        UnionField other = (UnionField) obj;
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
