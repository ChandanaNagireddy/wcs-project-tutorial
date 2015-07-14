
package uk.co.manifesto.wcs.mvc.factory;

import javax.servlet.http.HttpSession;

import COM.FutureTense.Interfaces.ICS;
import uk.co.manifesto.wcs.mvc.controller.DefaultModel;
import uk.co.manifesto.wcs.mvc.controller.Model;
import uk.co.manifesto.wcs.mvc.injection.Producer;

public class BaseFactory {

    @Producer(cache = false)
    public static Model makeModel() {
        return new DefaultModel();
    }

    @SuppressWarnings("deprecation")
    @Producer
    public static HttpSession makeSession(ICS ics) {
        return ics.getIServlet().getServletRequest().getSession();
    }

}
