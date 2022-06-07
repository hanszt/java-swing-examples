# Minesweeper
A Java implementation of Minesweeper complete with custom graphics, game saving/loading, and command line parameters. Originally created for assignment 1.6 of Computer Science 2 at Oklahoma State University for Dr. Crick. The goal of the project was to create a game of Minesweeper with a full GUI following the MVC design pattern. Other requirements included facilitating the saving and loading of games and a difficulty selection menu.

## Building
This project assumes that your .class files will be placed in a folder at the same level as the assets folder. For instance, I have a "Minesweeper" folder which contains "assets", "src", and "build" folders. Both "assets" and "src" are exactly as they are here on GitHub and the "build" folder contains all of the .class files. To achieve this same file structure you can simply navigate to the “src” folder in a command prompt and type the following:
~~~
javac Minesweeper.java -d "..\build"
~~~

This hierarchy is due to the fact that the "Tile" class searches up one directory relative from its current location for the "assets" folder for the images contained within. If you wish to change the location of the assets and thus negate the need for a "build" folder then you must update the constants "MINE_IMG_PATH" and "FLAG_IMG_PATH" at lines 22 and 23 in Tile.java.

## Running
Minesweeper must be launched from the command line. To get a basic game running simply navigate to the Minesweeper.class file and run:
~~~
java Minesweeper
~~~
The game also supports several command line parameters to allow for manual game setup. To see a list of available options type:
~~~
java Minesweeper -help
~~~
To set the number of tiles wide and tall the game will be use the "numTiles" option followed by the number of tiles. For example, the following creates a 5x5 tile game:
~~~
java Minesweeper -numTiles 5
~~~
To set the probability of a tile containing a mine (and by extension the difficulty) use the "mineProb" option followed by the desired probability. For example, the following creates a game with a 50% probability of tiles containing mines:
~~~
java Minesweeper -mineProb 0.5
~~~
You may also wish to enter a custom random seed to allow for games to be replayed using the "seed" option. Two games with identical mine probabilities and number of tiles will place mines in the same locations if the same random seed is used on both. For example, launching the game with the following will always place mines in the same locations (provided the game is always played on the same computer):
~~~
java Minesweeper -seed 100
~~~
You may also combine several commands for a completely custom game. For example, the following produces a very hard 40x40 game with a 60% probability of a tile containing a mine and the random seed "256":
~~~
java Minesweeper -numTiles 40 -mineProb 0.6 -seed 256
~~~

## Starting a New Game
Clicking the "New" button at the top-left of the screen will present you with three difficulties. Each difficulty adds more tiles and increases the probability for tiles to contain mines.

## Playing Minesweeper
Once you have the game built and running you may wish to familiarize yourself with the game of Minesweeper. For those who have never played, the objective is to reveal all the "?" tiles without revealing a mine. Tiles are revealed by left-clicking the desired tile. A tile is considered "safe" if it does not contain a mine. If the tile is "safe" then it will indicate the number of hidden mines adjacent to it. Tiles with no adjacent mines will automatically reveal all adjacent tiles that are not mines. If the tile clicked does contain a mine then the game is over and the entire board is revealed to show the locations of mines.

To aid your memory you may place "flags" on tiles you suspect to contain mines. To place or remove a flag simply right-click a tile. Placing a flag will not reveal a tile and will not end the game if a tile contains a mine. You may only place one flag for each mine on the field (the number of mines is indicated in the lower left). If all flags are placed then you must remove one or more before placing another.

The game is won when all "safe" tiles have been revealed. Tiles containing mines do not have to be flagged to win. After a victory or loss your total time will be displayed at the top of the screen.

## Saving/Loading a Game
To save the current game simply click the "Save" button and enter a name for your saved game. To load a game click "Load" and enter the name of the game to load. Be warned that saving and loading a game may be viewed as cheating since it allows you to reveal the field, take note of mine positions, reload, and try again.

## Exiting
Simply click either the "Quit" button or the red button on the window to exit. Note that progress will not automatically be saved.

© Copyright 2015 Charles Duncan (CharlesETD@gmail.com)

## Source
- [Mine sweeper](https://github.com/charlesetd/Minesweeper)
