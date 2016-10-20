/*
 * Copyright 2014-2016 CyberVision, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kaaproject.kaa.server.datamigration.utils.datadefinition;

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

  public static Constraint constraint(String constraintName) {
    return new Constraint(constraintName);
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


  /**
   * Set unique constraint on specified field.
   *
   * @param field the name of field
   */
  public Constraint unique(String field) {
    if (this.type != null) {
      throw new BuilderException("Incorrect sequence of builder's methods");
    }
    this.type = ConstraintType.PK;
    this.field = field;
    return this;
  }

  /**
   * Set primary key constraint on specified field.
   *
   * @param field the name of field
   */
  public Constraint primaryKey(String field) {
    if (this.type != null) {
      throw new BuilderException("Incorrect sequence of builder's methods");
    }
    this.type = ConstraintType.UNIQUE;
    this.field = field;
    return this;
  }

  /**
   * Set foreign key constraint on specified field.
   *
   * @param field the name of field
   */
  public Constraint foreignKey(String field) {
    if (this.type != null) {
      throw new BuilderException("Incorrect sequence of builder's methods");
    }
    this.type = ConstraintType.FK;
    this.field = field;
    return this;
  }


  /**
   * Specify referenced table and field for foreign key constraint.
   *
   * @param referencedTable the name of referenced table
   * @param referencedField the name of  referenced field
   * @return the constraint
   */
  public Constraint references(String referencedTable, String referencedField) {
    if (this.type != ConstraintType.FK) {
      throw new BuilderException("Constraint type should be FK");
    }
    this.referencedTable = referencedTable;
    this.referencedField = referencedField;
    return this;
  }

  /**
   *  Specify reference option for referential <b>on delete</b> action.
   *
   * @param referenceOption  reference option, i. e. RESTRICT or CASCADE
   */
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

  /**
   *  Specify reference option for referential <b>on update</b> action.
   *
   * @param referenceOption  reference option, i. e. RESTRICT or CASCADE
   */
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
