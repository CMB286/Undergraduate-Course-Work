# Football Import
from espn_api.football import League
import requests
import pandas as pd
import numpy as np
import warnings
import json
from google.cloud import bigquery

# EXAMPLE of using the python version of ESPN v3 for importing a league

# making sure we can see the whole table
desired_width=320
pd.set_option('display.width', desired_width)
np.set_printoptions(linewidth=desired_width)
pd.set_option('display.max_columns', 25)
pd.set_option('mode.chained_assignment', None)

# K's personal cookie info
s2 = "AECAlZGGZ%2BKVVazn84JPRPKlzGn8c0h0mgcW%2BmMD7rZbRi7Eo%2FD0AftzOhmp%2Bp2OXGN4jU37nau7amjPktismejCclO5Nw2itHBLxlMp5ho0TL9dQYfP8ElaaVYyi8r1rL5VfDEz9ZvVDKtNE2kqhfi5yeNP5G4H75oQV7I6j4oIqUTPdvXSW6KExsDka6tNgzr5Nlrbo1VLR2v7wqsUwnPHjL2y930EX9xhSzVCvN7Nhq47eb%2B%2FTed93BQHkkt1zSj0pk6BF0cQZVSWymyq5%2BSV"
sw_id = "{D3FB8C91-2170-4F49-9BF1-CFF8AABC99A1}"

# initialize bigquery
client = bigquery.Client()
table_id_league = "funky-fantasy-hosting-1.ff_league_table.league_{id}"
table_id_league_players = "funky-fantasy-hosting-1.ff_player_table.league_{id}"
table_id_league_teams = "funky-fantasy-hosting-1.ff_team_table.league_{id}"


# Alternate examples
# public league
# league = League(league_id=1245, year=2018)
# private league with username and password
# league = League(league_id=1245, year=2018, username='userName', password='pass')
# debug mode
# league = League(league_id=1245, year=2018, debug=True)

# LEAGUE TYPES: 0 - Guillotine 1 - Vampire 2 - Pirate
def add_new_league(lid, l_type=0, week=1):
    # hard code example for testing (private league)
    # league = League(league_id=1151092, year=2019, espn_s2=s2, swid=sw_id, debug=True)
    league = League(league_id=lid, year=2022)
    df_league = pd.DataFrame(columns=['league_id', 'league_name', 'team_id', 'team_name', 'league_type', 'user_ids',
                                      'score_settings', 'league_size', 'league_commish'])

    score_settings_dict = {
        "PY": 0.04,     # Passing Yards
        "PTD": 4,       # Passing TDs
        "INT": -2,      # Interceptions
        "TWOPC": 2,       # 2 Point conversions
        "RY": 0.1,      # Rushing Yards
        "RTD": 6,       # Rushing TDs
        "TWOPR": 2,       # Rushing 2 point conversions
        "REY": 0.1,     # reception Yards
        "REC": 1,       # Receptions
        "RETD": 6,      # Reception TDs
        "TWOPRE": 2,      # Reception 2 point conversions
        "FUML": -2,     # Fumbles
    }

    # row for every team, right now assumption commish is first team
    for team in league.teams:  # loop to create dictionary
        df_league = df_league.append({'league_id': lid,
                                      'league_name': league.settings.name,
                                      'team_id': team.team_id,
                                      'team_name': team.team_name,
                                      'league_type': l_type,
                                      'scoring_settings': score_settings_dict,
                                      'league_size': len(league.teams),
                                      'league_commish': (1 if team.team_id == 1 else 0)}, ignore_index=True)

    # changing from float to int
    df_league.league_id = df_league.league_id.astype(int)
    df_league.league_type = df_league.league_type.astype(int)
    df_league.league_size = df_league.league_size.astype(int)

    # starting player table build
    df_player = pd.DataFrame(columns=['player_id',
                                      'player_name',
                                      'team_id',
                                      'player_status',
                                      'player_inj_status',
                                      'player_vulnerable'])

    for free_agent in league.free_agents(week):
        df_player = df_player.append({'player_id': free_agent.playerId,
                                      'player_name': free_agent.name,
                                      'team_id': 0,
                                      'player_status': 'FA',
                                      'player_inj_status': str(free_agent.injuryStatus),
                                      'player_vulnerable': 0},
                                       ignore_index=True)

    for team in league.teams:
        for player in team.roster:
            df_player = df_player.append({'player_id': player.playerId,
                                          'player_name': player.name,
                                          'team_id': team.team_id,
                                          'player_status': 'Rostered',
                                          'player_inj_status': str(player.injuryStatus),
                                          'player_vulnerable': 0},
                                           ignore_index=True)

    # removing defenses
    df_player.drop(df_player[df_player.player_id < 0].index, inplace=True)

    print(df_league)

    # Starting team table build
    df_team = pd.DataFrame(columns=['team_id', 'team_name', 'user_id',
                                    'lineup', 'record'])

    starter_list = []
    for team in league.teams:
        lineup = {'BENCH': [], 'STARTER': []}
        for player in team.roster:
            if len(starter_list) < 7:
                starter_list.append(player.playerId)
            elif player.playerId < 0:
                continue
            else:
                lineup['BENCH'].append(player.playerId)

        lineup['STARTER'] = starter_list
        df_team = df_team.append({'team_id': team.team_id,
                                    'team_name': team.team_name,
                                    'user_id': -1,
                                    'lineup': lineup,
                                    'record': '0-0'}, ignore_index=True)

    # saving to the league table
    job = client.load_table_from_dataframe(
        df_league, table_id_league.format(id=lid),
    )  # Make an API request.
    job.result()  # Wait for the job to complete.

    table = client.get_table(table_id_league.format(id=lid))  # Make an API request.
    print(
        "Loaded {} rows and {} columns to {}".format(
            table.num_rows, len(table.schema), table_id_league.format(id=lid)))

    # saving to the team table
    job = client.load_table_from_dataframe(
        df_team, table_id_league_teams.format(id=lid),
    )  # Make an API request.
    job.result()  # Wait for the job to complete.

    table = client.get_table(table_id_league_teams.format(id=lid))  # Make an API request.
    print(
        "Loaded {} rows and {} columns to {}".format(
            table.num_rows, len(table.schema), table_id_league_teams.format(id=lid)))


    # saving to the player table
    job = client.load_table_from_dataframe(
        df_player, table_id_league_players.format(id=lid),
    )  # Make an API request.
    job.result()  # Wait for the job to complete.

    table = client.get_table(table_id_league_players.format(id=lid))  # Make an API request.
    print(
        "Loaded {} rows and {} columns to {}".format(
            table.num_rows, len(table.schema), table_id_league_players.format(id=lid)))

    return df_league, df_player, df_team


def update_user(lid, uid, tid):
    job_config = bigquery.LoadJobConfig()
    job_config.write_disposition = "WRITE_TRUNCATE"

    # update TEAMS table
    query_string = """
    SELECT *
    FROM `funky-fantasy-hosting-1.ff_team_table.league_{lid}`
    """

    df_load_team = (
        client.query(query_string.format(lid=lid))
            .result()
            .to_dataframe(
            create_bqstorage_client=True,
        )
    )

    df_load_team['user_id'].loc[df_load_team['team_id'] == tid] = uid

    # saving to the LEAGUE table
    job = client.load_table_from_dataframe(
        df_load_team, table_id_league_teams.format(id=lid),
        job_config=job_config
    )  # Make an API request.
    job.result()  # Wait for the job to complete.

    # update LEAGUE table
    query_string = """
    SELECT *
    FROM `funky-fantasy-hosting-1.ff_league_table.league_{lid}`
    """

    df_load_league = (
        client.query(query_string.format(lid=lid))
            .result()
            .to_dataframe(
            create_bqstorage_client=True,
        )
    )

    df_load_league['user_ids'].loc[df_load_league['team_id'] == tid] = uid

    # saving to the player table
    job = client.load_table_from_dataframe(
        df_load_league, table_id_league.format(id=lid),
        job_config=job_config
    )  # Make an API request.
    job.result()  # Wait for the job to complete.

    return df_load_team


def update_lineup(lid, pid, position):
    return None


def get_gamelog(pid):
    return None

# Testing functions

# add league testing functions
# our public league id is: 721301807
# my private league id is: 1151092

# add_new_league(721301807, 2)
# update_user(721301807, 666, 1)

# example of pulling a player
# lamar_jackson = league.player_info(playerId=3916387)

# example of pulling free agents
# league.free_agents(week=3)