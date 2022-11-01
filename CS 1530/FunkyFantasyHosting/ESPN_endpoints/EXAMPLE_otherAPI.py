
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

# example of pulling stats for Lamar Jackson from his gamelog page on ESPN
res = requests.get('http://sports.core.api.espn.com/v2/sports/football/leagues/nfl/seasons/2021/types/2/athletes/3916387/statistics/0')
content = json.loads(res.content)



# print(content['categories'])

for key in content:
    print(key)
    print(content[key])

print('scooby')
#print(content['seasonTypes']['categories'])



# http://api.espn.com/v1/sports/baseball/mlb/athletes/3748
# /{version}/sports/{sport}/{league}/athletes/{athleteID}


if res.ok:
    data = res.json() #converting response from API to json response (list)


    #df = pd.DataFrame(content['seasonTypes'])

    #saving all projectsions to file (doubt need)
    #df.to_csv('fullyreaddata.csv')

## ISSUE: need to pull the right dictionary key from the JSON file for the stats
#print(df.head())



# we could just make a df with the column names as the categories, then convert the later json into a df, then separate
# the column into a string the populate the main df