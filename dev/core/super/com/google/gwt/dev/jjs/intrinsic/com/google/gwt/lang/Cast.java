/*
 * Copyright 2008 Google Inc.
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
package com.google.gwt.lang;

import com.google.gwt.core.client.JavaScriptObject;

// CHECKSTYLE_NAMING_OFF: Uses legacy conventions of underscore prefixes.

/**
 * This is a magic class the compiler uses to perform any cast operations that require code.<br />
 *
 * The cast operations are only as accurate as the contents of the castableTypeMaps and should not
 * be used directly by user code. The compiler takes care to record most cast operations in user
 * code so that it can build limited but accurate castableTypeMaps.
 */
final class Cast {

  /**
   * As plain JavaScript Strings (not monkey patched) are used to model Java Strings,
   * {@code  stringCastMap} stores runtime type info for cast purposes for string objects.
   *
   * NOTE: it is important that the field is left uninitialized so that Cast does not
   * require a clinit.
   */
  private static JavaScriptObject stringCastMap;

  static native boolean canCast(Object src, JavaScriptObject dstId) /*-{
    return src.@java.lang.Object::castableTypeMap && !!src.@java.lang.Object::castableTypeMap[dstId]
        || @com.google.gwt.lang.Cast::isJavaString(Ljava/lang/Object;)(src) &&
        !!@com.google.gwt.lang.Cast::stringCastMap[dstId];
  }-*/;

  static native boolean canCastClass(Class<?> srcClazz, Class<?> dstClass) /*-{
    var srcTypeId = srcClazz.@java.lang.Class::typeId;
    var dstTypeId = dstClass.@java.lang.Class::typeId;
    var prototype = @com.google.gwt.lang.JavaClassHierarchySetupUtil::prototypesByTypeId[srcTypeId];
    return @com.google.gwt.lang.Cast::canCast(*)(prototype, dstTypeId);
  }-*/;

  static native String charToString(char x) /*-{
    return String.fromCharCode(x);
  }-*/;

  static Object dynamicCast(Object src, JavaScriptObject dstId) {
    if (src != null && !canCast(src, dstId)) {
      throw new ClassCastException();
    }
    return src;
  }

  /**
   * Allow a dynamic cast to an object, always succeeding if it's a JSO.
   */
  static Object dynamicCastAllowJso(Object src, JavaScriptObject dstId) {
    if (src != null && !isJavaScriptObject(src) &&
        !canCast(src, dstId)) {
      throw new ClassCastException();
    }
    return src;
  }

  /**
   * Allow a cast to JSO only if there's no type ID.
   */
  static Object dynamicCastJso(Object src) {
    if (src != null && isJavaObject(src)) {
      throw new ClassCastException();
    }
    return src;
  }

  static boolean instanceOf(Object src, JavaScriptObject dstId) {
    return (src != null) && canCast(src, dstId);
  }

  static boolean instanceOfJso(Object src) {
    return (src != null) && isJavaScriptObject(src);
  }

  /**
   * Returns true if the object is a Java object and can be cast, or if it's a
   * non-null JSO.
   */
  static boolean instanceOfOrJso(Object src, JavaScriptObject dstId) {
    return (src != null) &&
        (isJavaScriptObject(src) || canCast(src, dstId));
  }

  static boolean isJavaObject(Object src) {
    return isRegularJavaObject(src) || isJavaString(src);
  }

  static boolean isJavaScriptObject(Object src) {
    return !isRegularJavaObject(src) && !isJavaString(src);
  }

  static boolean isJavaScriptObjectOrString(Object src) {
    return !isRegularJavaObject(src);
  }

  /**
   * Uses the not operator to perform a null-check; do NOT use on anything that
   * could be a String.
   */
  static native boolean isNotNull(Object src) /*-{
    // Coerce to boolean.
    return !!src;
  }-*/;

  /**
   * Uses the not operator to perform a null-check; do NOT use on anything that
   * could be a String.
   */
  static native boolean isNull(Object src) /*-{
    return !src;
  }-*/;

  static native boolean jsEquals(Object a, Object b) /*-{
    return a == b;
  }-*/;

  static native boolean jsNotEquals(Object a, Object b) /*-{
    return a != b;
  }-*/;

  static native Object maskUndefined(Object src) /*-{
    return (src == null) ? null : src;
  }-*/;

  /**
   * See JLS 5.1.3.
   */
  static native byte narrow_byte(double x) /*-{
    return x << 24 >> 24;
  }-*/;

  /**
   * See JLS 5.1.3.
   */
  static native char narrow_char(double x) /*-{
    return x & 0xFFFF;
  }-*/;

  /**
   * See JLS 5.1.3.
   */
  static native int narrow_int(double x) /*-{
    return ~~x;
  }-*/;

  /**
   * See JLS 5.1.3.
   */
  static native short narrow_short(double x) /*-{
    return x << 16 >> 16;
  }-*/;

  /**
   * See JLS 5.1.3 for why we do a two-step cast. First we round to int, then
   * narrow to byte.
   */
  static byte round_byte(double x) {
    return narrow_byte(round_int(x));
  }

  /**
   * See JLS 5.1.3 for why we do a two-step cast. First we round to int, then
   * narrow to char.
   */
  static char round_char(double x) {
    return narrow_char(round_int(x));
  }

  /**
   * See JLS 5.1.3.
   */
  static native int round_int(double x) /*-{
    // TODO: reference java.lang.Integer::MAX_VALUE when we get clinits fixed
    return ~~Math.max(Math.min(x, 2147483647), -2147483648);
  }-*/;

  /**
   * See JLS 5.1.3 for why we do a two-step cast. First we rount to int, then
   * narrow to short.
   */
  static short round_short(double x) {
    return narrow_short(round_int(x));
  }

  /**
   * Check a statically false cast, which can succeed if the argument is null.
   * Called by compiler-generated code based on static type information.
   */
  static Object throwClassCastExceptionUnlessNull(Object o)
      throws ClassCastException {
    if (o != null) {
      throw new ClassCastException();
    }
    return o;
  }

  public static native JavaScriptObject getNullMethod() /*-{
    return @null::nullMethod();
  }-*/;

  /**
   * Returns whether the Object is a Java String.
   *
   * Java strings are translated to JavaScript strings.
   */
  // Visible for getIndexedMethod()
  static native boolean isJavaString(Object src) /*-{
    // TODO(rluble): This might need to be specialized by browser.
    return typeof(src) == "string" || src instanceof String;
  }-*/;

  /**
   * Returns whether the Object is a Java Object but not a String.
   *
   * Depends on all Java Objects (except for String and Arrays) having the typeMarker field
   * generated, and set to the nullMethod for the current GWT module.  Note this
   * test essentially tests whether an Object is a java object for the current
   * GWT module.  Java Objects from external GWT modules are not recognizable as
   * Java Objects in this context.
   */
  // Visible for getIndexedMethod()
  static boolean isRegularJavaObject(Object src) {
    return Util.getTypeMarker(src) == getNullMethod() && !instanceofArray(src);
  }

  /**
   * Returns true if {@code src} is an array (native or not).
   */
  static native boolean instanceofArray(Object src) /*-{
    return Array.isArray(src);
  }-*/;
}

// CHECKSTYLE_NAMING_ON
