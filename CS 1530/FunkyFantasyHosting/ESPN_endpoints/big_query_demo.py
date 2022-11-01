from google.cloud import bigquery
import requests
import pandas as pd
import numpy as np
# from bs4 import BeautifulSoup as BS
import warnings
import json


warnings.filterwarnings('ignore')

# making sure we can see the whole table
desired_width=320
pd.set_option('display.width', desired_width)
np.set_printoptions(linewidth=desired_width)
pd.set_option('display.max_columns', 25)


bqclient = bigquery.Client()

# # Download query results.
# query_string = """
# SELECT *
# FROM `funky-fantasy-hosting-1.player_gamelogs_pivot.nfl_player_bio`
#
# """


# How to overwrite: https://cloud.google.com/bigquery/docs/reference/rest/v2/Job
# writeDisposition:
# WRITE_TRUNCATE: If the table already exists, BigQuery overwrites the table data and uses the schema from the query result.
# WRITE_APPEND: If the table already exists, BigQuery appends the data to the table.

# job_config = bigquery.CopyJobConfig()
# job_config.write_disposition = "WRITE_TRUNCATE"
# job = client.copy_table(
#     source_table_ref,
#     dest_table_ref,
#     location='US',
#     job_config=job_config)  # API request


query_string = """
UPDATE  `funky-fantasy-hosting-1.ff_league_table.league_721301807` 
SET user_ids = 5
WHERE user_ids is null 

"""

id = 721301807

test_dict = {'test': 5, 'scooby':3}

print(query_string.format(test=test_dict))

dataframe = (
    bqclient.query(query_string.format(test=test_dict))
    .result()
    .to_dataframe(
        # Optionally, explicitly request to use the BigQuery Storage API. As of
        # google-cloud-bigquery version 1.26.0 and above, the BigQuery Storage
        # API is used by default.
        create_bqstorage_client=True,
    )
)
print(dataframe.head())