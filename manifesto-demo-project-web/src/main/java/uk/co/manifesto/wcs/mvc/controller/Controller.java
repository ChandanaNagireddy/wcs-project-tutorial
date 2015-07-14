package uk.co.manifesto.wcs.mvc.controller;

import COM.FutureTense.Interfaces.ICS;

public interface Controller {
    
    Model handleRequest(ICS ics);

}
