package aradnezami.cambridgesignallingmap.NRFeed;

import aradnezami.cambridgesignallingmap.DiagramElements.*;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

/**
 * The SClassDecoder is used to convert S-Class messages from the NR feed (encoded in the byte and
 * address) into {@link Event}s, which describe the message's information on the real signalling
 * functions (signals, points, ect). The messages are mapped to their Event through a map file.
 * The map format is specified in Mapping_Syntax.md in the Resources directory. This class complies
 * with Mapping_Syntax.md
 */
public class SClassDecoder {
    private final HashMap<MappingReference, String[]> equipmentMap;

    private final int[] equipmentBytes;
    private final boolean[] isByteUpdated;
    
    private final int ADDRESS = 0;
    private final int BIT= 1;
    private final int TYPE = 2;
    @SuppressWarnings("FieldCanBeLocal")
    private final int ID = 3;
    private final int BACK_TYPE = 4;
    private final int BACK_ADDRESS = 5;
    private final int BACK_BIT = 6;



    /**
     * Creates an instance of SClass handler from the provided file path
     * @param path Map file path
     * @throws FileNotFoundException If the file cannot be accessed
     * @see ClassLoader#getResourceAsStream(String path) 
     */
    public SClassDecoder(String path) throws FileNotFoundException {
        equipmentMap = loadEquipmentMap(path);
        
        equipmentBytes = new int[256];
        isByteUpdated = new boolean[256];
    }


    /**
     * Resets all SClass state in the instance. The instance will behave as though
     * it has just been constructed with no knowledge of any state
     */
    public void reset() {
        for (int i = 0; i < equipmentBytes.length; i++) {
            equipmentBytes[i] = 0;
            isByteUpdated[i] = false;
        }
    }

    /**
     * Applies the S-Class message to the instance, updating its state and returning an
     * array of any {@link Event}s created by the change. If no changes occur, an empty
     * array is returned
     *
     * @param timestamp The timestamp of the message provided by the feed or -1L if none
     *                 is to be given
     * @param address The address of the byte being updated. Range 0-255
     * @param newByte The value of the updated byte. Range 0-255
     * @return An array of any events caused by the change
     */
    public Event[] SClassChange(long timestamp, int address, int newByte) {
        int originalByte = equipmentBytes[address];
        equipmentBytes[address] = newByte;
        
        int[] changes = getBitChanges(originalByte, newByte);
        if (!isByteUpdated[address]) {
            changes = new int[] {0,1,2,3,4,5,6,7};
            isByteUpdated[address] = true;
        }
        
        ArrayList<Event> events = new ArrayList<>();
        for (int changedBit : changes) {
            MappingReference key = new MappingReference(address, changedBit);
            String[] mapping = equipmentMap.get(key);
            if (mapping == null) { continue; }
            
            boolean bitState = getBitFromByte(newByte, changedBit);
            
            if (mapping.length <= 2) { continue; } // unmapped

            try {
                Event event = decodeChange(mapping, bitState);
                //noinspection DataFlowIssue
                events.add(new Event(timestamp, event.S_Type, event.S_State, event.S_Id));
            } catch (IllegalArgumentException ignored) {} // A backreference hasn't been updated yet
        }

        return events.toArray(new Event[]{});
    }


    /**
     * The events represented by applying an {@link #SClassChange(long, int, int)} sequentially
     * on each byte stored by this instance. Essentially flushing out the state stored by this
     * instance. Note that the state created by applying the returned events is only valid if
     * <b>all</b> the events are applied in the <b>given order</b>.
     * @return A list of all events stored
     */
    public Event[] allEvents() {
        ArrayList<Event> events = new ArrayList<>();
        for (int address=0; address<isByteUpdated.length; address++) {
            for (int bit=0; bit<=7; bit++) {
                MappingReference key = new MappingReference(address, bit);
                String[] mapping = equipmentMap.get(key);
                if (mapping == null) { continue; }

                boolean bitState = getBitFromByte(equipmentBytes[address], bit);
                try { events.add(decodeChange(mapping, bitState));}
                catch (IllegalArgumentException ignored) {} // A backreference hasn't been updated yet
            }
        }

        return events.toArray(new Event[]{});
    }

    
    private Event decodeChange(String[] mapping, boolean bitState) {
        int equipmentType;
        int state;
        
        switch (mapping[TYPE]) {
            case "NK", "RK": {
                state = pointChange(mapping, bitState);
                equipmentType = Point.TYPE;
                break;
            }
            case "DGK", "RGK", "OFFK", "SOFFK": {
                state = signalChange(mapping, bitState);
                equipmentType = Signal.ASPECT_TYPE;
                break;
            }
            case "T": {
                state = trackCircuitChange(bitState);
                equipmentType = TrackCircuit.TYPE;
                break;
            }
            case "B": {
                state = routeIndicatorChange(bitState);
                equipmentType = Signal.ROUTED_TYPE;
                break;
            }
            case "RM": {
                state = routeChange(bitState);
                equipmentType = Route.MAIN_TYPE;
                break;
            }
            case "RS": {
                state = routeChange(bitState);
                equipmentType = Route.SHUNT_TYPE;
                break;
            }
            case "RC": {
                state = routeChange(bitState);
                equipmentType = Route.CALL_ON_TYPE;
                break;
            }
            default: throw new IllegalMapFormatException(mapping[TYPE] + " is not a valid type");
        }
        
        return new Event(-1L, equipmentType, state, mapping[ID]);
    }
    
    
    
    
      
    private int pointChange(String[] mapping, boolean bitState) {
        final int IMPLICITLY_BACKREFERENCED_LEN = 4;
        final int UNMAPPED_LEN = 5;
        final int EXPLICITLY_BACKREFERENCED_LEN = 7;

        // Implicitly back-referenced
        if (mapping.length == IMPLICITLY_BACKREFERENCED_LEN && mapping[TYPE].equals("NK")) {
            int backAddress = Integer.parseInt(mapping[ADDRESS]);
            int backBit = Integer.parseInt(mapping[BIT]) + 1; // We imply that the RK is in the next bit (+1)
            
            boolean backReference = backreference(backAddress, backBit);
            
            return decodePoint(bitState, backReference);
            
        } else if (mapping.length == IMPLICITLY_BACKREFERENCED_LEN && mapping[TYPE].equals("RK")) {
            int backAddress = Integer.parseInt(mapping[ADDRESS]);
            int backBit = Integer.parseInt(mapping[BIT]) - 1; // We imply that the RK is in the next bit (-1)
            
            boolean backReference = backreference(backAddress, backBit);
            
            return decodePoint(backReference, bitState);
            
            
        } else if (mapping.length == UNMAPPED_LEN && mapping[TYPE].equals("NK") && mapping[BACK_TYPE].equals("UNMAPPED")) { // Unmapped
            return decodePoint(bitState, null);
            
        } else if (mapping.length == UNMAPPED_LEN && mapping[TYPE].equals("RK") && mapping[BACK_TYPE].equals("UNMAPPED")) {
            return decodePoint(null, bitState);
            
            
            // Explicitly Back-referenced
        } else if (mapping.length == EXPLICITLY_BACKREFERENCED_LEN && mapping[TYPE].equals("NK")) {
            int backAddress = Integer.parseInt(mapping[BACK_ADDRESS]);
            int backBit = Integer.parseInt(mapping[BACK_BIT]);
            
            boolean backReference = backreference(backAddress, backBit);
            
            return decodePoint(bitState, backReference);
            
        } else if (mapping.length == EXPLICITLY_BACKREFERENCED_LEN && mapping[TYPE].equals("RK")) {
            int backAddress = Integer.parseInt(mapping[BACK_ADDRESS]);
            int backBit = Integer.parseInt(mapping[BACK_BIT]);
            
            boolean backReference = backreference(backAddress, backBit);
            
            return decodePoint(backReference, bitState);
            
        } else {
            throw new IllegalMapFormatException("Not a valid map: " + Arrays.toString(mapping));
        }
        
    }
    
    private int decodePoint(Boolean NK, Boolean RK) {
        if (RK == null) {
            return (NK) ? Point.NORMAL : Point.REVERSE;
        } else if (NK == null) {
            return (RK) ? Point.REVERSE : Point.NORMAL;
        } else {
            return (NK) ?
                    ((RK) ? Point.BOTH : Point.NORMAL):
                    ((RK) ? Point.REVERSE : Point.NEITHER);
        }
    }


    
    private int signalChange(String[] mapping, boolean bitState) {
        final int BACKREFERENCED_LEN = 7;

        if (mapping.length==BACKREFERENCED_LEN
                && mapping[TYPE].equals("DGK")) { // Compound signal backreferencing soffk

            int backAddress = Integer.parseInt(mapping[BACK_ADDRESS]);
            int backBit = Integer.parseInt(mapping[BACK_BIT]);
            boolean soffk = backreference(backAddress, backBit);
            return decodeCompoundSignal(bitState, soffk);


        } else if (mapping.length==BACKREFERENCED_LEN
                && mapping[TYPE].equals("SOFFK")) { // Compound signal backreferencing dgk

            int backAddress = Integer.parseInt(mapping[BACK_ADDRESS]);
            int backBit = Integer.parseInt(mapping[BACK_BIT]);
            boolean dgk = backreference(backAddress, backBit);
            return decodeCompoundSignal(dgk, bitState);


        } else if (mapping.length==BACKREFERENCED_LEN
                && mapping[TYPE].equals("OFFK")
                && mapping[BACK_TYPE].equals("RM")) { // Compound signal backreferencing main route

            int backAddress = Integer.parseInt(mapping[BACK_ADDRESS]);
            int backBit = Integer.parseInt(mapping[BACK_BIT]);
            boolean route = backreference(backAddress, backBit);
            return decodeOffkCompoundSignal(bitState, route);

        } else if (mapping[TYPE].equals("OFFK")) {
            return (bitState) ? Signal.MAIN_OFF : Signal.ON;

        } else if (mapping[TYPE].equals("SOFFK")) {
            return (bitState) ? Signal.SHUNT_OFF : Signal.ON;

        } else if (mapping[TYPE].equals("RGK")) {
            return (bitState) ? Signal.ON : Signal.MAIN_OFF;

        } else if (mapping[TYPE].equals("DGK")) {
            return (bitState) ? Signal.MAIN_OFF : Signal.ON;

        } else {
            throw new IllegalMapFormatException("Invalid map: " + Arrays.toString(mapping));
        }
    }

    private int decodeCompoundSignal(boolean dgk, boolean sOffk) {
        return (dgk) ?
                ((sOffk) ? Signal.BOTH_OFF : Signal.MAIN_OFF):
                ((sOffk) ? Signal.SHUNT_OFF : Signal.ON);
    }
    private int decodeOffkCompoundSignal(boolean offk, boolean mainRoute) {
        return (offk) ?
                ((mainRoute) ? Signal.MAIN_OFF : Signal.SHUNT_OFF):
                Signal.ON;
    }
    
    
    private int trackCircuitChange(boolean bitState) {
        return (bitState) ? TrackCircuit.OCCUPIED : TrackCircuit.UNOCCUPIED;
    }
    
    private int routeIndicatorChange(boolean bitState) {
        return (bitState) ? Signal.ROUTE_SET : Signal.ROUTE_NOT_SET;
    }
    
    private int routeChange(boolean bitState) {
        return (bitState) ? Route.SET : Route.NOTSET;
    }


    
    
    /**
     * Gets the bit value as a boolean, from the bit position of the specified
     * byte address in {@link #equipmentBytes}. 0 = LSB, 7 = MSB
     * @param address The byte address
     * @param bit Bit index
     * @return The state of the specified biy
     * @throws IllegalArgumentException If the address has not been updated
     */
    private boolean backreference(int address, int bit) {
        if (!isByteUpdated[address]) {throw new IllegalArgumentException("Not updated byte");}
        int Byte = equipmentBytes[address];
        
        return getBitFromByte(Byte, bit);
    }


    /**
     * Gets the bit from a byte at a specified index. 0 = LSB, 7 = MSB
     * @param Byte A value between 0-255 inclusive
     * @param bitIndex The index of the bit to retrieve
     * @return False if bit is 0, True otherwise
     */
    private boolean getBitFromByte(int Byte, int bitIndex) {
        return ((Byte >> (bitIndex)) & 1) == 1;
    }


    /**
     * Returns the index of bits that differ between the 2 provided bytes
     * @param original A value between 0-255 inclusive
     * @param updated A value between 0-255 inclusive
     * @return The index of bits that are different. 0 = LSB, 7 = MSB
     */
    private static int[] getBitChanges(int original, int updated) {
        int change = (original ^ updated); // XOR, changed bits will be 1

        ArrayList<Integer> changedBitsList = new ArrayList<>();
        for (int i = 0; i <= 7 ; i++) { // Get indexes of changed bits
            if (((change >> (i)) & 1) == 1) {changedBitsList.add(i);}
        }
        
        int[] changedBits = new int[changedBitsList.size()]; // To array
        for (int i = 0; i < changedBitsList.size(); i++) {
            changedBits[i] = changedBitsList.get(i);
        }
        
        return changedBits;
    }


    /**
     * Loads the map from the given file
     * @return A HashMap where the value is a string array, each array element representing one
     * column in the mapping
     * @throws FileNotFoundException If the given file is not found
     */
    private HashMap<MappingReference, String[]> loadEquipmentMap(String path) throws FileNotFoundException {
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream mapStream = classLoader.getResourceAsStream(path);
        if (mapStream == null) {
            throw new FileNotFoundException("Could not find signalling equipment map. Path: " + path);
        }
        Scanner scanner = new Scanner(mapStream);
        scanner.nextLine(); // Skip headers


        HashMap<MappingReference, String[]> equipmentMap = new HashMap<>();
        while (scanner.hasNextLine()) {
            String[] line = scanner.nextLine().split(",");

            int address, bit;
            try {
                address = Integer.parseInt(line[ADDRESS]);
                bit = Integer.parseInt(line[BIT]);
            } catch (NumberFormatException e) {
                scanner.close();
                throw new IllegalMapFormatException("A map address or bit is not a parsable number");
            }

            equipmentMap.put(new MappingReference(address, bit), line);
        }

        scanner.close();
        return equipmentMap;
    }


    /**
     * Used to represent the position of a mapping in one variable, to use as a key in hashmaps
     */
    private record MappingReference(int address, int bit) {}
}
