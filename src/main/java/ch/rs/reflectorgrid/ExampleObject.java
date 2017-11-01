package ch.rs.reflectorgrid;

import ch.rs.reflectorgrid.TransferGrid.FieldType;

/**
 *
 */
public class ExampleObject {

  @TransferGrid
  private String name = "jey";

  @TransferGrid
  private int age = 20;

  @TransferGrid(options = {"you", "are", "nice"})
  private String choiceForYou = "you";

  @TransferGrid(fieldtype = FieldType.TEXT_AREA)
  private String textArea;

  @Override
  public String toString() {
    return "ExampleObject{"
        + "name='" + name + '\''
        + ", age=" + age
        + ", choiceForYou='" + choiceForYou + '\''
        + ", textArea='" + textArea + '\''
        + '}';
  }
}
