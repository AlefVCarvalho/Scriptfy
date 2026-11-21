CREATE DATABASE scriptfy CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE scriptfy;

CREATE TABLE musicas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    album VARCHAR(100) NOT NULL,
    artistas VARCHAR(100) NOT NULL,
    tempo_segundos INT NOT NULL,
    arquivo_audio VARCHAR(255),
    imagem_capa VARCHAR(255)
);