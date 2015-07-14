
package uk.co.manifesto.wcs.mvc.groovy;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import uk.co.manifesto.wcs.mvc.controller.ControllerResolver;

public class GroovyControllerContextListener implements ServletContextListener {

    public GroovyControllerContextListener() {

    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ControllerResolver resolver = make(sce.getServletContext());
        if (resolver instanceof ServletContextListener) {
            ServletContextListener scl = (ServletContextListener) resolver;
            scl.contextInitialized(sce);
        }

        sce.getServletContext().setAttribute(ControllerResolver.class.getName(), resolver);

    }

    protected ControllerResolver make(ServletContext context) {
        return new GroovyControllerResolver(context);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        ControllerResolver resolver = (ControllerResolver) sce.getServletContext().getAttribute(
                ControllerResolver.class.getName());
        if (resolver instanceof ServletContextListener) {
            ServletContextListener scl = (ServletContextListener) resolver;
            scl.contextDestroyed(sce);
        }
        sce.getServletContext().removeAttribute(ControllerResolver.class.getName());

    }

}
