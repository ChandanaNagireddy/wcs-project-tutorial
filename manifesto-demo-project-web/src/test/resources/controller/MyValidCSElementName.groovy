package controller

import uk.co.manifesto.wcs.mvc.controller.Controller
import uk.co.manifesto.wcs.mvc.controller.Model
import uk.co.manifesto.wcs.mvc.injection.Inject
import COM.FutureTense.Interfaces.ICS


class MyValidCSElementName implements Controller {
 
    @Inject Model model
 
    @Override
    public Model handleRequest(ICS ics) {
        model.put("somethingFromTheModel", "Magnificent work");
        return model;
    }
	
}