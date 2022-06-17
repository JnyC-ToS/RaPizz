DROP DATABASE IF EXISTS rapizz;
CREATE DATABASE rapizz;
USE rapizz;
SET GLOBAL DEFAULT_STORAGE_ENGINE = InnoDB;
-- SET GLOBAL TIME_ZONE = 'Europe/Paris';

/* Tables */

CREATE TABLE IF NOT EXISTS client (
	id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
	nom VARCHAR(255) NOT NULL,
	email VARCHAR(255) NOT NULL,
	mot_de_passe BINARY(64) NOT NULL,
	compte DECIMAL(7, 2) UNSIGNED DEFAULT 0.00 NOT NULL,
	compteur_fidelite TINYINT UNSIGNED DEFAULT '0' NOT NULL,
	CONSTRAINT id UNIQUE (id),
	CONSTRAINT idxu_client_email UNIQUE (email)
);

CREATE TABLE IF NOT EXISTS historique_operations_compte (
	id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
	compte BIGINT UNSIGNED NOT NULL,
	montant DECIMAL(7, 2) NOT NULL,
	date_operation TIMESTAMP NOT NULL,
	CONSTRAINT id UNIQUE (id),
	CONSTRAINT fk_historique_operations_compte_client FOREIGN KEY (compte) REFERENCES client (id) ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS ingredient (
	id SMALLINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
	nom VARCHAR(255) NOT NULL,
	cout DECIMAL(5, 2) UNSIGNED NOT NULL
);

CREATE TABLE IF NOT EXISTS livreur (
	id MEDIUMINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
	nom VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS pizza (
	id SMALLINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
	nom VARCHAR(255) NOT NULL,
	prix DECIMAL(5, 2) UNSIGNED NOT NULL
);

CREATE TABLE IF NOT EXISTS compose (
	pizza SMALLINT UNSIGNED NOT NULL,
	ingredient SMALLINT UNSIGNED NOT NULL,
	PRIMARY KEY (pizza, ingredient),
	CONSTRAINT fkComposeIngredient FOREIGN KEY (ingredient) REFERENCES ingredient (id) ON UPDATE CASCADE ON DELETE CASCADE,
	CONSTRAINT fkComposePizza FOREIGN KEY (pizza) REFERENCES pizza (id) ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS taille_pizza (
	id TINYINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
	nom VARCHAR(255) NOT NULL,
	modificateur_prix FLOAT UNSIGNED DEFAULT '1' NOT NULL
);

CREATE TABLE IF NOT EXISTS type_vehicule (
	id TINYINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
	nom VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS vehicule (
	id MEDIUMINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
	nom VARCHAR(255) NOT NULL,
	type TINYINT UNSIGNED NOT NULL,
	CONSTRAINT fk_vehicule_type_vehicule FOREIGN KEY (type) REFERENCES type_vehicule (id) ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS commande (
	id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
	client BIGINT UNSIGNED NOT NULL,
	pizza SMALLINT UNSIGNED NOT NULL,
	taille TINYINT UNSIGNED NOT NULL,
	date_commande TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
	gratuite TINYINT(1) DEFAULT 0 NOT NULL,
	livreur MEDIUMINT UNSIGNED NULL,
	vehicule MEDIUMINT UNSIGNED NULL,
	date_livraison TIMESTAMP NULL,
	CONSTRAINT id UNIQUE (id),
	CONSTRAINT fk_commande_client FOREIGN KEY (client) REFERENCES client (id) ON UPDATE CASCADE ON DELETE CASCADE,
	CONSTRAINT fk_commande_livreur FOREIGN KEY (livreur) REFERENCES livreur (id) ON UPDATE CASCADE ON DELETE SET NULL,
	CONSTRAINT fk_commande_pizza FOREIGN KEY (pizza) REFERENCES pizza (id) ON UPDATE CASCADE ON DELETE CASCADE,
	CONSTRAINT fk_commande_taille_pizza FOREIGN KEY (taille) REFERENCES taille_pizza (id) ON UPDATE CASCADE ON DELETE CASCADE,
	CONSTRAINT fk_commande_vehicule FOREIGN KEY (vehicule) REFERENCES vehicule (id) ON UPDATE CASCADE ON DELETE SET NULL
);

/* Views */

CREATE OR REPLACE VIEW __internal_classement_pizza AS
    SELECT p.*, count(c.id) AS nombre_de_commandes FROM pizza AS p LEFT OUTER JOIN commande AS c ON p.id = c.pizza GROUP BY p.id ORDER BY nombre_de_commandes DESC;

CREATE OR REPLACE VIEW classement_pizza AS
    SELECT ROW_NUMBER() OVER () AS classement, _.* FROM __internal_classement_pizza _;

DELIMITER $

/* Triggers */

CREATE TRIGGER log_historique_operations_compte_insert AFTER INSERT ON client FOR EACH ROW BEGIN
	IF NEW.compte != 0 THEN
		INSERT INTO historique_operations_compte (compte, montant, date_operation) VALUE (NEW.id, NEW.compte, NOW());
	END IF;
END $

CREATE TRIGGER log_historique_operations_compte_update AFTER UPDATE ON client FOR EACH ROW BEGIN
	IF NEW.compte != OLD.compte THEN
		INSERT INTO historique_operations_compte (compte, montant, date_operation) VALUE (NEW.id, NEW.compte - OLD.compte, NOW());
	END IF;
END $

/* Functions */

CREATE FUNCTION en_retard(id_commande BIGINT UNSIGNED) RETURNS BOOLEAN BEGIN
	DECLARE retard BOOLEAN;
	DECLARE CONTINUE HANDLER FOR NOT FOUND SET retard = FALSE;
	SELECT ((date_livraison IS NOT NULL) AND (TIMESTAMPDIFF(MINUTE, date_commande, date_livraison) > 30)) INTO retard FROM commande WHERE id = id_commande;
	RETURN retard;
END $

CREATE FUNCTION prix_commande(id_commande BIGINT UNSIGNED) RETURNS DECIMAL(5, 2) BEGIN
    DECLARE prix_final DECIMAL(5, 2);
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET prix_final = -1;
	SELECT pizza.prix * taille_pizza.modificateur_prix INTO prix_final FROM commande INNER JOIN pizza ON commande.pizza = pizza.id INNER JOIN taille_pizza ON commande.taille = taille_pizza.id WHERE commande.id = id_commande;
    RETURN prix_final;
END $

DELIMITER ;
