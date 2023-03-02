/*
 * Copyright © 2018 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package io.cdap.cdap.api.lineage.field;

import java.util.Objects;

/**
 * Abstract base class to represent an Operation. Each operation has a name and description. The
 * name of operation must be unique within all operations belonging to the same program's field
 * level lineage. Operation typically has input and output fields. Input field name to the operation
 * is qualified with the name of the operation(origin) from which it was originated. It is possible
 * that the field with the same name is generated by two different operations. Now if you want to
 * use such field as an input to the next operation, qualifying the field with the origin helps to
 * uniquely identify the operation input. Outputs are given as plain field names since the name of
 * the operation is implicit in them.
 */
public abstract class Operation {

  private final String name;
  private final OperationType type;
  private final String description;

  protected Operation(String name, OperationType type, String description) {
    this.name = name;
    this.type = type;
    this.description = description;
  }

  /**
   * @return the name of the operation for example, "read" or "concatenate".
   */
  public String getName() {
    return name;
  }

  /**
   * @return the type of the operation
   */
  public OperationType getType() {
    return type;
  }

  /**
   * @return the description associated with the operation
   */
  public String getDescription() {
    return description;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Operation operation = (Operation) o;
    return Objects.equals(name, operation.name) &&
        type == operation.type &&
        Objects.equals(description, operation.description);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, type, description);
  }
}
