package org.kaaproject.kaa.server.admin.shared.form;

public class LongField extends SizedField {

    private static final long serialVersionUID = -5046250549233854347L;

    private Long defaultValue;
    
    private Long value;
    
    public LongField() {
        super();
    }
    
    public LongField(String fieldName, 
            String displayName, 
            boolean optional) {
        super(fieldName, displayName, optional);
    }
    
    public Long getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(Long defaultValue) {
        this.defaultValue = defaultValue;
    }

    public Long getValue() {
        return value;
    }

    public void setValue(Long value) {
        this.value = value;
    }

    @Override
    public FieldType getFieldType() {
        return FieldType.LONG;
    }

    @Override
    public boolean isNull() {
        return value == null;
    }

    @Override
    protected FormField createInstance() {
        return new LongField();
    }
    
    @Override
    protected void copyFields(FormField cloned) {
        super.copyFields(cloned);
        LongField clonedLongField = (LongField)cloned;
        clonedLongField.defaultValue = defaultValue;
        clonedLongField.value = value;
    }

    @Override
    protected boolean valid() {
        return value != null;
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
        LongField other = (LongField) obj;
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
