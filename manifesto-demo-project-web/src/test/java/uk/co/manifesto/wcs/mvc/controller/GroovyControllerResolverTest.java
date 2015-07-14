package uk.co.manifesto.wcs.mvc.controller;

import javax.servlet.ServletContext;

import org.junit.Before;
import org.junit.Test;

import com.fatwire.cs.core.db.PreparedStmt;
import com.fatwire.cs.core.db.StatementParam;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

import COM.FutureTense.Interfaces.ICS;
import COM.FutureTense.Interfaces.IList;
import uk.co.manifesto.wcs.mvc.groovy.GroovyControllerResolver;

public class GroovyControllerResolverTest {

	private static final String GROOVY_CLASS_LOCATION = "controller/MyValidCSElementName.groovy";
	private static final String TEST_RESOURCES = "src/test/resources";
	private static final String ELEMENT_CATALOG_DIR = "CS.CatalogDir.ElementCatalog";
	private static final String VALID_CSELEMENT_NAME = "controller/MyValidCSElementName";
	
	private ServletContext mockServletContext = mock(ServletContext.class);
	private ICS mockICS = mock(ICS.class);
	private IList mockIList = mock(IList.class);
	
	@Before
	public void setup() throws Exception {
		when(mockICS.ResolveVariables(eq(ELEMENT_CATALOG_DIR))).thenReturn(TEST_RESOURCES);
		when(mockICS.IsElement(VALID_CSELEMENT_NAME)).thenReturn(true);
		when(mockICS.SQL(any(PreparedStmt.class), any(StatementParam.class), anyBoolean())).thenReturn(mockIList);
		when(mockIList.getValue("url")).thenReturn(GROOVY_CLASS_LOCATION);
	}
	
	@Test
	public void resolverReturnsControllerForValidCSElement() throws Exception {
		ControllerResolver resolver = new GroovyControllerResolver(mockServletContext);
		assertTrue(resolver.getController(mockICS, VALID_CSELEMENT_NAME) instanceof Controller);
	}
	
	@Test 
	public void theCorrectModelIsReturnedForAValidCSElement() {
		ControllerResolver resolver = new GroovyControllerResolver(mockServletContext);
		Controller resolvedController = resolver.getController(mockICS, VALID_CSELEMENT_NAME);
		Model returnedModel = resolvedController.handleRequest(mockICS);
		
		assertTrue(returnedModel.containsKey("somethingFromTheModel"));
		assertEquals(returnedModel.size(), 1);
		assertFalse(returnedModel.isEmpty());
		assertEquals(returnedModel.get("somethingFromTheModel"), "Magnificent work");
	}
}
