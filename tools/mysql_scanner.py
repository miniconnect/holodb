import argparse
import sys
import re
from collections import OrderedDict
import math
import mysql.connector
from mysql.connector import FieldType
import pyaml

parser = argparse.ArgumentParser(
    prog = 'HoloDB MySQL scanner',
    description = 'Scans a MySQL database and outputs a corresponding HoloDB config YAML',
)
parser.add_argument(
    '-H', '--host',
    default="localhost",
    required=False,
    help='MySQL server host',
)
parser.add_argument(
    '-P', '--port',
    type=int,
    default=3306,
    required=False,
    help='MySQL server port',
)
parser.add_argument(
    '-u', '--user',
    default="root",
    required=False,
    help='MySQL login user',
)
parser.add_argument(
    '-p', '--password',
    default="",
    required=False,
    help='MySQL login password',
)
parser.add_argument(
    '-d', '--database',
    required=False,
    help='MySQL database',
)
parser.add_argument(
    '-D', '--database-filter',
    required=False,
    help='Regex pattern for filtering MySQL database; fullmatch; ignored if --database is used',
)
parser.add_argument(
    '-t', '--table',
    required=False,
    help='MySQL table (for single table scan)',
)
parser.add_argument(
    '-T', '--table-filter',
    required=False,
    help='Regex pattern for filtering MySQL table; fullmatch; ignored if --table is used',
)
parser.add_argument(
    '-F', '--database-table-filter',
    required=False,
    help='Regex pattern for filtering MySQL table; fullmatch for "dbname.tablename"',
)
parser.add_argument(
    '-w', '--writeable',
    required=False,
    action='store_true',
    help='Make output tables writeable'
)
parser.add_argument(
    '-a', '--all-databases',
    required=False,
    action='store_true',
    help='Include mysql and information_schema databases'
)
args = parser.parse_args()

conn = mysql.connector.connect(
    host=args.host,
    port=args.port,
    user=args.user,
    password=args.password,
)

output = OrderedDict()
output["seed"] = 42
output["schemas"] = []

if args.database is not None:
    databases = [args.database]
else:
    databases = []
    databases_list_cursor = conn.cursor()
    databases_list_cursor.execute(f"SHOW DATABASES")
    for row in databases_list_cursor:
        database = row[0]
        if (
            (args.all_databases or database.lower() not in ['information_schema', 'mysql']) and
            (args.database_filter is None or re.fullmatch(args.database_filter, database))
        ):
            databases.append(database)
    databases_list_cursor.close()

foreign_keys = {}

foreign_keys_cursor = conn.cursor()
foreign_keys_cursor.execute(
    """SELECT
      TABLE_SCHEMA,
      TABLE_NAME,
      COLUMN_NAME,
      REFERENCED_TABLE_SCHEMA,
      REFERENCED_TABLE_NAME,
      REFERENCED_COLUMN_NAME
    FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
    WHERE REFERENCED_COLUMN_NAME IS NOT NULL
    GROUP BY CONSTRAINT_NAME 
    HAVING COUNT(*) = 1"""
)
for row in foreign_keys_cursor:
    key = f"{row[0]}.{row[1]}.{row[2]}"
    if row[0] == row[3]:
        foreign_keys[key] = [row[4], row[5]]
    else:
        foreign_keys[key] = [row[3], row[4], row[5]]
foreign_keys_cursor.close()

for database in databases:
    escaped_database = "`" + database.replace("`", "``") + "`"

    if args.table is not None:
        tables = [args.table]
    else:
        tables = []
        table_list_cursor = conn.cursor()
        table_list_cursor.execute(f"SHOW TABLES IN {escaped_database}")
        for row in table_list_cursor:
            table = row[0]
            if args.table_filter is None or re.fullmatch(args.table_filter, table):
                tables.append(table)
        table_list_cursor.close()

    if args.database_table_filter is not None:
        tables = list(filter(lambda table: re.fullmatch(args.database_table_filter, f"{database}.{table}"), tables))

    schema_output = OrderedDict()
    schema_output["name"] = database
    schema_output["tables"] = []
    output["schemas"].append(schema_output)

    for table in tables:
        escaped_table = "`" + table.replace("`", "``") + "`"

        count_cursor = conn.cursor()
        count_cursor.execute(f"SELECT COUNT(*) FROM {escaped_database}.{escaped_table}")
        table_size = count_cursor.fetchall()[0][0]
        count_cursor.close()

        table_cursor = conn.cursor()
        table_cursor.execute(f"SELECT * FROM {escaped_database}.{escaped_table} LIMIT 1000")
        column_metadata = table_cursor.description
        column_count = len(column_metadata)
        column_datas = [[] for i in range(column_count)]
        for row in table_cursor:
            for i in range(column_count):
                column_datas[i].append(row[i])
        table_cursor.close()

        describe_cursor = conn.cursor()
        describe_cursor.execute(f"DESCRIBE {escaped_database}.{escaped_table}")
        describe_data = describe_cursor.fetchall()
        describe_cursor.close()

        table_output = OrderedDict()
        table_output["name"] = table
        table_output["size"] = table_size
        if (args.writeable):
            table_output["writeable"] = True
        table_output["columns"] = []
        schema_output["tables"].append(table_output)

        for i in range(column_count):
            column_info = column_metadata[i]
            describe_info = describe_data[i]
            column = column_info[0]
            column_type = FieldType.get_info(column_info[1])
            is_nullable = column_info[6] != 0

            integral_types = ["BIT", "TINY", "SHORT", "LONG", "LONGLONG", "INT24"]
            numeric_types = FieldType.get_number_types()
            string_types = FieldType.get_string_types()
            timestamp_types = FieldType.get_timestamp_types()

            column_output = OrderedDict()
            column_output["name"] = column

            if f"{database}.{table}.{column}" in foreign_keys:
                column_output["valuesForeignColumn"] = foreign_keys[f"{database}.{table}.{column}"]
            elif describe_info[3] == "PRI":
                column_output["mode"] = "COUNTER"
            elif column_info[1] in timestamp_types:
                column_output["valuesPattern"] = "202[0-3]\\-0[1-9]\\-[0-2][0-8] [0-1][0-9]:[0-5][0-9]:[0-5][0-9]"
            elif column_info[1] in timestamp_types:
                column_output["valuesPattern"] = "202[0-3]\\-0[1-9]\\-[0-2][0-8] [0-1][0-9]:[0-5][0-9]:[0-5][0-9]"
            elif column_type == "DATE" or column_type == "NEWDATE":
                column_output["valuesPattern"] = "202[0-3]\\-0[1-9]\\-[0-2][0-8]"
            elif column_type == "TIME":
                column_output["valuesPattern"] = "[0-1][0-9]:[0-5][0-9]:[0-5][0-9]"
            elif column_type == "YEAR":
                column_output["type"] = "java.lang.Integer"
                column_output["valuesPattern"] = "202[0-3]"
            elif column_info[1] in string_types:
                if re.match(r"enum\(", describe_info[1], flags=re.IGNORECASE):
                    column_output["values"] = [
                        re.sub(r"\\(.)", "\\1", x.group(1).replace("''", "'"))
                        for x in re.finditer(r"'((?:[^']|'')*)'", describe_info[1], flags=re.IGNORECASE)
                    ]
                else:
                    # TODO: guess
                    column_output["valuesBundle"] = "lorem"
            elif column_type == "BIT":
                column_output["type"] = "java.lang.Boolean"
                column_output["valuesRange"] = [0, 1]
            elif column_type in integral_types:
                sorted_values = sorted(filter(lambda value: value is not None, column_datas[i]))
                sorted_values_length = len(sorted_values)
                if sorted_values_length > 0:
                    column_output["valuesRange"] = [sorted_values[0], sorted_values[-1]]
                else:
                    column_output["type"] = "java.lang.Integer"
                    column_output["valuesRange"] = [1, 100]
            elif column_info[1] in numeric_types:
                column_output["type"] = "java.lang.Integer"
                column_output["valuesRange"] = [1, 10]
            elif is_nullable:
                column_output["type"] = "java.lang.Void"
                column_output["nullCount"] = table_size
            else:
                # FIXME: what to do?
                column_output["values"] = ["0"]

            if is_nullable and "nullCount" not in column_output:
                column_output["nullCount"] = math.floor(table_size * 0.2)

            table_output["columns"].append(column_output)

conn.close()

pyaml.dump(output, sys.stdout)
