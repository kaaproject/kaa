package org.kaaproject.kaa.server.admin.shared.form;

public class IntegerField extends SizedField {

    private static final long serialVersionUID = -5046250549233854347L;

    private Integer defaultValue;

    private Integer value;
    
    public IntegerField() {
        super();
    }

    public IntegerField(String fieldName, 
            String displayName, 
            boolean optional) {
        super(fieldName, displayName, optional);
    }
    
    public Integer getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(Integer defaultValue) {
        this.defaultValue = defaultValue;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    @Override
    public FieldType getFieldType() {
        return FieldType.INTEGER;
    }
    
    @Override
    public boolean isNull() {
        return value == null;
    }

    @Override
    protected FormField createInstance() {
        return new IntegerField();
    }
    
    @Override
    protected void copyFields(FormField cloned) {
        super.copyFields(cloned);
        IntegerField clonedIntegerField = (IntegerField)cloned;
        clonedIntegerField.defaultValue = defaultValue;
        clonedIntegerField.value = value;
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
        IntegerField other = (IntegerField) obj;
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
