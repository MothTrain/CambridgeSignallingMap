#!/usr/bin/env python3

# Standard
import argparse
import json
import atexit
from time import sleep

# Third party
import stomp
from stomp.exception import ConnectFailedException

# Internal
from StompListener import Listener


def cleanup(connection):
    connection.disconnect()


if __name__ == "__main__":
    with open("src/main/java/aradnezami/cambridgesignallingmap/NRFeedClient/PythonCommunications/secrets.json") as f:
        feed_username, feed_password, hostName = json.load(f)

    parser = argparse.ArgumentParser()
    parser.add_argument("-d", "--durable", action='store_true',
                        help="Request a durable subscription. Note README before trying this.")
    action = parser.add_mutually_exclusive_group(required=False)
    action.add_argument('--td', action='store_true', help='Show messages from TD feed', default=True)

    args = parser.parse_args()

    # https://stomp.github.io/stomp-specification-1.2.html#Heart-beating
    # We're committing to sending and accepting heartbeats every 5000ms
    connection = stomp.Connection([('publicdatafeeds.networkrail.co.uk', 61618)], keepalive=True, heartbeats=(5000, 5000))
    connection.set_listener('', Listener(connection, args.durable))

    # Connect to feed
    connect_headers = {
        "username": feed_username,
        "passcode": feed_password,
        "wait": True,
    }
    if args.durable:
        # The client-id header is part of the durable subscription - it should be unique to your account
        connect_headers["client-id"] = feed_username

    try:
        connection.connect(**connect_headers)
        atexit.register(cleanup, connection)
    except ConnectFailedException:
        print("MSG:-1", flush=True)
        exit(1)

    # Determine topic to subscribe
    topic = None
    if args.td:
        topic = "/topic/TD_ALL_SIG_AREA"

    # Subscription
    subscribe_headers = {
        "destination": topic,
        "id": 1,
    }
    if args.durable:
        # Note that the subscription name should be unique both per connection and per queue
        subscribe_headers.update({
            "activemq.subscriptionName": hostName + topic,
            "ack": "client-individual"
        })
    else:
        subscribe_headers["ack"] = "auto"

    connection.subscribe(**subscribe_headers)

    while connection.is_connected():
        sleep(1)

