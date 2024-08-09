import stomp
import json

import td

class Listener(stomp.ConnectionListener):
    _mq: stomp.Connection

    def __init__(self, mq: stomp.Connection, durable=False):
        self._mq = mq
        self.is_durable = durable

    def on_message(self, frame):
        headers, message_raw = frame.headers, frame.body
        parsed_body = json.loads(message_raw)

        if self.is_durable:
            # Acknowledging messages is important in client-individual mode
            self._mq.ack(headers["message-id"], subscription=headers["subscription"])

        if "TD_" in headers["destination"]:
            messages = td.getMessages(parsed_body)
            if messages is None: return

            for message in messages:
                print(message, flush=True)


        else:
            print("WARN Unknown destination: ", headers["destination"], flush=True)

    def on_error(self, frame):
        print('WARN Received an error {},{}'.format(frame.body,frame.headers), flush=True)

    def on_disconnected(self):
        print('INFO Disconnected', flush=True)
