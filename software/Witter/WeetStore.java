/**
 * CS126 Design of Information Structures
 * WeetStore.java
 * 
 * For storing and displaying Users' weets, I implemented 3 data structures:
 *      - an array for non-sorted weet operations -- adding and getting, etc.;
 *      - a B-Tree for storing users by date joined, and sorting chronologically;
 *      - a Linked List for displaying trending tags in a sorted manner.
 *
 * Complexity analyses of the main methods:
 *        -- addWeet()
 *              O(N): Insertion into a sorted array.
 *
 *        -- getWeet()
 *              O(log(N)): Searching a sorted array of size N.
 *
 *        -- getWeets()
 *              O(N): Simple retrieval of sorted array.
 *
 *        -- getWeetsByUser()
 *              O(log(N)): Searching a sorted array of size N.
 *
 *        -- getWeetsContaining()
 *              O(N): Traversal a sorted tree.
 *
 *        -- getWeetsOn()
 *              O(N): As above.
 *
 *        -- getWeetsBefore()
 *              O(N): As above.
 *
 *        -- getTrending()
 *              O(10): The method merely retrieves the initial ten items in the linked list, and add them to an array of size 10.
 *
 *
 * @author: Zak Edwards
 * @version: 1.0 10/03/15
 */

package uk.ac.warwick.java.cs126.services;

import uk.ac.warwick.java.cs126.models.User;
import uk.ac.warwick.java.cs126.models.Weet;

import java.io.BufferedReader;
import java.util.Date;
import java.io.FileReader;
import java.text.ParseException;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;


public class WeetStore implements IWeetStore {

    public WeetStore() {
    }

    public int capacity     = 10;                          // For returning the top 10 weets.
    Weet[] weets            = new Weet[capacity];          // Define a new Weet array for basic non-sorted weet operations

    BinaryTree tree         = new BinaryTree();            // Define a new BinaryTree, wherein users are stored by date joined
    TrendingLinkedList list = new TrendingLinkedList();    // Define a new TrendingLinkedList, wherein weets are stored by common usage
    WeetArray array         = new WeetArray();             // Define a new WeetArray, wherein weets are stored

    public boolean addWeet(Weet weet) {
        /**
         * Method for adding a new weet to the weets we already have saved in our store.
         */
        return array.addWeetToStore(weet);
    }
    
    public Weet getWeet(int wid) {
        /**
         * Returns the weet with the given ID ('wid'); sortedness is not necessary here,
         * thus a simple table-lookup is sufficient.
         */
        return array.getWeetFromStore(wid);
    }

    public Weet[] getWeets() {
        /**
         * Returns an array of all weets, such that the most recently posted weet is 
         * first in the array.
         */
        return array.getWeetsFromStore();
        //return tree.toArray();
    }

    public Weet[] getWeetsByUser(User usr) {
        /**
         * Returns an array of all the weets posted by a given user, such that the
         * most recently posted weet is first in the array. Although of a sorted nature,
         * the array implementation is used in place of an additional B-Tree, which 
         * would be sorted in accordance with *both* User and Date.
         */
        return array.getWeetsByUserFromStore(usr);
    }

    public Weet[] getWeetsContaining(String query) {
        /**
         * Returns an array of those weets containing the given String 'query', with the
         * most recently posted weet first. Due to the sorted nature, the BinaryTree (or B-Tree)
         * data type is utilised henceforth.
         */
        Weet[] a = tree.toArray(query);

        if (a.length != 0) {
            return a;
        }
        return null;
    }

    public Weet[] getWeetsOn(Date dateOn) {
        /**
         * Returns an array of weets posted on a given date.
         */
        return tree.toOnArray(dateOn);
    }

    public Weet[] getWeetsBefore(Date dateBefore) {
        /**
         * Returns an array of weets posted before a given date.
         */
        return tree.toArray(dateBefore);
    }

    public String[] getTrending() {
        /**
         * Returns an array containing the trending hashtags, with the most popular
         * hashtag first in the array.
         */
        return list.getTags();
    }

    /* ------------------------------ Implementations for displaying Weets (Array) ----------------------------- */
    public class WeetArray {

        private int count;        // Declare a variable for storing the number of non-null elements, i.e., weets.
        private int nullCount;    // Declare a variable for storing the number of null elements.

        /* Constructors, Getters and Setters */
        public WeetArray() {
            count     = 0;
            nullCount = 0;
        }

        public int getCount() {
            return count;
        }

        public int getNullCount() {
            return nullCount;
        }
        /* --------------------------------- */

        public int excludeNull() {
            /**
             * Exclude null elements such that only non-null, genuine weets are included in the array.
             */
            int nullCount = 0;

            /* Check each element in the array via iteration for nullity */
            for (int i = 0; i < weets.length; i++) {
                if (weets[i] == null) {
                    nullCount++;
                }
            }
            return nullCount;
        }
 
        public Weet[] sortArray(Weet[] nonNullWeets) {
            /**
             * Sort and return an array such that weets are in chronological order, i.e., the weets
             * associated with the most recent dates are first in the array.
             */
            for (int j = 1; j < nonNullWeets.length; j++) {
                /* Check all possibilities for comparison */
                for (int k = 0; k < ((nonNullWeets.length) - j); k++) {
                    /**
                     * Use native comparison function to return true iff the non-null weet at index k
                     * was posted strictly earlier than the non-null weet at index (k + 1). If true,
                     * shift the earlier weet by one index using a temporary Weet.
                     */
                    if ((nonNullWeets[k].getDateWeeted().before(nonNullWeets[k + 1].getDateWeeted()))) {
                        Weet tempWeet       = nonNullWeets[k];
                        nonNullWeets[k]     = nonNullWeets[k + 1];
                        nonNullWeets[k + 1] = tempWeet;
                    }
                }
            }
            return nonNullWeets;
        }

        public boolean addWeetToStore(Weet weet) {
            /**
             * Primary method for adding weets to the store.
             */
            boolean result = true;
            String[] tags = weet.getMessage().split(" ");

            for (int i = 0; i < weets.length; i++) {
                /**
                 * If the weet passed as an argument is equal to the weet associated
                 * with any index of the array, return false; i.e., do not add the weet to the store.
                 */
                if  (weet == weets[i]) {
                    result = false;
                }
                /**
                 * If the result has remained true -- i.e., if the weet is distinct from other weets in
                 * the array.
                 */
                else {                       
                    if ((i == weets.length - 1) & (result != false)) {
                        result = true;
                        weets[count] = weet;
                    }
                    /*
                    for (int j = 0; j < tags.length; j++) {
                        if (tags[j].charAt(0) == '#') {
                            list.addTrending(tags[j]);
                        }
                    }
                    */
                }
            }

            /* Weets are stored up to and including (weets.length - 1). */
            if (count   == capacity - 1) {
                capacity = capacity + 1;                                   // Increase size by 1 for space efficiency.
                Weet[] moreWeets = new Weet[capacity];                     // Declare an array of size 'capacity'.
                System.arraycopy(weets, 0, moreWeets, 0, capacity - 1);    // Copy added weets into new array.
                weets = moreWeets;                                         // Make weets equal to larger array.
            }

            /* Increment the count if a weet is added. */
            if (result = true) {
                count++;
            }

            return result;
        }

        public Weet getWeetFromStore(int wid) {
            /**
             * Used by the getWeet(int wid) method; returns the weet with the given ID ('wid').
             */

            /* If the requested weet doesn't exist, null will be defaultly outputted. */
            Weet getWeet  =  null;

            /**
             * Go through each weet in the weets array and check if the ID of each weet
             * matches the given ID. If yes, then make getWeet equal to that weet.
             */
            for (int j = 0; j < count + 1; j++) {
                if (weets[j].getId() == wid) {
                    getWeet = weets[j];
                }
            }

            return getWeet;
        }

        public Weet[] getWeetsFromStore() {
            /**
             * Used by the getWeets() method to return an array of all weets, such that
             * the most recently posted weet is first in the array.
             */

            /**
             * Declares an array of size equal to number of weets we have, deducting nullelements
             * which increase the size uselessly.
             */
            Weet[] nonNullWeets = new Weet[(weets.length) - (excludeNull())];

            /**
             * C.ps all weets fr. an array with ptntl null elements at the end to array of size
             * eql to the no. of elements :. excluding nullspaces. Sort all weets in new array
             * (i.e. one without nulls) chronologically.
             */
            System.arraycopy(weets, 0, nonNullWeets, 0, nonNullWeets.length);
            //nonNullWeets = weets.clone(); 
            return sortArray(nonNullWeets);
        }

        public Weet[] getWeetsByUserFromStore(User usr) {
            /**
             * Used by the getWeetsByUser(User usr) method; returns an array of all the weets
             * posted by a given user, such that the most recently posted weet is first in the array.
             */
            int uid    = usr.getId();                    // Declare a variable for storing the ID of the given user.
            int weetNo = 0;                              // Declare a variable for storing the number of weets by the given user.

            Weet[] allWeets = new Weet[weets.length];    // Array within which to contain all by the specified user, including null.
            Weet[] uidWeets = new Weet[weetNo];          // Array which will contain all weets by the specified user.

            /**
             * Iterate through the array of weets. If the ID of the user who weeted the weet at index
             * i in the array is equal to the given ID, insert it into the next available space in
             * the allWeets array. Increment the variable storing the number of weets by the given user.
             */
            for (int i = 0; i < weets.length; i++) {
                if (weets[i].getUserId() == uid) {
                    weets[i] = allWeets[weetNo];
                    weetNo++;
                }
            }

            /* Copy allWeets into the uidWeets array to eliminate null elements/sort the array. */
            System.arraycopy(allWeets, 0, uidWeets, 0, uidWeets.length);
            //uidWeets = allWeets.clone();
            sortArray(uidWeets);

            return uidWeets;
        }
    }

    /* ------------------------------ Implementations for displaying 'Trends' by popularity (LinkedList) ----------------------------- */
    /**
     * Create a LinkedList data type from scratch.
     */
    public class TrendingLinkedList {

        private Node head;
        private Node tail;

        private class Node {
            private Node next;
            private Node previous;
            private String message;
            private int counter;

            private Node(String msg) {
                message = msg;
                counter   = 1;
            }
        }

        private void addTrending(String tag) {
            /**
             * Method for adding items to the tail of the linked list.
             */
            Node temp = head;

            /**
             * Iterate through the elements of the linked list, starting at the head
             * of the list. If the tag exists, increment the counter.
             */
            while (temp != null) {
                if (temp.message.equals(tag)) {
                    temp.counter++;
                    while ((temp.previous != null) && (temp.counter > temp.previous.counter)) {
                        Node aux = new Node(temp.message);
                        aux.counter           = temp.counter;
                        temp                  = temp.previous;
                        temp.counter          = temp.previous.counter;
                        temp.message          = temp.previous.message;
                        temp.previous.counter = aux.counter;
                        temp.previous.message = aux.message;
                    }
                    return;
                }
                temp = temp.next;
            }

            if (head == null) {
                head = tail = new Node(tag);
            } else {
                Node tmp = new Node(tag);
                tmp.previous = tail;
                tail.next    = tmp;
                tail         = tmp;
            }
        }

        private String[] getTags() {
            /**
             * Used by getTrending(); primary method for returning the trending
             * hashtags.
             */
            String[] tags = new String[10];
            Node temp = head;
            int i = 0;

            while ((temp != null) && i < 10) {
                tags[i] = temp.message;
                temp    = temp.next;
                i++;
            }
            return tags;
        }
    }

    /* ------------------------------ Implementations for weet store by date (BinaryTree) ----------------------------- */
    /**
     * Create a BinaryTree data type from scratch.
     */
    private class BinaryTree  {

        private int childMax = 4;    // Every internal node contains a maximum of (childMax) children; the number of elements of any node is thus (childMax - 1) by definition.
        private final class Node {
            private int childNo;                                       // Declare a variable to store the number of children a node currently has.
            private NodeData[] ChildArray = new NodeData[childMax];    // Declare an array of ChildArray, of size 4 (max. child nodes).

            private Node(int c) {
                this.childNo = c;                                      // Construct a node with c children.
            }
        }

        private final class NodeData {
            /**
             * whenWeeted and genWeet constitute a key-value pair. Internal nodes use whenWeeted and next;
             * external (leaf) nodes use whenWeeted and genWeet.
             */
            private Date whenWeeted;
            private Weet genWeet;
            private Node next;

            public NodeData(Date whenWeeted, Weet genWeet, Node next) {
                this.whenWeeted = whenWeeted;    // genWeet.getDateWeeted();
                this.genWeet    = genWeet;
                this.next       = next;
            }
        }

        /* -- Generic configuration for the BinaryTree -- */
        private Node root;          // Declare the root of the BinaryTree.
        private int size;           // Declare a variable to store the number of weets paired with dates ('key-value pairs') in the BinaryTree.
        private int height;         // Declare a variable to monitor the height of the BinaryTree.
        private int c;              // Declare a variable to monitor the children per node.
        private boolean altered;    // Declare a variable to monitor the state of the tree; i.e., monitor if a node has been *inserted*.

        /* Constructors, Getters and Setters */
        private BinaryTree() {
            root = new Node(0);
        }

        private int getSize() {
            return size;
        }

        public int getHeight() {
            return height;
        }
        /* --------------------------------- */

        private Node splitNode(Node currentNode) {
            /**
             * Method for B-Tree node splitting.
             */
            Node tempNode = new Node(childMax/2);    // Create new Node with (childMax/2) children
            currentNode.childNo  =  (childMax/2);

            /**
             * Iterate through to associate the indices of the old Node's rightmost children with
             * the indices new Node's leftmost children.
             */
            for (int i = 0; i < (childMax/2); i++) {
                tempNode.ChildArray[i] = currentNode.ChildArray[(childMax/2) + i];
            }
            return tempNode;
        }

        private Node insert(Node currentNode, Date date, Weet genWeet, int height) {
            /**
             * Method for B-Tree element insertion.
             */
            int i;
            NodeData t = new NodeData(date, genWeet, null);

            /* Considers case where the node is external, i.e., a leaf */
            if (height == 0) {
                for (i = 0; i < currentNode.childNo; i++) {
                    /**
                     * Use native comparison function to return true iff any children of the current node
                     * joined strictly earlier than the instant represented by 'date'. If true, break.
                     */ 
                    if ((currentNode.ChildArray[i].whenWeeted).before(date)) {
                        break;
                    }
                }
            }
            /* Considers case where the node is internal */
            else {
                for (i = 0; i < currentNode.childNo; i++) {
                    if (((i + 1) == currentNode.childNo) || (currentNode.ChildArray[i + 1].whenWeeted).before(date)) {
                        Node inserted = insert(currentNode.ChildArray[i++].next, date, genWeet, (height - 1));
                        if (inserted == null) {
                            return null;
                        }
                        t.whenWeeted = inserted.ChildArray[0].whenWeeted;
                        t.next       = inserted;
                        break;
                    }
                }
            }

            for (int j = currentNode.childNo; j > i; j--) {
                currentNode.ChildArray[j] = currentNode.ChildArray[j - 1];
            }

            currentNode.ChildArray[i] = t;
            currentNode.childNo++;
            if (currentNode.childNo < childMax) {
                return null;
            } else {
                return splitNode(currentNode);
            }
        }

        public void insertPair(Weet genWeet) {
            /**
             * Method for B-Tree Date-Weet pair insertion.
             */
            Node inserted = insert(root, genWeet.getDateWeeted(), genWeet, height);
            altered       = true;    // The tree's state has been altered, i.e., we have a new insertion.
            size++;

            if (inserted == null) {
                return;
            }

            /* Split root */
            Node t = new Node(2);
            t.ChildArray[0] = new NodeData(root.ChildArray[0].whenWeeted, null, root);
            t.ChildArray[1] = new NodeData(inserted.ChildArray[0].whenWeeted, null, inserted);
            root = t;
            height++;
        }

        /* Declare an array of weets. */
        private Weet[] WeetArray;

        /* ---------- toArray Methods ---------- */
        /**
         * Functions called in those main methods which require sortedness.
         */
        public Weet[] toArray() {
            /**
             * Used by getWeets() for returning a chronological array of weets.
             */

            /* If no insertions have been carried out, return the array of weets with no further operations */
            if (altered == false) {
                return WeetArray;
            }
            /**
             * Otherwise, define a new array dwArray (If M is the number of Date-Weet pairs, dwArray has size M),
             * make a call to an overloaded method with the generics of the B-Tree and dwArray as parameters,
             * set WeetArray equal to the new array and force WeetArray to return by setting 'altered' to false.
             */
            else {
                c = 0;
                altered = false;
                Weet[] dwArray = new Weet[size];
                toArray(root, height, dwArray);
                WeetArray = dwArray;
                return dwArray;
            }
        }

        public Weet[] toArray(String query) {
            /**
             * Used by getWeetsContaining(String query) for returning a chronological array of weets
             * whose names contain the given string.
             */
            c = 0;

            Weet[] dwArray = new Weet[size];    // If M is the number of Date-Weet pairs,   dwArray has size M. 
            Weet[] chArray = new Weet[c];       // If N is the number of children per node, chArray has size N. 

            /**
             * Make a call to an overloaded method with the generics of the B-Tree, the to-be-queried array
             * and the given query as parameters.
             */
            toArray(root, height, dwArray, query);
            /**
             * Clone dwArray into chArray for indices up to and including 'c', in order to return
             * exclusively those weets requested.
             */
            chArray = dwArray.clone();
            return chArray;
        }

        public Weet[] toArray(Date date) {
            /**
             * Used by getWeetsBefore(Date dateBefore) for returning a chronological array of weets
             * posted before a given date. Exact analogue of the above with a different parameter.
             */
            c = 0;

            Weet[] dwArray = new Weet[size];
            Weet[] chArray = new Weet[c]; 

            toArray(root, height, dwArray, date);
            chArray = dwArray.clone();
            return chArray;
        }

        private void toArray(Node currentNode, int height, Weet[] dwArray) {
            /**
             * Overloaded function that takes the parameters passed by toArray().
             * -> return dwArray -> return WeetArray
             */
            NodeData[] ChildArray = currentNode.ChildArray;

            /**
             * dwArray is populated via iteration with precisely those indices of children that
             * are of type Weet, thus producing a chronologically sorted array of all Weets in the B-Tree.
             */
            if (height == 0) {
                for (int j = 0; j < currentNode.childNo; j++) {    // Cycle through Z up to the number of children belonging to the current node.
                    dwArray[c] = ChildArray[j].genWeet;            // Associate the indices of dwArray with the Weet-nodes of ChildArray.
                    c++;
                }
            }
            /**
             * If the height of the position is non-zero, traverse the tree downwards (and decrement the height variable accordingly)
             * and make a recursive call.
             */
            else {
                for (int j = 0; j < currentNode.childNo; j++) {
                    toArray(ChildArray[j].next, (height - 1), dwArray);
                }
            }
        }

        private Weet toArray(Node currentNode, Date date, int height) {
            /**
             * Method for B-Tree element search?.
             */
            NodeData[] ChildArray = currentNode.ChildArray;

            /* Considers case where the node is external, i.e., a leaf */
            if (height == 0) {
                for (int i = 0; i < currentNode.childNo; i++) {
                    /**
                     * Use native comparison function to return true iff any children of the current node
                     * joined strictly earlier than the instant represented by 'date'. If true, break.
                     */ 
                    if ((ChildArray[i].whenWeeted).after(date)) {
                        return ChildArray[i].genWeet;
                    }
                }
            }
            /* Considers case where the node is internal */
            else {
                for (int i = 0; i < currentNode.childNo; i++) {
                    if (((i + 1) == currentNode.childNo) || (ChildArray[i].whenWeeted).after(date)) {
                        return toArray(ChildArray[i].next, date, height-1);
                    }
                }
            }
            return null;
        }

        private void toArray(Node currentNode, int height, Weet[] dwArray, String query) {
            /**
             * Overloaded function that takes the parameters passed by toArray(String query).
             */
            NodeData[] ChildArray = currentNode.ChildArray;

            /**
             * An exact analogue of the above method, with the addition of a .contains() function for filtering.
             */
            if (height == 0) {
                for (int j = 0; j < currentNode.childNo; j++) {
                    if ((ChildArray[j].genWeet.getMessage()).contains(query)) {    // Return true if the weet contains the given string.
                        dwArray[c] = ChildArray[j].genWeet;
                        c++;
                    }
                }
            }
            else {
                for (int j = 0; j < currentNode.childNo; j++) {
                    toArray(ChildArray[j].next, (height - 1), dwArray, query);
                }
            }
        }

        private void toArray(Node currentNode, int height, Weet[] dwArray, Date date) {
            /**
             * Overloaded function that takes the parameters passed by toArray(Date date).
             */
            NodeData[] ChildArray = currentNode.ChildArray;

            if  (height == 0) {
                 for (int j = 0; j < currentNode.childNo; j++) {
                      if ((ChildArray[j].genWeet.getDateWeeted()).before(date)) {    // Return true if the weet was posted before the given date.
                          dwArray[c] = ChildArray[j].genWeet;
                          c++;
                          }
                      }
                 }
            else {
                 for (int j = 0; j < currentNode.childNo; j++) {
                      toArray(ChildArray[j].next, (height - 1), dwArray, date);
                      }
                 }
            }

        private void toArray(Node currentNode, Date date, int height, Weet[] dwArray) {
            /**
             * Overloaded function that takes the parameters passed by toOnArray(Date date).
             */
            NodeData[] ChildArray = currentNode.ChildArray;

            if (height == 0) {
                for (int j = 0; j < currentNode.childNo; j++) {
                    if ((ChildArray[j].whenWeeted).equals(date)) {    // Return true if the weet was posted on the given date.
                        dwArray[c] = ChildArray[j].genWeet;
                        c++;
                    }
                    if ((ChildArray[j].whenWeeted).before(date)) { 
                        return;
                    }
                }
            } else {
                for (int j = 0; j < currentNode.childNo; j++) {
                    toArray(ChildArray[j].next, date, (height - 1), dwArray);
                }
            }
        }

        /* ---------- Exceptional toArray Method ---------- */
        private Weet[] toOnArray(Date date) {
            /**
             * Used by getWeetsOn(Date dateOn) for returning an array of weets posted on a given date.
             * A differently named method is here required as the order and type of parameters would otherwise
             * conflict with the method toArray(Date date). Calling the function of type toArray(root, height, dwArray, date)
             * would lead to the execution of an inappropriate Date comparison between the given date and the weet's date.
             */
            c = 0;

            Weet[] dwArray = new Weet[size];
            Weet[] chArray = new Weet[c]; 

            toArray(root, date, height, dwArray);
            chArray = dwArray.clone();
            return chArray;
        }
    }
}