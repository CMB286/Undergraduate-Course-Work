//Credit to mkreiser for example script

const _ = require('lodash');
//const { Client } = require('./node-dev');
const { Client } = require("espn-fantasy-football-api/node");

const myClient = new Client({
  //Using random league Keyon owns. It is private, which means you need the espnS2 and SWID seeds. A public league will not need this
  leagueId:1151092,
  espnS2: "AECAlZGGZ%2BKVVazn84JPRPKlzGn8c0h0mgcW%2BmMD7rZbRi7Eo%2FD0AftzOhmp%2Bp2OXGN4jU37nau7amjPktismejCclO5Nw2itHBLxlMp5ho0TL9dQYfP8ElaaVYyi8r1rL5VfDEz9ZvVDKtNE2kqhfi5yeNP5G4H75oQV7I6j4oIqUTPdvXSW6KExsDka6tNgzr5Nlrbo1VLR2v7wqsUwnPHjL2y930EX9xhSzVCvN7Nhq47eb%2B%2FTed93BQHkkt1zSj0pk6BF0cQZVSWymyq5%2BSV",
  SWID:"D3FB8C91-2170-4F49-9BF1-CFF8AABC99A1"
  
});

class Psychic {
  static filterPosition(boxscorePlayer, position) {
    return (
      boxscorePlayer.position === position ||
      _.includes(boxscorePlayer.player.eligiblePositions, position)
    );
  }

  static handleNonFlexPosition(lineup, position) {
    const players = _.filter(lineup, (player) => this.filterPosition(player, position));
    const sortedPlayers = _.sortBy(players, ['totalPoints']);
    return _.last(sortedPlayers);
  }

  static analyzeLineup(lineup, score) {
    let bestSum = 0;
    const bestRoster = [];
    let numChanges = 0;

    const bestQB = this.handleNonFlexPosition(lineup, 'QB')
    bestRoster.push(bestQB.player.fullName);
    bestSum += bestQB.totalPoints;
    if (bestQB.position === 'Bench') {
      numChanges += 1;
    }

    const bestDefense = this.handleNonFlexPosition(lineup, 'D/ST')
    bestRoster.push(bestDefense.player.fullName);
    bestSum += bestDefense.totalPoints;
    if (bestDefense.position === 'Bench') {
      numChanges += 1;
    }

    const bestKicker = this.handleNonFlexPosition(lineup, 'K')
    bestRoster.push(bestKicker.player.fullName);
    bestSum += bestKicker.totalPoints;
    if (bestKicker.position === 'Bench') {
      numChanges += 1;
    }


    const flexPlayers = _.filter(lineup, (player) => this.filterPosition(player, 'RB') ||
      this.filterPosition(player, 'WR') ||
      this.filterPosition(player, 'TE')
    );
    const sortedFlexPlayers = _.sortBy(flexPlayers, ['totalPoints']);

    const flexPos = { RB: 2, WR: 2, TE: 1, FLEX: 1 };

    while (_.sum(_.values(flexPos)) && !_.isEmpty(sortedFlexPlayers)) {
      const player = sortedFlexPlayers.pop();
      const acceptPlayer = () => {
        bestRoster.push(player.player.fullName);
        bestSum += player.totalPoints;
        if (player.position === 'Bench') {
          numChanges += 1;
        }
      }

      if (flexPos.RB && _.includes(player.player.eligiblePositions, 'RB')) {
        acceptPlayer();
        flexPos.RB -= 1;
      } else if (flexPos.WR && _.includes(player.player.eligiblePositions, 'WR')) {
        acceptPlayer();
        flexPos.WR -= 1;
      } else if (flexPos.TE && _.includes(player.player.eligiblePositions, 'TE')) {
        acceptPlayer();
        flexPos.TE -= 1;
      } else if (flexPos.FLEX) {
        acceptPlayer();
        flexPos.FLEX -= 1;
      }
    }

    return {
      bestSum,
      bestRoster,
      currentScore: score,
      numChanges
    };
  }

  static runForWeek({ seasonId, matchupPeriodId, scoringPeriodId }) {
    const bestLineups = {};
    return myClient.getBoxscoreForWeek({ seasonId, matchupPeriodId, scoringPeriodId }).then((boxes) => {
      _.forEach(boxes, (box) => {
        bestLineups[box.awayTeamId] = this.analyzeLineup(box.awayRoster, box.awayScore);
        bestLineups[box.homeTeamId] = this.analyzeLineup(box.homeRoster, box.homeScore);
      });

      return bestLineups;
    });
  }
}

Psychic.runForWeek({ seasonId: 2019, matchupPeriodId: 4, scoringPeriodId: 4 }).then((result) => {
  console.log(result);
  return result;
});
