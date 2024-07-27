package aradnezami.cambridgesignallingmap;

/**
 * OutputHandler is an interface which specifies a class which outputs data from the td feed
 * (usually in a human-readable format)
 */
public interface OutputHandler {
    /**
     * Called to output an C-Class change. This is the berth related movements.
     * @param OutBerth The berth that the describer is leaving (null for berth interpose)
     * @param InBerth The berth that the describer is entering (null for berth cancel)
     * @param describer The train describer
     */
    void CClassChange(String OutBerth, String InBerth, String describer);
    
    
    /**
     * Called to output an S-Class change. This is signalling related events
     * The {@code type} and {@code state} come from the constants in the Diagram package.
     * The ID should be as it appears in the mapping table.
     * @param type The type of equipment changing state
     * @param state The new state of the equipment
     * @param id The ID of the equipment
     */
    void SClassChange(int type, int state, String id);
    
    /**
     * Called to output when a placeholder bit (see Mapping_Syntax.md in resources) becomes positive
     * @param address address of the placeholder change
     * @param bit bit index of the placeholder bit changed
     */
    void PlaceholderBitSet(int address, int bit);
}
