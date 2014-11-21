package org.kaaproject.kaa.server.admin.shared.form;

public abstract class SizedField extends FormField {

    private static final long serialVersionUID = 6539576598668221454L;
    
    private static final int DEFAULT_MAX_LENGTH = 255;
    
    private Integer maxLength;
    
    public SizedField() {
        super();
    }
    
    public SizedField(String fieldName, 
            String displayName, 
            boolean optional) {
        super(fieldName, displayName, optional);
    }
    
    public int getMaxLength() {
        if (maxLength != null) {
            return maxLength.intValue();
        }
        else {
            return DEFAULT_MAX_LENGTH;
        }
    }
    
    public void setMaxLength(int maxLength) {
        this.maxLength = Integer.valueOf(maxLength);
    }

}
