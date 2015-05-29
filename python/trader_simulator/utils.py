#!/usr/bin/env python2.7
# -*- coding: utf-8 -*-
#
# Authors:
#    Roger Solé <roger.sole@gmail.com> - 2015
#

currencies_iso = ['USD', 'EUR', 'CAD', 'GBP', 'AUD', 'AED', 'ANG', 'AOA', 'ARS', 'BAM', 'BBD', 'BGL', 'BHD', 'BND',
                  'BRL', 'CHF', 'CLF', 'CLP', 'CNY', 'COP', 'CRC', 'CZK', 'DKK', 'EEK', 'EGP', 'FJD', 'GTQ', 'HKD',
                  'HRK', 'HUF', 'IDR', 'ILS', 'INR', 'JOD', 'JPY', 'KES', 'KRW', 'KWD', 'KYD', 'LTL', 'LVL', 'MAD',
                  'MVR', 'MXN', 'MYR', 'NGN', 'NOK', 'NZD', 'OMR', 'PEN', 'PHP', 'PLN', 'QAR', 'RON', 'RUB', 'SAR',
                  'SEK', 'SGD', 'THB', 'TRY', 'TTD', 'TWD', 'UAH', 'VEF', 'VND', 'XCD', 'ZAR']


countries_iso2 = ["AF",  "AL",  "DZ",  "AS",  "AD",  "AO",  "AI",  "AQ",  "AG",  "AR",  "AM",  "AW",  "AU",  "AT",
                  "AZ",  "BS",  "BH",  "BD",  "BB",  "BY",  "BE",  "BZ",  "BJ",  "BM",  "BT",  "BO",  "BA",  "BW",
                  "BV",  "BR",  "IO",  "VG",  "BN",  "BG",  "BF",  "BI",  "KH",  "CM",  "CA",  "CV",  "KY",  "CF",
                  "TD",  "CL",  "CN",  "CX",  "CC",  "CO",  "KM",  "CD",  "CG",  "CK",  "CR",  "CI",  "CU",  "CY",
                  "CZ",  "DK",  "DJ",  "DM",  "DO",  "EC",  "EG",  "SV",  "GQ",  "ER",  "EE",  "ET",  "FO",  "FK",
                  "FJ",  "FI",  "FR",  "GF",  "PF",  "TF",  "GA",  "GM",  "GE",  "DE",  "GH",  "GI",  "GR",  "GL",
                  "GD",  "GP",  "GU",  "GT",  "GN",  "GW",  "GY",  "HT",  "HM",  "VA",  "HN",  "HK",  "HR",  "HU",
                  "IS",  "IN",  "ID",  "IR",  "IQ",  "IE",  "IL",  "IT",  "JM",  "JP",  "JO",  "KZ",  "KE",  "KI",
                  "KP",  "KR",  "KW",  "KG",  "LA",  "LV",  "LB",  "LS",  "LR",  "LY",  "LI",  "LT",  "LU",  "MO",
                  "MK",  "MG",  "MW",  "MY",  "MV",  "ML",  "MT",  "MH",  "MQ",  "MR",  "MU",  "YT",  "MX",  "FM",
                  "MD",  "MC",  "MN",  "MS",  "MA",  "MZ",  "MM",  "NA",  "NR",  "NP",  "AN",  "NL",  "NC",  "NZ",
                  "NI",  "NE",  "NG",  "NU",  "NF",  "MP",  "NO",  "OM",  "PK",  "PW",  "PS",  "PA",  "PG",  "PY",
                  "PE",  "PH",  "PN",  "PL",  "PT",  "PR",  "QA",  "RE",  "RO",  "RU",  "RW",  "SH",  "KN",  "LC",
                  "PM",  "VC",  "WS",  "SM",  "ST",  "SA",  "SN",  "CS",  "SC",  "SL",  "SG",  "SK",  "SI",  "SB",
                  "SO",  "ZA",  "GS",  "ES",  "LK",  "SD",  "SR",  "SJ",  "SZ",  "SE",  "CH",  "SY",  "TW",  "TJ",
                  "TZ",  "TH",  "TL",  "TG",  "TK",  "TO",  "TT",  "TN",  "TR",  "TM",  "TC",  "TV",  "VI",  "UG",
                  "UA",  "AE",  "GB",  "UM",  "US",  "UY",  "UZ",  "VU",  "VE",  "VN",  "WF",  "EH",  "YE",  "ZM",
                  "ZW"]
