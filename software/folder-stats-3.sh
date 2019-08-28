#!/bin/bash

# folder-stats-3.sh, auth. Zak Edwards

# ----------------------------------------------------------------
#                       Set inital variables                      
# ----------------------------------------------------------------

filec=0     # Non-hidden files in the specified directory
hfilec=0    # Hidden files in the specified directory

dirc=0      # Non-hidden directories in the specified directory
hdirc=0     # Hidden directories in the specified directory

totals=0    # Total space usage of the directory in bytes

# ----------------------------------------------------------------
#                   Declare an associative array                  
# ----------------------------------------------------------------

declare -A array

# ----------------------------------------------------------------
#   Conditional to consider the case where no path is specified   
# ----------------------------------------------------------------

if [ "$#" -lt "1" ]    # If no input; i.e., if the number of command-line arguments passed to the shell is less than 1
    then
        printf "Please specify a directory.\nusage: ./folder-stats-1.sh <directory>\n"    # Print an appropriate message
        exit 0
fi

# ----------------------------------------------------------------
#                      The 'count' function                       
#                                                                 
# Determines whether items in the given directory are (standard)  
# files or directories and increments the appropriate variables.  
# In either case, numerical data is added to the 'totals' variable
# by using 'ls' and an 'awk' filter. Data of individual files is  
# also stored in an associative array.                            
#                                                                 
# This function considers two cases:                              
# 1) Non-hidden items (in "$1"/*)                                 
# 2) Hidden items (in "$1"/.*), excluding the directories './' and
# '../'. If these directories were included the function would be 
# caught in an infinite loop.                                     
# ----------------------------------------------------------------

count() {
    # Store the number of non-hidden files and directories in the variables 'filec' and 'dirc', respectively
    for item in "$1"/*
        do
            if [[ -f "$item" && ! -L "$item" ]]                                         # If $item is a regular file and not symbolic,
                then                                                                    # then...
                    ((filec++)) ;                                                       # Increment the 'filec' variable; a file has been found
                    totals=$[$totals+$(ls -l "$item" | awk -F " " '{ print $5 }')] ;    # Read the fifth column of the output of 'ls'; add this data to 'totals'
                    array[$item]=$(ls -dl "$item" | awk -F " " '{print $5}')            # Assign the element 'item' to its file size in bytes
            elif [[ -d "$item" && ! -L "$item" ]]                                       # If $item is a directory and not symbolic,
                then                                                                    # then...
                    ((dirc++)) ;                                                        # Increment the 'dirc' variable; a directory has been found
                    totals=$[$totals+$(ls -dl "$item" | awk -F " " '{ print $5 }')] ;
                    count "$item"                                                       # Recursion; allow the function to explore sub-directories
            fi
        done

    # Repeat the process for hidden files; store values in the variables 'hfilec' and 'hdirc'
    for item in "$1"/.*
        do
            if [[ -f "$item" && ! -L "$item" ]]
                then
                    ((hfilec++)) ;
                    totals=$[$totals+$(ls -l "$item" | awk -F " " '{ print $5 }')] ;
            	      array[$item]=$(ls -dl "$item" | awk -F " " '{print $5}')
            elif [[ -d "$item" && ! -L "$item" &&  $item != "$1/.." && $item != "$1/." ]]
                then
                    ((hdirc++)) ;
                    totals=$[$totals+$(ls -dl "$item" | awk -F " " '{ print $5 }')] ;
                    count "$item"
            fi
        done
}

# ----------------------------------------------------------------
#                      The 'convert' function                     
#                                                                 
# Converts a file's size in bytes to a human-readable format.  
# ----------------------------------------------------------------

convert() {
    #if [ "$1" -ge "500" ]
    #   then
            echo $1 | awk '{ 
                            sum=$1 ;
    			                  hum[1024**3]="GB";hum[1024**2]="MB";hum[1024]="KB";
    			                  for (x=1024**3; x>=1024; x/=1024) { 
    				                    if (sum>=x) { 
                                    printf "%.2f %s\n",sum/x,hum[x];
    			                          break;  
                                }
                            } if (sum<1024) printf sum "B"; 
                           }'
    #else
    #    echo "$1"B
    #fi

    #echo "$1" | awk '{
    #    ex   = index("KMG", substr($1, length($1)))
    #    val  = substr($1, 0, length($1) - 1)
    #    prod = val * 10^(ex * 3)
    #    sum += prod
    #}
    #END {print sum}'

    #echo "$1" | awk -v sum="$1" ' BEGIN {
    #                                      hum[1024^3]="Gb"; 
    #                                      hum[1024^2]="Mb"; 
    #                                      hum[1024]="Kb"; 
    #                                      for (x=1024^3; x>=1024; x/=1024) { 
    #                                          if (sum>=x) { 
    #                                              printf "%.2f %s\n",sum/x,hum[x]; 
    #                                              break; 
    #                                          } 
    #                                      } 
    #                                      if (sum<1024) printf sum "B"; 
    #                                    } '
}

# ----------------------------------------------------------------
#                    The 'sortarray' function                     
#                                                                 
# Prints the largest files in the directory, in descending order  
# of file size.                                                   
# ----------------------------------------------------------------

sortarray() {
    for a in "${!array[@]}"                                # For every element 'a' in the array...
        do
            printf "\t%-30s %s\n" "$a" "${array["$a"]}"    # Print the value of 'a' (the file path) and its associated value (the file size)  #"$(convert ${array["$a"]})"
        done |                                             # Pipe the output of 'printf' to 'sort'
    sort -rn -k2 | awk '{ 
                          sum=$2 ;
    			                hum[1024**3]="GB";
                          hum[1024**2]="MB";
                          hum[1024]="KB";
    			                for (x=1024**3; x>=1024; x/=1024) { 
    				                  if (sum>=x) { 
                                  printf "%.2f %s\n",sum/x,hum[x];
    			                        break;  
                              }
                          } if (sum<1024) printf sum "B\n"; 
                        }' | head -n 5                     # Order the file size in descending order of magnitude, pipe the output to 'head'

}

# ----------------------------------------------------------------
#                      The 'main' function                        
#                                                                 
# Calls the 'count function' and prints the desired information to
# the terminal screen.                                            
# ----------------------------------------------------------------

main() {
    count "$1"

    # Print the number of files and directories
    echo "Files found: $filec (+ $hfilec hidden)"
    echo "Folders found: $dirc (+ $hdirc hidden)"

    # Print the total number of files and directories
    let totalf=$filec+$hfilec+$dirc+$hdirc    # Declare a new variable to store the quantity sum of all files/directories
    echo "Total files and folders: $totalf"

    # Print the total space usage of the directory in bytes
    echo "Total size: $(convert $totals)"

    # Print the top five files of greatest size by calling the 'sortarray' function
    printf "Top 5 files:\n"
    sortarray
}

# Call the 'main' function
main "$1"