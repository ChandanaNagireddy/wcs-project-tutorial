
package uk.co.manifesto.wcs.mvc.factory;

public interface Factory {

    public <T> T getObject(String name, Class<T> fieldType);

}
