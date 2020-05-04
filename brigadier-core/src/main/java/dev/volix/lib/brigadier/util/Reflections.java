package dev.volix.lib.brigadier.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * @author Tobias Büser
 */
public class Reflections {

    /**
     * Checks a method for different conditions
     *
     * @param method      The method
     * @param beStatic    Method must be static?
     * @param bePublic    Method must be public?
     * @param returnType  Method must return ..?
     * @param annotations Method must have annotations..?
     * @param parameter   Method must have parameter..?
     *
     * @return The result
     */
    public static boolean checkMethodErasure(Method method, boolean beStatic, boolean bePublic, Class<?> returnType,
                                             Class<? extends Annotation>[] annotations, Class<?>[] parameter) {
        // check modifier
        if ((beStatic != Modifier.isStatic(method.getModifiers()))
            || (bePublic != Modifier.isPublic(method.getModifiers()))) {
            return false;
        }

        // check return type
        if ((returnType == null && !method.getReturnType().equals(Void.TYPE))
            || (returnType != null && !returnType.isAssignableFrom(method.getReturnType()))) {
            return false;
        }

        // check annotations
        for (Class<? extends Annotation> an : annotations) {
            if (!method.isAnnotationPresent(an)) {
                return false;
            }
        }

        // check parameter
        for (int i = 0; i < method.getParameters().length; i++) {
            if (i >= parameter.length) return false;
            Class<?> paramType = method.getParameters()[i].getType();
            if (!parameter[i].isAssignableFrom(paramType)) {
                return false;
            }
        }
        return true;
    }

    public static boolean checkMethodErasure(Method method, Class<?> returnType, Class<? extends Annotation> annotation, Class<?>[] parameter) {
        return checkMethodErasure(method, false, true, returnType, new Class[] {annotation}, parameter);
    }

    public static boolean checkMethodErasure(Method method, Class<? extends Annotation> annotation, Class<?>[] parameter) {
        return checkMethodErasure(method, false, true, null, new Class[] {annotation}, parameter);
    }

}
