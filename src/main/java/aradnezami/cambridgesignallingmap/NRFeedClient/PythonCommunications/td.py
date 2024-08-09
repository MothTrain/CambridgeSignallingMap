# TD message types

C_BERTH_STEP = "CA"       # Berth step      - description moves from "from" berth into "to", "from" berth is erased
C_BERTH_CANCEL = "CB"     # Berth cancel    - description is erased from "from" berth
C_BERTH_INTERPOSE = "CC"  # Berth interpose - description is inserted into the "to" berth, previous contents erased
C_HEARTBEAT = "CT"        # Heartbeat       - sent periodically by a train describer

S_SIGNALLING_UPDATE = "SF"            # Signalling update
S_SIGNALLING_REFRESH = "SG"           # Signalling refresh
S_SIGNALLING_REFRESH_FINISHED = "SH"  # Signalling refresh finished

def getMessages(parsed_body):
    messages = []

    # Each message in the queue is a JSON array
    for outer_message in parsed_body:
        # Each list element consists of a dict with a single entry - our real target - e.g. {"CA_MSG": {...}}
        message = list(outer_message.values())[0]

        message_type = message["msg_type"]
        timestamp = message["time"]

        if message["area_id"] != "CA": continue


        if message_type in [C_BERTH_STEP, C_BERTH_INTERPOSE, C_BERTH_CANCEL]:
            toBerth = message.get("to")
            fromBerth = message.get("from")

            if toBerth is None: toBerth = "NONE"
            if fromBerth is None: fromBerth = "NONE"

            describer = message.get("descr")

            messages.append(f"C,{timestamp},{fromBerth},{toBerth},{describer}")

        elif message_type == S_SIGNALLING_UPDATE:
            address = message.get("address")
            byte = message.get("data")

            messages.append(f"S,{timestamp},{address},{byte}")

        elif message_type in [S_SIGNALLING_REFRESH, S_SIGNALLING_REFRESH_FINISHED]:
            address = int(message.get("address"))
            bytesRaw = message.get("data")
            bytesSplit = splitSigRefresh(bytesRaw, address)

            if address == 0:
                messages.append("MSG:1")
            elif message_type == S_SIGNALLING_REFRESH_FINISHED:
                messages.append("MSG:2")

            for separateMsg in bytesSplit:
                messages.append(f"S,{timestamp},{separateMsg[0]},{separateMsg[1]}")

        return messages




def splitSigRefresh(strBytes, minAddress):
    split = []
    for position in range(0, 7, 2): # Step 2 as 1 byte is represented by to hex digits
        address = int(minAddress + position/2)
        if address > 200: break # Find the byte address we are parsing. If it is more than 200 (max address of Cambridge PSB), ignore it

        byte = strBytes[position : position+2]
        split.append((address, byte))

    return split