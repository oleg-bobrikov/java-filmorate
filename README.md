# java-filmorate
## ER Diagram (https://dbdiagram.io/d/6414ac1e296d97641d88d727)
![ER Diagramm](filmorate.png)
## SQL Examples
### 1. Get films
```
SELECT *
FROM films
```
### 2. Get film by id
```
SELECT *
FROM films
WHERE id = &id
```
### 3. Add film
```
INSERT INTO films (name, description, release_date, duration, mpa_rating_id)
VALUES (&name, &description, &release_date, &duration, &mpa_rating_id)
```
### 4. Get users
```
SELECT *
FROM users
```
### 5. Get user by id
```
SELECT *
FROM users
WHERE id = &id
```
### 6. Add user
```
INSERT INTO users (email, login, name, birthday)
VALUES (&email, &login, &name, &birthday)
```
### 7. Get user friends
```
SELECT friend_id
FROM user_relations
WHERE user_id = &user_id and friendship_status = 'confirmed'
```
