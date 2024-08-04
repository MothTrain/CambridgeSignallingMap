package aradnezami.cambridgesignallingmap;

import aradnezami.cambridgesignallingmap.Diagram.*;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

public class SClassHandler {
    private final HashMap<Integer, String[]> equipmentMap;
    private final OutputHandler out;
    
    private final short[] equipmentBytes;
    private final boolean[] isByteUpdated;
    
    private final int ADDRESS = 0;
    private final int BIT= 1;
    private final int TYPE = 2;
    private final int ID = 3;
    private final int BACK_TYPE = 4;
    private final int BACK_ADDRESS = 5;
    private final int BACK_BIT = 6;
    
    /**
     * Creates an instance of SClass handler, with the specified output handler to forward
     * events to. A map is created from the default map
     * @param out The output handler
     * @throws FileNotFoundException If {@code SignallingEquipmentMap.csv} cannot be accessed
     */
    SClassHandler(OutputHandler out) throws FileNotFoundException {
        equipmentMap = loadEquipmentMap();
        this.out = out;
        
        equipmentBytes = new short[201];
        isByteUpdated = new boolean[201];
    }
    
    
    /**
     * Creates an instance of SClass handler, with the specified output handler to forward
     * events to. A map is created from the default map and the initial equipment state bytes
     * and whether those bytes have been updated are inputted by {@code initState} and {@code initUpdated}
     * respectively
     * @param out The output handler
     * @param initState The state of signalling equipment bytes to initialise with
     * @param initUpdated Whether the respective bytes have been updated
     * @throws FileNotFoundException If {@code SignallingEquipmentMap.csv} cannot be accessed
     */
    SClassHandler(OutputHandler out, short[] initState, boolean[] initUpdated) throws FileNotFoundException {
        equipmentMap = loadEquipmentMap();
        this.out = out;
        
        equipmentBytes = initState;
        isByteUpdated = initUpdated;
    }
    
    public void SClassChange(short address, short newByte) {
        short originalByte = equipmentBytes[address];
        equipmentBytes[address] = newByte;
        
        int[] changes = getBitChanges(originalByte, newByte);
        if (!isByteUpdated[address]) {
            changes = new int[] {0,1,2,3,4,5,6,7};
            isByteUpdated[address] = true;
        }
        
        
        for (int changedBit : changes) {
            
            Integer key = Arrays.hashCode(new short[]{address, (short) changedBit});
            String[] mapping = equipmentMap.get(key);
            if (mapping == null) {return;}
            
            boolean bitState = getBitFromByte(newByte, changedBit);
            
            if (mapping.length <= 2) {
                continue;
            } // unmapped
            else if (mapping.length == 3 && mapping[TYPE].equals("PLACEHOLD") && bitState) {
                out.PlaceholderBitSet(address, changedBit);
            } else if (mapping.length == 3 && mapping[TYPE].equals("PLACEHOLD")) {
                continue;
            } else {
                try {
                    delegateChange(mapping, bitState);
                } catch (IllegalArgumentException e) {
                    continue;
                }
            }
        }
        
    }

    
    private void delegateChange(String[] mapping, boolean bitState) {
        int equipmentType;
        int state;
        
        switch (mapping[TYPE]) {
            case "NK", "RK": {
                state = pointChange(mapping, bitState);
                equipmentType = Point.TYPE;
                break;
            }
            case "DGK", "RGK", "OFFK": {
                state = signalChange(mapping, bitState);
                equipmentType = Signal.TYPE;
                break;
            }
            case "T": {
                state = trackCircuitChange(bitState);
                equipmentType = TrackCircuit.TYPE;
                break;
            }
            case "B": {
                state = routeIndicatorChange(bitState);
                equipmentType = RouteIndicator.TYPE;
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
            default: throw new IllegalMapFormatException("A Valid type was not used");
        }
        
        out.SClassChange(equipmentType, state, mapping[ID]);
    }
    
    
    
    
      
    private int pointChange(String[] mapping, boolean bitState) {
        
        // Implicitly back-referenced
        if (mapping.length == 4 && mapping[TYPE].equals("NK")) {
            int backAddress = Integer.parseInt(mapping[ADDRESS]);
            int backBit = Integer.parseInt(mapping[BIT]) + 1; // We imply that the RK is in the next bit (+1)
            
            boolean backReference = backreference(backAddress, backBit);
            
            return decodePoint(bitState, backReference);
            
        } else if (mapping.length == 4 && mapping[TYPE].equals("RK")) {
            int backAddress = Integer.parseInt(mapping[ADDRESS]);
            int backBit = Integer.parseInt(mapping[BIT]) - 1; // We imply that the RK is in the next bit (-1)
            
            boolean backReference = backreference(backAddress, backBit);
            
            return decodePoint(backReference, bitState);
            
            
        } else if (mapping.length == 5 && mapping[TYPE].equals("NK") && mapping[BACK_TYPE].equals("UNMAPPED")) { // Unmapped
            return decodePoint(bitState, null);
            
        } else if (mapping.length == 5 && mapping[TYPE].equals("RK") && mapping[BACK_TYPE].equals("UNMAPPED")) {
            return decodePoint(null, bitState);
            
            
            // Explicitly Back-referenced
        } else if (mapping.length == 7 && mapping[TYPE].equals("NK")) {
            int backAddress = Integer.parseInt(mapping[BACK_ADDRESS]);
            int backBit = Integer.parseInt(mapping[BACK_BIT]);
            
            boolean backReference = backreference(backAddress, backBit);
            
            return decodePoint(bitState, backReference);
            
        } else if (mapping.length == 7 && mapping[TYPE].equals("RK")) {
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
        if (mapping[TYPE].equals("DGK")) {
            return (bitState) ? Signal.CLEAR : Signal.RESTRICTIVE;
            
        } else if (mapping[TYPE].equals("OFFK")) {
            return (bitState) ? Signal.OFF : Signal.DANGER;
            
        } else if (mapping[TYPE].equals("RGK")) {
            return (bitState) ? Signal.DANGER : Signal.OFF;
            
        } else {
            throw new IllegalMapFormatException("Invalid map: " + Arrays.toString(mapping));
        }
    }
    
    
    private int trackCircuitChange(boolean bitState) {
        return (bitState) ? TrackCircuit.OCCUPIED : TrackCircuit.UNOCCUPIED;
    }
    
    private int routeIndicatorChange(boolean bitState) {
        return (bitState) ? RouteIndicator.PRESSED : RouteIndicator.RELEASED;
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
        short Byte = equipmentBytes[address];
        
        return getBitFromByte(Byte, bit);
    }
    
    private boolean getBitFromByte(int Byte, int bitIndex) {
        return ((Byte >> (bitIndex)) & 1) == 1;
    }
    
    
    private static int[] getBitChanges(short original, short updated) {
        short change = (short) (original ^ updated); // XOR, changed bit will be 1
        
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
    
    
    
    
    protected HashMap<Integer, String[]> loadEquipmentMap() throws FileNotFoundException {
        ClassLoader classLoader = getClass().getClassLoader();
        
        InputStream mapStream = classLoader.getResourceAsStream("SignallingEquipmentMap.csv");
        if (mapStream == null) {
            throw new FileNotFoundException("Could not find signalling equipment map. Path: " + "SignallingEquipmentMap.csv");
        }
        Scanner scanner = new Scanner(mapStream);
        scanner.nextLine();
        
        HashMap<Integer, String[]> equipmentMap = new HashMap<>();
        while (scanner.hasNextLine()) {
            String[] line = scanner.nextLine().split(",");
            
            short[] locator = new short[2];
            try {
                locator[0] = Short.parseShort(line[ADDRESS]);
                locator[1] = Short.parseShort(line[BIT]);
            } catch (NumberFormatException e) {
                throw new IllegalMapFormatException("The map address or bit is not a parsable number");
            }
            
            equipmentMap.put(Arrays.hashCode(locator), line);
        }
        
        return equipmentMap;
    }
}
