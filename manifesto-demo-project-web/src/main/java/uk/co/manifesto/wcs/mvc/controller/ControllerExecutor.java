
package uk.co.manifesto.wcs.mvc.controller;

import java.util.Map.Entry;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import COM.FutureTense.Interfaces.ICS;

public class ControllerExecutor {

    private static final Log LOG_TIME = LogFactory.getLog(ControllerExecutor.class.getName() + ".time");

    private ControllerExecutor() {
        super();
    }

    @SuppressWarnings("deprecation")
    private static ServletContext getServletContext(ICS ics) {
        return ics.getIServlet().getServlet().getServletContext();
    }

    /**
     * Calls {@link Controller#handleRequest(ICS)} and copies the model the the request scope.
     * 
     * @param ics
     * @param context
     * @param name the name of the Controller to call
     */
    public static void executePageController(final ICS ics, String name) {
        ServletContext ctx = getServletContext(ics);
        @SuppressWarnings("deprecation")
        HttpServletRequest r = ics.getIServlet().getServletRequest();
        Model model = lookupAndHandleRequest(ics, ctx, name);
        if (model != null) {
            for (Entry<String, Object> e : model) {
                if (r.getAttribute(e.getKey()) == null) {
                    r.setAttribute(e.getKey(), e.getValue());
                }
            }
        }

    }

    /**
     * Calls {@link Controller#handleRequest(ICS)} and returns the model.
     * 
     * @param ics
     * @param context
     * @param name the name of the Controller to
     */

    public static Model lookupAndHandleRequest(final ICS ics, ServletContext context, String name) {
        final long start = LOG_TIME.isDebugEnabled() ? System.nanoTime() : 0;
        final ControllerResolver resolver = getControllerResolver(context);
        if (resolver == null) {
            throw new IllegalStateException(
                    "The ControllerResolver cannot be found. Is a ServletContextListener that registers a ControllerResolver defined?");
        }
        final Controller controller = resolver.getController(ics, name);
        if (start > 0) {
            LOG_TIME.debug(String.format("Locating Controller took %d us", (System.nanoTime() - start) / 1000));
        }
        if (controller != null) {

            final long beforeHandleRequest = LOG_TIME.isDebugEnabled() ? System.nanoTime() : 0;
            final Model model = controller.handleRequest(ics);

            if (beforeHandleRequest > 0) {
                LOG_TIME.debug(String.format("Executing Controller %s took %d us", controller.getClass().getName(),
                        (System.nanoTime() - beforeHandleRequest) / 1000));

            }
            return model;
        } else {
            throw new IllegalArgumentException("Controller with name '" + name + "' cannot be found.");
        }
    }

    protected static ControllerResolver getControllerResolver(ServletContext context) {
        final Object o = context.getAttribute(ControllerResolver.class.getName());
        if (o instanceof ControllerResolver) {
            return (ControllerResolver) o;
        }
        return null;
    }

}
