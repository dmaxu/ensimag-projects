-- fichier avec la création de nos tables

DROP TABLE Utilisateur CASCADE CONSTRAINT;
DROP TABLE Produit CASCADE CONSTRAINT;
DROP TABLE Categorie CASCADE CONSTRAINT;
DROP TABLE SalleVente CASCADE CONSTRAINT;
DROP TABLE Vente CASCADE CONSTRAINT;
DROP TABLE VenteLibre CASCADE CONSTRAINT;
DROP TABLE VenteLimite CASCADE CONSTRAINT;
DROP TABLE Offre CASCADE CONSTRAINT;
DROP TABLE Caracteristique CASCADE CONSTRAINT;
DROP TABLE DescProduit CASCADE CONSTRAINT;

-- Table User
CREATE TABLE Utilisateur(
    email VARCHAR(50) CONSTRAINT pk_user PRIMARY KEY,
    nom_user VARCHAR(20) NOT NULL,
    prenom_user VARCHAR(20) NOT NULL,
    adresse_user VARCHAR(100) 
);

-- Table Produit 
CREATE TABLE Produit(
    id_produit INTEGER CONSTRAINT pk_produit PRIMARY KEY,
    nom_produit VARCHAR(20) NOT NULL,
    prix_revient DEC(10,2) CONSTRAINT check_prixRevient CHECK(prix_revient > 0),
    stock_vente INTEGER CONSTRAINT check_stockVente CHECK(stock_vente >= 0),
    email VARCHAR(50),
    CONSTRAINT FkEmail_Prod FOREIGN KEY (email) REFERENCES Utilisateur (email)
);

-- Table Categorie 
CREATE TABLE Categorie(
    nom_categorie VARCHAR(20) CONSTRAINT pk_categorie PRIMARY KEY,
    desc_cat VARCHAR(500)
);

-- Table SalleVente
CREATE TABLE SalleVente(
    id_salle INTEGER CONSTRAINT pk_salle PRIMARY KEY,
    offre_multiple NUMBER(1),
    revocable NUMBER(1),
    montante NUMBER(1),
    libre NUMBER(1),
    nom_categorie VARCHAR(20),
    CONSTRAINT FkCat_Vente FOREIGN KEY (nom_categorie) REFERENCES Categorie (nom_categorie)
);

-- Table Vente 
CREATE TABLE Vente(
    id_vente INTEGER CONSTRAINT pk_vente PRIMARY KEY,
    date_debut TIMESTAMP,
    prix_depart DEC(10,2) CONSTRAINT check_prixDepart CHECK(prix_depart > 0),
    offre_multiple NUMBER(1) CONSTRAINT check_offreMultiple CHECK(offre_multiple IN (0,1)),
    revocable NUMBER(1) CONSTRAINT check_revocable CHECK(revocable IN (0,1)),
    prix_baisse DEC(10,2) CONSTRAINT check_prixBaisse CHECK(prix_baisse >= 0),
    email VARCHAR(50),
    encours NUMBER(1) default 1,
    id_produit INTEGER,
    id_salle INTEGER,
    CONSTRAINT FkEmail_Vente FOREIGN KEY (email) REFERENCES Utilisateur (email),
    CONSTRAINT FkProduit_Vente FOREIGN KEY (id_produit) REFERENCES Produit (id_produit),
    CONSTRAINT FkSalle_Vente FOREIGN KEY (id_salle) REFERENCES SalleVente (id_salle)
);

-- Table Vente Libre
CREATE TABLE VenteLibre(
    id_vente INTEGER CONSTRAINT pk_ventelibre PRIMARY KEY,
    CONSTRAINT FkId_VenteLibre FOREIGN KEY (id_vente) REFERENCES Vente (id_vente)
);

--Table VenteLimite
CREATE TABLE VenteLimite(
    id_vente INTEGER CONSTRAINT pk_ventelimite PRIMARY KEY,
    date_fin TIMESTAMP NOT NULL,
    CONSTRAINT FkId_VenteLimite FOREIGN KEY (id_vente) REFERENCES Vente (id_vente)
);

-- Table Offre
CREATE TABLE Offre(
    date_offre TIMESTAMP,
    email VARCHAR(50),
    prix_offre DEC(10,2) CONSTRAINT check_prix CHECK (prix_offre > 0),
    quantite INTEGER CONSTRAINT check_quantite CHECK(quantite > 0),
    id_vente INTEGER,
    CONSTRAINT PkOffre PRIMARY KEY (date_offre, email),
    CONSTRAINT FkEmail_Offre FOREIGN KEY (email) REFERENCES Utilisateur (email),
    CONSTRAINT FkVente_Offre FOREIGN KEY (id_vente) REFERENCES Vente (id_vente)
);

-- Table Caractéristique    
CREATE TABLE Caracteristique(
    nom_caracteristique VARCHAR(20) CONSTRAINT pk_caracteristique PRIMARY KEY
);

-- Table pour décrire un produit
CREATE TABLE DescProduit(
    id_produit INTEGER,
    nom_caracteristique VARCHAR(20),
    valeur VARCHAR(100) NOT NULL,
    CONSTRAINT PkDescProduit PRIMARY KEY (id_produit, nom_caracteristique),
    CONSTRAINT FkIdProd_DescProd FOREIGN KEY (id_produit) REFERENCES Produit (id_produit),
    CONSTRAINT FkNomCar_DescProd FOREIGN KEY (nom_caracteristique) REFERENCES Caracteristique (nom_caracteristique)
);
