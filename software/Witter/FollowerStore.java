/**
 * CS126 Design of Information Structures
 * FollowerStore.java
 *
 * For storing and displaying Users' follower relationships, I decided to implement exclusively the generalised Binary Search Tree (B-Tree). The store in its entirety
 * is therefore a single B-Tree, wherein each node -- representing the Users -- has two children; the followers of that specific User, and the 'follows'
 * (those Users whom that specific User is following). In order to conduct sorting I implemented two primary classes:
 *        -- a 'FollowerStore', wherein Users are stored and sorted by their unique IDs;
 *        -- and a 'DateStore', wherein Users are sorted by their date joined.
 *
 * Advantages of the exclusive implementation of the B-Tree ADT:
 *        -- Due to the nature of the application, selections is performed at a greater rate than insertions into the tree, which is conducive to the
 *           efficiency of the B-Tree ADT.
 *        -- Speed of accessibility is of great importance for this application; the B-Tree is better in this regard than ADT implementations
 *           where the time to access data exceeds the time spent processing it.
 *
 * Disadvantages:
 *        -- Increasing the required space by a multiplication magnitude of 3.
 *
 * Complexity analyses of the main methods:
 *        -- addFollower()
 *              O(log(M) + log(N) + log(P)): Time complexity of a B-Tree search is O(log(N)), thus we have (worst case) O(log(M)) for M users in FollowerTree;
 *              An insertion is also O(log(N)), and we have two insertions to establish a follower relation between two users.
 *
 *        -- getFollowers()
 *              O(log(M) + N): Requires a search of FollowerTree (complexity O(log(M))) and a space traversal of DateTree (complexity O(N)).
 *
 *        -- getFollows()
 *              O(log(M) + N): As above.
 *
 *        -- isAFollower()
 *              O(log(M) + log(N)): Worst case scenario search of both trees.
 *
 *        -- getNumFollowers()
 *              O(log(N)): Search a sorted tree.
 *
 *        -- getMutualFollowers()
 *              O(2log(M) + N): Search the sorted trees of two given users (O(log(M) + log(M))) and traverse the resultant subtree (of size N) of mutuals
 *
 *        -- getMutualFollows()
 *              O(2log(M) + N): As above.
 *
 *        -- getTopUsers()
 *              O(N + N(log(N))): Traversal of a single B-Tree plus the complexity of a partition-exchange sort.
 *
 * @author: Zak Edwards
 * @version: 1.0 10/03/15
 */

package uk.ac.warwick.java.cs126.services;

import uk.ac.warwick.java.cs126.models.Weet;
import uk.ac.warwick.java.cs126.models.User;

import java.util.Date;


public class FollowerStore implements IFollowerStore {

    public FollowerStore() {
           }

    FollowerTree tree = new FollowerTree(); 

    public boolean addFollower(int uid1, int uid2, Date followDate) {

           /**
            * Add a follower relationship to the data store; if the relationship already exists, it is not added.
            * Return true if the relationship was successfully added, false otherwise.
            */

           tree.insertFollow(uid1, uid2, followDate);
           return tree.insertFollower(uid2, uid1, followDate);

           }  

    public int[] getFollowers(int uid) {

           /**
            * Return an array -- sorted such that the most recent Follower is first -- of IDs of all Users that
            * follow the user with the given ID (uid).
            */

           return tree.getFollowers(uid).toUserArray();

           }

    public int[] getFollows(int uid) {

           /**
            * Return an array of IDs, chronologically sorted as above, of all users that the user with the given ID (uid) follows.
            */

           return tree.getFollows(uid).toUserArray();

           }

    public boolean isAFollower(int uidFollower, int uidFollows) {

           /**
            * Return true if the User with ID 'uidFollower' is a follower of the User with ID 'uidFollows'.
            */

           return tree.isAFollower(uidFollower,uidFollows);

           }

    public int getNumFollowers(int uid) {

           /**
            * Return the number of followers that the user with given ID (uid) currently has.
            */

           return tree.getNumFollowers(uid);

           }

    public int[] getMutualFollowers(int uid1, int uid2) {

           /**
            * Return the IDs of all the users that follow both the User with id 'uid1' and the User with id 'uid2'.
            */

           return tree.getMutualFollowers(uid1,uid2);

           }

    public int[] getMutualFollows(int uid1, int uid2) {

           /**
            * Return the IDs of all the users that are followed by both the User with id 'uid1' and the User with id 'uid2'.
            */

           return tree.getMutualFollows(uid1,uid2);

           }

    public int[] getTopUsers() {

           /**
            * Return the IDs of all Users, sorted such that the User with the most followers is first.
            */

           return tree.getTopUsers();

           }

    /* ------------------------------ Implementations for follower store by ID *and* date (FollowerTree) ----------------------------- */

    /**
     * Create a BinaryTree data type from scratch.
     */

private class FollowerTree  {

           private static final int childMax = 4;         // Every internal node contains a maximum of (childMax) children; the number of elements of any node is thus (childMax - 1) by definition.

                   private final class Node {

                          private int childNo;                                            // Declare a variable to store the number of children a node currently has.
                          private NodeData[] ChildArray = new NodeData[childMax];         // Declare an array of children, of size 4 (max. child nodes).

                          private Node(int c) {
                                 this.childNo = c;                                        // Construct a node with c children.
                                 }

                          }

                   private final class NodeData {

                          /**
                           * Generic configuration for B-Tree entries; reference DateTree
                           * in order to allow followers and 'follows' to be sorted chronologically.
                           */

                          private int key;
                          private Node next;
                          private DateTree followers;                   // Declare a chronologically sorted B-Tree to store followers.
                          private DateTree follows;                     // Declare a chronologically sorted B-Tree to store 'follows', i.e., those whom a user is following.

                                 public NodeData(int key, Node next) {
                                        followers = new DateTree();
                                        follows   = new DateTree();
                                        this.key  = key;
                                        this.next = next;
                                        }

                          }

           /* -- Generic configuration for the BinaryTree -- */

           private Node root;                                            // Declare the root of the BinaryTree.
           private int size;                                             // Declare a variable to store the number of users associated with followers and follows in the BinaryTree.
           private int height;                                           // Declare a variable to monitor the height of the BinaryTree.
           private int c;                                                // Declare a variable to monitor the children per node.
           private Follow[] TopUsers;

                   /* Constructors, Getters and Setters */

                   public FollowerTree() {
                          root = new Node(0);
                          }

                   public int getSize() {
                          return size;
                          }

                   public int getHeight() {
                          return height;
                          }

                   public DateTree getFollows(int key) {
                          return findFollows(root, key, height);
                          }

                   public DateTree getFollowers(int key) {
                          return findFollowers(root, key, height);
                          }

                   public int getNumFollowers(int key) {
                          return getFollowers(key).getSize();
                          }

                   /* --------------------------------- */

           /* ---------------------------------------------- */

                   /* ------ Checking functions ------- */

                   public boolean checkExistsFollower(int key) {

                          /**
                           * Check if the given key is in the ID tree (FollowerTree).
                           */

                          c = 0;
                          searchForID(root, key, height);
                          return (c != 0);

                          }

                   public boolean isAFollower(int key, int followedKey) {

                          /**
                           * Return true if 'key' is a follower of 'followedKey'.
                           */

                          return getFollowers(followedKey).checkExistsDate(key);

                          }

                   /* ------- Sorting functions ------- */

                   private void swap(Follow[] TopUsers, int i, int j) {

                          /**
                           * Swap the elements at indices i and j of the array TopUsers; called by subsequent
                           * methods in order to chronologically order Users in terms of quantity of followers.
                           */

                          Follow swap = TopUsers[i];
                          TopUsers[i] = TopUsers[j];
                          TopUsers[j] = swap;

                          }

                   int part(Follow[] TopUsers, int left, int right) {

                          /**
                           * Partition.
                           */

                          int i = left - 1;
                          int j = right;

                          while (true) {
                                 while ((TopUsers[++i].count)   < (TopUsers[right].count));
                                 while ((TopUsers[right].count) < (TopUsers[--j].count)) {
                                        if (j == left) {
                                            break;
                                            }
                                        }
                                 if (i >= j) {
                                     break;
                                     }
                                 swap(TopUsers, i, j);
                                 }

                          swap(TopUsers, i, right);
                          return i;

                          }

                   private void sort(Follow[] TopUsers, int left, int right) {

                          /**
                           * Called by the method getTopUsers(). Functionally a quick sort, constituted by the
                           * methods swap() and part() (i.e., a partition-exchange sort).
                           */

                          if (right <= left) {
                              return;
                              }

                          int i = part(TopUsers, left, right);
                          sort(TopUsers, left, i-1);
                          sort(TopUsers, i+1, right);

                          }

                   private Node splitNode(Node currentNode) {

                          /**
                           * Method for B-Tree node splitting.
                           */

                          Node tempNode = new Node(childMax/2);         // Create new Node with (childMax/2) children.
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

                   /* ------ Inserting functions ------ */

                   private Node insert(Node currentNode, int key, int height) {

                          /**
                           * Method for B-Tree element insertion.
                           */

                          int i;
                          NodeData t = new NodeData(key, null);

                          /* Considers case where the node is external, i.e., a leaf */
                          if  (height == 0) {
                               for (i = 0; i < currentNode.childNo; i++) {
                                    /**
                                     * Use native comparison function to return true iff any children of the current node
                                     * joined strictly earlier than the instant represented by 'date'. If true, break.
                                     */ 
                                    if ((currentNode.ChildArray[i].key) < key) {
                                         break;
                                         }
                                    }
                               }
                          /* Considers case where the node is internal */
                          else {
                               for (i = 0; i < currentNode.childNo; i++) {
                                    if (((i + 1) == currentNode.childNo) || ((currentNode.ChildArray[i + 1].key) < key)) {
                                          Node inserted = insert(currentNode.ChildArray[i++].next, key, (height - 1));
                                          if (inserted == null) {
                                              return null;
                                              }
                                          t.key  = inserted.ChildArray[0].key;
                                          t.next = inserted;
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

                   public boolean insertPair(int key) {

                          /**
                           * Method for Follower B-Tree Date-Follow pair insertion.
                           */

                          Node inserted = insert(root, key, height);
                          size++;

                          if (inserted == null) {
                              return true;
                              }

                          /* Split root */

                          Node t = new Node(2);
                          t.ChildArray[0] = new NodeData(root.ChildArray[0].key, root);
                          t.ChildArray[1] = new NodeData(inserted.ChildArray[0].key, inserted);
                          root = t;
                          height++;

                          return true;

                          }

                   public boolean insertFollow(int key, int followKey, Date date) {

                          /**
                           * Method for Follower B-Tree Follow insertion.
                           */

                          /**
                           * If the User has no followers and no follows -- i.e., if it is
                           * not in the FollowerTree -- insert it into the tree.
                           */
                          if (!checkExistsFollower(key)) {
                               insertPair(key);
                              }

                          DateTree follows = getFollows(key);                            // Declare a DateTree within which to store follows.

                          /**
                           * If the follow exists in the User's tree,
                           * do not insert it.
                           */
                          if (follows.checkExistsDate(followKey)) {
                              return false;
                              }

                          follows.insertPair(new FollowPair(date, followKey));
                          return true;

                          }

                   public boolean insertFollower(int key, int followerKey, Date date) {

                          /**
                           * Method for Follower B-Tree Follower insertion; exact analogue of
                           * the above method for followers as opposed to follows.
                           */

                          if (!checkExistsFollower(key)) {
                               insertPair(key);
                              }

                          DateTree followers = getFollowers(key);

                          if (followers.checkExistsDate(followerKey)) {
                              return false;
                              }

                          followers.insertPair(new FollowPair(date, followerKey));
                          return true;

                          }

                   /* ------ Searching functions ------ */

                   private DateTree findFollows(Node x, int key, int height) {

                          /**
                           * Return those indices of ChildArray which store as elements
                           * the follows of the user. Used by the method getFollows().
                           */

                          NodeData[] ChildArray = x.ChildArray;

                          /* Considers case where the node is external, i.e., a leaf */
                          if  (height == 0) {
                               for (int j = 0; j < x.childNo; j++) {
                                    if (key == ChildArray[j].key) {
                                        return ChildArray[j].follows;
                                        }
                                    }
                               }

                          /* Considers case where the node is internal */
                          else {
                               for (int j = 0; j < x.childNo; j++) {
                                    if ((j + 1) == x.childNo || (ChildArray[j + 1].key) < key) {
                                        return findFollows(ChildArray[j].next, key, height-1);
                                        }
                                    }
                               }

                            return null;

                          }

                   private DateTree findFollowers(Node x, int key, int height) {

                          /**
                           * Return those indices of ChildArray which store as elements
                           * the followers of the user. Used by the method getFollowers().
                           */

                          NodeData[] ChildArray = x.ChildArray;

                          /* Considers case where the node is external, i.e., a leaf */
                          if  (height == 0) {
                               for (int j = 0; j < x.childNo; j++) {
                                    if (key == ChildArray[j].key) {
                                        return ChildArray[j].followers;
                                        }
                                    }
                               }

                          /* Considers case where the node is internal */
                          else {
                               for (int j = 0; j < x.childNo; j++) {
                                    if ((j + 1) == x.childNo || (ChildArray[j+1].key) < key) {
                                        return findFollowers(ChildArray[j].next, key, height-1);
                                        }
                                    }
                               }

                            return null;

                          }

                   private void searchForID(Node x, int key, int height) {

                          /**
                           * Searching function for User IDs.
                           */

                          NodeData[] ChildArray = x.ChildArray;

                          /* Considers case where the node is external, i.e., a leaf */
                          if  (height == 0) {
                               for (int j = 0; j < x.childNo; j++) {
                                    if (key == ChildArray[j].key) {
                                        c++;
                                        }
                                    }
                               }

                          /* Considers case where the node is internal */
                          else {
                               for (int j = 0; j < x.childNo; j++) {
                                    if ((j + 1) == x.childNo || key > ChildArray[j + 1].key) {
                                         searchForID(ChildArray[j].next, key, height-1);
                                        }
                                    }
                               }

                          }

                   private void searchFollows(Node x, int height) {

                          /**
                           * Searching function for follows.
                           */

                         NodeData[] ChildArray = x.ChildArray;

                          /* Considers case where the node is external, i.e., a leaf */
                          if (height == 0) {
                              for (int j = 0; j < x.childNo; j++) {
                                   TopUsers[c] = (new Follow(ChildArray[j].key, ChildArray[j].followers.getSize()));
                                   c++;
                                   }
                              }

                          /* Considers case where the node is internal */
                          else {
                               for (int j = 0; j < x.childNo; j++) {
                                    searchFollows(ChildArray[j].next, height-1);
                                    }
                               }

                          }

                   /* -------- 'Get' functions -------- */

                   public int[] getMutualFollowers(int key1, int key2) {

                          /**
                           * Return an array of those users who are followers of the users with IDs
                           * corresponding to the parameters 'key1' and 'key2'.
                           */

                          FollowPair[] a;
                          DateTree     b;

                          if  (getFollowers(key1).getSize() < getFollowers(key2).getSize()) {
                               a = getFollowers(key1).toArray();
                               b = getFollowers(key2);
                               }
                          else {
                               a = getFollowers(key2).toArray();
                               b = getFollowers(key1);
                               }

                          DateTree mutual = new DateTree();

                          for (int i = 0; i < a.length; i++) {
                               FollowPair x = b.getUser(a[i].userID);
                               if (x != null) {
                                   if (x.whenJoined.before(a[i].whenJoined)) {
                                       x.whenJoined = a[i].whenJoined;
                                       }
                                   mutual.insertPair(x);
                                   }
                               }

                          return mutual.toUserArray();

                          }


                   public int[] getMutualFollows(int key1, int key2) {

                          /**
                           * Return an array of those users who follow the users with IDs corresponding
                           * to the parameters 'key1' and 'key2'.
                           */

                          FollowPair[] a;
                          DateTree     b;

                          if  (getFollows(key1).getSize() < getFollows(key2).getSize()) {
                               a = getFollows(key1).toArray();
                               b = getFollows(key2);
                               }
                          else {
                               a = getFollows(key2).toArray();
                               b = getFollows(key1);
                               }

                          DateTree mutual = new DateTree();

                          for (int i = 0; i < a.length; i++) {
                               FollowPair x = b.getUser(a[i].userID);
                               if (x != null) {
                                   if (x.whenJoined.before(a[i].whenJoined)) {
                                       x.whenJoined = a[i].whenJoined;
                                       }
                                   mutual.insertPair(x);
                                   }
                               }

                          return mutual.toUserArray();

                          }

                   public int[] getTopUsers() {

                          TopUsers    = new Follow[size];
                          int[] array = new int[size];

                          c = 0;

                          searchFollows(root,height);
                          sort(TopUsers, 0, (TopUsers.length - 1));

                          c = 0;

                          for (int i = (TopUsers.length - 1); i >= 0; i--) {
                               array[c] = TopUsers[i].id;
                               c++;
                               }

                            return array;

                          }

           private class Follow {

                  private int count;
                  private int id;
                  private Follow(int newid, int size) {
                          id    = newid;
                          count = size;
                          }
                  }

           }


    private class FollowPair {

           private Date whenJoined;
           private int userID;

           private FollowPair(Date date, int iduser) {
                   this.whenJoined = date;
                   userID          = iduser;
                   }

           }
    /* ------------------------------ Implementations for follower store by date (DateTree) ----------------------------- */

    private class DateTree  {

           private static final int childMax = 4;         // Every internal node contains a maximum of (childMax) children; the number of elements of any node is thus (childMax - 1) by definition.

                   private final class Node {

                          private int childNo;                                            // Declare a variable to store the number of children a node currently has.
                          private NodeData[] ChildArray = new NodeData[childMax];         // Declare an array of children, of size 4 (max. child nodes).

                          private Node(int c) {
                                 childNo = c;                                             // Construct a node with c children.
                                 }

                          }

                   private final class NodeData {

                          /**
                           * whenJoined and genUser constitute a key-value pair. Internal nodes use whenJoined and next;
                           * external (leaf) nodes use whenJoined and genUser.
                           */

                          private Date whenJoined;
                          private FollowPair genUser;
                          private Node next;

                                 public NodeData(Date whenJoined, FollowPair genUser, Node next) {
                                        this.whenJoined = whenJoined;    // genUser.getDateJoined();
                                        this.genUser = genUser;
                                        this.next = next;
                                        }

                          }

           /* -- Generic configuration for the BinaryTree -- */

           private Node root;                                            // Declare the root of the BinaryTree.
           private int size;                                             // Declare a variable to store the number of users paired with dates ('key-value pairs') in the BinaryTree.
           private int height;                                           // Declare a variable to monitor the height of the BinaryTree.
           private int c;                                                // Declare a variable to monitor the children per node.
           private FollowPair Temp;

                   /* Constructors, Getters and Setters */

                   public DateTree() {
                          root = new Node(0);
                          }

                   public int getSize() {
                          return size;
                          }

                   public int getHeight() {
                          return height;
                          }

                   public FollowPair getUser(int key) {
                          Temp = null;
                          c = 0;
                          search(root, key, height);
                          return Temp;
                          }

                   /* --------------------------------- */

           /* ---------------------------------------------- */

                   /* ------ Checking functions ------- */

                   public boolean checkExistsDate(int key) {

                          /**
                           * Check if the given key is in the date tree (DateTree).
                           */

                          c = 0;
                          searchForID(root, key, height);
                          return (c != 0);

                          }

                   /* ------- Sorting functions ------- */

                   private Node splitNode(Node currentNode) {

                          /**
                           * Method for B-Tree node splitting.
                           */

                          Node tempNode = new Node(childMax/2);         // Create new Node with (childMax/2) children.
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

                   /* ------ Inserting functions ------ */

                   public void insertPair(FollowPair genUser) {

                          /**
                           * Method for B-Tree Date-User pair insertion.
                           */

                          Date date = genUser.whenJoined;

                          Node inserted = insert(root, date, genUser, height);
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

                   private Node insert(Node currentNode, Date date, FollowPair genUser, int height) {

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

                   /* ------ Searching functions ------ */

                   private void search(Node x, int key, int height) {

                          NodeData[] ChildArray = x.ChildArray;

                          /* Considers case where the node is external, i.e., a leaf */
                          if  (height == 0) {
                               for (int j = 0; j < x.childNo; j++) {
                                    if (key == ChildArray[j].genUser.userID) {
                                        Temp = ChildArray[j].genUser;
                                        }
                                    }
                               }

                          /* Considers case where the node is internal */
                          else {
                               for (int j = 0; j < x.childNo; j++) {
                                    search(ChildArray[j].next, key, height-1);
                                    }
                               }

                          }

                   private void searchForID(Node x, int key, int height) {

                          NodeData[] ChildArray = x.ChildArray;

                          /* Considers case where the node is external, i.e., a leaf */
                          if  (height == 0) {
                               for (int j = 0; j < x.childNo; j++) {
                                    if (key == ChildArray[j].genUser.userID) {
                                        c++;
                                        return;
                                        }
                                    }
                               }

                          /* Considers case where the node is internal */
                          else {
                               for (int j = 0; j < x.childNo; j++) {
                                    searchForID(ChildArray[j].next, key, height-1);
                                    }
                               }

                          }

                   /* ---------- toArray Methods ---------- */

                   /**
                    * Functions used (indirectly, for toArray(*)) in the main methods.
                    */

                   public FollowPair[] toArray() {

                          c = 0;

                          FollowPair[]  result = new FollowPair[size];
                          toArray(root, height, result);

                          return result;

                          }

                   private void toArray(Node currentNode, int height, FollowPair[] result) {

                          NodeData[] ChildArray = currentNode.ChildArray;

                          if  (height == 0) {
                               for (int j = 0; j < currentNode.childNo; j++) {
                                    result[c] = ChildArray[j].genUser;
                                    c++;
                                    }
                               }
                          else {
                               for (int j = 0; j < currentNode.childNo; j++) {
                                    toArray(ChildArray[j].next, height-1, result);
                                    }
                               }

                          }

                   public int[] toUserArray() {

                          c = 0;

                          int[] result = new int[size];
                          toUserArray(root, height, result);

                          return result;

                          }

                   private void toUserArray(Node currentNode, int height, int[] result) {

                          NodeData[] ChildArray = currentNode.ChildArray;

                          if  (height == 0) {
                               for (int j = 0; j < currentNode.childNo; j++) {
                                    result[c] = ChildArray[j].genUser.userID;
                                    c++;
                                    }
                               }
                          else {
                               for (int j = 0; j < currentNode.childNo; j++) {
                                    toUserArray(ChildArray[j].next, height-1, result);
                                    }
                               }

                          }

           }

    }