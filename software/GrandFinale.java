import uk.ac.warwick.dcs.maze.logic.IRobot;
import java.util.ArrayList;
import java.util.Stack;

public class GrandFinale {

  private int pollRun = 0;    // Incremented after each pass
  private int explorerMode;   // 1 = explore, 0 = backtrack

  private Stack<Integer> stack = new Stack<Integer>();        // Create an initial stack
  private Stack<Integer> penStack = new Stack<Integer>();     // The penultimate stack
  private Stack<Integer> finalStack = new Stack<Integer>();   // The stack containing the final, direct route; penStack with required removals

  public void controlRobot(IRobot robot) {

    int direction = IRobot.AHEAD;

       /* On the first run; set the robot to explore */

       if ((robot.getRuns() == 0) && (pollRun == 0))
            explorerMode = 1;

       if (robot.getRuns() == 0) {         // Ensure that subsequent runs are direct to the target

              switch (countExits(robot, IRobot.WALL).size()) {         // Consider possible values for the number of surrounding walls

                case (4) : break;                                      // The robot is trapped
                case (3) : if (pollRun != 0)                           // The robot is at a deadend
                               explorerMode = 0;
                           direction = deadEnd(robot);
                           break;
                case (2) : direction = corridor(robot);                // The robot is in a corridor
                           break;
                default  : if (explorerMode == 1)                      // The robot is at a junction; consider the current
                               direction = exploreControl(robot);      // status of explorerMode.
                           else
                               direction = backtrackControl(robot);

                }

           robot.face(direction);    // Face the robot in this direction

          if (explorerMode == 1) {   // Populate the stack with the robot's heading

                  penStack.push(robot.getHeading());
              }
        else {                                                     // If the stack is not empty, remove and return the top element
              if (!penStack.isEmpty())
                   penStack.pop();
                   }

                   /* Print the stack route to console */

                   String headings = ("\n"+penStack);

                   String route    = headings.replace("1000", "N")
                                             .replace("1001", "E")
                                             .replace("1002", "S")
                                             .replace("1003", "W");

                   System.out.println(route);

        }

       /* On subsequent runs; set the robot's to correspond with the elements of finalStack */

       if (robot.getRuns() != 0) {
           //explorerMode = 0;
           if (finalStack.isEmpty()) {
               while (!penStack.isEmpty()) {
                       finalStack.push(penStack.pop());
                       }
               }

             robot.setHeading(finalStack.pop());

           }

        pollRun++; // Increment pollRun so that the data is not reset each time the robot moves

    }

    /**
     * Create an ArrayList that stores integers; add exits (general term for surrounding squares,
     * relative to the robot) to the ArrayList
     */

    private ArrayList<Integer> countExits(IRobot robot, int exit) {
            ArrayList<Integer> exits = new ArrayList<Integer>();
                          for (int i = IRobot.AHEAD; i <= IRobot.LEFT; i++)

                           if (robot.look(i) == exit)
                               exits.add(i);

                   return exits;

            }

    private int deadEnd(IRobot robot) {

       int direction = IRobot.AHEAD;
       int randno;

            do {

                /* Select a random number with equal probability */

                randno = (int) (Math.random()*(4));

                /* Convert this to a direction */

                    if (randno == 1)
                      direction = IRobot.AHEAD;
               else if (randno == 2)
                      direction = IRobot.RIGHT;
               else if (randno == 3)
                      direction = IRobot.BEHIND;
               else
                      direction = IRobot.LEFT;
               }

                while (robot.look(direction) == IRobot.WALL); 

            return direction;

       }

    private int exploreControl(IRobot robot) {

      return junction(robot);   // All cases are handled under the junction method

      }

    private int backtrackControl(IRobot robot) {

        ArrayList<Integer> passages = countExits(robot, IRobot.PASSAGE);    // Declare the ArrayList passages as
                                                                            // the number of surrounding passages.
        if (passages.size() > 0) {

            /* If there is a positive integer value of passages, set explorerMode to 1 */

            explorerMode = 1;

            /**
             * Return a pseudo-random number based on the size of the passages array, return the largest
             * integer less than or equal to this number, then retrieve the value at this numerical position of the array
             */

            return passages.get((int) Math.floor(Math.random() * passages.size()));

            }

        int arrived  = stack.pop();      // Remove and return the value at the top of the stack, i.e., the direction the robot arrived from
            arrived -= 2;                // 'Decrease by 2', that is, reverse the robot's headed direction.

            if (arrived  < IRobot.NORTH) // Ensure arrived has a value within the range 1000--1003 (NORTH--WEST). Cases where arrived < 996 are impossible.
                arrived += 4;

            robot.setHeading(arrived);

          return IRobot.AHEAD;

        }

    private int corridor(IRobot robot) {

       int direction = IRobot.AHEAD;

                 if (robot.look(IRobot.AHEAD) != IRobot.WALL)
                     direction = IRobot.AHEAD;
            else if (robot.look(IRobot.LEFT) != IRobot.WALL)
                     direction = IRobot.LEFT;
            else if (robot.look(IRobot.RIGHT) != IRobot.WALL)
                     direction = IRobot.RIGHT;

          return direction;

       }

    private int junction(IRobot robot) {

       int direction = IRobot.AHEAD;
       int randno;

                 if (countExits(robot, IRobot.BEENBEFORE).size() <= 1)    // Iff less than or equal to 1, the robot has not
                                                                          // previously visited the junction.
                     stack.push(robot.getHeading());                      // Pushes robot's heading to the top of the stack

                 if (countExits(robot, IRobot.PASSAGE).size() == 0) {     // If there are no passage exits
                     do {

                         /* Select a random number with equal probability */

                         randno = (int) (Math.random()*(3));

                         /* Convert this to a direction */

                             if (randno == 1)
                               direction = IRobot.AHEAD;
                        else if (randno == 2)
                               direction = IRobot.RIGHT;
                        else
                               direction = IRobot.LEFT;
                        }

                         while (robot.look(direction) == IRobot.WALL);

                     }

              else {
                     do {
                          do {

                              /* Select a random number with equal probability */

                              randno = (int) (Math.random()*(3));

                              /* Convert this to a direction */

                                  if (randno == 1)
                                    direction = IRobot.AHEAD;
                             else if (randno == 2)
                                    direction = IRobot.RIGHT;
                             else
                                    direction = IRobot.LEFT;
                             }

                              while (robot.look(direction) == IRobot.WALL);

                        }

                         while (robot.look(direction) == IRobot.BEENBEFORE);

                   }

          return direction;

       }

    /* Method that returns the number of beenBefore exits */

    private int beenbeforeExits(IRobot robot) {
          return (countExits(robot, IRobot.BEENBEFORE).size());
          }

    public void reset() { explorerMode = 1; }

  }