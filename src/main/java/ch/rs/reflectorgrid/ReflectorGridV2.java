package ch.rs.reflectorgrid;

import ch.rs.reflectorgrid.LabelDisplayOrder.InsertionPosition;
import ch.rs.reflectorgrid.typeconversion.TypeConverterCollection;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ComboBoxBase;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.layout.GridPane;
import javafx.util.Pair;

/**
 * A small rewrite of the reflector grid.
 */
public class ReflectorGridV2 {

  private LabelDisplayOrder labelDisplayOrder;
  private double nodeWidthLimit;
  private FieldNamingStrategy fieldNamingStrategy;
  private TypeConverterCollection typeConverterCollection;

  public ReflectorGridV2(LabelDisplayOrder labelDisplayOrder, double nodeWidthLimit) {
    this.labelDisplayOrder = labelDisplayOrder;
    this.nodeWidthLimit = nodeWidthLimit;

    this.fieldNamingStrategy = DefaultFieldNamingStrategy.VERBATIM;
    this.typeConverterCollection = new TypeConverterCollection();
  }

  /**
   * @param fieldNamingStrategy The {@link FieldNamingStrategy} to use
   * @return This object
   */
  public ReflectorGridV2 setFieldNamingStrategy(FieldNamingStrategy fieldNamingStrategy) {
    this.fieldNamingStrategy = fieldNamingStrategy;

    return this;
  }

  public TypeConverterCollection getTypeConverterCollection() {
    return typeConverterCollection;
  }

  /**
   * @param object The value object
   * @return The resulting {@link GridPane}
   */
  public GridPane transformObjectToGrid(Object object) {
    Objects.requireNonNull(object, "object can not be null!");

    GridPane gridPane = createGridPane();

    List<Field> fields = ReflectionHelper.getAllFieldsInClassHierachy(
        object.getClass(), this::shouldTransferToGrid
    );

    InsertionPosition insertionPosition = new InsertionPosition(0, 0);

    for (Field field : fields) {
      Pair<Label, Node> nodes = getNodePairForField(field, object);
      insertionPosition = labelDisplayOrder
          .addNode(insertionPosition, nodes.getKey(), nodes.getValue(), gridPane);
    }

    return gridPane;
  }

  private GridPane createGridPane() {
    return new GridPane();
  }

  private boolean shouldTransferToGrid(Field field) {
    return field.isAnnotationPresent(TransferGrid.class);
  }

  private Pair<Label, Node> getNodePairForField(Field field, Object handle) {
    Label label = new Label(fieldNamingStrategy.toString(field));
    Control node;

    Consumer<String> changeListener = string -> {
      Object value = typeConverterCollection.fromString(field.getType(), string);
      ReflectionHelper.setFieldValue(field, handle, value);
    };

    TransferGrid annotation = field.getAnnotation(TransferGrid.class);

    if (annotation.options().length > 0) {
      node = createComboBox(annotation, field, handle, changeListener);
    } else {
      switch (annotation.fieldtype()) {
        case TEXT_FIELD:
          node = createTextField(field, handle, changeListener);
          break;
        case TEXT_AREA:
          node = createTextArea(field, handle, changeListener);
          break;
        default:
          throw new IllegalArgumentException("Unknown field type: " + annotation.fieldtype());
      }
    }

    adjustNodeProperties(annotation, node);

    return new Pair<>(label, node);
  }

  private ComboBox<String> createComboBox(TransferGrid annotation, Field field,
      Object handle, Consumer<String> changeListener) {
    ComboBox<String> comboBox = new ComboBox<>(
        FXCollections.observableArrayList(annotation.options())
    );

    comboBox.getSelectionModel().select(
        objectToString(ReflectionHelper.getFieldValue(field, handle))
    );
    comboBox.getSelectionModel().selectedItemProperty()
        .addListener((obs, ov, newValue) -> changeListener.accept(newValue));

    return comboBox;
  }

  private TextField createTextField(Field field, Object handle, Consumer<String> changeListener) {
    TextField textField = new TextField(
        objectToString(ReflectionHelper.getFieldValue(field, handle))
    );

    textField.textProperty().addListener((obs, ov, newValue) -> changeListener.accept(newValue));

    return textField;
  }

  private TextArea createTextArea(Field field, Object handle, Consumer<String> changeListener) {
    TextArea textArea = new TextArea(objectToString(ReflectionHelper.getFieldValue(field, handle)));

    textArea.textProperty().addListener((obs, ov, newValue) -> changeListener.accept(newValue));

    return textArea;
  }

  private String objectToString(Object object) {
    return object == null ? "" : object.toString();
  }

  private void adjustNodeProperties(TransferGrid annotation, Control node) {
    setEditable(node, annotation.editable());
    node.setMouseTransparent(!annotation.editable());
    node.setFocusTraversable(annotation.editable());

    node.setMaxWidth(nodeWidthLimit);
  }

  private void setEditable(Control node, boolean editable) {
    if (node instanceof TextInputControl) {
      ((TextInputControl) node).setEditable(editable);
    } else if (node instanceof ComboBoxBase) {
      ((ComboBoxBase) node).setEditable(false);
    } else {
      throw new IllegalArgumentException("Can't make node uneditable: " + node);
    }
  }
}
