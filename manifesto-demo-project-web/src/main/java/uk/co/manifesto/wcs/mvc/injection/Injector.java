
package uk.co.manifesto.wcs.mvc.injection;

import COM.FutureTense.Interfaces.ICS;

public interface Injector {

    void inject(ICS ics, Object object) throws Exception;

}
