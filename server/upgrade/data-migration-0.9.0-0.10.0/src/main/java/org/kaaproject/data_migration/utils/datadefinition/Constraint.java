package org.kaaproject.data_migration.utils.datadefinition;


public class Constraint {
    private String constraintName;
    private String field;
    private ConstraintType type;
    private String referencedTable;
    private String referencedField;
    private ReferenceOptions onDeleteOpt;
    private ReferenceOptions onUpdateOpt;

    public Constraint(String constraintName) {
        this.constraintName = constraintName;
    }

    public String getConstraintName() {
        return constraintName;
    }

    public String getField() {
        return field;
    }

    public ConstraintType getType() {
        return type;
    }

    public String getReferencedTable() {
        return referencedTable;
    }

    public String getReferencedField() {
        return referencedField;
    }

    public ReferenceOptions getOnDeleteOpt() {
        return onDeleteOpt;
    }

    public ReferenceOptions getOnUpdateOpt() {
        return onUpdateOpt;
    }

    public static Constraint constraint(String constraintName) {
        return new Constraint(constraintName);
    }


    public Constraint unique(String field) {
        if (this.type != null) {
            throw new BuilderException("Incorrect sequence of builder's methods");
        }
        this.type = ConstraintType.PK;
        this.field = field;
        return this;
    }

    public Constraint primaryKey(String field) {
        if (this.type != null) {
            throw new BuilderException("Incorrect sequence of builder's methods");
        }
        this.type = ConstraintType.UNIQUE;
        this.field = field;
        return this;
    }

    public Constraint foreignKey(String field) {
        if (this.type != null) {
            throw new BuilderException("Incorrect sequence of builder's methods");
        }
        this.type = ConstraintType.FK;
        this.field = field;
        return this;
    }


    public Constraint references(String referencedTable, String referencedField) {
        if (this.type != ConstraintType.FK) {
            throw new BuilderException("Constraint type should be FK");
        }
        this.referencedTable = referencedTable;
        this.referencedField = referencedField;
        return this;
    }

    public Constraint onDelete(ReferenceOptions referenceOption) {
        if (this.type != ConstraintType.FK) {
            throw new BuilderException("Constraint type should be FK");
        }
        if (this.referencedTable == null) {
            throw new BuilderException("Referenced table not defined");
        }
        onDeleteOpt = referenceOption;
        return this;
    }

    public Constraint onUpdate(ReferenceOptions referenceOption) {
        if (this.type != ConstraintType.FK) {
            throw new BuilderException("Constraint type should be FK");
        }
        if (this.referencedTable == null) {
            throw new BuilderException("Referenced table not defined");
        }
        onUpdateOpt = referenceOption;
        return this;
    }


}
