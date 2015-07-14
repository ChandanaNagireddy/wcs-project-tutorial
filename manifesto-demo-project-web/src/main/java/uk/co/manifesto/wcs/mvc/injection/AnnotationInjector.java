
package uk.co.manifesto.wcs.mvc.injection;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.manifesto.wcs.mvc.factory.Factory;

public class AnnotationInjector {

    private static final Log LOG = LogFactory.getLog(AnnotationInjector.class.getPackage().getName());
    private static final Log LOG_TIME = LogFactory.getLog(AnnotationInjector.class.getPackage().getName() + ".time");

    private AnnotationInjector() {
        // static methods only
    }

    /**
     * Inject Factory created objects into the object. Objects flagged with the {@link Inject} annotation will be
     * populated by this method by retrieving the value from the {@link Factory#getObject(String,Class)}
     * method.
     * 
     * @param object the object to inject into
     * @param factory the factory that created the objects that need to be injected.
     */
    public static final void inject(final Object object, final Factory factory) {
        if (object == null) {
            throw new IllegalArgumentException("object cannot be null.");
        }
        if (factory == null) {
            throw new IllegalArgumentException("factory cannot be null.");
        }
        final long start = LOG_TIME.isDebugEnabled() ? System.nanoTime() : 0L;
        try {
            Class<?> c = object.getClass();
            // first to all annotated public setter methods.
            for (final Method method : c.getMethods()) {
                if (method.isAnnotationPresent(Inject.class)) {
                    injectIntoMethod(object, factory, method);
                }
            }
            // and then all annotated fields.
            while (c != Object.class && c != null) {
                for (final Field field : c.getDeclaredFields()) {
                    if (field.isAnnotationPresent(Inject.class)) {
                        injectIntoField(object, factory, field);
                    }

                }

                c = c.getSuperclass();
            }
        } finally {
            if (start > 0) {
                LOG_TIME.debug(String.format("inject model for %s in %d us", object.getClass().getName(),
                        (System.nanoTime() - start) / 1000));
            }

        }
    }

    /**
     * @param object
     * @param factory
     * @param field
     * @throws SecurityException
     */
    public static void injectIntoField(final Object object, final Factory factory, final Field field)
            throws SecurityException {

        final Inject ifr = field.getAnnotation(Inject.class);

        String name = ifr.value();
        final Object injectionValue = factory.getObject(name, field.getType());
        if (injectionValue == null) {
            throw new InjectionException(factory.getClass().getName() + " does not know how to inject '"
                    + field.getType().getName() + "' into the field '" + field.getName() + "' for '"
                    + object.getClass().getName() + "'.");
        }
        field.setAccessible(true); // make private fields accessible
        if (LOG.isTraceEnabled()) {
            LOG.trace("Injecting " + injectionValue.getClass().getName() + " into field '" + field.getName()
                    + "' for '" + object.getClass().getName() + "'.");
        }
        try {
            field.set(object, injectionValue);
        } catch (final IllegalArgumentException e) {
            throw new InjectionException("IllegalArgumentException injecting " + injectionValue + " into field '"
                    + field.getName() + "' for '" + object.getClass().getName() + "'.", e);
        } catch (final IllegalAccessException e) {
            throw new InjectionException("IllegalAccessException injecting " + injectionValue + " into field '"
                    + field.getName() + "' for '" + object.getClass().getName() + "'.", e);
        }
    }

    /**
     * @param object
     * @param factory
     * @param method
     * @throws SecurityException
     */
    public static void injectIntoMethod(final Object object, final Factory factory, final Method method)
            throws SecurityException {
        // LOG.trace("Found annotated field: "+field.getName());
        final Inject ifr = method.getAnnotation(Inject.class);

        String name = ifr.value();

        final Class<?> type = method.getParameterTypes()[0];
        final Object injectionValue = factory.getObject(name, type);
        if (injectionValue == null) {
            throw new InjectionException(factory.getClass().getName() + " does not know how to inject '"
                    + type.getName() + "' into the method '" + method.getName() + "' for an action.");
        }

        // accessible
        if (LOG.isDebugEnabled()) {
            LOG.debug("Injecting " + injectionValue.getClass().getName() + " into method " + method.getName()
                    + " for '" + object.getClass().getName() + "'.");
        }
        try {
            method.invoke(object, injectionValue);
        } catch (final IllegalArgumentException e) {
            throw new InjectionException("IllegalArgumentException injecting '" + injectionValue + "' into method '"
                    + method.getName() + "' for '" + object.getClass().getName() + "'.", e);
        } catch (final IllegalAccessException e) {
            throw new InjectionException("IllegalAccessException injecting '" + injectionValue + "' into method '"
                    + method.getName() + "' for '" + object.getClass().getName() + "'.", e);
        } catch (final InvocationTargetException e) {
            throw new InjectionException("InvocationTargetException injecting '" + injectionValue + "' into method '"
                    + method.getName() + "' for '" + object.getClass().getName() + "'.", e);
        }
    }

}
