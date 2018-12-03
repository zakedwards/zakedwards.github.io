/**
 * The main genetic algorithm, in addition to a programmatic initialisation of the Robocode environment.
 * We assume Robocode is installed at /opt/robocode; all files are written to the "/opt/robocode/robots/sampleex" directory.
 *
 *
 * @author:  Zak Edwards
 * @version: 1.0 25/03/16
 */


import java.util.Random;
import java.io.FileWriter;
import java.io.IOException;

public class GeneticBot {

    /* -------------------------- Implementation of the genetic algorithm -------------------------- */

    /**
     *  Store references all competing robots in an array.
     */
    final static String[] opponents = {
        //"sample.SuperCrazy",
        //"sample.SuperTracker"
        //"sample.SuperTrackFire",
        "sample.SuperRamFire",
        //"ary.micro.Weak 1.2"
        //"sheldor.nano.Sabreur_1.1.1"
        //"sample.Sabreur"
        //"mld.LittleBlackBook_1.69e"
        //"mld.Moebius_2.9.3"
    };

    /**
     * Declare global variables, characterising the behaviour of the algorithm/environment.
     */
    final static int
        popSize = 10,   // Determines how many bots are compiled
        maxGen  = 400,  // Maximum number of generations of the genetic algorithm

        rounds  = 30,   // The number of rounds to be played against the specified opponents
        trnSize = 4;    // The size of the 'tournament'; arbitrary chromosomes from the population

    static double
        pCrossover   = 0.87,  // Probability of crossover
        pMutation    = 0.03,  // Probability of mutation
        pReplication = 0.10,  // Probability of replication

        fitnesses[]     = new double[popSize],  // Store all chromosomes' fitnesses
        meanFitnesses[] = new double[maxGen],   // Store the mean fitnesses for each generation

        meanNumNodes[]  = new double[maxGen];   // Store the mean number of nodes for each generation

    static String iterations[] = new String[popSize];  // Store each recently generated chromosome
    static int generation = 0, competitor;             // Initialise the generation counter; record each generation's optimal phenotype
    static Random rand;

    static Phenotype
        solution,                                      // (Current) optimal phenotype
        phenome[]       = new Phenotype[maxGen],       // Candidate solutions; the evolved chromosomes
        population[]    = new Phenotype[popSize],
        newPopulation[] = new Phenotype[popSize];

    /**
     * MAIN METHOD.
     */
    public static void main(String args[]) {

        solution = new Phenotype(-1, 0);
        solution.fitness = 0;

        System.out.println("Initialising the population...");
        initPopulation();
        compPopulation();
        System.out.println("Initialisation completed.");

        double totalFitness, meanFitness, meanNumNode;  // Generation-relative fitness total; fitness and number-of-nodes averages

        while (generation < maxGen) {
            for (int i = 0; i < popSize; i++)
                iterations[i] = population[i].path;
            populateFitnesses(opponents);
            competitor = 0;
            totalFitness = meanFitness =  meanNumNode = 0;
            for (int i = 0; i < popSize; i++) {
                totalFitness += (population[i].fitness = fitnesses[i]);
                if (population[i].fitness > population[competitor].fitness)
                    competitor = i;
                meanNumNode += population[i].countNodes();
            }
            meanFitness = (totalFitness/popSize);
            meanFitnesses[generation] = meanFitness;
            meanNumNodes[generation]  = (meanNumNode /= popSize);
            phenome[generation]      = population[competitor];
            if (population[competitor].fitness > solution.fitness)
                solution = population[competitor];
            System.out.println(
                "\nRound " + generation + "."
              + "\nMean fitness: " + meanFitness
              + "\nMean no. of nodes: " + meanNumNodes[generation] + "(" + phenome[generation].numNodes  + "nodes)"
              + "\nLEADING ROBOT: In round:" + phenome[generation].iteration + " - " + phenome[generation].fitness
                             + "\tIn tournament:" + solution.iteration + " - " + solution.fitness + "\n");
            writeFile(generation, meanFitness, population[competitor].fitness, meanNumNode, population[competitor].numNodes, population[competitor].path);
            generation++;
            System.out.println("Breeding the next generation...");
            breedPopulation();
            population = newPopulation;
            newPopulation = new Phenotype[popSize];
            compPopulation();
            Phenotype.clear((generation - 1), popSize, phenome[generation - 1].id);
        }

        for (int i = 0; i < generation; i++)
            System.out.println(
                "Round " + i + "."
              + "\nMean fitness: " + meanFitnesses[i]
              + "\tMean no. of nodes: " + meanNumNodes[i]
              + "(" + phenome[i].numNodes + "nodes)");

    }

    /**
     * Generate the Robocode environment and populate an array with mean fitnesses
     * for each iteration.
     */
    private static void populateFitnesses(String[] rivals) {
        Environment env = new Environment();
        fitnesses = env.exec(iterations, rivals, rounds);
    }

    /**
     * An implementation of the widely-documented 'Tournament Selection' algorithm.
     */
    private static int tournamentSelection() {
        int tournament[] = new int[trnSize];
        int fittest = tournament[0];
        for (int i = 0; i < trnSize; i++) {
            tournament[i] = rand.nextInt(popSize);
            if (i != 0) {
                if (population[tournament[i]].fitness > population[fittest].fitness)
                    fittest = tournament[i];
            }
        }
        return fittest;
    }

    /**
     * Methods for initialising, compiling and breeding the population.
     */
    private static void initPopulation() {
        for (int i = 0; i < popSize; i++) {
            population[i] = new Phenotype(0, i);
            population[i].init();
        }
    }

    private static void compPopulation() {
        for (Phenotype agent : population) {
            agent.construct();
            agent.compile();
        }
    }

    private static void breedPopulation() {

        int i = 2;
        double rnd;

        newPopulation[0] = phenome[generation - 1].replicate(generation, 0);    // Optimal chromosome in completed round
        newPopulation[1] = solution.replicate(generation, 1);                   // Optimal chromosome in population, thus far

        while (i < popSize) {
            rnd = rand.nextDouble();
            if ((rnd -= pCrossover) <= 0) {
                int P1, P2;
                P1 = P2 = tournamentSelection();
                System.out.println("Commencing crossover of agents " + P1 + " and " + P2 + " into " + i + ".");
                newPopulation[i] = population[P1].crossover(population[P2], generation, i);
                //newPopulation[i] = population[tournamentSelection()].crossover(population[tournamentSelection()], (generation + 1), i);
            } else if ((rnd -= pMutation) <= 0) {
                System.out.println("Commencing mutation of agent.");
                newPopulation[i] = population[tournamentSelection()].mutate(generation, i);
            } else {
                System.out.println("Commencing replication of agent.");
                newPopulation[i] = population[tournamentSelection()].replicate(generation, i);
            }
            i++;
        }

    }

    /**
     * Write the relevant data to their respective files in ~/run.
     */
    public static void writeFile(int round, double meanFit, double bestFit, double meanNode, double bestNode, String sol) {
        FileWriter fw;
        try {
            fw = new FileWriter("run/all.txt", true);
            fw.write(round + "\t"
                   + meanFit + "\t" + bestFit + "\t"
                   + meanNode + "\t" + bestNode + "\n");
            fw.close();

            fw = new FileWriter("run/fitness.txt", true);
            fw.write(meanFit + "\t" + bestFit + "\n");
            fw.close();

            fw = new FileWriter("run/nodes.txt", true);
            fw.write(meanNode + "\t" + bestNode + "\n");
            fw.close();

            fw = new FileWriter("run/phenome.txt", true);
            fw.write(sol + "\n");
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}