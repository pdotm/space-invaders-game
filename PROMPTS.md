## Prompt 1 (seed prompt):
Prompt used: I'm building Space Invaders in Java using Swing, split into three files: GameModel.java, GameView.java, and GameController.java. GameView should extend JPanel and be hosted in a JFrame. GameController should have the main method and wire the three classes together. GameModel must have no Swing imports. For now, just create the three class shells with placeholder comments describing what each class will do. The program should compile and open a blank window.

Result: This prompt made a blank window open and created the minimal version of three classes to create the MVC skeleton.

Fixes: AI set the width of the window to 800. I changed that width to 600 to match the height.

Observation: AI added comments to each file giving a brief summary of its purpose. I think this is a good idea because later it could help me or AI better understand the code.

## Prompt 2 (seed prompt):
Prompt used: Fill in GameModel.java. The model should track: the player's horizontal position, the alien formation (5 rows of 11), the player's bullet (one at a time), alien bullets, the score, and lives remaining (start with 3). Add logic to: move the player left and right, fire a player bullet if one isn't already in flight, advance the player's bullet each tick, move the alien formation right until the edge then down and reverse, fire alien bullets at random intervals, and detect collisions between bullets and aliens or the player. No Swing imports.

Result: AI built the game logic in the GameModel file and added Alien and Bullet classes for those objects.

Fixes: AI added a bunch of comments to the code. Some were helpful, but a few were not needed, so I removed them.

Observation: AI made all state fields private and added getters for them. Great to see that it chose this safer approach without even needing directions for it in the prompt.

## Prompt 3 (seed prompt):
Prompt used: Fill in GameView.java. It should take a reference to the model and draw everything the player sees: the player, the alien formation, both sets of bullets, the score, and remaining lives. Show a centered game-over message when the game ends. The view should only read from the model — it must never change game state.

Result: This prompt made the game window show the aliens, the game score, and the lives remaining.

Fixes: No fixes needed for this code.

Observation: AI added some fonts like this: new Font(Font.SANS_SERIF, Font.BOLD, 42). Worth asking if there is a way to define fonts as veriables so that we can just use that variable in all places.

## Prompt 4 (seed prompt):
Prompt used: Fill in GameController.java. Add keyboard controls so the player can move left and right with the arrow keys and fire with the spacebar. Add a game loop using a Swing timer that updates the model each tick and redraws the view. Stop the loop when the game is over.

Result: This prompt made the game run. This included making the aliens move and fire bullets, and allowing the player to move and fire bullets.

Fixes: The player ship was out of view and could not fire more than once. Both of these bugs were fixed with prompt 6.

Observation: AI defined the TICK_MS constant but only used it in one place. Worth asking why it defined this constant and if it is needed.

## Prompt 5 (seed prompt):
Prompt used: Create a separate file called ModelTester.java with a main method. It should create a GameModel, call its methods directly, and print PASS or FAIL for each check. Write tests for at least five behaviors: the player cannot move past the left or right edge, firing while a bullet is already in flight does nothing, a bullet that reaches the top is removed, destroying an alien increases the score, and losing all lives triggers the game-over state. No testing libraries — just plain Java.

Result: Created a test file and added tests to it.

Fixes: No fixes needed for this code.

Observation: AI used a boolean named "ok" in a lot of tests. Worth asking if this is the naming convention for Java tests that I should use too when coding.

## Prompt 6:
Prompt used: The player spaceship is not visible within the window. Additionally, I cannot fire more than one bullet, even after my first bullet has dissappeared after hitting an alien ship. Find the cause of these bugs and fix.

Result: This prompt fixed both bugs that were described, making the player visible and fixing the firing functionality.

Fixes: No fixes needed for this code.

Observation: AI used the frame.pack(); method to make all the things that are drawn go into the frame. Worth asking how this method works.

## Prompt 7: 
Prompt used: When aliens reach the bottom of the screen, they keep moving down, making it impossible for the game to end. To fix this, when an alien reaches the bottom of the screen, subtract a life and remove that alien to guarantee the game always ends. Do not let lives go below 0.

Result: This prompt made the player lose 1 life when an alien got to the bottom of the screen, which guarantees that the game gets to a terminal state if aliens get to the bottom of the screen.

Fixes: This prompt did not work as I expected because it allowed the lives count to go below 0. Initially, the prompt did not have the last sentence, causing lives to go below 0 when many aliens touched the bottom in the same frame. To fix this, I undid the changes the initial prompt made, added the last sentence to the prompt, and reran the prompt.

Observation: AI used this when updating the lives count: Math.max(0, lives - 1). I like this approach because it is simpler than an if statement, and I will keep it in mind for similar problems in the future.

## Prompt 8:
Prompt used: Restyle the player ship and the alien ships. Change their shape to make them look more like ships rather than squares. Keep their size the same and keep the player ship bigger than the alien ships. Make the background look more like space by adding stars.

Result: Both the player's ship and the aliens changed shape and style to look more like what they represent, rather than just rectangles.

Fixes: I did not like the initial coloring too much, so I changed the aliens to the built in green color and the spaceship to cyan. I also removed some of the comments AI added that were unnecessary.

Observation: I noticed that some new Color() calls had a fourth parameter. I asked AI what this parameter does and it explained that it was for controlling opacity.

## Prompt 9:
Prompt used: Add a powerup that appears 10 seconds into the game. It should be in a place the player can fire at (not behind aliens). If a player bullet hits the powerup, the player gets it. It should make the player ship fire three bullets at a time for the rest of the game.

Result: This prompt added the tripe shot power up. When aquired, the player ship fires three bullets at a time for the rest of the game.

Fixes: I changed the POWERUP_SPAWN_TICK constant from 625 to 500 to make the powerup spawn a little earlier in the gameplay.

Observation: AI is creating and utilizing constants it created earlier a lot. This seems like a good pattern for improving changeability that I should follow when coding on my own.

## Prompt 10:
Prompt used: Change the game not to start automatically. When the window opens, a high score screen should be shown with the high score and a start button. This screen should also be shown after the game ends, with a special message if the high score is broken.

Result: A high score screen with the game title and a start button is shown when the program starts after every game, updating the high score if it is beaten. 

Fixes: No fixes needed for this code.

Observation: AI removed the "Game Over" overlay when it became unused with these changes. It is awesome that it identified that aspect of the task and did the cleanup without instructions to do it in the prompt.