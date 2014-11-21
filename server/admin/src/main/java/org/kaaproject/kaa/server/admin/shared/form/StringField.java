package org.kaaproject.kaa.server.admin.shared.form;

public class StringField extends SizedField {

    private static final long serialVersionUID = -5046250549233854347L;
    
    private String defaultValue;

    private String value;
    
    public StringField() {
        super();
    }
    
    public StringField(String fieldName, 
            String displayName, 
            boolean optional) {
        super(fieldName, displayName, optional);
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public FieldType getFieldType() {
        return FieldType.STRING;
    }
    
    @Override
    public boolean isNull() {
        return value == null;
    }
    
    @Override
    protected FormField createInstance() {
        return new StringField();
    }
    
    @Override
    protected void copyFields(FormField cloned) {
        super.copyFields(cloned);
        StringField clonedStringField = (StringField)cloned;
        clonedStringField.defaultValue = defaultValue;
        clonedStringField.value = value;
    }

    @Override
    protected boolean valid() {
        return !strIsEmpty(value);
    }

    private static boolean strIsEmpty(String str) {
        return str == null || str.length() == 0;
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
        StringField other = (StringField) obj;
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
