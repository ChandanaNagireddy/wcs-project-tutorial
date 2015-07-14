
package uk.co.manifesto.wcs.mvc.jsp;

import java.io.IOException;
import java.util.Map.Entry;

import javax.servlet.ServletContext;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import COM.FutureTense.Cache.CacheManager;
import COM.FutureTense.Interfaces.ICS;
import uk.co.manifesto.wcs.mvc.controller.ControllerExecutor;
import uk.co.manifesto.wcs.mvc.controller.Model;

import com.openmarket.xcelerate.publish.PubConstants;

public class ControllerTag extends SimpleTagSupport {

    public static final String ICS_VARIABLE_NAME = "ics";

    private String name;

    public ControllerTag() {

    }

    /* (non-Javadoc)
     * @see javax.servlet.jsp.tagext.SimpleTagSupport#doTag()
     */
    @Override
    public void doTag() throws JspException, IOException {
        super.doTag();
        final ICS ics = getICS();
        if (ics == null) {
            throw new JspException("ics is not found on the page context. This tags needs to be placed within the <cs:ftcs> tag.");
        }
        if (ics.GetVar("tid") != null) {
            logDep(ics, "Template", ics.GetVar("tid"));
        }
        if (ics.GetVar("seid") != null) {
            logDep(ics, "SiteEntry", ics.GetVar("seid"));
        }
        if (ics.GetVar("eid") != null) {
            logDep(ics, "CSElement", ics.GetVar("eid"));
        }
        CacheManager.RecordItem(ics, "controller" + PubConstants.SEPARATOR + name);
        ServletContext context = getPageContext().getServletContext();
        copyModel(ControllerExecutor.lookupAndHandleRequest(ics, context, name));
    }

    private void logDep(ICS ics, String c, String cid) {
        CacheManager.RecordItem(ics, PubConstants.CACHE_PREFIX + cid + PubConstants.SEPARATOR + c);
    }

    /**
     * @return the controller
     */
    public String getController() {
        return name;
    }

    /**
     * @param controller the controller to set
     */
    public void setController(final String controller) {
        this.name = controller;
    }

    /**
     * Copies the data from the Model to the jsp page scope
     * 
     * @param a the Model to copy from.
     */
    protected void copyModel(final Model model) {
        if (model == null) {
            return;
        }
        for (final Entry<String, ?> e : model) {
            // don't overwrite or worse, delete in case value is null.
            PageContext pc = getPageContext();
            if (pc.getAttribute(e.getKey(), PageContext.PAGE_SCOPE) == null) {
                pc.setAttribute(e.getKey(), e.getValue(), PageContext.PAGE_SCOPE);
            }
        }
    }

    protected final ICS getICS() {
        final Object o = getPageContext().getAttribute(ICS_VARIABLE_NAME, PageContext.PAGE_SCOPE);
        if (o instanceof ICS) {
            return (ICS) o;
        }
        throw new RuntimeException("Can't find ICS object on the page context.");
    }

    protected final PageContext getPageContext() {
        return (PageContext) getJspContext();
    }

}
