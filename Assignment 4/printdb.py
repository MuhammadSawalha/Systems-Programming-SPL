from persistence import *

def main():
    activities = repo.execute_command("""SELECT * FROM activities order by date;""")
    print("Activities") 
    for row in activities:
        array=(row[0],row[1],row[2],row[3].decode())
        print(array)
    branches = repo.execute_command("""SELECT * FROM branches order by id;""")
    print("Branches") 
    for row in branches:
        array=(row[0],row[1].decode(),row[2])
        print(array)
    print("Employees")
    employees = repo.execute_command("""SELECT * FROM employees order by id;""")
    for row in employees:
        array=(row[0],row[1].decode(),row[2],row[3])
        print(array)
    print("Products")
    products = repo.execute_command("""SELECT * FROM products order by id;""")
    for row in products:
        array=(row[0],row[1].decode(),row[2],row[3])
        print(array)
    print("Suppliers")
    suppliers = repo.execute_command("""SELECT * FROM suppliers order by id;""")
    for row in suppliers:
        array=(row[0],row[1].decode(),row[2].decode())
        print(array)
    print("\nEmployees report")
    employe_report = repo.execute_command("""SELECT employees.name, employees.salary, branches.location, COAlESCE(SUM(products.price * ABS(activities.quantity)),0)
    FROM employees
    left join activities ON employees.id = activities.activator_id
    left join products ON activities.product_id = products.id
    left join branches ON employees.branche = branches.id
    group by employees.name 
    order by employees.name""")
    for row in employe_report:
        line = row[0].decode()+" "+str(row[1])+" "+row[2].decode()+" "+str(row[3])
        print(line)
    activitie_report= repo.execute_command("""SELECT activities.date, products.description, activities.quantity, employees.name, suppliers.name
    FROM activities
    left join products ON activities.product_id = products.id
    left join employees ON activities.activator_id = employees.id
    left join suppliers ON activities.activator_id = suppliers.id
    order by date""")
    print("\nActivities report")
    for row in activitie_report:
        if(row[3]!= None):
            array=(row[0].decode(),row[1].decode(),row[2],row[3].decode(),row[4])
            print(array)
        if(row[4]!= None):
            array=(row[0].decode(),row[1].decode(),row[2],row[3],row[4].decode())
            print(array)
    pass

if __name__ == '__main__':
    main()