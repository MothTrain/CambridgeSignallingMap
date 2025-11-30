package aradnezami.cambridgesignallingmap;

import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

public class SignallingMapValidatorTest {
    private final int ADDRESS = 0;
    private final int BIT= 1;
    private final int TYPE = 2;
    private final int ID = 3;
    private final int BACK_TYPE = 4;
    private final int BACK_ADDRESS = 5;
    private final int BACK_BIT = 6;
    
    @Test
    void validateSignallingMap() {
        String[] mappings = loadMappings();
        
        for (String mappingUnparsed : mappings) {
            String[] mapping = mappingUnparsed.split(",");
            
            ensureValidLength(mapping);
            ensureAddressAndBitValid(mapping);
            ensureCorrectTypesUsed(mapping);
        }
        ensureBackreferencesValid(mappings);
    }
    
    
    
    private  void ensureValidLength(String[] mapping) {
        final int[] validLengths = {2,3,4,5,7};
        int length = mapping.length;
        
        if (!contains(validLengths, length)) {
            fail(lineErrorMessage("Line was invalid length", mapping));
        }
        
        if (length == 3) {
            assertEquals("PLACEHOLD", mapping[TYPE], lineErrorMessage("mapping of length 3 must be a placehold.",mapping));
        }
        if (length == 5) {
            assertEquals("UNMAPPED", mapping[BACK_TYPE], lineErrorMessage("mapping of length 5 must be an unmapped point.", mapping));
        }
    }
    
    private  void ensureAddressAndBitValid(String[] mapping) {
        assertDoesNotThrow(() -> {
            Integer.parseInt(mapping[ADDRESS]);
            Integer.parseInt(mapping[BIT]);
        }, "Address and Bit must be an integer");
        
        int address = Integer.parseInt(mapping[ADDRESS]);
        int bitIndex = Integer.parseInt(mapping[BIT]);
        
        assertTrue(address >= 0,   lineErrorMessage("Address must not be negative.", mapping)) ;
        assertTrue(address <= 200, lineErrorMessage("Address must be less than 200.", mapping));
        
        assertTrue(bitIndex >= 0, lineErrorMessage("BitIndex must not be negative: ", mapping));
        assertTrue(bitIndex <= 7, lineErrorMessage("BitIndex must be 7 or less", mapping));
        
        
        if (mapping.length != 7) {return;}
        
        assertDoesNotThrow(() -> {
            Integer.parseInt(mapping[BACK_ADDRESS]);
            Integer.parseInt(mapping[BACK_BIT]);
        }, "Backreference Address and Bit must be an integer");
        
        address = Integer.parseInt(mapping[BACK_ADDRESS]);
        bitIndex = Integer.parseInt(mapping[BACK_BIT]);
        
        assertTrue(address >= 0,   lineErrorMessage("Backreference address must not be negative.", mapping)) ;
        assertTrue(address <= 200, lineErrorMessage("Backreference address must be less than 200.", mapping));
        
        assertTrue(bitIndex >= 0, lineErrorMessage("Backreference bitIndex must not be negative: ", mapping));
        assertTrue(bitIndex <= 7, lineErrorMessage("Backreference bitIndex must be 7 or less", mapping));
    }
    
    private  void ensureCorrectTypesUsed(String[] mapping) {
        String[] validTypes = {"DGK", "RGK", "OFFK", "SOFFK", "NK", "RK", "T", "B", "RM", "RS", "RC", "PLACEHOLD"};
        String[] backreferencableTypes = {"NK", "RK", "DGK", "SOFFK", ""};
        
        if (mapping.length < 4) {return;}
        assertTrue(contains(validTypes, mapping[TYPE]), lineErrorMessage("Invalid equipment type used.", mapping));
        
        if (mapping.length != 7) {return;}
        
        boolean isValidFlag = contains(validTypes, mapping[BACK_TYPE]);
        if (mapping[BACK_TYPE].isEmpty() && (contains(backreferencableTypes, mapping[BACK_TYPE])) ) {
            isValidFlag = true;
        }
        assertTrue(isValidFlag, lineErrorMessage("Invalid equipment type for backreference.", mapping));
    }
    
    private  void ensureBackreferencesValid(String[] mappings) {
        final String[] backreferenceableTypes = {"NK", "RK", "DGK", "SOFFK"};
        
        HashMap<String, String[]> map = new HashMap<>() {{
            
            for (String mapping : mappings) {
                String[] split = mapping.split(",");
                put(split[ADDRESS]+","+split[BIT], split);
            }
        }};
        
        for (String unparsedMapping : mappings) {
            String[] mapping = unparsedMapping.split(",");
            
            if (mapping.length != 7) {continue;}
            
            String[] backreference = map.get(mapping[BACK_ADDRESS]+","+mapping[BACK_BIT]);
            
            assertNotNull(backreference, lineErrorMessage("Backreferenced to nonexistant mapping", mapping));
            assertEquals(mapping[ID], backreference[ID], lineErrorMessage("Backreferenced map must have same IDs", mapping));
            assertEquals(mapping[BACK_TYPE], backreference[TYPE], lineErrorMessage("Backreferenced map does not have the specified equipment type", mapping));
            assertTrue(contains(backreferenceableTypes, mapping[BACK_TYPE]), lineErrorMessage("Cannot backreference this type.", mapping));
            assertNotEquals(mapping[BACK_TYPE], mapping[TYPE], lineErrorMessage("Backreference must not be of the same type as the mapping", mapping));
        }
    }
    
    
    private String[] loadMappings() {
        try (Scanner scanner = new Scanner(getMapStream())) {
            return readMappings(scanner);
        
        } catch (NoSuchElementException | NullPointerException e) {
            fail("Failed to load file", e);
            throw new RuntimeException("This error should never throw. It should be escaped by fail()");
        }
    }
    
    private InputStream getMapStream() {
        ClassLoader classLoader = getClass().getClassLoader();
        
        InputStream mapStream = classLoader.getResourceAsStream("SignallingEquipmentMap.csv");
        assertNotNull(mapStream, "Could not load the Signalling Equipment Map");
        return mapStream;
    }
    
    private String[] readMappings(Scanner scanner) {
        ArrayList<String> lines = new ArrayList<>();
        
        scanner.nextLine(); // First line is ignored for headers
        while (scanner.hasNextLine()) {
            lines.add(scanner.nextLine());
        }
        
        String[] returnVal = new String[lines.size()];
        return lines.toArray(returnVal);
    }
    
    
    
    private  boolean contains(String[] array, String value) {
        for (String i : array) {
            if (i.equals(value)) {return true;}
        }
        
        return false;
    }
    private  boolean contains(int[] array, int value) {
        for (int i : array) {
            if (i == value) {return true;}
        }
        
        return false;
    }
    
    
    private  String lineErrorMessage(String msg, String[] line) {
        StringBuilder sb = new StringBuilder();
        for (String token : line) {
            sb.append(token).append(",");
        }
        sb.delete(sb.length()-1, sb.length());
        
        return msg + " At line: " + sb;
    }
}
