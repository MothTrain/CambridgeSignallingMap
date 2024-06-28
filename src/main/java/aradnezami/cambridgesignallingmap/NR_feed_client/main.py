# Standard
import argparse
import json
import sys
from time import sleep

# Third party
import stomp

# Internal
from Communications import trust, td


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
            self._mq.ack(id=headers["ack"], subscription=headers["subscription"])



        messages = td.get_td_frame(parsed_body)
        if len(messages) == 0 or messages is None:
            return

        for message in messages:
            print(message)
            sys.stdout.flush()





    def on_error(self, frame):
        print('INFO received an error {}'.format(frame.body))
        sys.stdout.flush()
    def on_disconnected(self):
        print('INFO disconnected')
        sys.stdout.flush()

if __name__ == "__main__":
    with open("secrets.json") as f:
        feed_username, feed_password = json.load(f)
        print(feed_password)
        print(feed_username)

    parser = argparse.ArgumentParser()
    parser.add_argument("-d", "--durable", action='store_true',
                        help="Request a durable subscription. Note README before trying this.")
    action = parser.add_mutually_exclusive_group(required=False)
    action.add_argument('--td', action='store_true', help='Show messages from TD feed', default=True)
    action.add_argument('--trust', action='store_true', help='Show messages from TRUST feed')

    args = parser.parse_args()

    # https://stomp.github.io/stomp-specification-1.2.html#Heart-beating
    # We're committing to sending and accepting heartbeats every 5000ms
    connection = stomp.Connection([('publicdatafeeds.networkrail.co.uk', 61618)], keepalive=True, heartbeats=(50000, 50000))
    connection.set_listener('', Listener(connection))

    # Connect to feed
    connect_headers = {
        "username": feed_username,
        "passcode": feed_password,
        "wait": True,
    }
    if args.durable:
        # The client-id header is part of the durable subscription - it should be unique to your account
        connect_headers["client-id"] = feed_username

    # Determine topic to subscribe
    topic = None
    connection.connect(**connect_headers)

    if args.trust:
        topic = "/topic/TRAIN_MVT_ALL_TOC"
    elif args.td:
        topic = "/topic/TD_ALL_SIG_AREA"

    # Subscription
    subscribe_headers = {
        "destination": topic,
        "id": 1,
    }
    if args.durable:
        # Note that the subscription name should be unique both per connection and per queue
        subscribe_headers.update({
            "activemq.subscriptionName": feed_username + topic,
            "ack": "client-individual"
        })
    else:
        subscribe_headers["ack"] = "auto"

    connection.subscribe(**subscribe_headers)


    while connection.is_connected():

        sleep(1)
