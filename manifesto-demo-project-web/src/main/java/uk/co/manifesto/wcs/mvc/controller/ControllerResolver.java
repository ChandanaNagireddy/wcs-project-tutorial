
package uk.co.manifesto.wcs.mvc.controller;

import COM.FutureTense.Interfaces.ICS;

public interface ControllerResolver {

    Controller getController(ICS ics, String name);

}
