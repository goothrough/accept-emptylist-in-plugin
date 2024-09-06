# accept-emptylist-in-plugin

## Overview
Custom plugin for Mybatis Generator.<br>
Helps to accept an empty list when using in-clause.

## Technologies Used
<p style="display: inline">
    <img src="https://img.shields.io/badge/-Java-007396.svg?logo=java&style=plastic">
    <img src="https://img.shields.io/badge/-Mysql-4479A1.svg?logo=mysql&style=plastic">
</p>

## The Purpose
This plugin allows to use an in-clause with an empty list.<br>
With a neutral Mybatis Generator, an empty list is NOT acceptable when using in-clause.<br>
Because it generates a where-clause naturally with "in".<br>
For example, imagine a user table.

| id  | name | age | country |
| --- | ---- | --- | ------- |
| 1   | John | 33  | Canada  |
| 2   | Lin  | 25  | China   |
| 3   | Sho  | 28  | Japan   |

If you want to pick up people who are from Canada or China,<br>
the SQL is to be "`select * from user where country in ("Canada","China");`".<br>
In the program, you can set the condition with a string list of "Canada" and "China".<br>
But, if the list was dynamic because of the user's operation,
the list would sometimes be empty.<br>
In this case, the where-clause would be like "`select * from user where country in`".<br>
This would cause an error because the SQL is incorrect.<br>

This plugin allows to use an in-clause with an expected empty list by changing the where-clause to just "false" if only the list is empty.<br>
The where-clause is generated like "`select * from user where false;`".
