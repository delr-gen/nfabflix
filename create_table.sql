DROP DATABASE IF EXISTS moviedb; 
CREATE DATABASE moviedb;
USE moviedb;

CREATE TABLE IF NOT EXISTS movies(
	id varchar(10) PRIMARY KEY,
    title varchar(100) NOT NULL,
    year integer NOT NULL,
    director varchar(100) NOT NULL
    );
    
CREATE TABLE IF NOT EXISTS stars(
	id varchar(10) PRIMARY KEY NOT NULL,
    name varchar(100) NOT NULL,
    birthYear integer
    );

CREATE TABLE IF NOT EXISTS stars_in_movies(
	starId varchar(10) NOT NULL REFERENCES stars(id),
    movieId varchar(10) NOT NULL REFERENCES movies(id)
    );
    
CREATE TABLE IF NOT EXISTS genres(
	id integer PRIMARY KEY AUTO_INCREMENT,
    name varchar(32) NOT NULL
    );

CREATE TABLE IF NOT EXISTS genres_in_movies(
	genreId integer NOT NULL REFERENCES genres(id),
    movieId varchar(10) NOT NULL REFERENCES movies(id)
    );

CREATE TABLE IF NOT EXISTS customers(
    id integer PRIMARY KEY AUTO_INCREMENT,
    firstName varchar(50) NOT NULL,
    lastName varchar(50) NOT NULL,
    ccId varchar(20) NOT NULL REFERENCES creditcards(id),
    address varchar(200) NOT NULL,
    email varchar(50) NOT NULL,
    password varchar(20) NOT NULL
    );
    
CREATE TABLE IF NOT EXISTS sales(
	id integer PRIMARY KEY AUTO_INCREMENT,
    customerId integer NOT NULL REFERENCES customers(id),
    movieId varchar(10) NOT NULL REFERENCES movies(id),
    saleDate date NOT NULL
    );

CREATE TABLE IF NOT EXISTS creditcards(
	id varchar(20) PRIMARY KEY,
    firstName varchar(50) NOT NULL,
    lastName varchar(50) NOT NULL,
    expiration date NOT NULL
    );

CREATE TABLE IF NOT EXISTS ratings(
	movieId varchar(10) NOT NULL REFERENCES movies(id),
    rating float NOT NULL,
    numVotes integer NOT NULL
    );
    
CREATE TABLE IF NOT EXISTS employees(
	email varchar(50) primary key,
	password varchar(128) not null,
	fullname varchar(100)
);

ALTER TABLE movies ADD FULLTEXT(title);