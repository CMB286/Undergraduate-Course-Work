from lib2to3.pgen2.token import NEWLINE
from google.cloud import bigquery
from google.cloud.exceptions import NotFound

client = bigquery.Client()

# TODO(developer): Set dataset_id to the ID of the dataset to determine existence.
dataset_id = "funky-fantasy-hosting-1.ff_league_table"

# Uncomment block below and run this file to test sql query stuff
# -------------------------------------------------------------
# sql = """
#     SELECT *
#     FROM `funky-fantasy-hosting-1.ff_player_table.league_721301807`
# """

# df = client.query(sql).to_dataframe()
# for x in range(len(df.index)):
#     #print(df.iloc[x])
#     y = df.iloc[x]
#     print (y.loc['player_id'])
# print(len(df.index))
# commish = df.loc[df['league_commish'] == 1]
# print(commish)
# team_name = commish.iloc[0]['team_name']
# print(type(team_name))
# if team_name == "Team Hedayati":
#     print("yay")

#export GOOGLE_APPLICATION_CREDENTIALS=ff_big_query.json
#---------------------------------------------------------------



#This function takes the league ID and returns the entire league table.
def get_league_df(id):
    sql = """
    SELECT *
    FROM `funky-fantasy-hosting-1.ff_league_table.league_{idins}`
    """.format(idins = id)
    df = client.query(sql).to_dataframe()
    return df

def get_team_df(id):
    sql = """
    SELECT *
    FROM `funky-fantasy-hosting-1.ff_team_table.league_{idins}`
    """.format(idins = id)
    df = client.query(sql).to_dataframe()
    return df

def get_player_df(id):
    sql = """
    SELECT *
    FROM `funky-fantasy-hosting-1.ff_player_table.league_{idins}`
    """.format(idins = id)
    df = client.query(sql).to_dataframe()
    return df

def get_player_info():
    sql = """
    SELECT *
    FROM `funky-fantasy-hosting-1.espn_nfl_player_bios.nfl_player_bio`
    """
    df = client.query(sql).to_dataframe()
    return df

# df = get_player_info()
# pl_df = get_player_df(721301807) #The player dataframe
# df.set_index("id", inplace = True)
# print(pl_df.iloc[44])
# index = str(pl_df.iloc[44].loc['player_id'])
# row = df.loc[index]
# print(len(row))
# print(row)
# print(row.loc['first_name'])