package ch.rs.reflectorgrid;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * A small helper for Reflection.
 */
class ReflectionHelper {

  /**
   * Returns all fields from the class and all superclasses.
   *
   * @param start The start {@link Class}
   * @param filter A filter for the fields, in order to only return some results.
   * @return All fields matching the {@link Predicate} from the whole class hierarchy.
   */
  static List<Field> getAllFieldsInClassHierachy(Class<?> start, Predicate<Field> filter) {
    List<Field> fields = new ArrayList<>();
    Class<?> currentClass = start;

    while (currentClass != null) {
      Collections.addAll(fields, currentClass.getDeclaredFields());
      currentClass = currentClass.getSuperclass();
    }

    return fields.stream()
        .filter(filter)
        .collect(Collectors.toList());
  }

  /**
   * Returns the value of a field.
   *
   * @param field The {@link Field} to get the value from.
   * @param handle The handle object to use (according to {@link Field#get(Object)})
   * @param <T> The type of the return value you expect
   * @return The value of the field.
   * @throws ClassCastException if the type is not what you stored it as
   * @throws ReflectionHelperException if any {@link ReflectiveOperationException} occurs.
   */
  static <T> T getFieldValue(Field field, Object handle) {
    try {
      field.setAccessible(true);
      @SuppressWarnings("unchecked")
      T t = (T) field.get(handle);
      return t;
    } catch (ReflectiveOperationException e) {
      throw new ReflectionHelperException(e);
    }
  }

  /**
   * Sets the value of a field.
   *
   * @param field The {@link Field} to set the value for.
   * @param handle The handle object to use (according to {@link Field#set(Object, Object)})
   * @param value The value to set it to
   * @throws ReflectionHelperException if any {@link ReflectiveOperationException} occurs.
   */
  static void setFieldValue(Field field, Object handle, Object value) {
    try {
      field.setAccessible(true);
      field.set(handle, value);
    } catch (ReflectiveOperationException e) {
      throw new ReflectionHelperException(e);
    }
  }

  static class ReflectionHelperException extends RuntimeException {

    ReflectionHelperException(Throwable cause) {
      super(cause);
    }
  }
}
