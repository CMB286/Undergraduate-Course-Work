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

df_final = pd.DataFrame()

# initialize bigquery
client = bigquery.Client()

table_id = "funky-fantasy-hosting-1.espn_nfl_player_bios.nfl_player_bio"

def get_player_id(row):
    res = requests.get(row)             # pass the URL from the row
    player_dict = pd.Series(row)
    answer = pd.DataFrame(player_dict)  # create a return dataframe that can be appended to the table

    answer = answer.rename(columns={0: 'access_url'})       # so can be merged later

    # available fields: id, uid, guid, type, alternate id, weight, height, age, DOB, debut year, position, active status
    # available links: playercard, college, stats, and many more,

    if res.ok:
        data = res.json()                                   # access the data at the URL
        athlete_id = data['id']
        first_name= data['firstName']
        last_name = data['lastName']
        display_name = data['displayName']
        short_name = data['shortName']
        player_links  = data['links']
        player_overview = player_links[0]['href']
        active_status = data['active']

        # some players don't have bio information cause they don't exist, or something else
        try:
            player_headshot = data['headshot']['href']
            player_weight = data['weight']
            player_height = data['height']
            player_display_height = data['displayHeight']
            player_age = data['age']
            player_dob = data['dateOfBirth']
        except KeyError:
            empty_dict = {'href': '', 'alt': ''}
            player_headshot = empty_dict['href']
            player_weight = ''
            player_height = ''
            player_display_height = ''
            player_age = ''
            player_dob = ''

        # position information
        position_dict = data['position']
        pos_name = position_dict['name']
        pos_abb = position_dict['abbreviation']

        # adding to return dataframe
        answer['id'] = athlete_id
        answer['first_name'] = first_name
        answer['last_name'] = last_name
        answer['display_name'] = display_name
        answer['short_name'] = short_name
        answer['player_weight'] = player_weight
        answer['player_height'] = player_height
        answer['player_display_weight'] = player_display_height
        answer['player_age'] = player_age
        answer['player_dob'] = player_dob

        answer['pos'] = pos_abb
        answer['pos_name'] = pos_name
        answer['active_status'] = active_status
        answer['player_headshot'] = player_headshot
        answer['player_overview'] = player_overview

    return answer


def check_position(pos):
    accepted_pos = ['QB', 'RB', 'WR', 'TE', 'PK']
    result = False

    for p in accepted_pos:
        if pos == p:
            return True

    return result


espn_api = 'https://sports.core.api.espn.com/v2/sports/football/leagues/nfl/athletes?limit=1000&page={pagenumber}'
# espn_api = 'https://sports.core.api.espn.com/v2/sports/football/leagues/nfl/athletes?limit=1000&page=1'

# when decide to run, need to cycle through all the page numbers
for i in range(1, 18):

    res = requests.get(espn_api.format(pagenumber = i))
    #res = requests.get(espn_api)

    if res.ok:
        data = res.json()           # converting response from API to json response (list)
        df_nested_list = pd.json_normalize(data, record_path=[
            'items'])               # cleans up JSON data down to category level where data is

        df_nested_list = df_nested_list.rename(columns={'$ref':'access_url'})

        for index, row in df_nested_list.iterrows():
            df_final = df_final.append(get_player_id(row['access_url']), ignore_index=True)

            # make sure it is a position we care about. Done after the fact, because we need the access URL to check
            if not check_position(df_final.iloc[-1]['pos']) or not df_final.iloc[-1]['active_status']:
                df_final.drop(df_final.tail(1).index, inplace=True)

# need to drop any row that has blank for bio stuff like weight, age, DOB (not relevant players)
df_final['player_weight'].replace('', np.nan, inplace=True)     # change blank strings to official nan
df_final.dropna(axis=0, subset=['player_weight'], inplace=True) # drop any row with nan

# df_final.to_csv('df_final.csv')
print(df_final.head())


job = client.load_table_from_dataframe(
    df_final, table_id,
)  # Make an API request.
job.result()  # Wait for the job to complete.

table = client.get_table(table_id)  # Make an API request.
print(
    "Loaded {} rows and {} columns to {}".format(
        table.num_rows, len(table.schema), table_id
    )
)


