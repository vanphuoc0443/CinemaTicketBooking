-- Migration: Add director, imdb_rating, language, imdb_id to movies table

USE cinema_booking;

ALTER TABLE movies
    ADD COLUMN director VARCHAR(255) DEFAULT NULL AFTER description,
    ADD COLUMN imdb_rating VARCHAR(10) DEFAULT NULL AFTER director,
    ADD COLUMN `language` VARCHAR(100) DEFAULT NULL AFTER imdb_rating,
    ADD COLUMN imdb_id VARCHAR(20) DEFAULT NULL AFTER `language`;

SELECT '✓ Added director, imdb_rating, language, imdb_id columns to movies table' AS Status;
