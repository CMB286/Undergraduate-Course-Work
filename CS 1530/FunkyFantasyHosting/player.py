import team, bigquery_fun

class Player:
    def __init__(self, player_df, row):
        if(len(row)!=15):
            row = row.iloc[0];
        self.id = player_df.loc['player_id']
        self.name = player_df.loc['player_name']
        self.nflTeam = None
        self.leagueTeam = player_df.loc['team_id']#The ID of the league team
        self.leagueStatus = player_df.loc['player_status']
        self.fantasyMetrics = None
        self.injuryStatus = player_df.loc['player_inj_status']
        self.playerPos = row.loc['pos']
        self.playerRankings = None
        self.leagueTransactionHistory = None
        self.fantasyPoints = None
        self.news = None
        self.link = row.loc['player_overview']
        self.playerBio = None
        self.vunerableFlag = None
        self.headshot = row.loc['player_headshot']

    def __repr__(self):
        return None

    def get_id(self):
        return self.id

    def get_name(self):
        return self.name

    def get_league_team(self):
        return self.leagueTeam
    
    def get_link(self):
        return self.link

    def get_headshot(self):
        return self.headshot

    def get_pos(self):
        return self.playerPos

    def set_league_team(self, team):
        self.leagueTeam = team.get_id()
        return True