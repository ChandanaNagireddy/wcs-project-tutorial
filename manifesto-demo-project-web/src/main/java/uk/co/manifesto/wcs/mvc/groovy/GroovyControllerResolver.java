
package uk.co.manifesto.wcs.mvc.groovy;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fatwire.cs.core.db.PreparedStmt;
import com.fatwire.cs.core.db.StatementParam;
import com.fatwire.gst.foundation.facade.runtag.render.LogDep;

import COM.FutureTense.Interfaces.ICS;
import COM.FutureTense.Interfaces.IList;
import COM.FutureTense.Interfaces.Utilities;
import groovy.util.GroovyScriptEngine;
import groovy.util.ResourceException;
import groovy.util.ScriptException;
import uk.co.manifesto.wcs.mvc.controller.Controller;
import uk.co.manifesto.wcs.mvc.controller.ControllerResolver;
import uk.co.manifesto.wcs.mvc.injection.Injector;

public class GroovyControllerResolver implements ControllerResolver {

    private Log logger = LogFactory.getLog(getClass());
    private GroovyScriptEngine groovyScriptEngine;
    private int minimumRecompilationInterval = 0;
    private boolean isLoaded = false;
    private PreparedStmt stmt;
    private Injector injector;

    public GroovyControllerResolver(ServletContext context) {
		stmt = new PreparedStmt("SELECT * FROM ElementCatalog WHERE elementname=?", Collections.singletonList("CSElement"));
		stmt.setElement(0, java.sql.Types.VARCHAR);
    }

    @Override
    public Controller getController(ICS ics, String resourceName) {
    	if (!isLoaded) {
			initialiseGroovyScriptEngine(ics);
			isLoaded = true;
		}
    	
    	if (logger.isDebugEnabled())
			logger.debug("Loading groovy script " + resourceName);
		if (ics.IsElement(resourceName)) {

			String resourcePath = getCSElementLocation(ics, resourceName);
			
			// prevent case where resourcename is same as a jsp element.
			if (resourcePath.endsWith(".groovy")) {
				if (logger.isDebugEnabled()) {
					logger.debug("Found element for " + resourceName + " => " + resourcePath);
				}
				try {
					
			        return getControllerFromGroovyScript(ics, resourceName, groovyScriptEngine.loadScriptByName(resourcePath));
					
		        } catch (ResourceException e) {
		            throw new RuntimeException("1 Unable to find a groovy resource with name '" + resourceName + "'.", e);
		        } catch (ScriptException e) {
		            throw new RuntimeException("ScriptException during instantiation of Controller with name '" + resourceName + "'.", e);
		        }
			} else {
	            throw new RuntimeException("2 Unable to find a groovy resource with name '" + resourceName + "'.");			
			}
		} else {
            throw new RuntimeException("Unable to find and element with name '" + resourceName + "'.");
		}
    	
    }

	private Controller getControllerFromGroovyScript(ICS ics, String resourceName, Class<?> c) {
		Object x;
		try {
		    x = c.newInstance();
		    if (x instanceof Controller) {
		        Controller controller = (Controller) x;
		        inject(ics, controller);
		        return controller;
		    } else {
		        throw new RuntimeException("Groovy class with name '" + x.getClass().getName()
		                + "' does not implement Controller.");
		    }

		} catch (InstantiationException e) {
		    throw new RuntimeException("InstantiationException during instantiation of Controller with name '" + resourceName
		            + "'.", e);
		} catch (IllegalAccessException e) {
		    throw new RuntimeException("IllegalAccessException during instantiation of Controller with name '" + resourceName
		            + "'.", e);
		} catch (Exception e) {
		    throw new RuntimeException("Exception during instantiation of Controller with name '" + resourceName + "'.", e);

		}
	}

	private String getCSElementLocation(ICS ics, String resourceName) {
		Map<String, String> rowMap = getCSElementAttributes(ics, resourceName);
		String url = rowMap.get("url");
		String res1 = rowMap.get("resdetails1");
		String res2 = rowMap.get("resdetails2");
		
		Map<String, String> m = new HashMap<String, String>();
		Utilities.getParams(res1, m, false);
		Utilities.getParams(res2, m, false);
		String tid = m.get("tid");
		String eid = m.get("eid");
		if (StringUtils.isNotBlank(tid)) {
			LogDep.logDep(ics, "Template", tid);
		}
		if (StringUtils.isNotBlank(eid)) {
			LogDep.logDep(ics, "CSElement", eid);
		}
		return url;
	}

	private Map<String, String> getCSElementAttributes(ICS ics, String resourceName) {
		final StatementParam param = stmt.newParam();
		param.setString(0, resourceName);

		Map<String,String> rowMap = new HashMap<String,String>();
	    IList i = ics.SQL(stmt, param, true);
	    if (ics.GetErrno() != -101) {
	    	i.moveTo(0);
	    	try {
				rowMap.put("url", i.getValue("url"));
				rowMap.put("resdetails1", i.getValue("resdetails1"));
				rowMap.put("resdetails2", i.getValue("resdetails2"));
			} catch (NoSuchFieldException e) {
				throw new RuntimeException("NoSuchFieldException returned " + ics.GetErrno() + " and errstr: " + " for " + stmt.toString());
			}
	        ics.ClearErrno();
	    } else if (ics.GetErrno() != 0) {
	        throw new RuntimeException("ics.SQL returned " + ics.GetErrno() + " and errstr: " + " for " + stmt.toString());
	    }
		return rowMap;
	}
    
    private void initialiseGroovyScriptEngine(ICS ics) {

		GroovyScriptEngine gse;
		try {
			gse = new GroovyScriptEngine(getScriptRootLocations(ics), Thread.currentThread().getContextClassLoader());
			gse.getConfig().setRecompileGroovySource(true);
			gse.getConfig().setMinimumRecompilationInterval(this.minimumRecompilationInterval);
			groovyScriptEngine = gse;

		} catch (IOException e) {
			throw new RuntimeException(e);
		}
        injector = new GroovyInjector(groovyScriptEngine.getGroovyClassLoader());   	
    }

	private String[] getScriptRootLocations(ICS ics) {
		String[] root = {ics.ResolveVariables("CS.CatalogDir.ElementCatalog")};
		return root;
	}

    private void inject(ICS ics, Controller controller) throws Exception {
        Injector injector = getInjector();
        injector.inject(ics, controller);
    }

    public Injector getInjector() {
        return injector;
    }

}
