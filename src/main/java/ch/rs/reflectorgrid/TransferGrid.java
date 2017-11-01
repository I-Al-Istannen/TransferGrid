/*
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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is supposed to be used in conjunction with {@link ReflectorGrid}. It is used to
 * set different options during the creation of the grid.
 *
 * @author Ricardo S., RS
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface TransferGrid {

  /**
   * This defines the possible options for the TextInputField.
   */
  enum FieldType {
    TEXT_FIELD, TEXT_AREA
  }

  /**
   * This defines if a variable is write and read or read only. default is set to true.
   *
   * <br>true == write/read
   *
   * <br>false == read only
   */
  boolean editable() default true;

  /**
   * This defines if the variable is supposed to be set using a ComboBox.
   *
   * <p>This takes precedence over {@link #fieldtype()}. If you give options any input, it will turn
   * into a ComboBox, no matter the setting in {@link #fieldtype()}.
   *
   * <p><strong>Example:</strong>
   *
   * <br>{@code TransferGrid(options = {"full", "half", "none"});}
   */
  String[] options() default {};


  /**
   * This defines what kind of TextInputField you want.
   *
   * <p>Default value is set to {@link FieldType#TEXT_FIELD}. You can manually change it to {@link
   * FieldType#TEXT_AREA} for something like "notes" in your object.
   *
   * <p><strong>WARNING:</strong>
   *
   * <br>If {@link #options()} has been filled, whatever you set here is ignored.
   */
  FieldType fieldtype() default FieldType.TEXT_FIELD;

}
