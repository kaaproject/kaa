package org.kaaproject.kaa.server.common.core.plugin.generator.common;

public class ImportStatement {

    private String body;

    public ImportStatement(String body) {
        this.body = body;
    }

    private static final String TEMPLATE = "import %s";

    @Override
    public String toString() {
        return String.format(TEMPLATE, this.body);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((body == null) ? 0 : body.hashCode());
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
        ImportStatement other = (ImportStatement) obj;
        if (body == null) {
            if (other.body != null) {
                return false;
            }
        } else if (!body.equals(other.body)) {
            return false;
        }
        return true;
    }
}
