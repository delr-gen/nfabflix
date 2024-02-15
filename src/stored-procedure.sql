USE moviedb;

DROP PROCEDURE IF EXISTS add_star;
DROP PROCEDURE IF EXISTS add_movie;

DELIMITER $$

CREATE PROCEDURE add_star(IN starId varchar(20), IN starName varchar(20), IN birthYear INT)
BEGIN
    INSERT INTO stars(id, name, birthYear) VALUES (starId, starName, birthYear);
END
$$

CREATE PROCEDURE add_movie(IN movieId varchar(20), IN title varchar(20), IN yr int, IN director varchar(20), IN starName varchar(20), IN genre varchar(20))
BEGIN
    INSERT INTO movies(id, title, year, director) VALUES (movieId, title, yr, director);
    
END
$$

DELIMITER ;