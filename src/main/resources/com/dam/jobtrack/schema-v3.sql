CREATE TABLE IF NOT EXISTS CONTACTO (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_empresa INT NOT NULL,
    nombre VARCHAR(150) NOT NULL,
    email VARCHAR(254) NOT NULL,
    telefono VARCHAR(40),
    puesto VARCHAR(150),
    fechaContacto DATE NOT NULL,
    notas TEXT,
    CONSTRAINT fk_contacto_empresa FOREIGN KEY (id_empresa) REFERENCES EMPRESA(id)
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
