
package uk.co.manifesto.wcs.mvc.groovy;

import groovy.lang.GroovyClassLoader;
import uk.co.manifesto.wcs.mvc.factory.AnnotationFactory;
import uk.co.manifesto.wcs.mvc.factory.BaseFactory;
import uk.co.manifesto.wcs.mvc.factory.Factory;
import uk.co.manifesto.wcs.mvc.injection.AnnotationInjector;
import uk.co.manifesto.wcs.mvc.injection.Injector;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import COM.FutureTense.Interfaces.ICS;

public class GroovyInjector implements Injector {
    private static final Log LOG = LogFactory.getLog(GroovyInjector.class.getPackage().getName());
    private final GroovyClassLoader classLoader;
    private Class<?> generalFactoryClass;
    private BaseFactory base = new BaseFactory();

    public GroovyInjector(GroovyClassLoader gcl) {

        this.classLoader = gcl;
        try {
            this.generalFactoryClass = classLoader.loadClass("mvc.ObjectFactory", true, false, false);
        } catch (ClassNotFoundException e) {
            // ignore
            this.generalFactoryClass = null;
        }

    }

    @Override
    public void inject(ICS ics, Object object) throws Exception {
        Factory factory = (Factory) ics.GetObj(Factory.class.getName());
        if (factory == null) {
            factory = new AnnotationFactory(ics, getFactories(ics));
            ics.SetObj(Factory.class.getName(), factory);
        }
        AnnotationInjector.inject(object, factory);

    }

    private Object[] getFactories(ICS ics) throws InvocationTargetException {

        String site = ics.GetVar("site");
        Class<?> siteClass = null;

        if (StringUtils.isNotBlank(site)) {
            try {
                siteClass = classLoader.loadClass("mvc." + site.toLowerCase() + ".ObjectFactory", true, false, false);
            } catch (ClassNotFoundException e) {
                // ignore
            }
        }
        if (siteClass == null) {
            if (generalFactoryClass == null) {
                return make();
            } else {
                return make(generalFactoryClass);
            }
        } else if (generalFactoryClass == null) {
            return make(siteClass);
        } else {
            return make(siteClass, generalFactoryClass);
        }

    }

    private Object[] make(Class<?>... classes) throws InvocationTargetException {
        Object[] o = new Object[classes.length + 1];
        for (int i = 0; i < classes.length; i++) {
            o[i] = construct(classes[i]);
        }
        o[classes.length] = base;
        return o;
    }

    private Object construct(Class<?> reflectionClass) throws InvocationTargetException {
        Constructor<?> ctor;

        try {
            ctor = reflectionClass.getConstructor();
            if (Modifier.isPublic(ctor.getModifiers())) {
                return ctor.newInstance();
            } else {
                throw new RuntimeException(reflectionClass.getName() + " does not have a public constructor.");
            }
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(reflectionClass.getName() + " should have a public constructor.");
        } catch (InstantiationException e) {
            LOG.error(e.getMessage());
            throw new RuntimeException(e);
        } catch (IllegalArgumentException e) {
            LOG.error("Huh, Can't happen, the arguments are checked: " + e.getMessage());
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            LOG.error("Huh, Can't happen, the modifier is checked for public: " + e.getMessage());
            throw new RuntimeException(e);
        }

    }

}
