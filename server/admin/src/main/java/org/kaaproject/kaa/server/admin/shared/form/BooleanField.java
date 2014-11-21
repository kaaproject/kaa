package org.kaaproject.kaa.server.admin.shared.form;

public class BooleanField extends FormField {

    private static final long serialVersionUID = -7841564200726288319L;
    
    private Boolean defaultValue;
    
    private Boolean value;
    
    public BooleanField() {
        super();
    }

    public BooleanField(String fieldName, 
            String displayName, 
            boolean optional) {
        super(fieldName, displayName, optional);
    }
    
    public Boolean getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(Boolean defaultValue) {
        this.defaultValue = defaultValue;
    }

    public Boolean getValue() {
        return value;
    }

    public void setValue(Boolean value) {
        this.value = value;
    }

    @Override
    public FieldType getFieldType() {
        return FieldType.BOOLEAN;
    }
    
    @Override
    public boolean isNull() {
        return false;
    }

    @Override
    protected FormField createInstance() {
        return new BooleanField();
    }
    
    @Override
    protected void copyFields(FormField cloned) {
        super.copyFields(cloned);
        BooleanField clonedBoolenField = (BooleanField)cloned;
        clonedBoolenField.defaultValue = defaultValue;
        clonedBoolenField.value = value;
    }

    @Override
    protected boolean valid() {
        return true;
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
        BooleanField other = (BooleanField) obj;
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
