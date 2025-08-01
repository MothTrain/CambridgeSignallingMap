## Signalling map
Signalling maps allow the programme to map the data signalling sent from the
TD feed to the state of real equipment Eg: A signal aspect or track circuit indications.

The following section is important context for the rest of the document. <br>
[Decoding S-Class data - Data Format](https://wiki.openraildata.com/index.php?title=Decoding_S-Class_Data#Data_Format)

## Syntax
The maps are in a comma-separated text-encoded document.
The top row is ignored for headers.
"Address,Bit Index,Type,ID,Type(2),Address(2),Bit(2)" is recommended.

The first 2 columns are hold the byte address (denary) and bit number of the mapping,
with **0 being the LSB, and 7 is the MSB**. Not all the rows have to be provided or have to be in order 
however this is recommended for readability.

The 3rd column (Type) indicates what type of equipment is represented by the bit. <br>
The valid types are:
- DGK - Is the main signal off (this appears to work just like an RGK in my experience: see Signal Mapping below)
- RGK - Is the main signal not off (use only when DGK is not provided)
- OFFK - Is the main signal off (In the "PIIU" interlockings, this tends to only refer to shunt 
signals, so use SOFFK instead)
- SOFFK - Is the shunt signal off 
- NK - Is the point showing normal
- RK - Is the point showing reverse
- B - Is a route set from this signal
- T - Is the track circuit/axle counter section occupied
- RM - Is a main (regular) route set
- RS - Is a shunt route set
- RC - Is a call-on route set

They are loosely based off of their type names in SOP tables. See [Signalling Nomenclature](https://wiki.openraildata.com/index.php?title=Signalling_Nomenclature)

The 4th column (id) is a unique name for a piece of equipment. It is advised to name this after its real name
in SOP tables, so for example, for signal 194: ID it as 194.

## Signal Mapping
There are 3 available types for signals. DGK, OFFK and RGK.
In the following interlockings: Cambridge A PIIU, Cambridge B PIIU, Foxton PIIU, Chesterton Jn PIIU and 
Whittlesford PIIU, signal types behave as such: <br>
DGK and RGK refer to main signals and OFFKs refer to shunt signals (including subsidiary shunt signals)

In the interlockings titled "SSI" (solid state interlocking) or "Std Indication", signal types behave as such: <br>
DGKs and RGKs are not present. OFFK refers to all signals, including signals which have both a main and shunt aspect,
which will from hereon be referred to as compound signals.
In cases where a compound signal is referred to by one OFFK indication, a main route from the signal can be
backreferenced to determine if the shunt or main aspect is off

## Back-referencing 
Sometimes we get more than one bit of information for one piece of equipment. This applies to other equipment types 
as well. <br>

Back referencing can only performed by:  NK, RK, DGK and SOFFK.

The 5th column (Type (2)) refers the type of the equipment we are back-referencing to. So for example if one
row is mapping a point's NK, we back-reference it's RK, so type (2) will be RK.
The 6th and 7th column are the address and bit number of the equipment we are back-referencing.

### Points
With points, we _usually_ get a reverse and normal bit indications. This means when one bit of its state 
changes, we can refer to the other bit and get more information on its state, so if the NK and RK are 1 
or 0 at the same time, we can imply that the points are currently shifting positions.

If the NK and RK are next to each other (with NK first)
you can leave Address (2), and Bit (2) blank: the program will assume that they are next to each other. This can
be disabled by placing UNMAPPED in the type column. This feature does not work if the indications are in different
bytes, or if the RK is first. In this case you will have to manually specify the type, address and bit.

### Compound Signals
In PIIU interlockings, where a compound signal has a DGK for the main aspect and SOFFK for the shunt aspect,
the SOFFK and DGK **must** backreference each-other.

In SSI and Std indication interlockings, where the 2 aspects of a compound signal is represented by a 
single OFFK, the OFFK may backreference an RM (but not vice versa) that whose entry is from the same signal. If the
main route is set when the signal clears, then the main aspect is considered off. Otherwise, the shunt aspect
is considered to be off.