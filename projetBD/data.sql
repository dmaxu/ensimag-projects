INSERT INTO Utilisateur (email, nom_user, prenom_user, adresse_user)VALUES('alice@gmail.com', 'Doe', 'Alice', '123 Apple St');
INSERT INTO Utilisateur (email, nom_user, prenom_user, adresse_user)VALUES('bob@gmail.com', 'Smith', 'Bob', '456 Orange Ave');
INSERT INTO Utilisateur (email, nom_user, prenom_user, adresse_user)VALUES('charlie@gmail.com', 'Johnson', 'Charlie', '789 Banana Blvd');

INSERT INTO Categorie (nom_categorie, desc_cat)VALUES ('Electronics', 'Devices such as phones, laptops, and accessories');
INSERT INTO Categorie (nom_categorie, desc_cat)VALUES ('Furniture', 'Various home and office furniture items');
INSERT INTO Categorie (nom_categorie, desc_cat)VALUES ('Clothing', 'Fashionable clothes for men, women, and children');

INSERT INTO Produit (id_produit, nom_produit, prix_revient, stock_vente, email) VALUES (1, 'Laptop', 500.00, 5, 'alice@gmail.com');
INSERT INTO Produit (id_produit, nom_produit, prix_revient, stock_vente, email) VALUES (2, 'Sofa', 150.00, 3, 'alice@gmail.com');
INSERT INTO Produit (id_produit, nom_produit, prix_revient, stock_vente, email) VALUES (3, 'Jacket', 50.00, 10, 'alice@gmail.com');

INSERT INTO Caracteristique (nom_caracteristique) VALUES ('brand');
INSERT INTO Caracteristique (nom_caracteristique) VALUES ('color');
INSERT INTO Caracteristique (nom_caracteristique) VALUES ('size');

INSERT INTO DescProduit (id_produit, nom_caracteristique, valeur) VALUES (1, 'brand', 'Apple');
INSERT INTO DescProduit (id_produit, nom_caracteristique, valeur) VALUES (2, 'color', 'Red');
INSERT INTO DescProduit (id_produit, nom_caracteristique, valeur) VALUES (3, 'size', 'L');

commit;
