// Player's to be swapped
var player1;
var p1Row;
var button;
var player2;
var p2Row;

function swapPlayers(player) {
	if (player1 == null) {
		// Grab tr of player they wish to swap
		player1 = player;
		p1Row = document.getElementById(player["name"]);
		button = p1Row.getElementsByTagName("td")[4].firstChild;
		button.classList.add("Selected");
	}
	else {
		// Grab tr of second player they wish to swap
		player2 = player;
		p2Row = document.getElementById(player["name"]);

		// Swap their rows
		if (player1["position"] == player2["position"]) {
			let parent1 = p1Row.parentNode;
			let parent2 = p2Row.parentNode;
			let sibling2 = p2Row.nextElementSibling;

			parent1.insertBefore(p2Row, p1Row);
			parent2.insertBefore(p1Row, sibling2);

			let p1Pos = p1Row.getElementsByTagName("td")[0];
			let p2Pos = p2Row.getElementsByTagName("td")[0];

			if (p1Pos.innerText == "Bench") {
				p1Pos.innerText = p2Pos.innerText;
				p2Pos.innerText = "Bench";
			}
			else if (p2Pos.innerText == "Bench") {
				p2Pos.innerText = p1Pos.innerText;
				p1Pos.innerText = "Bench";
			}
		}

		player1 = null;
		player2 = null;
		button.classList.remove("Selected");
	}
}