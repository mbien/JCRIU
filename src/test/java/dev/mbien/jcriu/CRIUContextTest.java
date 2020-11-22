package dev.mbien.jcriu;

import java.lang.reflect.Field;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author mbien
 */
public class CRIUContextTest {
    

    @Test
    public void basicContextTest() {
        
        try (CRIUContext criu = CRIUContext.create()) {
            
            assertNotNull(criu);
            
            String version = criu.getVersion();
            assertNotNull(version);
            assertFalse(version.isBlank());
            
            System.out.println("CRIU version: "+version);
            
        }
        
        try {
            Field field = CRIUContextImpl.class.getDeclaredField("initialized");
            field.setAccessible(true);
            boolean initialized = (boolean) field.get(null);
            
            assertFalse(initialized);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
            fail("can't check field, did the impl change?", ex);
        }
        
    }
    
    
    // TODO have to wait for rootless criu before adding more tests
    // running maven as root is no fun
    
}
