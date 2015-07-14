
package uk.co.manifesto.wcs.mvc.factory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import COM.FutureTense.Interfaces.ICS;
import uk.co.manifesto.wcs.mvc.injection.AnnotationInjector;
import uk.co.manifesto.wcs.mvc.injection.Producer;

public class AnnotationFactory implements Factory {
    private static final Log LOG = LogFactory.getLog(AnnotationFactory.class);
    private final ICS ics;

    private final Map<String, Object> objectCache = new HashMap<String, Object>();
    private final Object[] factories;

    public AnnotationFactory(ICS ics, Object... factories) {
        super();
        if (ics == null)
            throw new IllegalArgumentException("ics cannot be null");

        this.ics = ics;
        if (factories == null || factories.length == 0)
            throw new IllegalArgumentException("factories cannot be null");
        this.factories = factories;

    }

    /* (non-Javadoc)
     * @see com.manifesto.wcs.mvc.factory.Factory#getObject(java.lang.String, java.lang.Class)
     */
    @Override
    public final <T> T getObject(final String name, final Class<T> fieldType) {

        T o;
        try {
            o = locate(name, fieldType);
            if (o == null) {
                o = ctorStrategy(name, fieldType);
            }
        } catch (InvocationTargetException e) {
            Throwable t = e.getTargetException();
            if (t == null) {
                throw new RuntimeException(e);
            } else if (t instanceof RuntimeException) {
                throw (RuntimeException) t;
            } else {
                throw new RuntimeException(t);
            }
        }
        return o;
    }

    /**
     * Internal method to check for Services or create Services.
     * 
     * @param askedName
     * @param c
     * @return the found service, null if no T can be created.
     * @throws InvocationTargetException
     */
    @SuppressWarnings("unchecked")
    protected <T> T locate(final String askedName, final Class<T> c) throws InvocationTargetException {
        if (ICS.class.isAssignableFrom(c)) {
            return (T) ics;
        }
        if (c.isArray()) {
            throw new IllegalArgumentException("Arrays are not supported");
        }
        final String name = StringUtils.isNotBlank(askedName) ? askedName : c.getSimpleName();

        Object o = objectCache.get(name);
        if (o != null && !c.isAssignableFrom(o.getClass()))
            throw new IllegalStateException("Name conflict: '" + name + "' is in cache and is of type  '"
                    + o.getClass() + "' but a '" + c.getName()
                    + "' was asked for. Please check your factories for naming conflicts.");
        if (o == null) {
            o = namedAnnotationStrategy(name, c);
        }
        if (o == null) {
            o = unnamedAnnotationStrategy(name, c);
        }

        return (T) o;
    }

    /**
     * Tries to create the object based on the {@link Producer} annotation where the names match.
     * 
     * @param name
     * @param c
     * @return
     * @throws InvocationTargetException
     */
    protected <T> T namedAnnotationStrategy(String name, Class<T> c) throws InvocationTargetException {

        for (Object factory : factories) {
            Class<?> reflectionClass = factory.getClass();
            for (Method m : reflectionClass.getMethods()) {
                if (m.isAnnotationPresent(Producer.class) && c.isAssignableFrom(m.getReturnType())) {
                    String n = m.getAnnotation(Producer.class).name();
                    if (name.equals(n)) {
                        return createFromMethod(name, c, m, Modifier.isStatic(m.getModifiers()) ? null : factory);
                    }

                }

            }
        }
        return null;
    }

    /**
     * Tries to create the object based on the {@link Producer} annotation without a name.
     * 
     * @param name
     * @param c
     * @return
     * @throws InvocationTargetException
     */
    protected <T> T unnamedAnnotationStrategy(String name, Class<T> c) throws InvocationTargetException {

        for (Object factory : factories) {
            Class<?> reflectionClass = factory.getClass();
            for (Method m : reflectionClass.getMethods()) {
                if (m.isAnnotationPresent(Producer.class) && c.isAssignableFrom(m.getReturnType())) {
                    String n = m.getAnnotation(Producer.class).name();
                    if (StringUtils.isBlank(n)) {
                        return createFromMethod(name, c, m, Modifier.isStatic(m.getModifiers()) ? null : factory);
                    }
                }
            }
        }
        return null;
    }

    /**
     * @param name name of the object
     * @param c the type of the object to create
     * @param m the method to use to create the object
     * @return
     * @throws InvocationTargetException
     */
    @SuppressWarnings("unchecked")
    protected <T> T createFromMethod(String name, Class<T> c, Method m, Object from) throws InvocationTargetException {
        Object o = null;
        if (LOG.isTraceEnabled()) {
            LOG.trace("trying to create a " + c.getName() + " object with name " + name + "  from method "
                    + m.toGenericString());
        }

        if (c.isAssignableFrom(m.getReturnType())) {
            if (m.getParameterTypes().length == 2 && m.getParameterTypes()[0].equals(ICS.class)
                    && m.getParameterTypes()[1].isAssignableFrom(getClass())) {
                o = invokeCreateMethod(m, from, name, ics, this);
            } else if (m.getParameterTypes().length == 1 && m.getParameterTypes()[0].equals(ICS.class)) {
                o = invokeCreateMethod(m, from, name, ics);
            } else if (m.getParameterTypes().length == 0) {
                o = invokeCreateMethod(m, from, name);
            }
            if (o != null) {
                AnnotationInjector.inject(o, this);
            }
            if (shouldCache(m))
                objectCache.put(name, o);

        }
        return (T) o;
    }

    /**
     * @param m method in invoke
     * @param from object to invoke from
     * @param name the name of the object
     * @param arguments the arguments to pass to the method
     * @return
     * @throws InvocationTargetException
     */
    protected Object invokeCreateMethod(Method m, Object from, String name, Object... arguments)
            throws InvocationTargetException {
        try {
            return m.invoke(from, arguments);
        } catch (IllegalArgumentException e) {
            LOG.error("Huh, Can't happen, the arguments are checked: " + m.toString() + ", " + e.getMessage());
        } catch (IllegalAccessException e) {
            LOG.error("Huh, Can't happen, the modifier is checked for public: " + m.toString() + ", " + e.getMessage());
        }
        return null;
    }

    protected boolean shouldCache(Method m) {
        boolean r = false;
        if (m.isAnnotationPresent(Producer.class)) {
            Producer annon = m.getAnnotation(Producer.class);
            r = annon.cache();
        }
        return r;
    }

    /**
     * @param name
     * @param c
     * @return
     * @throws InvocationTargetException
     */
    protected <T> T ctorStrategy(final String name, final Class<T> c) throws InvocationTargetException {
        T o = null;
        try {
            if (c.isInterface() || Modifier.isAbstract(c.getModifiers())) {
                if (LOG.isDebugEnabled())
                    LOG.debug("Could not create  a " + c.getName() + " via a constructor. The class '" + c.getName()
                            + "' is an interface or abstract class, giving up as a class cannot be constructed.");
                return null;
            }

            final Constructor<T> constr = c.getConstructor(ICS.class);
            o = constr.newInstance(ics);
        } catch (final NoSuchMethodException e1) {
            LOG.debug("Could not create  a " + c.getName() + " via a constructor method.");
        } catch (IllegalArgumentException e) {
            LOG.debug("Could not create  a " + c.getName() + " via a constructor method.");
        } catch (InstantiationException e) {
            LOG.debug("Could not create  a " + c.getName() + " via a constructor method.");
        } catch (IllegalAccessException e) {
            LOG.debug("Could not create  a " + c.getName() + " via a constructor method.");
        }
        return o;
    }

    /**
     * @return the ics
     */
    protected ICS getIcs() {
        return ics;
    }

}
