-- Instalación nueva. No elimina ni modifica tablas ya existentes.
CREATE DATABASE IF NOT EXISTS proyecto_cotizaciones CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE proyecto_cotizaciones;
CREATE TABLE IF NOT EXISTS Cotizacion (
    idCotizacion INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    numCotizacion VARCHAR(15) NOT NULL UNIQUE,
    descripcion VARCHAR(50) NOT NULL,
    precio FLOAT NOT NULL,
    porcentaje FLOAT NOT NULL,
    plazos INT NOT NULL,
    fecha DATE NOT NULL,
    status INT NOT NULL DEFAULT 0
) ENGINE=InnoDB;
-- Convención del profesor: 0 = habilitada, 1 = deshabilitada.
