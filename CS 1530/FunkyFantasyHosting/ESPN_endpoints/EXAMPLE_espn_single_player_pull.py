import requests
import pandas as pd
import numpy as np
# from bs4 import BeautifulSoup as BS
import warnings
import json
from google.cloud import bigquery

warnings.filterwarnings('ignore')

# making sure we can see the whole table
desired_width=320
pd.set_option('display.width', desired_width)
np.set_printoptions(linewidth=desired_width)
pd.set_option('display.max_columns', 25)

# initialize bigquery
client = bigquery.Client()

table_id_gamelog = "funky-fantasy-hosting-1.player_gamelogs.nfl_player_bio"
table_id_gamelog_pivot = "funky-fantasy-hosting-1.player_gamelogs_pivot.nfl_player_bio"

# example of pulling stats for Lamar Jackson from his gamelog page on ESPN
athlete_url = 'https://site.web.api.espn.com/apis/common/v3/sports/football/nfl/athletes/{athlete_id}/gamelog'
athlete_list = [15818, 3916387] # list of potential athletes first one is Keenan Allen, next is Lamar Jackson 3916387
df_gamelog_final = pd.DataFrame()   # will concat all players to this final df

def convert_to_dict(row):
    row_dict = row.to_dict()
    print(row_dict)



for athlete_id in athlete_list:
    access_url = athlete_url.format(athlete_id=athlete_id)
    res = requests.get(access_url)

    if res.ok:
        data = res.json() # converting response from API to json response (list)

        df_nested_list = pd.json_normalize(data['seasonTypes'], record_path =['categories'])     # cleans up JSON data down to category level where data is
        df1 = pd.DataFrame(df_nested_list['events'])        # goes one level down into 'events', which gives us a list of dictionaries again
        print(df1)
        df1_tolist = df1['events'].tolist()         # since it is a list of dictionaries inside a df, have to make it an actual list
        df2 = pd.DataFrame(df1_tolist[0])           # take the list and turn back into a df
        df_stats = pd.DataFrame(df2['stats'].to_list(), columns=data['displayNames'])
        event_column = df2['eventId']               # need to add the events back in, so making it a list to add

        df_final = pd.concat([df_stats, event_column], axis=1)

        df_final['athlete_id'] = athlete_id         # add id f
        event_column = df_final.pop('eventId')  # moving the event column to front
        df_final.insert(0, 'eventId', event_column)
        print(df_final)
        # df_final is a df where each column is a stat, and there is event ID, and athlete id. Keeping intact, just in
        # case we prefer that in the future


        # print(data['events'])  # keyed off eventid
        # print(data['events']['401326368']['week'])  # gets me game week
        # print(data['events']['401326368']['gameDate'])  # gets actual game


        df_test = pd.DataFrame()            # creating new dataframe so we keep original format just in case
        df_test = df_final                  # copy the original dataframe so can convert into a dictionary
        df_test = df_test.drop(['athlete_id'], axis = 1)    # remove athlete id before collapsing
        df_test = df_test.to_dict(orient='records')         # df_test is now a dictionary list

        df_gamelog = pd.DataFrame(columns=['athlete_id'])
        df_gamelog.at[0, 'athlete_id'] = athlete_id
        for event in df_test:
            df_gamelog.insert(len(df_gamelog), 'week{no}'.format(no=data['events'][event['eventId']]['week']), [event], True)

        df_gamelog_final = df_gamelog_final.append(df_gamelog)
        df_gamelog_final.to_csv('df_gamelog_final.csv')
        print(df_gamelog_final.head())

        # df_final.to_csv('df_final.csv')     # saving to csv for fun

# column names can't have spaces
df_final.columns = df_final.columns.str.replace(' ', '_')

# sending to bigquery tables for gamelog each column a stat
job = client.load_table_from_dataframe(
    df_final, table_id_gamelog,
)  # Make an API request.
job.result()  # Wait for the job to complete.

table = client.get_table(table_id_gamelog)  # Make an API request.
print(
    "Loaded {} rows and {} columns to {}".format(
        table.num_rows, len(table.schema), table_id_gamelog
    )
)

# column names can't have spaces
df_gamelog_final.columns = df_gamelog_final.columns.str.replace(' ', '_')

# sending to bigquery tables for gamelog each column week
job = client.load_table_from_dataframe(
    df_gamelog_final, table_id_gamelog_pivot,
)  # Make an API request.
job.result()  # Wait for the job to complete.

table = client.get_table(table_id_gamelog_pivot)  # Make an API request.
print(
    "Loaded {} rows and {} columns to {}".format(
        table.num_rows, len(table.schema), table_id_gamelog_pivot
    )
)