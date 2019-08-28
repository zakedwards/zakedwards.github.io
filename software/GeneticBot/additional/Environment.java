/**
 * A class for initialising and generating a Robocode environment.
 * We base our initialisation on an application, authored by Flemming N. Larsen, that runs a battle in Robocode
 * for 5 rounds on the default battlefield.
 *   - Available at: http://robocode.sourceforge.net/docs/robocode/robocode/control/package-summary.html
 *
 *
 * @author:  Zak Edwards
 * @version: 1.0 25/03/16
 */

import robocode.*;
import robocode.control.*;
import robocode.control.events.*;
import robocode.Event;
import robocode.ScannedRobotEvent;
import robocode.BattleResults;

public class Environment {

    /* ------------------------- Initialisation of the Robocode environment ------------------------- */
    RobocodeEngine engine;
    BattleObserver observer;
    BattlefieldSpecification specification;

    public Environment() {
        engine        = new RobocodeEngine(new java.io.File("/opt/robocode"));
        observer      = new BattleObserver();
        specification = new BattlefieldSpecification(800, 600);

        engine.addBattleListener(observer);
        engine.setVisible(false);
    }

    public double[] exec(String iterations[], String opponents[], int rounds) {
        double fitnesses[] = new double[iterations.length];
        String iteration, opponent;
        BattleResults[] results;

        engine = new RobocodeEngine(new java.io.File("/opt/robocode"));

        for (int i = 0; i < iterations.length; i++) {
            double totalMeasure = 0;
            for (int j = 0; j < opponents.length; j++) {
                iteration = iterations[i];
                opponent  = opponents[j];

                RobotSpecification[] rSpecification = engine.getLocalRepository(iteration + ", " + opponent);
                BattleSpecification  bSpecification = new BattleSpecification(rounds, specification, rSpecification);
                engine.runBattle(bSpecification, true);

                results = observer.getResults();

                int agent = (results[0].getTeamLeaderName().equals(iterations[i]) ? 0 : 1);
                int rival = ((agent == 1) ? 0 : 1);
                int score = results[agent].getScore();
                double total   = (score + results[rival].getScore());    // the sum of the agent's and opponents' scores
                double measure = (0.1 + score) / (0.1 + total);          // The fitness measure for each round

                totalMeasure += measure;
            }
            fitnesses[i] = (totalMeasure / opponents.length);
        }
        return fitnesses;
    }
}

class BattleObserver extends BattleAdaptor {

    /* -------------------- Battle listener for handling the environmental event -------------------- */
    robocode.BattleResults[] results;

    // Called when the battle is completed successfully with battle results
    public void onBattleCompleted(BattleCompletedEvent e) {
        results = e.getIndexedResults();

        System.out.println("-- Battle has completed --");

        // Print out the sorted results with the robot names
        System.out.println("Battle results:");
        for (robocode.BattleResults result : e.getSortedResults()) {
            System.out.println("  " + result.getTeamLeaderName() + ": " + result.getScore());
        }
    }

    // Called when the game sends out an information message during the battle
    public void onBattleMessage(BattleMessageEvent e) {
        System.out.println("Msg> " + e.getMessage());
    }

    // Called when the game sends out an error message during the battle
    public void onBattleError(BattleErrorEvent e) {
        System.out.println("Err> " + e.getError());
    }

    public BattleResults[] getResults() {
        return results;
    }
}