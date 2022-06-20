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

/* Data */

-- SOURCE data/rapizz_client.sql;
-- SOURCE data/rapizz_historique_operations_compte.sql;
-- SOURCE data/rapizz_pizza.sql;
-- SOURCE data/rapizz_ingredient.sql;
-- SOURCE data/rapizz_compose.sql;
-- SOURCE data/rapizz_taille_pizza.sql;
-- SOURCE data/rapizz_livreur.sql;
-- SOURCE data/rapizz_type_vehicule.sql;
-- SOURCE data/rapizz_vehicule.sql;
-- SOURCE data/rapizz_commande.sql;

DELIMITER $

/* Triggers */

CREATE TRIGGER log_historique_operations_compte_insert AFTER INSERT ON client FOR EACH ROW BEGIN
	IF NEW.compte != 0 THEN
		INSERT INTO historique_operations_compte (compte, montant, date_operation) VALUE (NEW.id, NEW.compte, NOW());
	END IF;
END $

CREATE TRIGGER log_historique_operations_compte_update AFTER UPDATE ON client FOR EACH ROW BEGIN
	IF NEW.compte != OLD.compte THEN
		INSERT INTO historique_operations_compte (compte, montant, date_operation) VALUE (NEW.id, CAST(NEW.compte AS DECIMAL(7, 2)) - OLD.compte, NOW());
	END IF;
END $

CREATE TRIGGER gestion_commande_insert AFTER INSERT ON commande FOR EACH ROW BEGIN
	IF NEW.gratuite THEN
		UPDATE client SET compteur_fidelite = 0 WHERE id = NEW.client;
	ELSE
		UPDATE client SET compteur_fidelite = compteur_fidelite + 1, compte = compte - prix_commande(NEW.id) WHERE id = NEW.client;
	END IF;
END $

CREATE TRIGGER gestion_commande_update AFTER UPDATE ON commande FOR EACH ROW BEGIN
	IF NOT NEW.gratuite AND OLD.date_livraison IS NULL AND NEW.date_livraison IS NOT NULL AND en_retard(NEW.id) THEN
		UPDATE client SET compte = compte + prix_commande(NEW.id) WHERE id = NEW.client;
	END IF;
END $

CREATE TRIGGER gestion_commande_delete AFTER DELETE ON commande FOR EACH ROW BEGIN
	IF OLD.gratuite THEN
		UPDATE client SET compteur_fidelite = 10 WHERE id = OLD.client;
	ELSE
		UPDATE client SET compteur_fidelite = GREATEST(compteur_fidelite - 1, 0), compte = compte + prix_commande(OLD.id) WHERE id = OLD.client;
	END IF;
END $

/* Functions */

CREATE FUNCTION en_retard(id_commande BIGINT UNSIGNED) RETURNS BOOLEAN READS SQL DATA BEGIN
	DECLARE retard BOOLEAN;
	DECLARE CONTINUE HANDLER FOR NOT FOUND SET retard = FALSE;
	SELECT ((date_livraison IS NOT NULL) AND (TIMESTAMPDIFF(MINUTE, date_commande, date_livraison) > 30)) INTO retard FROM commande WHERE id = id_commande;
	RETURN retard;
END $

CREATE FUNCTION prix_commande(id_commande BIGINT UNSIGNED) RETURNS DECIMAL(5, 2) READS SQL DATA BEGIN
    DECLARE prix_final DECIMAL(5, 2);
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET prix_final = 0;
	SELECT pizza.prix * taille_pizza.modificateur_prix INTO prix_final FROM commande INNER JOIN pizza ON commande.pizza = pizza.id INNER JOIN taille_pizza ON commande.taille = taille_pizza.id WHERE commande.id = id_commande;
    RETURN prix_final;
END $

DELIMITER ;
