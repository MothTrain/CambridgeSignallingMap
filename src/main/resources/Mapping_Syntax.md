Note: Al

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
with 0 being the MSB, and 7 is the LSB. Not all the rows have to be provided or have to be in order.

The 3rd column (Type) indicates what type of equipment is represented by the bit. <br>
The valid types are:
- DGK - Is the signal green
- RGK - Is the signal at danger
- OFFK - Is the signal not at danger (just the inverse of an RGK)
- NK - Is the point showing normal
- RK - Is the point showing reverse
- B - Is a route set from this signal (the buttons on the signalling panels light up when a route is set from them!)
- T - Is the track circuit/axle counter section occupied
- RM - A main (regular) route set
- RS - A shunt route set
- RC - A call-on route
They are loosely based off of their type names in SOP tables. See [Signalling Nomenclature](https://wiki.openraildata.com/index.php?title=Signalling_Nomenclature)

The 4th column (id) is a unique name for a piece of equipment. It is advised to name this after its real name
in SOP tables, so for example, for signal 194: ID it as 194.

## Back-referencing 
Sometimes we get more than one bit of information for one piece of equipment (For example with points we _usually_
get a reverse and normal bit indications). This means when one bit of its state changes, we can refer to the other bit 
and get more information on its state, so if the NK and RK are 1 or 0 at the same time, we can imply that the
points are currently shifting positions. This applies to other equipment types as well. <br>
Back referencing is not required and its relevant columns can be left blank.

Back referencing can only be done on: DGK, RGK, OFFK, NK and RK.

The 5th column (Type (2)) refers the type of the equipment we are back-referencing to. So for example if one
row is mapping a point's NK, we back-reference it's RK, so type (2) will be RK.
The 6th and 7th column are the address and bit number of the equipment we are back-referencing.

Back-referencing points have some exceptions for simplicity. If the NK and RK are next to each other (with NK first)
you can leave Address (2), and Bit (2) blank: the program will assume that they are next to each other. This can
be disabled by placing UNMAPPED in the type column. This feature does not work if the indications are in different
bytes, or if the NK and RK are reversed. In this case you will have to manually specify the address and bit.

## Placeholders
Sometime signalling engineers leave spaces in SOP tables, either to make positioning of elements in the table more
reasonable and/or to leave space for new equipment to be added later. Since the SOP table for Cambridge PSB is
pretty outdated (it is before Cambridge North!), new equipment has been added. In the gaps in the tables leave 
PLACEHOLD in the Type columns.

If the bit is truly blank then it will stay at 0 but if equipment has been added there then it may change to 1
when the indications change (as normal equipment does). In this case, the program will log this specially, so
it can be investigated.