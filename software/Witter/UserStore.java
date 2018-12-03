/**
 * CS126 Design of Information Structures
 * UserStore.java
 * 
 * For storing Users, I implemented 2 data structures:
 *      - a Hash Table for basic non-sorted operations (adding and getting);
 *      - a B-Tree sorted by the dates the Users joined.
 *
 * Complexity analyses of the main methods:
 *        -- addUser()
 *              O(log(M) + 1): One tree insertion, one table insertion.
 *
 *        -- getUser()
 *              O(1): Simple retrieval from a table.
 *
 *        -- getUsers()
 *              O(N): Traversal of a sorted B-Tree.
 *
 *        -- getUsersContaining()
 *              O(N): As above.
 *
 *        -- getUsersJoinedBefore()
 *              O(N): As above.
 *
 *
 * @author: Zak Edwards
 * @version: 1.0 10/03/15
 */

package uk.ac.warwick.java.cs126.services;

import uk.ac.warwick.java.cs126.models.User;

import java.util.Date;


public class UserStore implements IUserStore {

    public UserStore() {
           }

    HashTable table = new HashTable();        // Define a new HashTable,  wherein users are stored by ID
    BinaryTree tree = new BinaryTree();       // Define a new BinaryTree, wherein users are stored by date joined

    public boolean addUser(User usr) {

           /**
            * Use both tree and table.
            */

           tree.insertPair(usr);
           return table.addUserToStore(usr.getName(), usr);

           }

    public User getUser(int uid) {

           /**
            * Returns the user that corresponds to the given ID ('uid'); sortedness is not necessary here,
            * thus a simple table-lookup is sufficient.
            */

           return table.getUserFromStore(uid);

           }

    public User[] getUsers() {

           /**
            * Returns an array of Users sorted by date joined, with the most recently joined user first;
            * due to the sorted nature, the BinaryTree (or B-Tree) data type is utilised henceforth.
            */

           return tree.toArray();

    }

    public User[] getUsersContaining(String query) {

           /**
            * Returns an array of those users whose names contain the given String 'query', with the most
            * recently joined user first.
            */

           return tree.toArray(query);

           }

    public User[] getUsersJoinedBefore(Date dateBefore) {

           /**
            * Returns an array of those users who joined before the given Date 'dateBefore', with the most
            * recently joined user first.
            */

           return tree.toArray(dateBefore);

           }

    /* ------------------------------ Implementations for user store by ID (HashTable) ----------------------------- */

    /**
     * Create a HashTable data type from scratch.
     */

    public class HashTable {

           private LinkedList[] table;
           private int size = 97;

                   public HashTable() {

                          /**
                           * The table size is set to a suitably large prime number to decrease
                           * the probability of collision.
                           */

                          this.size = size;
                          table = new LinkedList[size];

                          /* Construct the HashTable through iteration */

                          for (int i = 0; i < table.length; i++) {
                               table[i] = new LinkedList();
                               }

                          }


                   public int hash(String key) {

                          /**
                           * Here, a known cyclic shift hash function is utilised.
                           * (c.f. http://www.cpp.edu/~ftang/courses/CS240/lectures/hashing.htm)
                           */

                          int hashed = 0;

                          for (int i = 0; i < key.length(); i++) {            // goes through each space in the string
                               hashed  = (hashed << 5) | (hashed >>> 27);     // 5-bit cyclic shift of the running sum
                               hashed += (int) key.charAt(i);                 // add in next character
                               }

                            return hashed;                                    // returns a hashed number

                          }

                   public boolean addUserToStore(String key, User usr) {        // WAS void.

                          /* int hashed   = hash(key);
                          int location = (hashed % table.length);
                          table[location].add(new KeyValuePair(key, usr)); */

                          if (getUserFromStore(usr.getId())==null) {
                              int location = usr.getId() % size;
                              table[location].add(usr);
                              return true;
                              }

                            return false;

                          }

                   /* public KeyValuePair findHashedKey(String key) {

                            int hashed = hash(key);
                            int location = (hashed % table.length);
                            return table[location].find(key);

                            } */


                   public User getUserFromStore(int uid) {

                          int location = uid % size;
                          return table[location].findID(uid);

                          }

           }

    /**
     * Create a KeyValuePair data type from scratch.
     */

    public class KeyValuePair {

           private String key;
           private User value;

                   /* Constructors, Getters and Setters */

                   public KeyValuePair(String k, User v) {
                          key   = k;
                          value = v;
                          }

                   public String getKey() {
                          return key;
                          }

                   public User getValue() {
                          return value;
                          }

                   /* --------------------------------- */

           }

    /**
     * Create a LinkedList data type from scratch.
     */

    public class LinkedList {

           private LinkedListElement head;
           private int size;

                   /* Constructors, Getters and Setters */

                   public LinkedList() {

                          /* Constructor for the LinkedList class; initialise values of private variables.  */

                          head = null;
                          size = 0;

                          }

                   public int getSize() {
                          return size;
                          }

                   public LinkedListElement getHead() {
                          return head;
                          }

                   /* --------------------------------- */

                   public void add(User genUser) {

                          /**
                           * Elementary 'add' method, called directly by addUserToStore and ultimately
                           * by the main addUser method.
                           */

                          LinkedListElement tempElement = new LinkedListElement(genUser, head);   // Set a newly created LinkedListElement...
                          head = tempElement;                                                     // ...to the head of the LinkedList.
                          size++;                                                                 // Increment the size of the LinkedList.

                          }

                   public User findID(int uid) {

                          LinkedListElement tempElement = head;

                          if  (tempElement != null) {
                               if  (tempElement.getGenUser().getId() == uid) {
                                    return tempElement.getGenUser();
                                    }
                               else {
                                    while (tempElement.getNext() != null) {
                                           tempElement = tempElement.getNext();
                                           if ((tempElement.getGenUser().getId()) == uid) {
                                                return tempElement.getGenUser();
                                                }
                                           }
                                    }
                               }

                             return null;

                          }

                   public KeyValuePair findKey(String key) {

                          /* Analogue of the findID method for keys. */

                          LinkedListElement tempElement = head;

                          while (tempElement != null) {
                                 if (tempElement.getData().getKey().equals(key)) {
                                     return tempElement.getData();
                                     }
                                 tempElement = tempElement.getNext();
                                 }

                          return null;

                          }

           }

    /**
     * Create a LinkedListElement data type from scratch.
     */

    public class LinkedListElement {

           private KeyValuePair data;
           private User genUser;
           private LinkedListElement next = null;

                   /* Constructors, Getters and Setters */

                   public LinkedListElement(User genUser, LinkedListElement next) {
                          this.genUser = genUser;
                          this.next = next;
                          }

                   /* public LinkedListElement(KeyValuePair data, LinkedListElement next) {

                            this.data = data;
                            this.next = next;

                            } */

                   public User getGenUser() {
                          return genUser;
                          }

                   public Date getDateJoined() {
                          return genUser.getDateJoined();
                          }

                   public KeyValuePair getData() {
                          return data;
                          }

                   public LinkedListElement getNext() {
                          return next;
                          }

                   public void setNext(LinkedListElement genElement) {
                          next = genElement;
                          }

                   /* --------------------------------- */

           }

    /* ------------------------------ Implementations for user store by date (BinaryTree)  ----------------------------- */

    /**
     * Create a BinaryTree data type from scratch.
     */

    private class BinaryTree  { 

           private int childMax = 4;         // Every internal node contains a maximum of (childMax) children; the number of elements of any node is thus (childMax - 1) by definition.

                   private final class Node {

                          private int childNo;                                            // Declare a variable to store the number of children a node currently has.
                          private NodeData[] ChildArray = new NodeData[childMax];         // Declare an array of children, of size 4 (max. child nodes).

                          private Node(int c) {
                                 this.childNo = c;                                        // Construct a node with c children.
                                 }

                          }

                   private final class NodeData {

                          /**
                           * whenJoined and genUser constitute a key-value pair. Internal nodes use whenJoined and next;
                           * external (leaf) nodes use whenJoined and genUser.
                           */

                          private Date whenJoined;
                          private User genUser;
                          private Node next;

                                 public NodeData(Date whenJoined, User genUser, Node next) {
                                        this.whenJoined = whenJoined;    // genUser.getDateJoined();
                                        this.genUser    = genUser;
                                        this.next       = next;
                                        }

                          }

           /* -- Generic configuration for the BinaryTree -- */

           private Node root;                                            // Declare the root of the BinaryTree.
           private int size;                                             // Declare a variable to store the number of users paired with dates ('key-value pairs') in the BinaryTree.
           private int height;                                           // Declare a variable to monitor the height of the BinaryTree.
           private int c;                                                // Declare a variable to monitor the children per node.
           private boolean altered;                                      // Declare a variable to monitor the state of the tree; i.e., monitor if a node has been *inserted*.

                   /* Constructors, Getters and Setters */

                   public BinaryTree() {
                          root = new Node(0);
                          }

                   public int getSize() {
                          return size;
                          }

                   public int getHeight() {
                          return height;
                          }

                   /* --------------------------------- */

           /* ---------------------------------------------- */

                   private Node splitNode(Node currentNode) {

                          /**
                           * Method for B-Tree node splitting.
                           */

                          Node tempNode = new Node(childMax/2);         // Create new Node with (childMax/2) children
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

                   private Node insert(Node currentNode, Date date, User genUser, int height) {

                          /**
                           * Method for B-Tree element insertion.
                           */

                          int i;
                          NodeData t = new NodeData(date, genUser, null);

                          /* Considers case where the node is external, i.e., a leaf */
                          if  (height == 0) {
                               for (i = 0; i < currentNode.childNo; i++) {
                                    /**
                                     * Use native comparison function to return true iff any children of the current node
                                     * joined strictly earlier than the instant represented by 'date'. If true, break.
                                     */ 
                                    if ((currentNode.ChildArray[i].whenJoined).before(date)) {
                                         break;
                                         }
                                    }
                               }
                          /* Considers case where the node is internal */
                          else {
                               for (i = 0; i < currentNode.childNo; i++) {
                                    if (((i + 1) == currentNode.childNo) || (currentNode.ChildArray[i + 1].whenJoined).before(date)) {
                                         Node inserted = insert(currentNode.ChildArray[i++].next, date, genUser, (height - 1));
                                         if (inserted == null) {
                                             return null;
                                             }
                                         t.whenJoined = inserted.ChildArray[0].whenJoined;
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
                          if  (currentNode.childNo < childMax) {
                               return null;
                               }
                          else {
                               return splitNode(currentNode);
                               }

                          }

                   public void insertPair(User genUser) {

                          /**
                           * Method for B-Tree Date-User pair insertion.
                           */

                          Node inserted = insert(root, genUser.getDateJoined(), genUser, height);
                          altered       = true;        // The tree's state has been altered, i.e., we have a new insertion.
                          size++;

                          if (inserted == null) {
                              return;
                              }

                          /* Split root */

                          Node t = new Node(2);
                          t.ChildArray[0] = new NodeData(root.ChildArray[0].whenJoined, null, root);
                          t.ChildArray[1] = new NodeData(inserted.ChildArray[0].whenJoined, null, inserted);
                          root = t;
                          height++;

                          }

           /* Declare an array of users. */

           private User[] UserArray;

                   /* ---------- toArray Methods ---------- */

                   /**
                    * Functions called in those main methods which require sortedness.
                    */

                   public User[] toArray() {

                          /**
                           * Used by getUsers() for returning a chronological array of users.
                           */

                          /* If no insertions have been carried out, return the array of users with no further operations */
                          if  (altered == false) {
                               return UserArray;
                               }
                          /**
                           * Otherwise, define a new array duArray (If M is the number of Date-User pairs, duArray has size M),
                           * make a call to an overloaded method with the generics of the B-Tree and duArray as parameters,
                           * set UserArray equal to the new array and force UserArray to return by setting 'altered' to false.
                           */
                          else {
                               c = 0;
                               altered = false;
                               User[] duArray = new User[size];
                               toArray(root, height, duArray);
                               UserArray = duArray;
                               return duArray;
                               }
                          }

                   public User[] toArray(String query) {

                          /**
                           * Used by getUsersContaining(String query) for returning a chronological array of users
                           * whose names contain the given string.
                           */

                          c = 0;

                          User[] duArray = new User[size];              // If M is the number of Date-User pairs,   duArray has size M. 
                          User[] chArray = new User[c];                 // If N is the number of children per node, chArray has size N. 

                          /**
                           * Make a call to an overloaded method with the generics of the B-Tree, the to-be-queried array
                           * and the given query as parameters.
                           */
                          toArray(root, height, duArray, query);
                          /**
                           * Clone duArray into chArray for indices up to and including 'c', in order to return
                           * exclusively those users requested.
                           */
                          chArray = duArray.clone();
                          return chArray;

                          }

                   public User[] toArray(Date date) {

                          /**
                           * Used by getUsersJoinedBefore(Date dateBefore) for returning a chronological array of users
                           * who joined before a given date. Exact analogue of the above with a different parameter.
                           */

                          c = 0;

                          User[] duArray = new User[size];
                          User[] chArray = new User[c]; 

                          toArray(root, height, duArray, date);
                          chArray = duArray.clone();
                          return chArray;

                          }

                   private void toArray(Node currentNode, int height, User[] duArray) {

                          /**
                           * Overloaded function that takes the parameters passed by toArray().
                           * -> return duArray -> return UserArray
                           */

                          NodeData[] ChildArray = currentNode.ChildArray;

                          /**
                           * duArray is populated via iteration with precisely those indices of ChildArray that
                           * are of type User, thus producing a chronologically sorted array of all users in the B-Tree.
                           */
                          if  (height == 0) {
                               for (int j = 0; j < currentNode.childNo; j++) {    // Cycle through Z up to the number of children belonging to the current node.
                                    duArray[c] = ChildArray[j].genUser;           // Associate the indices of duArray with the User-nodes of ChildArray.
                                    c++;
                                    }
                               }
                          /**
                           * If the height of the position is non-zero, traverse the tree downwards (and decrement the height variable accordingly)
                           * and make a recursive call.
                           */
                          else {
                               for (int j = 0; j < currentNode.childNo; j++) {
                                    toArray(ChildArray[j].next, (height - 1), duArray);
                                    }
                               }

                          }

                   private void toArray(Node currentNode, int height, User[] duArray, String query) {

                          /**
                           * Overloaded function that takes the parameters passed by toArray(String query).
                           */

                          NodeData[] ChildArray = currentNode.ChildArray;

                          /**
                           * An exact analogue of the above method, with the addition of a .contains() function for filtering.
                           */
                          if  (height == 0) {
                               for (int j = 0; j < currentNode.childNo; j++) {
                                    if ((ChildArray[j].genUser.getName()).contains(query)) {        // Return true if the username contains the given string.
                                        duArray[c] = ChildArray[j].genUser;
                                        c++;
                                        }
                                    }
                               }
                          else {
                               for (int j = 0; j < currentNode.childNo; j++) {
                                    toArray(ChildArray[j].next, (height - 1), duArray, query);
                                    }
                               }

                          }

                   private void toArray(Node currentNode, int height, User[] duArray, Date date) {

                          /**
                           * Overloaded function that takes the parameters passed by toArray(Date date).
                           */

                          NodeData[] ChildArray = currentNode.ChildArray;

                          if  (height == 0) {
                               for (int j = 0; j < currentNode.childNo; j++) {
                                    if ((ChildArray[j].genUser.getDateJoined()).before(date)) {    // Return true if the user joined before the given date.
                                        duArray[c] = ChildArray[j].genUser;
                                        c++;
                                        }
                                    }
                               }
                          else {
                               for (int j = 0; j < currentNode.childNo; j++) {
                                    toArray(ChildArray[j].next, (height - 1), duArray, date);
                                    }
                               }

                          }

           }

    }