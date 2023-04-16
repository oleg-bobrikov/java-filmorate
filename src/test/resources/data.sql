MERGE INTO mpa_film_ratings KEY (ID) VALUES (1, 'G');
MERGE INTO mpa_film_ratings KEY (ID) VALUES (2, 'PG');
MERGE INTO mpa_film_ratings KEY (ID) VALUES (3, 'PG-13');
MERGE INTO mpa_film_ratings KEY (ID) VALUES (4, 'R');
MERGE INTO mpa_film_ratings KEY (ID) VALUES (5, 'NC-17');
MERGE INTO GENRES KEY (ID) VALUES (1, 'Комедия');
MERGE INTO GENRES KEY (ID) VALUES (2, 'Драма');
MERGE INTO GENRES KEY (ID) VALUES (3, 'Мультфильм');
MERGE INTO GENRES KEY (ID) VALUES (4, 'Триллер');
MERGE INTO GENRES KEY (ID) VALUES (5, 'Документальный');
MERGE INTO GENRES KEY (ID) VALUES (6, 'Боевик');

INSERT INTO USERS (ID,EMAIL,LOGIN,"name",BIRTHDAY) VALUES
	 (1,'ivan@ya.ru','oleg@ya.ru','ivan','1990-01-01'),
	 (2,'oleg@ya.ru','oleg@ya.ru','oleg','1981-05-21');