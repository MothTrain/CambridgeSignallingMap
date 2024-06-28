# Python standard
import sys
from datetime import datetime

# Third party
from pytz import timezone


TIMEZONE_LONDON: timezone = timezone("Europe/London")

# TD message types

C_BERTH_STEP = "CA"       # Berth step      - description moves from "from" berth into "to", "from" berth is erased
C_BERTH_CANCEL = "CB"     # Berth cancel    - description is erased from "from" berth
C_BERTH_INTERPOSE = "CC"  # Berth interpose - description is inserted into the "to" berth, previous contents erased
C_HEARTBEAT = "CT"        # Heartbeat       - sent periodically by a train describer

S_SIGNALLING_UPDATE = "SF"            # Signalling update
S_SIGNALLING_REFRESH = "SG"           # Signalling refresh
S_SIGNALLING_REFRESH_FINISHED = "SH"  # Signalling refresh finished


def get_td_frame(parsed_body):
    messages = []
    # Each message in the queue is a JSON array
    for outer_message in parsed_body:
        # Each list element consists of a dict with a single entry - our real target - e.g. {"CA_MSG": {...}}
        message = list(outer_message.values())[0]

        if message.get("area_id") != "CA":
            continue

        msg_type = message.get("msg_type")

        if msg_type == S_SIGNALLING_UPDATE:
            address = message.get("address")
            data = message.get("data")
            messages.append(f"S,{address},{data}")

        elif msg_type in [C_BERTH_STEP,C_BERTH_INTERPOSE,C_BERTH_CANCEL]:
            toBerth = message.get("to")
            if toBerth is None: toBerth = "NONE"

            fromBerth = message.get("from")
            if fromBerth is None: fromBerth = "NONE"

            describer = message.get("descr")

            messages.append(f"C,{fromBerth},{toBerth},{describer}")

        if msg_type in [S_SIGNALLING_REFRESH, S_SIGNALLING_REFRESH_FINISHED]:
            address = message.get("address")
            data = message.get("data") # composed of 4 bytes


            for i in range(0,7,2): # output each byte of refresh as normal S message
                byteData= data[i:i+2]
                messages.append(f"S,{address},{byteData}")

                address = int(address, 16)
                address += 1
                if address > 200: break # Highest address of Cambridge PSB is 200
                address = format(address, 'x')

            if address == 0:
                print("INFO Signalling refresh started")
                sys.stdout.flush()
            elif msg_type == S_SIGNALLING_REFRESH_FINISHED:
                print("INFO Signalling refresh complete")
                sys.stdout.flush()


    return messages