
package uk.co.manifesto.wcs.mvc.controller;

import java.util.Map;
import java.util.Set;

public interface Model extends Iterable<Map.Entry<String, Object>> {
    /**
     * 
     * @see java.util.Map#size()
     */
    int size();

    /**
     * 
     * @see java.util.Map#isEmpty()
     */
    boolean isEmpty();

    /**
     * @see java.util.Map#containsKey(java.lang.Object)
     */
    boolean containsKey(Object key);

    /**
     * @see java.util.Map#get(java.lang.Object)
     */
    Object get(Object key);

    /**
     * @see java.util.Map#put(java.lang.Object, java.lang.Object)
     */
    Object put(String key, Object value);

    /**
     * @see java.util.Map#remove(java.lang.Object)
     */
    Object remove(Object key);

    /**
     * @see java.util.Map#putAll(java.util.Map)
     */
    void putAll(Map<? extends String, ? extends Object> m);

    /**
     * 
     * @see java.util.Map#clear()
     */
    void clear();

    /**
     * @see java.util.Map#keySet()
     */
    Set<String> keySet();

}
