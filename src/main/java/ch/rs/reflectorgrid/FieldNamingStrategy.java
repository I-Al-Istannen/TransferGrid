package ch.rs.reflectorgrid;

import java.lang.reflect.Field;

/**
 * Defines how a {@link Field} name is mapped to a label in the GUI.
 */
public interface FieldNamingStrategy {

  /**
   * @param field The {@link Field} to transform
   * @return The name of the field, according to this strategy
   */
  String toString(Field field);
}
