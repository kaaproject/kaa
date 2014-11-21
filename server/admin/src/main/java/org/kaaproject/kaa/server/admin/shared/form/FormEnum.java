package org.kaaproject.kaa.server.admin.shared.form;

import java.io.Serializable;

public class FormEnum implements Serializable {

    private static final long serialVersionUID = 816172078410943534L;
    
    private final String enumSymbol;
    private final String displayValue;
    
    public FormEnum(String enumSymbol, String displayValue) {
        this.enumSymbol = enumSymbol;
        this.displayValue = displayValue;
    }

    public String getEnumSymbol() {
        return enumSymbol;
    }

    public String getDisplayValue() {
        return displayValue;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((enumSymbol == null) ? 0 : enumSymbol.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        FormEnum other = (FormEnum) obj;
        if (enumSymbol == null) {
            if (other.enumSymbol != null) {
                return false;
            }
        } else if (!enumSymbol.equals(other.enumSymbol)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return enumSymbol;
    }

}
