import java.util.*;

public class CoffeeMakerQuestImpl implements CoffeeMakerQuest {

	// TODO: Add more member variables and methods as needed.
	Player player;
	Room curRoom;
	LinkedList<Room> game;
	boolean gameOver;
	
	CoffeeMakerQuestImpl() {
		// TODO
		player = new Player();
		game = new LinkedList<Room>();
		curRoom = null;
		gameOver = false;
	}

	/**
	 * Whether the game is over. The game ends when the player drinks the coffee.
	 * 
	 * @return true if successful, false otherwise
	 */
	public boolean isGameOver() {
		// TODO
		return gameOver;
	}

	/**
	 * Set the player to p.
	 * 
	 * @param p the player
	 */
	public void setPlayer(Player p) {
		// TODO
		player = p;
	}
	
	/**
	 * Add the first room in the game. If room is null or if this not the first room
	 * (there are pre-exiting rooms), the room is not added and false is returned.
	 * 
	 * @param room the room to add
	 * @return true if successful, false otherwise
	 */
	public boolean addFirstRoom(Room room) {
		// TODO
		if(room == null || !game.isEmpty()) {
			return false;
		}
		
		game.add(room);
		return true;
	}

	/**
	 * Attach room to the northern-most room. If either room, northDoor, or
	 * southDoor are null, the room is not added. If there are no pre-exiting rooms,
	 * the room is not added. If room is not a unique room (a pre-exiting room has
	 * the same adjective or furnishing), the room is not added. If all these tests
	 * pass, the room is added. Also, the north door of the northern-most room is
	 * labeled northDoor and the south door of the added room is labeled southDoor.
	 * Of course, the north door of the new room is still null because there is
	 * no room to the north of the new room.
	 * 
	 * @param room      the room to add
	 * @param northDoor string to label the north door of the current northern-most room
	 * @param southDoor string to label the south door of the newly added room
	 * @return true if successful, false otherwise
	 */
	public boolean addRoomAtNorth(Room room, String northDoor, String southDoor) {
		// TODO
		if(room == null || northDoor == null || southDoor == null || game.isEmpty()) {
			return false;
		}
		
		for(int i = 0; i < game.size(); i++) {
			if(room.getFurnishing().equals(game.get(i).getFurnishing()) || room.getAdjective().equals(game.get(i).getAdjective())) {
				return false;
			}
		}
		
		game.getLast().setNorthDoor(northDoor);
		room.setSouthDoor(southDoor);
		game.add(room);
		
		return true;
	}

	/**
	 * Returns the room the player is currently in. If location of player has not
	 * yet been initialized with setCurrentRoom, returns null.
	 * 
	 * @return room player is in, or null if not yet initialized
	 */ 
	public Room getCurrentRoom() {
		// TODO
		return curRoom;
	}
	
	/**
	 * Set the current location of the player. If room does not exist in the game,
	 * then the location of the player does not change and false is returned.
	 * 
	 * @param room the room to set as the player location
	 * @return true if successful, false otherwise
	 */
	public boolean setCurrentRoom(Room room) {
		// TODO
		if(!game.contains(room)) {
			return false;
		}
		curRoom = room;
		return true;
	}
	
	/**
	 * Get the instructions string command prompt. It returns the following prompt:
	 * " INSTRUCTIONS (N,S,L,I,D,H) > ".
	 * 
	 * @return command prompt string
	 */
	public String getInstructionsString() {
		// TODO
		return " INSTRUCTIONS (N,S,L,I,D,H) > ";
	}
	
	/**
	 * Processes the user command given in String cmd and returns the response
	 * string. For the list of commands, please see the Coffee Maker Quest
	 * requirements documentation (note that commands can be both upper-case and
	 * lower-case). For the response strings, observe the response strings printed
	 * by coffeemaker.jar. The "N" and "S" commands potentially change the location
	 * of the player. The "L" command potentially adds an item to the player
	 * inventory. The "D" command drinks the coffee and ends the game. Make
     * sure you use Player.getInventoryString() whenever you need to display
     * the inventory.
	 * 
	 * @param cmd the user command
	 * @return response string for the command
	 */
	public String processCommand(String cmd) {
		// TODO
		cmd = processCommandInternal(cmd);
		
		return cmd;
	}
	
	private String processCommandInternal(String cmd) {
		int curIndex;
		cmd = cmd.toLowerCase();
		
		switch(cmd) {
		case "s":
			curIndex = game.indexOf(curRoom);
			if(curIndex - 1 >= 0) {
				curRoom = game.get(curIndex - 1);
				cmd = "";
			}
			else {
				cmd = "A door in that direction does not exist.\n";
			}
			
			return cmd;
		case "n":
			curIndex = game.indexOf(curRoom);
			if(curIndex + 1 < game.size()) {
				curRoom = game.get(curIndex + 1);
				cmd = "";
			}
			else {
				cmd = "A door in that direction does not exist.\n";
			}
			
			return cmd;
		case "l":
			if(curRoom.getItem() == Item.NONE) {
				cmd = "You don't see anything out of the ordinary.\n";
			}
			else {
				cmd = "There might be something here...";
			}
	
			if(curRoom.getItem().equals(Item.COFFEE)) {
				player.addItem(Item.COFFEE);
				cmd += "\nYou found some caffeinated coffee!\n";
			}
			else if (curRoom.getItem().equals(Item.SUGAR)) {
				player.addItem(Item.SUGAR);
				cmd += "\nYou found some sweet sugar!\n";
			}
			else if(curRoom.getItem().equals(Item.CREAM)) {
				player.addItem(Item.CREAM);
				cmd += "\nYou found some creamy cream!\n";
			}
			
			return cmd;
		case "d":
			gameOver = true;
		   
			cmd = player.getInventoryString() + "\n";
			
			if(player.checkCream() && player.checkCoffee() && player.checkSugar()) {
				cmd += "You drink the beverage and are ready to study!\n"
						+ "You win!\n";
				return cmd;
			}
			if(player.checkCream() && !player.checkCoffee() && !player.checkSugar()) {
				cmd += "You drink the cream, but without caffeine, you cannot study.\n"
						+ "You lose!\n";
				return cmd;
			}
			
			if(player.checkCream() && !player.checkCoffee() && player.checkSugar()) {
				cmd += "You drink the sweetened cream, but without caffeine you cannot study.\n"
						+ "You lose!\n";
				return cmd;
			}
			
			if(player.checkCream() && player.checkCoffee() && !player.checkSugar()) {
				cmd += "Without sugar, the coffee is too bitter. You cannot study.\n"
						+ "You lose!\n";
				return cmd;
			}
			
			if((!player.checkCream() && player.checkCoffee() && !player.checkSugar()) || (!player.checkCream() && player.checkCoffee() && player.checkSugar())) {
				cmd += "Without cream, you get an ulcer and cannot study.\n"
						+ "You lose!\n";
				return cmd;
			}
			
			if(!player.checkCream() && !player.checkCoffee() && player.checkSugar()) {
				cmd += "You eat the sugar, but without caffeine, you cannot study.\n"
						+ "You lose!\n";
				return cmd;
			}
			
			cmd += "You drink the air, as you have no coffee, sugar, or cream.\n"
					+ "The air is invigorating, but not invigorating enough. You cannot study.\n"
					+ "You lose!\n";
			
			return cmd;
		case "i":
			cmd = player.getInventoryString();
			return cmd;
		case "h":
			cmd = "N - Go north\nS - Go south\nL - Look and collect any items in the room\nI - Show inventory of items collected\nD - Drink coffee made from items in inventory\n";
			return cmd;
		}
		
		return "What?\n"; 
	}
	
}
