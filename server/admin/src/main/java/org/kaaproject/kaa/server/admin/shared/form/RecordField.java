package org.kaaproject.kaa.server.admin.shared.form;

import java.util.ArrayList;
import java.util.List;

public class RecordField extends FormField {

    private static final long serialVersionUID = -2006331166074707248L;
    
    private List<FormField> value;
    
    private String typeName;

    private String typeNamespace;

    public RecordField() {
        super();
        value = new ArrayList<>();
    }
    
    public RecordField(String fieldName, 
            String displayName, 
            boolean optional) {
        super(fieldName, displayName, optional);
        value = new ArrayList<>();
    }
    
    public List<FormField> getValue() {
        return value;
    }

    public void addField(FormField field) {
        value.add(field);
    }
    
    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getTypeNamespace() {
        return typeNamespace;
    }

    public void setTypeNamespace(String typeNamespace) {
        this.typeNamespace = typeNamespace;
    }
    
    public boolean isSameType(RecordField otherRecord) {
        return typeNamespace.equals(otherRecord.getTypeNamespace()) &&
                typeName.equals(otherRecord.getTypeName());
    }
    
    public String getTypeFullname() {
        return typeNamespace + "." + typeName;
    }

    @Override
    public FieldType getFieldType() {
        return FieldType.RECORD;
    }
    
    @Override
    public boolean isNull() {
        return false;
    }

    @Override
    protected FormField createInstance() {
        return new RecordField();
    }
    
    @Override
    protected void copyFields(FormField cloned) {
        super.copyFields(cloned);
        RecordField clonedRecordField = (RecordField)cloned;
        for (FormField field : value) {
            clonedRecordField.value.add(field.clone());
        }
        clonedRecordField.typeName = typeName;
        clonedRecordField.typeNamespace = typeNamespace;
    }

    @Override
    protected boolean valid() {
        boolean valid = true;
        for (FormField field : value) {
            valid &= field.isValid();
        }
        return valid;
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
        RecordField other = (RecordField) obj;
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
