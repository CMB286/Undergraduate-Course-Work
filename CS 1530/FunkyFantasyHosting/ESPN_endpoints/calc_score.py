# Football Import
from espn_api.football import League
import requests
import pandas as pd
import numpy as np
import warnings
import json
from google.cloud import bigquery

# initialize bigquery
client = bigquery.Client()
table_id_league = "funky-fantasy-hosting-1.ff_league_table.league_{id}"