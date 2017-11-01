/**
 * MIT License
 *
 * Copyright (c) 2017 Ricardo Daniel Monteiro Simoes
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE
 **/

package ch.rs.reflectorgrid;

import ch.rs.reflectorgrid.TransferGrid.FieldType;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javafx.geometry.HPos;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;

/**
 * This class is used in conjunction with TransferGrid.java and enables a user to generate a
 * GridPane with labels and TextFields/ComboBoxes/TextAreas for variables used in an Object. This
 * also supports variables from inhereted classes, aswell as private variables.
 *
 * In its current version, this class supports the following variable types: <b>int, String,
 * boolean, double, float</b>
 *
 * If your variable is not in here, the class will not be able to set it.
 *
 *
 * Please make sure you know the possibilities that @TransferGrid enables, before letting the end
 * user change variables he is not supposed to.
 *
 * This class uses the JavaFX GridPane and due to the way it is built, you only need one class to
 * generate a Grid. If the object changes to another object you want to show the Variables of, you
 * can just call turnObjectIntoGrid() again. Due to the nature of JavaFX it will automaticly update
 * the Grid on your GUI.
 *
 * @author Ricardo S., RS
 */
public class ReflectorGrid {

  /**
   * The GridPane that will get actualizied whenever you call turnObjectIntoGrid. This way we update
   * the grid according to the new object.
   */
  private GridPane grid = new GridPane();

  /**
   * This object is the one we give with turnObjectIntoGrid(). It is needed to set the field when
   * using the TextFields etc.
   */
  private Object gridObject = new Object();

  //This is used to generate the Grid, formatted like:
  //Label | Field
  //Label | Field
  //or as a one column grid formated like:
  //Label
  //------
  //Field
  //-----
  //Label
  //etc.
  private boolean isSideBySide = true;

  //This variable is used to define the width limit of the Field. Default is
  //Set to 300.
  private double NODE_WIDTH_LIMIT = 300;

  public GridPane turnObjectIntoGrid(Object object) {
    gridObject = object;
    List<Label> labels = new ArrayList<>();
    List<Node> nodes = new ArrayList<>();
    List<Field> fields = getAllFieldsForClassHierarchy(object).stream()
        .filter(field -> field.isAnnotationPresent(TransferGrid.class))
        .collect(Collectors.toList());

    //Iterates trough all the found fields.
    for (Field field : fields) {
      TransferGrid annotation = field.getAnnotation(TransferGrid.class);

      labels.add(new Label(field.getName()));

      try {
        field.setAccessible(true);
        //First option is for ComboBox
        if (hasOptions(annotation)) {
          ComboBox<String> valueNode = new ComboBox<>();
          valueNode.getItems().addAll(annotation.options());
          valueNode.setEditable(annotation.editable());

          if (valueNode.isEditable()) {
            setValueChangerFunction(field, valueNode);
          } else {
            valueNode.setMouseTransparent(!valueNode.isEditable());
            valueNode.setFocusTraversable(valueNode.isEditable());
          }

          selectCurrentValue(field, valueNode);

          nodes.add(valueNode);

          //second option for TextField
        } else if (isTextField(annotation)) {
          TextField valueNode = new TextField(getText(field));
          valueNode.setEditable(annotation.editable());

          if (valueNode.isEditable()) {
            setValueChangerFunction(field, valueNode);
          } else {
            valueNode.setMouseTransparent(!valueNode.isEditable());
            valueNode.setFocusTraversable(valueNode.isEditable());
          }

          nodes.add(valueNode);

          //last option for TextArea. This was made as else if in case
          //of future additions
        } else if (isTextArea(annotation)) {
          TextArea valueNode = new TextArea(getText(field));
          valueNode.setEditable(annotation.editable());

          if (valueNode.isEditable()) {
            setValueChangerFunction(field, valueNode);
          } else {
            valueNode.setMouseTransparent(!valueNode.isEditable());
            valueNode.setFocusTraversable(valueNode.isEditable());
          }

          nodes.add(valueNode);
        }

        //Sets the width of the last added Node.
        //This mnakes sure we arent trying to set the width of the ComboBox...
        if (nodes.get(nodes.size() - 1).getClass() != ComboBox.class) {
          setMaxWidth((TextInputControl) nodes.get(nodes.size() - 1));
        }

      } catch (Exception e) {
        System.err.println("Error whilst constructing the getting"
            + "the fields and annotations for the Grid!" + e.getMessage());
      }
    }

    //Generates the Grid and returns it.
    return generateGrid(labels, nodes);
  }

  private List<Field> getAllFieldsForClassHierarchy(Object object) {
    List<Field> fields = new ArrayList<>();

    Class objClass = object.getClass();
    while (hasSuperClass(objClass)) {
      Collections.addAll(fields, objClass.getDeclaredFields());
      objClass = objClass.getSuperclass();
    }

    return fields;
  }

  private void setMaxWidth(TextInputControl field) {
    field.setMaxWidth(NODE_WIDTH_LIMIT);
  }

  private boolean isTextField(TransferGrid anot) {
    return ((anot).fieldtype() == FieldType.TEXT_FIELD);
  }

  private boolean isTextArea(TransferGrid anot) {
    return ((anot).fieldtype() == FieldType.TEXT_AREA);
  }

  private void selectCurrentValue(Field field, ComboBox<String> combo) {
    try {
      combo.getSelectionModel().select((String) field.get(gridObject));
    } catch (Exception e) {
      System.err.println("Couldn't set the value of the ComboBox " + e.getMessage());
    }
  }

  private boolean hasOptions(TransferGrid anot) {
    return !((anot).options().length == 0);
  }

  private String getText(Field field) throws IllegalArgumentException, IllegalAccessException {
    if (field.get(gridObject) == null) {
      return "";
    }
    return String.valueOf(field.get(gridObject));
  }

  private boolean hasSuperClass(Class object) {
    return (object.getSuperclass() != null);
  }

  private GridPane generateGrid(List<Label> labels, List<Node> nodes) {
    grid.getChildren().clear();
    int row = 0;
    for (int i = 0; i < labels.size(); i++) {
      Label label = labels.get(i);
      GridPane.setHalignment(label, HPos.LEFT);
      label.setLayoutY(100);
      grid.add(label, 0, row);

      if (isSideBySide) {
        grid.add(nodes.get(i), 1, row);
      } else {
        row++;
        grid.add(nodes.get(i), 0, row);
      }

      row++;
    }
    return grid;
  }

  private void setValueChangerFunction(Field field, TextField tempfield) {
    tempfield.setOnKeyPressed(event -> {
      if (event.getCode() == KeyCode.ENTER) {
        setObjectToField(field, tempfield);
      }
    });
  }

  private void setValueChangerFunction(Field field, ComboBox<String> comboBox) {
    comboBox.valueProperty().addListener((ov, t, t1) -> setObjectToField(field, comboBox));

  }

  private void setValueChangerFunction(Field field, TextArea textArea) {
    textArea.textProperty().addListener((ov, s, s2) -> setObjectToField(field, textArea));
  }

  private void setObjectToField(Field field, TextField tempfield) {
    String text = tempfield.getText();
    try {
      switch (field.getType().getName()) {
        case "int":
          field.set(gridObject, Integer.parseInt(text));
          break;
        case "java.lang.String":
          field.set(gridObject, text);
          break;
        case "float":
          field.set(gridObject, Float.parseFloat(text));
          break;
        case "boolean":
          field.set(gridObject, Boolean.parseBoolean(text));
          break;
        case "double":
          field.set(gridObject, Double.parseDouble(text));
          break;
        default:
          System.err.println("no case for " + field.getType().getName());
      }
    } catch (ReflectiveOperationException e) {
      try {
        System.err.println("Couldn't cast the value to the desired field. " + e.getMessage());
        tempfield.setText(String.valueOf(field.get(gridObject)));
      } catch (ReflectiveOperationException ee) {
        System.err.println("Couldn't reset the value of the field to the"
            + " GUI TextField. " + e.getMessage());
      }

    }
  }

  private void setFieldValue(Field field, String option) {
    try {
      field.set(gridObject, option);
    } catch (ReflectiveOperationException e) {
      System.err.println("Couldn't set the option to the field." + e.getMessage());
    }
  }

  private void setObjectToField(Field field, ComboBox<String> combo) {
    setFieldValue(field, combo.getSelectionModel().getSelectedItem());
  }

  private void setObjectToField(Field field, TextArea area) {
    setFieldValue(field, area.getText());
  }

  /**
   * Changes the generation method of the Grid to Side by Side. Example: Label | Field Label |
   * Field
   */
  public void setSideBySide() {
    isSideBySide = true;
  }

  /**
   * Changes the generation method of the Grid to Above eachother. Example: Label Field Label Field
   */
  public void setAboveEach() {
    isSideBySide = false;
  }

  /**
   * Manually set your own limit to how wide TextInputs can get. Standard is set to 300. <b>This
   * does not change the Width of an already generated Grid! Call turnObjectIntoGrid() again to
   * generate with the new Width!</b>
   */
  public void setNodeWidthLimit(double limit) {
    NODE_WIDTH_LIMIT = limit;
  }

}
