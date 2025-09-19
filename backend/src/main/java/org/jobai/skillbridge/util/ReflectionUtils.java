package org.jobai.skillbridge.util;

import java.lang.reflect.Method;

public class ReflectionUtils {
    
    /**
     * Helper method to set field value using reflection
     * @param obj The object to set the field value on
     * @param fieldName The name of the field
     * @param value The value to set
     */
    public static void setFieldValue(Object obj, String fieldName, Object value) {
        if (obj == null || fieldName == null || value == null) {
            return;
        }
        
        try {
            String capitalizedFieldName = Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
            String setterName = "set" + capitalizedFieldName;
            
            // Find the appropriate setter method
            Method setter = null;
            for (Method method : obj.getClass().getMethods()) {
                if (method.getName().equals(setterName) && method.getParameterCount() == 1) {
                    Class<?> paramType = method.getParameterTypes()[0];
                    if (paramType.isAssignableFrom(value.getClass()) || 
                        isPrimitiveCompatible(paramType, value.getClass())) {
                        setter = method;
                        break;
                    }
                }
            }
            
            if (setter != null) {
                setter.invoke(obj, value);
            }
        } catch (Exception e) {
            System.err.println("Could not set field " + fieldName + " in " + obj.getClass().getName());
            e.printStackTrace();
        }
    }
    
    /**
     * Check if primitive types are compatible
     * @param primitiveType The primitive type
     * @param wrapperType The wrapper type
     * @return True if compatible, false otherwise
     */
    private static boolean isPrimitiveCompatible(Class<?> primitiveType, Class<?> wrapperType) {
        if (!primitiveType.isPrimitive()) {
            return false;
        }
        
        if (primitiveType == boolean.class && wrapperType == Boolean.class) return true;
        if (primitiveType == byte.class && wrapperType == Byte.class) return true;
        if (primitiveType == char.class && wrapperType == Character.class) return true;
        if (primitiveType == double.class && wrapperType == Double.class) return true;
        if (primitiveType == float.class && wrapperType == Float.class) return true;
        if (primitiveType == int.class && wrapperType == Integer.class) return true;
        if (primitiveType == long.class && wrapperType == Long.class) return true;
        if (primitiveType == short.class && wrapperType == Short.class) return true;
        
        return false;
    }
}