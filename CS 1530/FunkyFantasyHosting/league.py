from ast import IsNot
import team, user, player, matchup, playoffs, bigquery_fun
from ESPN_endpoints import league_api_endpoints

class League:

    #Global Constants related to the League class

    #The different types of leagues that can be created, assigned to arbitrary integers to prevent misspelling and unneccesary string comparisons
    DEFAULT, GUILLOTINE, VAMPIRE, PIRATE = 0, 1, 2, 3

    #The differnt types of waivers, which is either waiver or faab
    WAIVER, FAAB = 0, 1

    def __init__(self, id):
        self.id = id
        try:
            lg_df = bigquery_fun.get_league_df(id) #The league dataframe
            tm_df = bigquery_fun.get_team_df(id) #The team dataframe
            pl_df = bigquery_fun.get_player_df(id) #The player dataframe
            pl_info_df = bigquery_fun.get_player_info()
            commish_df = lg_df.loc[lg_df['league_commish'] == 1]
            pl_info_df.set_index("id", inplace = True)
        except Exception:
            print("Import error")
            return None

        self.name = "Placeholder"
        self.type = lg_df.iloc[0]['league_type']
        self.teamList = []
        #populate the team list
        for x in range(len(tm_df.index)):
            teamToAdd = team.Team(tm_df.iloc[x])
            self.teamList.append(teamToAdd)

        self.teamList.sort(key = lambda x: x.get_id())
        for x in self.teamList:
            print(x.get_id())
        self.rosterMax = 10
        self.commish = commish_df.iloc[0]['user_ids']
        self.playerList = []
        #populate the player list
        for x in range(len(pl_df.index)):
            pid = str(pl_df.iloc[x].loc['player_id'])
            playerToAdd = player.Player(pl_df.iloc[x], pl_info_df.loc[pid])
            self.playerList.append(playerToAdd)
            if(playerToAdd.get_league_team() != 0):
                self.teamList[playerToAdd.get_league_team()-1].add_player_c(playerToAdd)
        self.rosterSettings = None
        self.scoringSettings = None
        self.memberSettings = None
        #populate the matchup list
        self.matchupList = None
        self.size = lg_df.iloc[0]['league_size']
        self.waiverType = None
        self.FAABBudget = None
        self.waiverDropPeriod = None
        self.tradeTimeline = None
        self.tradeDeadline = None
        self.playoffStartTime = None
        self.playoffTeamNumber = None
        self.injuredReserveSlots = None
        self.irSettingRules = None
        #self.teamStandings = None #Don't need because teamList is sorted from last to first
        self.week = None
        self.winWeight = 2 #How much a win counts for in terms of standings
        self.tieWeight = 1 #How much a tie counts for in terms of standings
        self.lossWeight = 0 #How much a loss counts for in terms of standings

    def __repr__(self):
        return None

    def get_id(self):
        return self.id

    def get_teams(self):
        return self.teamList

    def get_players(self):
        return self.playerList

    def get_free_agents(self):
        faList = []
        for player in self.playerList:
            if(player.get_league_team() == 0):
                faList.append(player)
        return faList

    #No checking is happening, we need to check to make sure that all the steps that happen in this function actually happen
    #If dropping player 2 fails somehow, we have to be able to undo or not do the dropping of player 1
    def trade_player(self, player1, player2):
        team1 = self.teamList(self.find_team(player1.get_league_team()))
        team2 = self.teamList(self.find_team(player2.get_league_team()))

        team1.drop_player(player1)
        team2.drop_player(player2)
        team1.add_player(player2)
        team2.add_player(player1)

        return True

    #Sorts the team list from worst team (index 0) to best team
    def sort_team_list(self):
        self.teamList.sort(key = lambda x: x.eval_record(self.winWeight, self.tieWeight, self.lossWeight))
        return True

    #Adds a team to the team list
    def add_team(self, team):
        for tm in self.teamList:
            if tm.get_id() == team.get_id(): #Teams cannot have the same ID
                return(False)
            if tm.get_name() == team.get_name(): #Teams in the same league cannot have the same name
                return(False)
        self.teamList.add(team)
        return True
    
    def remove_team(self, team):
        index = 0
        for tm in self.teamList:
            if tm.get_id() == team.get_id():
                self.teamList.pop(index)
                return True
            index += 1
        return False
        
    def find_team(self, id):
        index = 0
        for tm in self.teamList:
            if tm.get_id() == id:
                return index
            index += 1
        return -1

    #Remove the last place team from the league
    def guillotine_team(self):
        self.sort_team_list()
        return self.remove_team(self.teamList(0))

    def set_scoring():
        return None

    def set_matchup(self, homeTeam, awayTeam, week):
        if self.find_team(homeTeam.get_id()) == -1 or self.find_team(awayTeam.get_id()) == -1:
            return False
        
        newMatchup = matchup(homeTeam, awayTeam, self.get_id(), week)

        return True
        
    def set_FAAB():
        return None

    def lock_waivers():
        return None

    def process_trade():
        return None

    def set_pirate():
        return None

    def set_vampire():
        return None

    def set_pirate_settings():
        return None

    def set_vampire_settings():
        return None

    def import_league(lid, ltype, uid, week = 1):
        try:
            league_api_endpoints.add_new_league(lid, ltype, week)
            league_api_endpoints.update_user(lid, uid, 1)
            return True
        except Exception:
            print(Exception)
            return Exception


