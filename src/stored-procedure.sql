USE moviedb;

DROP PROCEDURE IF EXISTS add_star;
DROP PROCEDURE IF EXISTS add_movie;
DROP PROCEDURE IF EXISTS add_genre;

DELIMITER $$

CREATE PROCEDURE add_star(IN starName varchar(20), IN birthYear INT)
BEGIN
    INSERT INTO stars(id, name, birthYear) SELECT CONCAT("nm", CAST((CAST(SUBSTRING(max(id),3) AS UNSIGNED)+1) AS char(18))), starName, birthYear FROM stars; 
END
$$


CREATE PROCEDURE add_genre(IN genreName varchar(20))
BEGIN
    INSERT INTO genres(id, name) SELECT MAX(id)+1 AS id, genreName FROM genres;
END
$$


CREATE PROCEDURE add_movie(IN movieId varchar(20), IN title varchar(20), IN yr int, IN director varchar(20), IN starName varchar(20), IN genre varchar(20))
BEGIN
    INSERT INTO movies(id, title, year, director) VALUES (movieId, title, yr, director);

    IF NOT (SELECT COUNT(*) FROM stars WHERE name=starName) THEN
        CALL add_star(starName, null);
    END IF;

    IF NOT (SELECT COUNT(*) FROM genres WHERE name=genre) THEN
        CALL add_genre(genre);
    END IF;

    INSERT INTO genres_in_movies(genreId, movieId) SELECT id, movieId FROM genres WHERE name=genre;
    INSERT INTO stars_in_movies(starId, movieId) SELECT id, movieId FROM stars WHERE name=starName LIMIT 1;
END
$$

DELIMITER ;