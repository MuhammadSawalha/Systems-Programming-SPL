from persistence import *

import sys


def sale(splittedline : list):
    
    pass
def supply(splittedline : list):
    
    product=repo._conn.execute("""SELECT * FROM products where id = ? """,[splittedline[0]]).fetchall()
    New_quantity=str(int(splittedline[1])+int(product[0][3]))
    repo._conn.execute("""UPDATE products SET quantity = ? where id = ?""",[New_quantity,splittedline[0]])
    pass




def main(args : list):
    inputfilename : str = args[1]
    with open(inputfilename) as inputfile:
        for line in inputfile:
            splittedline : list[str] = line.strip().split(", ")
            repo.activities.insert(Activitie(splittedline[0],splittedline[1],splittedline[2],splittedline[3]))
            product=repo._conn.execute("""SELECT * FROM products where id = ? """,[splittedline[0]]).fetchall()
            New_quantity=str(int(splittedline[1])+int(product[0][3]))
            repo._conn.execute("""UPDATE products SET quantity = ? where id = ?""",[New_quantity,splittedline[0]])
            # if(int(splittedline[1])>0):
            #     supply(splittedline)
            # else:
            #     sale(splittedline)

if __name__ == '__main__':
    main(sys.argv)