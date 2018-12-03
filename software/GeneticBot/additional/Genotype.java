/**
 * @author:  Zak Edwards
 * @version: 1.0 25/03/16
 */

import java.util.ArrayList;
import java.util.Random;


public class Genotype {

    /* ------------------- Determine the character and behaviour of the phenotype ------------------- */

    final static String
        Cnst[] =
                    {
                     "0.001",
                     "Math.PI",
                     "Math.random()",
                     "Math.floor(Math.random()*10)",
                     "Double.toString(rand.nextDouble())"   // Ephemeral random constants
                    },
        Univ[] =
                    {
                     "getEnergy()",  "getVelocity()",
                     "getHeight()",  "getWidth()",
                     "getHeading()", "getBearing()",
                     "getX()",       "getY()"
                    },
        Evnt[] =
                    {
                     "e.getEnergy",    "e.getVelocity()",
                     "e.getHeading()", "e.getBearing()",
                     "e.getDistance()"
                    },
        Term[][] =
                    {
                     Cnst, Univ, Evnt
                    },
        Func[][] =
                    {
                     {"Math.sin(", ")"}, {"Math.asin(", ")"},
                     {"Math.cos(", ")"}, {"Math.acos(", ")"},
                     {"Math.toDegrees(", ")"}, {"Math.toRadians(", ")"},
                     {"Math.min(", ",",  ")"}, {"Math.max(", ",",  ")"}, {"Math.abs(", ")"},
                     {"", " * -1"}    // Sign inversion
                    },
        Bnry[][] =
                    {
                     {"", "+", ""},
                     {"", "-", ""},
                     {"", "/", ""},
                     {"", "*", ""}
                    },
        Cond1[][] =
                    {
                     /* If-then-else conditionals */
                     {"", " > 0 ? ", " : ", ""}
                    },
        Cond2[][] =
                    {
                     {"", " > ",  " ? ", " : ", ""},
                     {"", " == ", " ? ", " : ", ""},
                     /* While statement */
                     {"while", "", ""}
                    },
        Expr[][][] =
                    {
                     Term, Func, Bnry, Cond1, Cond2
                    };

    /* Probabilities for terminal nodes characterised by the BNF grammar */
    final static double
        pTermCnst = 0.15,   // Terminals of BNF-type <cnst>
        pTermUniv = 0.35,   // Terminals of BNF-type <univ>
        pTermEvnt = 0.40,   // Terminals of BNF-type <evnt>
        pTermERC  = 0.10,   // Ephemeral Random Constants of BNF-type <cnst>
        pTerm[]   = {
                     pTermCnst,
                     pTermUniv,
                     pTermEvnt,
                    },

        pBnry   = 0.60,
        pFunc   = 0.20,
        pCond1  = 0.05,     // p(Cond[1][])
        pCond2  = 0.15,     // p(Cond[2-4][])
        pExpr[] = {
                   pBnry,
                   pFunc,
                   pCond1,
                   pCond2
                  };

    /* Determine the constraints on the depths of expression trees */
    final static int
        minDepth = 3,
        maxDepth = 10;

    /* Probabilities for operations on expression trees */
    final static double
        /* Crossover */
        pCrossTerm = 0.10,
        pCrossRoot = 0.30,
        pCrossFunc = 0.60,
        /* Mutation */
        pMutateTerm = 0.10,
        pMutateRoot = 0.30,
        pMutateFunc = 0.60;

    int depth = 0, arity = -1;   // We define arity of a vertex to be the number of children for which the vertex is a parent
    boolean isTerm = true;       // return true if the arity of the concerned vertex is zero

    Genotype children[];         // An array to populated with vertices of non-zero depth
    String expression[];         // An array to be populated with legal expressions

    static Random rand = new Random(System.currentTimeMillis());

    public Genotype(int depth) {
        this.depth = depth;
    }

    public Genotype(int depth, int arity, boolean isTerm) {
        this.depth  = depth;
        this.arity  = arity;
        this.isTerm = isTerm;
    }

    /**
     * Conduct depth-first tree traversal (composition) on the expression tree.
     */
    public String compose() {
        String wff = expression[0];
        if (this.arity == -1)
            System.err.println("Err> nonpositive arity encountered.");
        for (int i = 0; i < this.arity; i++) {
            //System.out.println(arity);
            try {
                wff += (children[i].compose() + expression[i + 1]);
            } catch (ArrayIndexOutOfBoundsException e) {
                throw new RuntimeException("Tried to access index " + i + " but array has only " + expression.length + " elements", e);
            }
        }
        wff = "(" + wff + ")";   // Ensure syntactic ambiguity is avoided
        System.out.println("  " + wff);
        return wff;
    }

    public int countNodes() {
        int count = 1;
        for (int i = 0; i < this.arity; i++)
            count += this.children[i].countNodes();
        return count;
    }

    /**
     * A function, recursively-called, for the expansion of the expression tree.
     */
    public void expand(int depth, int expr) {
        setArity(depth);
        assignExpr(depth, expr);
    }

    private void assignExpr(int depth, int expr) {
        if (this.arity == 0) {
            assignTerm();
        } else {
            //System.out.print(arity + " ");
            this.expression = Expr[arity][rand.nextInt(Expr[arity].length)];
            for (int i = 0; i < arity; i++) {
                children[i] = new Genotype(depth + 1);
                children[i].expand((depth + 1), expr);
            }
        }
    }

    private void assignTerm() {
        expression = new String[1];
        double rnd = rand.nextDouble();
        this.children = null;
        for (int i = 0; i < Term.length; i++) {
           if ((rnd -= pTerm[i]) <= 0) {
               expression[0] = Expr[0][i][rand.nextInt(Expr[0][i].length)];
               break;
           }
        }
        if (rnd > 0) {
            //if (pTermERC < rand.nextDouble())
            //    return;
            expression[0] = Double.toString(rand.nextDouble());
        }
    }

    public int getMaxNode() {
        int maxNode = this.depth;
        for (int i = 0; i < this.arity; i++)
            maxNode = Math.max(children[i].getMaxNode(), maxNode);
        return maxNode;
    }

    public int getMinTerm() {
        if (isTerm) {
            return this.depth;
        } else {
            int minTerm = (maxDepth + 1);
            for (Genotype expr : children)
                minTerm = Math.min(minTerm, expr.getMinTerm());
            return minTerm;
        }
    }

    /**
     * Assign arity to vertices via selection subroutine.
     */
    public void setArity(int depth) {
        /* (depth > minDepth) -> isTerm; (depth == maxDepth) -> (isTerm v !isTerm) */
        if (((depth > minDepth) && (rand.nextDouble() < 0.35)) || depth == maxDepth) {
            this.arity = 0;
            isTerm = true;
        } else {
            isTerm = false;
            double rnd = rand.nextDouble();
            /* Selection subroutine */
            for (int i = 0; i < pExpr.length; i++) {
                if ((rnd -= pExpr[i]) <= 0) {
                    this.arity = (i + 1);
                    break;
                }
            }
            if (rnd > 0) {
                this.arity = pExpr.length;
            }
            children = new Genotype[this.arity];
        }
    }

    public void setDepths(int depth) {
        this.depth = depth;
        for (int i = 0; i < this.arity; i++)
            this.children[i].setDepths(depth + 1);
    }

    public void insert(Genotype newNode) {
        int maxNode     = this.getMaxNode();
        int minTerm     = newNode.getMinTerm();
        int minSubTerm  = (minTerm - newNode.depth);
        int maxSubDepth = (newNode.getMaxNode() - newNode.depth);

        int ceiling = (maxDepth - maxSubDepth);
        int floor   = Math.max(1, (minDepth - maxSubDepth));

        int targetDepth = (rand.nextInt((ceiling - floor) + 1) + floor);

        if ((targetDepth + minSubTerm) < minDepth)
            targetDepth = minDepth - minSubTerm;

        if (maxNode < targetDepth) {
            this.insertAt(newNode, maxNode);
        } else {
            this.insertAt(newNode, targetDepth);
        }
    }

    public void insertAt(Genotype newNode, int target) {
        if (this.depth == target) {
            this.replace(newNode);
        } else {
            ArrayList<Integer> genome = new ArrayList<Integer>();
            for (int i = 0; i < this.arity; i++) {
                if (children[i].getMaxNode() >= target)
                    genome.add(i);
            }
            int targetBranch = genome.get(rand.nextInt(genome.size()));
            children[targetBranch].insertAt(newNode, target);
        }
    }

    public void replace(Genotype newNode) {
        this.arity      = newNode.arity;
        this.isTerm     = newNode.isTerm;
        this.expression = new String[newNode.expression.length];
        for (int i = 0; i < this.expression.length; i++)
            this.expression[i] = newNode.expression[i];
        if (newNode.isTerm) {
            this.children = null;
        } else {
            this.children = new Genotype[arity];
            for (int i = 0; i < newNode.arity; i++) {
                this.children[i] = new Genotype(depth + 1);
                this.children[i].replace(newNode.children[i]);
            }
        }
    }

    public void mutateTerm() {
        if (!this.isTerm) {
            this.children[rand.nextInt(this.arity)].mutateTerm();
        } else {
            this.assignTerm();
        }
    }

    public void mutateFunc() {
        if (this.depth == 0) {
            this.children[rand.nextInt(this.arity)].mutateFunc();
        } else if (this.depth == (maxDepth - 1) || rand.nextDouble() < 0.3) {
            Genotype newSubTree = new Genotype(this.depth);
            newSubTree.expand(this.depth, 0);
            this.replace(newSubTree);
        }
    }

    public Genotype clone() {
        Genotype clone   = new Genotype(this.depth, this.arity, this.isTerm);
        clone.expression = new String[expression.length];
        for (int i = 0; i < expression.length; i++)
            clone.expression[i] = this.expression[i];
        if (isTerm) {
            clone.children = null;
        } else {
            clone.children = new Genotype[this.children.length];
            for (int i = 0; i < children.length; i++)
                clone.children[i] = this.children[i].clone();
        }
        return clone;
    }

    public Genotype getNodeAtDepth(int target) {
        if (this.depth == target) {
            return this;
        } else {
            ArrayList<Integer> genome = new ArrayList<Integer>();
            for (int i = 0; i < this.arity; i++) {
                if (children[i].getMaxNode() > target)
                    genome.add(i);
            }
            int targetBranch = genome.get(rand.nextInt(genome.size()));
            return children[targetBranch].getNodeAtDepth(target);
        }
    }

    public Genotype getSubTree(boolean term) {
        if (term) {
            if (arity == 0) {
                return this.clone();
            } else {
                return children[rand.nextInt(arity)].getSubTree(true);
            }
        } else {
            int target = (rand.nextInt(this.getMaxNode() - 1) + 1);
            return this.getNodeAtDepth(target);
        }
    }

}