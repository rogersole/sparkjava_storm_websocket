#!/usr/bin/env python2.7
# -*- coding: utf-8 -*-
#
# Authors:
#    Roger Solé <roger.sole@gmail.com> - 2015
#
from __future__ import division

import logging
import random
import requests
import ConfigParser

from os import path
from datetime import datetime
from utils import countries_iso2, currencies_iso

# configure logging
logging.basicConfig(level="DEBUG")
logger = logging.getLogger(__name__)

# change requests logger level to ERROR
logging.getLogger('requests').setLevel(logging.ERROR)


# load the config file
config = ConfigParser.ConfigParser()
if not config.read(path.join(path.dirname(__file__), '..', 'cfg', 'trader.cfg')):
    raise SystemError("Unable to load configuration file trader.cfg")


class Simulator(object):
    """ Simulates the trades randomly chosen
    """
    def __init__(self):
        """
        :return:
        """
        self._user_id = 1
        self.endpoint = config.get('consumer_endpoint', 'endpoint')
        self.authentication_header = config.get('requests', 'authentication_header')

    def start(self):
        """ Starts the simulation generating random Trade objects to be sent to the endpoint
        :return:
        """
        num_currencies = len(currencies_iso)
        num_countries = len(countries_iso2)
        headers = {'content-type': 'application/json', 'Authentication': self.authentication_header}

        while True:
            sell = random.randint(10, 1000000)
            buy = random.randint(10, 1000000)
            rate = buy / sell

            payload = {'userId': self._user_id,
                       'currencyFrom': currencies_iso[random.randint(0, num_currencies-1)],
                       'currencyTo': currencies_iso[random.randint(0, num_currencies-1)],
                       'amountSell': sell,
                       'amountBuy': buy,
                       'rate': rate,
                       'timePlaced': datetime.utcnow().strftime("%d-%b-%y %H:%M:%S").upper(),
                       'originatingCountry': countries_iso2[random.randint(0, num_countries-1)]}

            requests.post(self.endpoint, json=payload, headers=headers)
            self._user_id += 1
            if self._user_id % 100 == 0:
                logger.info("Sent %s trades...", self._user_id)


if __name__ == "__main__":
    simulator = Simulator()
    simulator.start()

