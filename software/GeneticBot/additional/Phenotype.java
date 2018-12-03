/**
 * A manifestation of the genotype (Genotype.java).
 *
 *
 * @author:  Zak Edwards
 * @version: 1.0 25/03/16
 */

import java.io.*;
import java.util.Random;


public class Phenotype {

    /* --------------- Determine the behaviour of the agent in the Robocode environment ------------- */

    /**
     *  Declare global variables, characterising phenotypical behaviours.
     */
    final static
        String PATH    = new String(".");
        String PACKAGE = new String("");
        String JARS    = new String("/opt/robocode/libs/*.jar");

    final static int
        chromosomes = 5,                // Determine the number of possible (ph/g)enotypes
        minDepth = Genotype.minDepth,   // Minimum depth of the Koza tree
        maxDepth = Genotype.maxDepth;   // Maximum depth of the Koza tree

    final static double
        /* Crossover */
        pCrossTerm = Genotype.pCrossTerm,     // pCrossTerm = 0.10*0.87
        pCrossRoot = Genotype.pCrossRoot,     // pCrossRoot = 0.30*0.87
        pCrossFunc = Genotype.pCrossFunc,     // pCrossFunc = 0.60*0.87
        pCrossJump = 0.05,
        /* Mutation */
        pMutateTerm = Genotype.pMutateTerm,   // pMutateTerm = 0.10*0.03
        pMutateRoot = Genotype.pMutateRoot,   // pMutateRoot = 0.30*0.03
        pMutateFunc = Genotype.pMutateFunc;   // pMutateFunc = 0.60*0.03

    public static String
        iteration = new String(),
        phenes[]  = new String[chromosomes],
        source    = new String(),
        path      = "";

    public int gen = 0, id = 0, numNodes = 0;
    public double fitness = 0;

    static Random rand = new Random(System.currentTimeMillis());
    Genotype genes[]   = new Genotype[chromosomes];

    public Phenotype(int gen, int id) {
        this.gen = gen;
        this.id  = id;
        iteration = "GeneticBot-" + gen + "." + id;
        path      = PACKAGE + "." + iteration;
    }

    /**
     *  Recursive functions for Koza-type expression trees.
     */
    public int countNodes() {
        this.numNodes = 0;
        for (int i = 0; i < genes.length; i++)
            numNodes += genes[i].countNodes();
        return numNodes;
    }

    public void setDepths() {
        for (Genotype expr : genes)
            expr.setDepths(0);
    }

    /**
     * Initialisation and construction methods.
     */
    public void init() {
        for (int i = 0; i < chromosomes; i++) {
            genes[i] = new Genotype(0);
            genes[i].expand(0, 0);
        }
    }

    public void construct() {
        System.out.println(" Constructing expressions...");
        for (int i = 0; i < chromosomes; i++) {
            phenes[i] = genes[i].compose();
            writeSource();
        }
    }

    /**
     * The groundwork of the agent, common to all iterations.
     * To be written to a file for each respective iteration.
     */
    private void writeSource() {
        source =
            "package" + PACKAGE + ";"
        + "\n"
        + "\nimport robocode.*;"
        + "\nimport robocode.util.Utils;"
        + "\n"
        + "\n"
        + "\npublic class " + iteration + " extends Robot {"
        + "\n"
        + "\n    static double R1, R2;"
        + "\n    R1 = R2 = 0;"
        + "\n"
        + "\n    /**"
        + "\n     * run: The default behavior of GeneticBot"
        + "\n     */"
        + "\n    public void run() {"
        + "\n        setAdjustGunForRobotTurn(true);"
        + "\n        while(true) {"
        + "\n            turnGunRight(Double.POSITIVE_INFINITY);"
        + "\n            turnRight(R1);"
        + "\n            setAhead(R2);"
        + "\n        }"
        + "\n    }"
        + "\n"
        + "\n    /**"
        + "\n     * onScannedRobot: What to do when you see another robot"
        + "\n     */"
        + "\n    public void onScannedRobot(ScannedRobotEvent e) {"
        + "\n        setAhead("          + phenes[0] + ");"
        + "\n        setTurnRight("      + phenes[1] + ");"
        + "\n        setTurnGunRight("   + phenes[2] + ");"
        + "\n        setTurnRadarRight(" + phenes[3] + ");"
        + "\n        setFire("           + phenes[4] + ");"
        + "\n    }"
        + "\n"
        + "\n    /**"
        + "\n     * onHitByBullet: What to do when you're hit by a bullet"
        + "\n     */"
        + "\n    public void onHitByBullet(HitByBulletEvent e) {"
        + "\n    }"
        + "\n"
        + "\n    /**"
        + "\n     * onHitWall: What to do when you hit a wall"
        + "\n     */"
        + "\n    public void onHitWall(HitWallEvent e) {"
        + "\n    }"
        + "\n"
        + "\n}";
    }

    /**
     * Crossover, mutation and replication methods.
     */
    public Phenotype crossover(Phenotype P, int gen, int id) {
        Phenotype child = new Phenotype(gen, id);

        int chromosome1 = rand.nextInt(chromosomes);
        int chromosome2 = rand.nextInt(chromosomes);

        for (int i = 0; i < chromosomes; i++)
            child.genes[i] = this.genes[i].clone();

        while (chromosome1 == chromosome2)
            chromosome2 = rand.nextInt(chromosomes);

        /* Perform a replacement operation on the chromosome */
        if (rand.nextDouble() < pCrossRoot) {
            if (rand.nextDouble() < pCrossJump) {
                child.genes[chromosome1].replace(P.genes[chromosome2]);
            } else {
                child.genes[chromosome1].replace(P.genes[chromosome1]);   // replacement on equal chromosome indices
            }
        } else {
            boolean term1 = (rand.nextDouble() < pCrossTerm) ? true : false;
            boolean term2 = (rand.nextDouble() < pCrossTerm) ? true : false;
            child.genes[chromosome1].insert(P.genes[chromosome1].getSubTree(term1));
            child.genes[chromosome2].insert(P.genes[chromosome2].getSubTree(term2));
        }

        child.setDepths();
        child.countNodes();

        return child;
    }

    public Phenotype mutate(int gen, int id) {
        Phenotype child = new Phenotype(gen, id);

        int g = rand.nextInt(chromosomes);

        if (rand.nextDouble() < pMutateRoot) {
            child.genes[g] = new Genotype(0);
            child.genes[g].expand(0, 0);
        } else if (rand.nextDouble() < pMutateTerm) {
            child.genes[g].mutateTerm();
        } else {
            child.genes[g].mutateFunc();
        }

        child.setDepths();
        child.countNodes();

        return child;
    }

    public Phenotype replicate(int gen, int id) {
        Phenotype child = new Phenotype(gen, id);

        for (int i = 0; i < chromosomes; i++) {
            child.genes[i] = new Genotype(0);
            child.genes[i].replace(this.genes[i]);
        }

        child.setDepths();
        child.numNodes = this.numNodes;

        return child;
    }

    /**
     * File handling (iteration) methods.
     */
    public String compile() {
        /**
         * Write and compile the GeneticBot code for each iteration;
         * return the absolute path to the resultant class file.
         */
        try {
            FileWriter     fw = new FileWriter(PATH + "/" + iteration + ".java");
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(source);
            bw.close();
        } catch(Exception e) {
            System.err.println("Err> " + e.getMessage());
        }
        try {
            execute("javac -classpath " + JARS + " " + PATH + "/" + iteration + ".java");
        } catch(Exception e) {
            e.printStackTrace();
        }
        return (PATH + "/" + iteration + ".class");
    }

    public static void clear(int gen, int size, int id) {
        File oldJava, oldClass;
        System.out.println("Commence removal of redundant iterations...");
        for (int i = 0; i < size; i++) {
            if ((i == id || gen == 0) && i < 10)
                continue;
            oldJava  = new File(PATH + "/" + "GeneticBot-" + iteration + "." + i + ".java");
            oldClass = new File(PATH + "/" + "GeneticBot-" + iteration + "." + i + ".class");
            oldJava.delete();
            System.out.println("  Removed " + oldJava + ".");
            oldClass.delete();
            System.out.println("  Removed " + oldClass + ".");
        }
    }

    public static void execute(String cmd) throws Exception {
        Process p = Runtime.getRuntime().exec(cmd);
        printMsg(cmd + "stdout> ", p.getInputStream());
        printMsg(cmd + "stderr> ", p.getErrorStream());
        p.waitFor();
        if (p.exitValue() != 0)
            System.out.println(cmd + " exited with value " + p.exitValue());
    }

    private static void printMsg(String str, InputStream ins) throws Exception {
        String line = null;
        BufferedReader in = new BufferedReader(new InputStreamReader(ins));
        while ((line = in.readLine()) != null) {
            System.out.println(str + " " + line);
        }
    }

}