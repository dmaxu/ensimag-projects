import java.sql.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Map;
import java.util.HashMap;
import java.text.SimpleDateFormat;

public class BaieElec {

    private static final String CONN_URL = "jdbc:oracle:thin:@oracle1.ensimag.fr:1521:oracle1";
    private static final String USER = "nairs";
    private static final String PASSWD = "nairs";
    private Connection con;

    public BaieElec() {
        try {
            // Register Oracle driver
            System.out.println("Loading Oracle driver...");
            DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());

            // Establish connection
            System.out.println("Connecting to the database...\n");
            Connection conn = DriverManager.getConnection(CONN_URL, USER, PASSWD);

            this.con = conn;
            
            // Close the connection
            //conn.close();

        } catch (SQLException e) {
            System.err.println("Connection failed");
            e.printStackTrace(System.err);
        }
    }

    public Connection getCon(){
        return this.con;
    }
    
    public void close_connection(){
        try{
            this.con.close();
        }
        catch(SQLException e){
            System.err.println("Failed to close connection");
            e.printStackTrace(System.err);
        }
        
    }

    /**
     * Cette méthode permet de créer un nouveau produit dans la base de données. 
     * Elle attribue un identifiant unique au produit, l'insère dans la table `Produit`, 
     * et associe les caractéristiques fournies au produit, si elles existent. 
     * En cas d'échec, la transaction est annulée.
     */
    public void createProduit(String nom_produit, double prix_revient, int stock_vente, String email, List<String[]> caracteristiques) {
        String maxIdQuery = "SELECT COALESCE(MAX(id_produit), 0) + 1 AS new_id FROM Produit";
        String insertProduitSQL = "INSERT INTO Produit (id_produit, nom_produit, prix_revient, stock_vente, email) VALUES (?, ?, ?, ?, ?)";
        try {
            this.con.setAutoCommit(false);
        } catch (SQLException e) {
            System.err.println("Erreur lors de la désactivation de l'auto-commit : " + e.getMessage());
        }
        try {
            // Récupérer le plus grand ID + 1 pour `id_produit`
            int newId = 1;
            try (PreparedStatement maxIdStmt = this.con.prepareStatement(maxIdQuery);
            ResultSet rs = maxIdStmt.executeQuery()) {
                if (rs.next()) {
                    newId = rs.getInt("new_id");
                }
            }

            // Insérer le produit
            try (PreparedStatement pstmt = this.con.prepareStatement(insertProduitSQL)) {
                pstmt.setInt(1, newId);          
                pstmt.setString(2, nom_produit); 
                pstmt.setDouble(3, prix_revient); 
                pstmt.setInt(4, stock_vente);     
                pstmt.setString(5, email);       
                pstmt.executeUpdate();
            }

            // Insérer les caractéristiques du produit si elles existent
            if (caracteristiques != null && !caracteristiques.isEmpty()) {
                for (String[] cv : caracteristiques) {
                    if (cv.length == 2) { 
                        checkAndInsertCaracteristique(cv[0]); 
                        addCaracteristiqueToProduit(newId, cv[0], cv[1]); 
                    }
                }
            }

            this.con.commit();
            System.out.println("Produit inséré avec succès avec l'ID : " + newId);

        } catch (SQLException e) {
            try {
                this.con.rollback();
            } catch (SQLException rollbackEx) {
                System.err.println("Échec du rollback : " + rollbackEx.getMessage());
            }
            System.err.println("Erreur lors de l'insertion du produit : " + e.getMessage());
        }
    }

    /**
     * Cette méthode ajoute une caractéristique à un produit spécifique dans la table `DescProduit'.
     * Elle s'assure que la caractéristique est associée à un produit existant, avec une valeur définie.
     */
    public void addCaracteristiqueToProduit(int id_produit, String nom_caracteristique, String valeur) {
        String insertDescProduitSQL = "INSERT INTO DescProduit (id_produit, nom_caracteristique, valeur) "
                                    + "VALUES (?, ?, ?)";

        try (PreparedStatement pstmt = this.con.prepareStatement(insertDescProduitSQL)) {
            pstmt.setInt(1, id_produit);
            pstmt.setString(2, nom_caracteristique);
            pstmt.setString(3, valeur);
            pstmt.executeUpdate();

            this.con.commit();
            System.out.println("Caractéristique ajoutée avec succès au produit !");

        } catch (SQLException e) {
            System.out.println("Erreur lors de l'ajout de la caractéristique au produit : " + e.getMessage());
        }
    }

    /**
     * Cette méthode vérifie si une caractéristique existe déjà dans la table `Caracteristique`. 
     * Si elle n'existe pas, elle l'ajoute à la base de données.
     */
    public void checkAndInsertCaracteristique(String nom_caracteristique) throws SQLException {
        String checkSQL = "SELECT COUNT(*) FROM Caracteristique WHERE nom_caracteristique = ?";
        String insertSQL = "INSERT INTO Caracteristique (nom_caracteristique) VALUES (?)";

        try (PreparedStatement checkStmt = this.con.prepareStatement(checkSQL)) {
            checkStmt.setString(1, nom_caracteristique);

            // Vérifier si la caractéristique existe
            ResultSet rs = checkStmt.executeQuery();
            rs.next();
            int count = rs.getInt(1);

            if (count == 0) {  // Si la caractéristique n'existe pas, l'insérer
                try (PreparedStatement insertStmt = this.con.prepareStatement(insertSQL)) {
                    insertStmt.setString(1, nom_caracteristique);
                    insertStmt.executeUpdate();
                    System.out.println("La caractéristique " + nom_caracteristique + " a été ajoutée à la base de données.");
                }
            }
        }
    }

    /**
     * Cette méthode vérifie si un email existe dans la table `Utilisateur`.
     * Elle retourne `true` si l'email existe, sinon `false`.
     */
    public static boolean verifyEmailExists(Connection conn, String email) {
        String query = "SELECT COUNT(*) FROM Utilisateur WHERE email = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, email);

            try (ResultSet rset = stmt.executeQuery()) {
                if (rset.next()) {
                    // Retourner vrai si le nombre est supérieur à 0
                    return rset.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la vérification de l'existence de l'email :");
            e.printStackTrace(System.err);
        }
        return false;
    }

    /**
     * Cette méthode insère un nouvel utilisateur dans la table `Utilisateur`. 
     * Elle utilise une gestion de transaction pour garantir l'intégrité des données.
     * Si l'utilisateur existe déjà, une exception est levée, et la transaction est annulée.
     */
    public static void createUser(Connection conn, String email, String nom, String prenom, String adresse) {
        // Désactiver l'auto-commit pour commencer une transaction
        try {
            conn.setAutoCommit(false);
        } catch (SQLException e) {
            System.err.println("Erreur lors de la désactivation de l'auto-commit : " + e.getMessage());
        }
        try {
            // Vérifier si l'utilisateur existe déjà
            String query = "SELECT email FROM Utilisateur WHERE email = ?";
            PreparedStatement stmtOwner = conn.prepareStatement(query);
            stmtOwner.setString(1, email);
            ResultSet rset = stmtOwner.executeQuery();

            String vEmailProprietaire = null;
            if (rset.next()) {
                vEmailProprietaire = rset.getString("email");
            }

            if (vEmailProprietaire != null && email.equals(vEmailProprietaire)) {
                throw new SQLException("Utilisateur existe déjà.");
            }

            query = "INSERT INTO Utilisateur (email, nom_user, prenom_user, adresse_user) VALUES (?, ?, ?, ?)";
            PreparedStatement stmtOffer = conn.prepareStatement(query);
            stmtOffer.setString(1, email);
            stmtOffer.setString(2, nom);
            stmtOffer.setString(3, prenom);
            stmtOffer.setString(4, adresse);

            stmtOffer.executeUpdate();

            conn.commit();
            System.out.println("Utilisateur créé.");

            stmtOwner.close();
            stmtOffer.close();
        } catch (SQLException e) {
            try {
                conn.rollback();
                System.err.println("Transaction annulée en raison d'une erreur : " + e.getMessage());
            } catch (SQLException rollbackEx) {
                System.err.println("Erreur lors de l'annulation de la transaction : " + rollbackEx.getMessage());
            }
            e.printStackTrace(System.err);
        }
    }

    /**
     * Cette méthode affiche la liste des offres d'un utilisateur spécifique à partir de son email.
     * Elle exécute une requête SQL pour récupérer les données des offres et les affiche dans un format tabulaire.
     */
    public static void afficherMaListeOffre(Connection conn, String email) {
        System.out.println("\nMa liste d'offres : ");
        try {
            String query = "SELECT P.nom_produit AS Nom_Produit, " +
            "O.date_offre AS Date_Offre, " +
            "O.quantite AS Quantite_Offre, " +
            "CASE " +
            "  WHEN encours = 1 THEN " +
            "    CASE " +
            "      WHEN O.prix_offre = (SELECT MAX(O2.prix_offre) " +
            "                          FROM Offre O2 " +
            "                          WHERE O2.id_vente = O.id_vente) " +
            "      THEN 'GAGNANT' " +
            "      ELSE 'PERDANT' " +
            "    END " +
            "  ELSE " +
            "    CASE " +
            "      WHEN O.prix_offre = (SELECT MAX(O2.prix_offre) " +
            "                          FROM Offre O2 " +
            "                          WHERE O2.id_vente = O.id_vente) " +
            "      THEN 'GAGNÉ' " +
            "      ELSE 'PERDU' " +
            "    END " +
            "END AS Status " +
            "FROM Offre O " +
            "JOIN Vente V ON O.id_vente = V.id_vente " +
            "JOIN Produit P ON V.id_produit = P.id_produit " +
            "WHERE O.email = ?";

            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, email);
            ResultSet rset = stmt.executeQuery();

            // Obtenir les métadonnées pour afficher les noms des colonnes
            ResultSetMetaData rsMeta = rset.getMetaData();
            int columnCount = rsMeta.getColumnCount();
            
            // Afficher les noms des colonnes
            for (int i = 1; i <= columnCount; i++) {
                System.out.print(rsMeta.getColumnName(i) + "\t");
            }
            System.out.println();

            // Afficher les lignes de données
            while (rset.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    System.out.print(rset.getString(i) + "\t");
                }
                System.out.println();
            }
            System.out.println();

            rset.close();
            stmt.close();
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'affichage des données");
            e.printStackTrace(System.err);
        }
    }
    
    /**
     * Cette méthode vérifie si une catégorie existe déjà dans la table `Categorie` de la base de données.
     * Elle renvoie `true` si la catégorie existe, sinon `false`.
     */
    public boolean doesCategorieExist(String nom_categorie) {
        String checkCategorieSQL = "SELECT COUNT(*) FROM Categorie WHERE nom_categorie = ?";
        try (PreparedStatement checkStmt = con.prepareStatement(checkCategorieSQL)) {
            checkStmt.setString(1, nom_categorie);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0; // Retourne vrai si la catégorie existe
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la vérification de l'existence de la catégorie : " + e.getMessage());
            e.printStackTrace(System.err);
        }
        return false; // Retourne faux en cas d'exception ou si la catégorie n'existe pas
    }

    /**
     * Cette méthode crée une nouvelle catégorie dans la table `Categorie` de la base de données.
     */
    public void createCategorie(String nom_categorie, String desc_categorie) {
        String insertCategorieSQL = "INSERT INTO Categorie (nom_categorie, desc_cat) VALUES (?, ?)";
        try (PreparedStatement insertStmt = con.prepareStatement(insertCategorieSQL)) {
            insertStmt.setString(1, nom_categorie);
            insertStmt.setString(2, desc_categorie); 
            insertStmt.executeUpdate();
            System.out.println("Catégorie créée avec succès avec la description : " + desc_categorie);
        } catch (SQLException e) {
            System.err.println("Erreur lors de la création de la catégorie : " + e.getMessage());
            e.printStackTrace(System.err);
        }
    }
    
    /**
     * Cette méthode vérifie si une salle de vente avec des critères spécifiques existe dans la base de données.
     * Si une salle de vente correspond aux critères, son identifiant est retourné.
     * Sinon, 0 est retourné.
     */
    public int doesSalleVenteExist(int offreMultiple, int revocable, int montante, int libre, String nomCategorie) {
        int exists = 0;
        String sql = "SELECT id_salle FROM SalleVente WHERE offre_multiple = ? AND revocable = ? AND montante = ? AND libre = ? AND nom_categorie = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, offreMultiple);  
            stmt.setInt(2, revocable);      
            stmt.setInt(3, montante);       
            stmt.setInt(4, libre);         
            stmt.setString(5, nomCategorie);
        
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1); // Retourne l'id_salle si une correspondance est trouvée
            }
        } catch (SQLException e) {
            e.printStackTrace(System.err);
        }
        return exists; // Retourne 0 si aucune correspondance n'est trouvée
    }

    /*
    * Cette fonction crée une nouvelle entrée dans la table "SalleVente".
    * Elle détermine le nouvel identifiant de la salle de vente en obtenant l'ID maximal de la table et en ajoutant 1.
    * Ensuite, elle insère les informations relatives à la salle de vente dans la table.
    * La fonction retourne l'ID de la nouvelle salle de vente.
    */
    public int createSalleVente(int offreMultiple, int revocable, int montante, int libre, String nomCategorie) {
        String maxIdQuery = "SELECT COALESCE(MAX(id_salle), 0) + 1 AS new_id FROM SalleVente";
        String insertSalleVenteSQL = "INSERT INTO SalleVente (id_salle, offre_multiple, revocable, montante, libre, nom_categorie) VALUES (?, ?, ?, ?, ?, ?)";

        int newId = 1; // Par défaut, l'ID commence à 1 si la table est vide
        try (
            PreparedStatement maxIdStmt = con.prepareStatement(maxIdQuery);
            ResultSet rs = maxIdStmt.executeQuery()
        ) {
                // Récupère l'ID maximal + 1
                if (rs.next()) {
                newId = rs.getInt("new_id");
            }

            // Insère la nouvelle SalleVente
            try (PreparedStatement insertStmt = con.prepareStatement(insertSalleVenteSQL)) {
                insertStmt.setInt(1, newId);
                insertStmt.setInt(2, offreMultiple);
                insertStmt.setInt(3, revocable);
                insertStmt.setInt(4, montante);
                insertStmt.setInt(5, libre);
                insertStmt.setString(6, nomCategorie);
                insertStmt.executeUpdate();
                System.out.println("SalleVente créée avec succès pour la catégorie : " + nomCategorie + " avec l'ID : " + newId);
        }
        } catch (SQLException e) {
            e.printStackTrace(System.err);
        }
        return newId;
    }
    
    /*
    * Cette fonction crée une nouvelle vente en suivant plusieurs étapes :
    * 1. Vérifie si la salle de vente existe déjà pour la catégorie donnée.
    * 2. Si la salle de vente n'existe pas, elle la crée.
    * 3. Vérifie si l'utilisateur est bien le propriétaire du produit.
    * 4. Insère les informations de vente dans la table "Vente" et enregistre également la vente dans la table "VenteLibre" ou "VenteLimite" selon le cas.
    */
    public void createVente(double prix_depart, int offre_multiple, int revocable, double prix_baisse, int montante,
                                String email, int id_produit, String nom_categorie, String desc_categorie,
                                int isVenteLibre, Timestamp date_debut, Timestamp date_fin) {
        try {
            this.con.setAutoCommit(false);
        } catch (SQLException e) {
            System.err.println("Erreur lors de la désactivation de l'auto-commit: " + e.getMessage());
        }
        int id_salle = doesSalleVenteExist(offre_multiple, revocable, montante, isVenteLibre, nom_categorie);            
        if ( id_salle == 0) {
            System.out.println("Aucune SalleVente trouvée pour cette catégorie. Création d'une nouvelle SalleVente...");

            if(!doesCategorieExist(nom_categorie)){
                createCategorie(nom_categorie, desc_categorie);
            }
            id_salle = createSalleVente(offre_multiple, revocable, montante, isVenteLibre, nom_categorie);
        }

        String checkProduitOwnershipSQL = "SELECT email FROM Produit WHERE id_produit = ?";

        String insertVenteSQL = "INSERT INTO Vente (id_vente, prix_depart, offre_multiple, revocable, prix_baisse, email, id_produit, id_salle, date_debut) "
                                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        String maxIdSQL = "SELECT COALESCE(MAX(id_vente), 0) + 1 AS new_id FROM Vente";

        try {
            // Étape 1 : Vérifier si le produit appartient à l'utilisateur
            try (PreparedStatement checkStmt = this.con.prepareStatement(checkProduitOwnershipSQL);
                PreparedStatement maxIdQuery = con.prepareStatement(maxIdSQL);) {
                checkStmt.setInt(1, id_produit);
                ResultSet rs = checkStmt.executeQuery();
                ResultSet rs_id = maxIdQuery.executeQuery();
                if (rs.next()) {
                    String productOwnerEmail = rs.getString("email");

                    // Valider si le produit appartient à l'utilisateur
                    if (productOwnerEmail.equals(email)) {
                        // Étape 2 : Insérer le record de la Vente si le produit appartient à l'utilisateur
                        int newId = 1;
                        if (rs_id.next()) {
                            newId = rs_id.getInt("new_id");
                        }
                        try (PreparedStatement insertStmt = this.con.prepareStatement(insertVenteSQL)) {
                            insertStmt.setInt(1, newId); 
                            insertStmt.setDouble(2, prix_depart);  
                            insertStmt.setInt(3, offre_multiple);  
                            insertStmt.setInt(4, revocable); 
                            insertStmt.setDouble(5, prix_baisse);  
                            insertStmt.setString(6, email); 
                            insertStmt.setInt(7, id_produit); 
                            insertStmt.setInt(8, id_salle); 
                            insertStmt.setTimestamp(9, date_debut);

                            insertStmt.executeUpdate();
                            System.out.println("Vente insérée avec succès !");

                            if (isVenteLibre == 1) {
                                String insertVenteLibreSQL = "INSERT INTO VenteLibre (id_vente) VALUES (?)";
                                try (PreparedStatement insertVenteLibreStmt = this.con.prepareStatement(insertVenteLibreSQL)) {
                                    insertVenteLibreStmt.setInt(1, newId);
                                    insertVenteLibreStmt.executeUpdate();
                                    System.out.println("Enregistrement VenteLibre inséré avec succès !");
                                }
                            } else {
                                if (date_debut != null && date_fin != null) {
                                    String insertVenteLimiteSQL = "INSERT INTO VenteLimite (id_vente, date_fin) "
                                                                + "VALUES (?, ?)";
                                    try (PreparedStatement insertVenteLimiteStmt = this.con.prepareStatement(insertVenteLimiteSQL)) {
                                        insertVenteLimiteStmt.setInt(1, newId);
                                        insertVenteLimiteStmt.setTimestamp(2, date_fin);
                                        insertVenteLimiteStmt.executeUpdate();
                                        System.out.println("Enregistrement VenteLimite inséré avec succès !");
                                    }
                                } else {
                                    System.out.println("VenteLimite nécessite des dates de début et de fin valides.");
                                    return;
                                }
                            }
                        }
                        this.con.commit();
                    } else {
                        System.out.println("Le produit n'appartient pas à l'utilisateur.");
                        return;
                    }
                } else {
                    System.out.println("Produit non trouvé.");
                    return;
                }
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la création de la vente: " + e.getMessage());
        } finally {
            try {
                if (this.con != null) this.con.setAutoCommit(true);
            } catch (SQLException ex) {
                System.err.println("Erreur lors de la restauration de l'auto-commit : " + ex.getMessage());
            }
        }
    }

    /*
    * Cette fonction affiche les produits d'un utilisateur (par email) qui sont disponibles à la vente.
    * Elle vérifie les produits qui ne sont pas encore en vente (pas de vente en cours) et ont un stock de vente positif.
    * Elle affiche les détails des produits disponibles et retourne un indicateur booléen (true si des produits sont trouvés, false sinon).
    */
    public boolean afficherProduitsVendreParEmail(String email) {
        String query = "SELECT P.id_produit, P.nom_produit, P.prix_revient, P.stock_vente, P.email " +
                    "FROM Produit P " +
                    "WHERE P.email = ? AND P.stock_vente > 0 " +
                    "AND NOT EXISTS (" +
                    "    SELECT 1 FROM Vente V WHERE V.id_produit = P.id_produit AND V.encours = 1" +
                    ")" +
                    "ORDER BY 1 ASC";

        PreparedStatement stmt = null;
        ResultSet rset = null;

        try {
            // Prépare la requête SQL pour récupérer les produits par email
            stmt = this.con.prepareStatement(query);
            stmt.setString(1, email); 

            rset = stmt.executeQuery();

            System.out.println();

            if (!rset.isBeforeFirst()) {
                System.out.println("Aucun produit disponible à vendre.");
                return false;
            }

            System.out.println("-----Ma Liste de Produits-----");
            // Affiche les détails des produits
            while (rset.next()) {
                int idProduit = rset.getInt("id_produit");
                String nomProduit = rset.getString("nom_produit");
                double prixRevient = rset.getDouble("prix_revient");
                int stockVente = rset.getInt("stock_vente");

                System.out.println(idProduit + ". " + nomProduit + ", " + prixRevient + " euros, " + stockVente + " unité(s)");
            }
            System.out.println("------------------------------");
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // Ferme les ressources pour éviter les fuites de mémoire
            try {
                if (rset != null) {
                    rset.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    /* 
    * Méthode pour afficher les détails des Produits en fonction de l'email
    */ 
    public void afficherProduitsParEmail(String email) {
        String query = "SELECT P.id_produit, P.nom_produit, P.prix_revient, P.stock_vente, P.email " +
                    "FROM Produit P " +
                    "WHERE P.email = ? " +
                    "ORDER BY 1 ASC";

        PreparedStatement stmt = null;
        ResultSet rset = null;

        try {
            // Prépare la requête SQL pour récupérer les produits par email
            stmt = this.con.prepareStatement(query);
            stmt.setString(1, email); 

            rset = stmt.executeQuery();
            
            System.out.println();
            
            if (!rset.isBeforeFirst()) {
                System.out.println("Aucun produit disponible à vendre.");
                return;
            }
            System.out.println("-----Ma Liste de Produits-----");

            // Affiche les détails des produits
            while (rset.next()) {
                int idProduit = rset.getInt("id_produit");
                String nomProduit = rset.getString("nom_produit");
                double prixRevient = rset.getDouble("prix_revient");
                int stockVente = rset.getInt("stock_vente");

                System.out.println(idProduit + ". " + nomProduit + ", " + prixRevient + " euros, " + stockVente + " unité(s)");
            }
            System.out.println("------------------------------");
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // Ferme les ressources pour éviter les fuites de mémoire
            try {
                if (rset != null) {
                    rset.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /*
    * Cette fonction permet d'afficher les catégories disponibles à partir de la table `SalleVente`.
    * Elle récupère toutes les catégories distinctes, les affiche à l'utilisateur, et lui demande de sélectionner
    * une catégorie en entrant le numéro associé. Ensuite, elle affiche les ventes associées à la catégorie sélectionnée.
    * Si l'utilisateur entre un numéro invalide ou une valeur incorrecte, un message d'erreur est affiché.
    */
    public void affichageCategorie(Scanner scanner, String email) {
        String query = "SELECT DISTINCT nom_categorie FROM SalleVente";
        PreparedStatement stmt = null;
        ResultSet rset = null;

        try {
            stmt = this.con.prepareStatement(query);
            rset = stmt.executeQuery();

            System.out.println();
            System.out.println("CATEGORIES");

            int i = 1;
            // Créer une liste des catégories pour la référence ultérieure
            List<String> categories = new ArrayList<>();

            while (rset.next()) {
                String categorie = rset.getString("nom_categorie");
                categories.add(categorie);
                System.out.println(i + " " + categorie);
                i++;
            }

            // Demander à l'utilisateur de saisir le numéro d'une catégorie
            System.out.print("Entrez le numéro de la catégorie : ");
            String input = scanner.nextLine();
            int cat = -1;

            try {
                cat = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Entrée invalide ! Veuillez entrer un numéro valide.");
                return; // Arrêter l'exécution si l'entrée n'est pas un nombre valide
            }

            // Vérifier que le numéro de catégorie est valide
            if (cat < 1 || cat > categories.size()) {
                System.out.println("Numéro de catégorie invalide.");
                return;
            }

            // Affichage des ventes pour la catégorie sélectionnée
            String selectedCategory = categories.get(cat - 1);
            affichageVentes(scanner, selectedCategory, email);

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // Fermer les ressources pour éviter les fuites de mémoire
            try {
                if (rset != null) {
                    rset.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
        
    /*
    * Cette fonction affiche les ventes en cours dans une catégorie spécifique, à l'exclusion des produits de l'utilisateur.
    * L'utilisateur peut voir les détails de chaque produit (nom, prix, stock, type d'offre) et choisir un produit 
    * en fonction de son ID. Ensuite, il peut entrer la quantité souhaitée et la fonction vérifie si cette quantité est 
    * valide par rapport au stock disponible. Si tout est correct, l'achat ou l'enchère est lancé via la méthode `offreVente`.
    */
    public void affichageVentes(Scanner scanner, String cat, String email) {
        String query = "SELECT v.id_vente, p.nom_produit, v.prix_depart, p.stock_vente, " +
                    "v.offre_multiple, v.revocable, v.prix_baisse, " +
                    "CASE WHEN vl.id_vente IS NOT NULL THEN 'temps libre' " +
                    "     WHEN vlm.id_vente IS NOT NULL THEN 'temps limite' " +
                    "     ELSE 'Unknown' END AS type_vente " +
                    "FROM Vente v " +
                    "JOIN Produit p ON v.id_produit = p.id_produit " +
                    "JOIN SalleVente s ON v.id_salle = s.id_salle " +
                    "LEFT JOIN VenteLibre vl ON v.id_vente = vl.id_vente " +
                    "LEFT JOIN VenteLimite vlm ON v.id_vente = vlm.id_vente " +
                    "WHERE s.nom_categorie = ? AND v.encours = 1" +
                    "AND V.email != ?";

        PreparedStatement stmt = null;
        ResultSet rset = null;

        try {
            stmt = this.con.prepareStatement(query);
            stmt.setString(1, cat);  // Définir la catégorie comme paramètre
            stmt.setString(2, email);
            rset = stmt.executeQuery();

            System.out.println();
            System.out.println("PRODUITS");

            // Si aucun produit n'est trouvé, prévenir l'utilisateur et sortir
            if (!rset.next()) {
                System.out.println("Aucun produit disponible dans cette catégorie.");
                return;
            }

            // Parcourir les produits
            do {
                int idVente = rset.getInt("id_vente");
                String nomProduit = rset.getString("nom_produit");
                Double prixDepart = rset.getDouble("prix_depart");
                int stockVente = rset.getInt("stock_vente");
                boolean offreMultiple = rset.getInt("offre_multiple") == 1;
                boolean revocable = rset.getInt("revocable") == 1;
                Double prixBaisse = rset.getDouble("prix_baisse");
                String typeVente = rset.getString("type_vente");

                String typeBidding = (prixBaisse == 0) ? ", montant" : ", descendant";

                System.out.println("(" + idVente + ")");
                System.out.println("Produit: " + nomProduit);
                System.out.println("Prix de départ: " + prixDepart);
                System.out.println("Stock disponible: " + stockVente);
                System.out.println("Type: " + (offreMultiple ? "Offre multiple" : "Offre unique") + (revocable ? ", revocable" : ", non-revocable") + typeBidding + ", " + typeVente);
                System.out.println("-----------------------------");

            } while (rset.next());

            // Demander à l'utilisateur de choisir un produit par ID
            System.out.print("Entrez l'ID du produit que vous souhaitez choisir : ");
            int venteId = scanner.nextInt();
            boolean validId = false;

            // Vérifier que l'ID de produit existe dans les résultats
            while (!validId) {
                stmt = this.con.prepareStatement(query);
                stmt.setString(1, cat);  // Redéfinir la catégorie
                stmt.setString(2, email);
                rset = stmt.executeQuery();

                while (rset.next()) {
                    int idVenteInDb = rset.getInt("id_vente");
                    if (idVenteInDb == venteId) {
                        validId = true;
                        break;
                    }
                }

                if (!validId) {
                    System.out.println("ID de produit invalide. Veuillez réessayer.");
                    System.out.print("Entrez l'ID du produit que vous souhaitez choisir : ");
                    venteId = scanner.nextInt();  // Redemander l'ID
                }
            }

            // Demander la quantité et valider
            int quantity = 0;
            boolean validQuantity = false;
            while (!validQuantity) {
                System.out.print("Entrez la quantité que vous souhaitez acheter/enchérir : ");
                quantity = scanner.nextInt();
                // Trouver le stock du produit choisi
                stmt = this.con.prepareStatement(query);
                stmt.setString(1, cat);
                stmt.setString(2, email);
                rset = stmt.executeQuery();
                int selectedStock = 0;
                while (rset.next()) {
                    if (rset.getInt("id_vente") == venteId) {
                        selectedStock = rset.getInt("stock_vente");
                        break;
                    }
                }

                // Valider la quantité
                if (quantity <= 0) {
                    System.out.println("La quantité doit être un nombre positif.");
                } else if (quantity > selectedStock) {
                    System.out.println("Stock insuffisant disponible. Veuillez entrer une quantité valide.");
                } else {
                    validQuantity = true;
                }
            }

            offreVente(scanner, venteId, quantity, email);

        } catch (SQLException e) {
                e.printStackTrace();
        } finally {
            try {
                if (rset != null) {
                    rset.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /*
    * Cette fonction permet à l'utilisateur de faire une offre (enchère) sur un produit en fonction de son type 
    * (enchère montante ou descendante). Elle vérifie si l'utilisateur a déjà fait une offre (si l'enchère est 
    * "montante") et si l'offre est valide par rapport à l'offre la plus élevée. Ensuite, l'enchère est placée ou 
    * l'utilisateur est averti si l'enchère a échoué.
    */
    public void offreVente(Scanner scanner, int venteId, int quantity, String email) {
        System.out.println();

        try {
            this.con.setAutoCommit(false);
        } catch (SQLException e) {
            System.err.println("Erreur lors de la désactivation de l'auto-commit: " + e.getMessage());
        }

        // Récupérer les détails de l'enchère : prix_depart et prix_baisse
        Map<String, Object> auctionDetails = getVenteDetails(venteId);
        double prixDepart = (double) auctionDetails.getOrDefault("prix_depart", 0.0);
        double prixBaisse = (double) auctionDetails.getOrDefault("prix_baisse", 0.0);
        int offreMultiple = (int) auctionDetails.get("offre_multiple");
        java.sql.Timestamp end_time = getEndTime(venteId);

        boolean isMontante = (prixBaisse == 0.0);

        // Récupérer l'enchère la plus élevée et sa quantité
        Map<String, Object> highestBidData = getHighestBidAndQuantity(venteId);
        double highestBid = highestBidData.get("highestBid") != null ? (double) highestBidData.get("highestBid") : 0.0;
        int highestQuantity = highestBidData.get("quantity") != null ? (int) highestBidData.get("quantity") : 0;

        System.out.println("Prix de départ: " + prixDepart);

        if (isMontante) {
            // Afficher l'enchère la plus élevée et sa quantité
            System.out.println("Enchère la plus élevée: " + highestBid);
            System.out.println("Quantité de l'enchère la plus élevée: " + highestQuantity);
            System.out.println("Heure de fin: " + end_time);

            // Demander à l'utilisateur de saisir son prix d'offre
            System.out.print("Entrez votre prix d'offre: ");
            double offerPrice = scanner.nextDouble();

            end_time = getEndTime(venteId);
            highestBidData = getHighestBidAndQuantity(venteId);
            highestBid = highestBidData.get("highestBid") != null ? (double) highestBidData.get("highestBid") : 0.0;
            highestQuantity = highestBidData.get("quantity") != null ? (int) highestBidData.get("quantity") : 0;

            // Vérifier que le prix d'offre est valide
            while (offerPrice < prixDepart || offerPrice <= highestBid) {
                System.out.println("\nPrix de départ: " + prixDepart);
                System.out.println("Enchère la plus élevée: " + highestBid);
                System.out.println("Quantité de l'enchère la plus élevée: " + highestQuantity);
                System.out.println("Heure de fin: " + end_time);
                System.out.println("Le prix d'offre doit être supérieur.");
                System.out.print("Entrez votre prix d'offre: ");
                offerPrice = scanner.nextDouble();

                end_time = getEndTime(venteId);
                highestBidData = getHighestBidAndQuantity(venteId);
                highestBid = highestBidData.get("highestBid") != null ? (double) highestBidData.get("highestBid") : 0.0;
                highestQuantity = highestBidData.get("quantity") != null ? (int) highestBidData.get("quantity") : 0;
            }

            // Vérifier si l'utilisateur a déjà fait une offre (si l'offre n'est pas multiple)
            if (offreMultiple == 0) {
                if (offerMade(venteId, email)) {
                    System.out.println("Vous avez déjà fait une offre.");
                    return;
                }
            }

            try {
                // Placer une enchère pour une enchère montante
                if (!isAuctionEnded(venteId)) {
                    placeMontanteBid(venteId, email, offerPrice, quantity);
                    this.con.commit();
                } else {
                    System.out.println("Enchère échouée. L'enchère est terminée.");
                    return;
                }
            } catch (SQLException e) {
                System.out.println("Erreur lors de la création de l'offre: " + e.getMessage());
            }
            

        } else {
            try {
                // Traiter la logique de l'enchère descendante
                handleDescendantAuction(scanner, venteId, email, prixDepart, prixBaisse, quantity);
                this.con.commit();
            } catch (SQLException e) {
                System.out.println("Erreur lors de la création de l'offre: " + e.getMessage());
            }
        }

        try {
            if (this.con != null) this.con.setAutoCommit(true);
        } catch (SQLException ex) {
            System.err.println("Erreur lors de la restauration de l'auto-commit : " + ex.getMessage());
        }
    }
    
    /*
    * Cette méthode récupère la plus haute offre (prix_offre) et la quantité correspondante (quantite) 
    * pour une enchère donnée (identifiée par `venteId`).
    * Elle exécute une requête SQL qui trie les offres par prix (dans l'ordre décroissant) et récupère la première ligne,
    * c'est-à-dire l'offre la plus élevée.
    */
    private Map<String, Object> getHighestBidAndQuantity(int venteId) {
        String query = "SELECT prix_offre, quantite FROM Offre WHERE id_vente = ? ORDER BY prix_offre DESC FETCH FIRST 1 ROWS ONLY";
        PreparedStatement stmt = null;
        ResultSet rset = null;
        Map<String, Object> result = new HashMap<>();
        
        try {
            stmt = this.con.prepareStatement(query);
            stmt.setInt(1, venteId);
            rset = stmt.executeQuery();
            
            // Si une offre est trouvée, on récupère le prix et la quantité
            if (rset.next()) {
                result.put("highestBid", rset.getDouble("prix_offre"));
                result.put("quantity", rset.getInt("quantite"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // Fermer les ressources pour éviter les fuites de mémoire
            try {
                if (rset != null) {
                    rset.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        
        return result; // Retourner le Map contenant la plus haute offre et la quantité
    }

    /*
    * Cette méthode récupère les détails d'une enchère spécifique (identifiée par `venteId`).
    * Elle extrait les informations suivantes de la table `Vente` :
    * - prix_depart : le prix de départ de l'enchère
    * - prix_baisse : la réduction de prix, si applicable
    * - offre_multiple : indique si plusieurs offres sont autorisées (0 ou 1)
    */
    private Map<String, Object> getVenteDetails(int venteId) {
        String query = "SELECT prix_depart, prix_baisse, offre_multiple FROM Vente WHERE id_vente = ?";
        PreparedStatement stmt = null;
        ResultSet rset = null;
        Map<String, Object> auctionDetails = new HashMap<>();
        
        try {
            stmt = this.con.prepareStatement(query);
            stmt.setInt(1, venteId);
            rset = stmt.executeQuery();
            
            // Si des détails sont trouvés pour l'enchère, les ajouter au Map
            if (rset.next()) {
                auctionDetails.put("prix_depart", rset.getDouble("prix_depart"));
                auctionDetails.put("prix_baisse", rset.getDouble("prix_baisse"));
                auctionDetails.put("offre_multiple", rset.getInt("offre_multiple"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // Fermer les ressources pour éviter les fuites de mémoire
            try {
                if (rset != null) rset.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        
        return auctionDetails; // Retourner le Map contenant les détails de l'enchère
    }
   
    /*
    * Cette méthode permet de placer une offre pour une enchère de type "Montante".
    * Elle enregistre les détails de l'offre dans la base de données, y compris :
    * - la date et l'heure de l'offre
    * - l'adresse email de l'utilisateur
    * - le prix de l'offre
    * - la quantité de produits concernés par l'offre
    * - l'ID de l'enchère
    */
    private void placeMontanteBid(int venteId, String email, double offerPrice, int quantity) {
        // Sauvegarder l'heure actuelle pour l'offre
        java.sql.Timestamp currentTimestamp = new java.sql.Timestamp(System.currentTimeMillis());
        java.sql.Date currentDate = new java.sql.Date(System.currentTimeMillis());
        
        // Préparer la requête d'insertion dans la base de données
        String query = "INSERT INTO Offre (date_offre, email, prix_offre, quantite, id_vente) " +
                        "VALUES (?, ?, ?, ?, ?)";
        PreparedStatement stmt = null;

        try {
            stmt = this.con.prepareStatement(query);
            stmt.setTimestamp(1, currentTimestamp); 
            stmt.setString(2, email);                
            stmt.setDouble(3, offerPrice);          
            stmt.setInt(4, quantity);                
            stmt.setInt(5, venteId);                  
        
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                System.out.println("Votre offre a été placée avec succès.");
            } else {
                System.out.println("Erreur : Impossible de placer votre offre.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // Fermer la déclaration préparée pour éviter les fuites de mémoire
            try {
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Cette méthode vérifie si une enchère est terminée. 
     * Selon le type d'enchère (VenteLibre ou VenteLimite), elle décide de la logique de fin d'enchère.
     */
    private boolean isAuctionEnded(int venteId) {
        // Requête SQL pour obtenir la date de fin d'une enchère et le prix de baisse pour les enchères à prix descendant
        String query = "SELECT vl.date_fin, v.prix_baisse FROM Vente v " +
                        "LEFT JOIN VenteLimite vl ON v.id_vente = vl.id_vente " +
                        "WHERE v.id_vente = ?";
        PreparedStatement stmt = null;
        ResultSet rset = null;

        try {
            // Préparation et exécution de la requête SQL
            stmt = this.con.prepareStatement(query);
            stmt.setInt(1, venteId);
            rset = stmt.executeQuery();

            if (rset.next()) {
                java.sql.Timestamp heureFin = rset.getTimestamp("date_fin"); // Récupère la date de fin de l'enchère
                Double prixBaisse = rset.getDouble("prix_baisse"); // Récupère le prix de baisse de l'enchère

                if (heureFin == null) {
                    // Enchère "temps libre" : l'enchère se termine 10 minutes après la dernière offre
                    // Si prix_baisse est 0.0, c'est une enchère classique à prix libre
                    return (prixBaisse == 0.0 ? isTempsLibreAuctionEnded(venteId) : isTempsLibreAuctionDescendantEnded(venteId));
                } else {
                    // Enchère "temps limite" : utiliser la date de fin fixe
                    return isTempsLimiteAuctionEnded(heureFin);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                // Fermeture des ressources
                if (rset != null) rset.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return false; 
    }

    /**
     * Cette méthode vérifie si une enchère de type "VenteLibre" avec un prix descendant est terminée.
     * Une enchère de type "VenteLibre" se termine 10 minutes après la dernière offre, mais avant cela, elle vérifie
     * s'il existe des offres pour l'enchère spécifiée.
     */
    private boolean isTempsLibreAuctionDescendantEnded(int venteId) {
        String query = "SELECT COUNT(*) FROM Offre WHERE id_vente = ?";
        PreparedStatement stmt = null;
        ResultSet rset = null;
        
        try {
            // Préparer et exécuter la requête pour vérifier si des offres existent pour l'enchère donnée (venteId)
            stmt = this.con.prepareStatement(query);
            stmt.setInt(1, venteId);
            rset = stmt.executeQuery();
            
            if (rset.next()) {
                int offerCount = rset.getInt(1); // Récupère le nombre d'offres pour cette vente
                
                // Si le nombre d'offres est supérieur à 0, cela signifie qu'il y a des offres, l'enchère est terminée.
                return offerCount > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rset != null) rset.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        
        return false; // Si aucune offre n'existe pour cette vente, l'enchère n'est pas terminée
    }
   
    /**
     * Cette méthode vérifie si une enchère de type "VenteLibre" (temps libre) est terminée.
     * Une enchère de type "VenteLibre" se termine 10 minutes après la dernière offre.
     */
    private boolean isTempsLibreAuctionEnded(int venteId) {
        // Récupère l'heure de la dernière offre (heure de fin de l'enchère pour "VenteLibre")
        java.sql.Timestamp lastBidTime = getEndTime(venteId);
        
        // Si l'heure de la dernière offre existe
        if (lastBidTime != null) {
            long auctionEndTime = lastBidTime.getTime(); // Heure de fin de l'enchère
            long currentTime = System.currentTimeMillis(); // Heure actuelle
            
            // Vérifie si l'heure actuelle dépasse l'heure de fin de l'enchère
            return currentTime > auctionEndTime;
        }
        
        return false; // Si aucune offre n'a été faite, l'enchère n'est pas terminée
    }

    /**
     * Cette méthode vérifie si une enchère de type "VenteLimite" (temps limite) est terminée.
     * Une enchère de type "VenteLimite" se termine à l'heure spécifiée dans la base de données.
     */
    private boolean isTempsLimiteAuctionEnded(java.sql.Timestamp heureFin) {
        // Si une heure de fin est définie
        if (heureFin != null) {
            long currentTime = System.currentTimeMillis(); // Heure actuelle
            
            // Vérifie si l'heure actuelle dépasse l'heure de fin de l'enchère
            return currentTime > heureFin.getTime(); // L'enchère se termine lorsque l'heure actuelle est supérieure à l'heure de fin
        }
        
        return false; // Si aucune heure de fin n'est définie, l'enchère n'est pas terminée
    }
  
    /**
     * Cette méthode détermine l'heure de fin d'une enchère en fonction de son type :
     * - Si c'est une enchère "VenteLimite", elle renverra l'heure de fin spécifiée dans la base de données.
     * - Si c'est une enchère "VenteLibre", elle renverra l'heure de fin calculée, qui est 10 minutes après l'offre la plus élevée.
     */
    private Timestamp getEndTime(int venteId) {
        // Requête pour vérifier si l'enchère est de type "VenteLimite" ou "VenteLibre"
        String checkAuctionTypeQuery = "SELECT v.id_vente, vl.date_fin, MAX(o.date_offre) as highest_bid_time "
                                    + "FROM Vente v "
                                    + "LEFT JOIN VenteLimite vl ON v.id_vente = vl.id_vente "
                                    + "LEFT JOIN Offre o ON v.id_vente = o.id_vente "
                                    + "WHERE v.id_vente = ? "
                                    + "GROUP BY v.id_vente, vl.date_fin";

        PreparedStatement stmt = null;
        ResultSet rset = null;

        try {
            stmt = this.con.prepareStatement(checkAuctionTypeQuery);
            stmt.setInt(1, venteId);
            rset = stmt.executeQuery();

            if (rset.next()) {
                // Récupère l'heure de fin de l'enchère si elle est de type "VenteLimite"
                Timestamp dateFin = rset.getTimestamp("date_fin");
                // Récupère l'heure de la dernière offre dans le cas d'une enchère "VenteLibre"
                Timestamp highestBidTime = rset.getTimestamp("highest_bid_time");

                if (dateFin != null) {
                    // Si l'enchère est de type "VenteLimite", retourne l'heure de fin fixée
                    return dateFin;
                } else if (highestBidTime != null) {
                    // Si l'enchère est de type "VenteLibre", l'enchère se termine 10 minute après la dernière offre
                    long auctionEndTimeMillis = highestBidTime.getTime() + (10 * 60 * 1000);
                    return new java.sql.Timestamp(auctionEndTimeMillis);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rset != null) rset.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        // Si aucune enchère n'est trouvée ou en cas d'erreur, retourne null
        return null;
    }

    /**
     * Cette méthode vérifie si un utilisateur a déjà fait une offre pour une enchère spécifique.
     * Elle interroge la base de données pour vérifier si l'email de l'utilisateur est associé 
     * à une offre déjà enregistrée pour l'enchère spécifiée.
     */
    private boolean offerMade(int venteId, String email) {
        String query = "SELECT email FROM Offre " +
                        "WHERE id_vente = ?" +
                        "AND email = ?";
        PreparedStatement stmt = null;
        ResultSet rset = null;

        try {
            stmt = this.con.prepareStatement(query);
            stmt.setInt(1, venteId);
            stmt.setString(2, email);
            rset = stmt.executeQuery();

            if (rset.next()) {
                String email_user = rset.getString("email");
                if (email_user != null) {
                    return true;
                } else {
                    return false;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rset != null) rset.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return false;
    }
     
    /**
     * Cette méthode gère une enchère de type "Descendant", où le prix d'un produit diminue chaque minute 
     * jusqu'à ce qu'un acheteur accepte d'acheter le produit ou que l'enchère se termine.
     * 
     * Elle prend en compte l'heure de début de l'enchère, le prix de départ, le montant de la baisse du prix 
     * par minute, ainsi que la quantité du produit. Le prix actuel est ajusté en fonction du temps écoulé 
     * depuis le début de l'enchère et l'utilisateur peut décider d'acheter le produit au prix actuel.
     */
    private void handleDescendantAuction(Scanner scanner, int venteId, String email, double prixDepart, double prixBaisse, int quantity) {
        // Récupérer l'heure de début de l'enchère
        java.sql.Timestamp startTime = getAuctionStartTime(venteId);
        if (startTime == null) {
            System.out.println("L'heure de début de l'enchère est introuvable. Impossible de continuer.");
            return;
        }

        // Récupérer le prix de revient (prixRevient) du produit
        double prixRevient = getProductCostPrice(venteId);
        if (prixRevient < 0) {
            System.out.println("Le prix de revient du produit est introuvable. Impossible de continuer.");
            return;
        }

        // Afficher les informations initiales de l'enchère
        System.out.println("L'enchère a commencé à : " + startTime);
        System.out.println("Prix de départ : " + prixDepart);
        System.out.println("Le prix diminuera de : " + prixBaisse + " chaque minute.");

        double currentPrice = prixDepart;
        boolean auctionEnded = isAuctionEnded(venteId);

        if (!auctionEnded) {
            // Calculer le nombre de minutes écoulées depuis le début de l'enchère
            long elapsedMinutes = (System.currentTimeMillis() - startTime.getTime()) / (60 * 1000);
            currentPrice = prixDepart - (elapsedMinutes * prixBaisse);

            // S'assurer que le prix ne tombe pas en dessous du prix de revient
            if (currentPrice < prixRevient) {
                currentPrice = prixRevient;
                System.out.println("Le prix a atteint la valeur minimale possible.");
            }

            System.out.println("Prix actuel : " + currentPrice);
            System.out.print("Voulez-vous acheter ce produit maintenant au prix actuel ? (oui/non) : ");
            String decision = scanner.next();

            auctionEnded = isAuctionEnded(venteId);

            if ("oui".equalsIgnoreCase(decision) && !auctionEnded) {
                elapsedMinutes = (System.currentTimeMillis() - startTime.getTime()) / (60 * 1000);
                currentPrice = prixDepart - (elapsedMinutes * prixBaisse);
                System.out.println("Vous avez acheté le produit pour : " + currentPrice);
                completeDescendantAuction(venteId, email, currentPrice, quantity);
                auctionEnded = true;
            } else if (auctionEnded) {
                System.out.println("Enchère échouée. L'enchère est terminée.");
                return;
            } else {
                return;
            }
        }

        System.out.println("L'enchère est terminée.");
    }
 
    /**
     * Cette méthode permet de récupérer l'heure de début d'une vente à partir de son ID.
     */
    private java.sql.Timestamp getAuctionStartTime(int venteId) {
        // Requête SQL pour récupérer l'heure de début de la vente
        String query = "SELECT date_debut FROM Vente WHERE id_vente = ?";
        PreparedStatement stmt = null;
        ResultSet rset = null;

        try {
            stmt = this.con.prepareStatement(query);
            stmt.setInt(1, venteId);  // Paramétrer l'ID de la vente dans la requête
            rset = stmt.executeQuery();

            // Vérifier si une ligne a été trouvée
            if (rset.next()) {
                return rset.getTimestamp("date_debut");  // Retourner l'heure de début de la vente
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rset != null) rset.close();  // Fermer le ResultSet
                if (stmt != null) stmt.close();  // Fermer le PreparedStatement
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return null; // Retourner null si l'heure de début n'a pas été trouvée
    }
      
    /**
     * Cette méthode permet de récupérer le prix de revient d'un produit associé à une vente spécifique.
     */
    private double getProductCostPrice(int venteId) {
        // Requête SQL pour récupérer le prix de revient du produit associé à la vente
        String query = "SELECT p.prix_revient FROM Produit p " +
                    "INNER JOIN Vente v ON p.id_produit = v.id_produit " +
                    "WHERE v.id_vente = ?";
        PreparedStatement stmt = null;
        ResultSet rset = null;

        try {
            stmt = this.con.prepareStatement(query);
            stmt.setInt(1, venteId);  // Paramétrer l'ID de la vente dans la requête
            rset = stmt.executeQuery();

            // Vérifier si une ligne a été trouvée
            if (rset.next()) {
                return rset.getDouble("prix_revient");  // Retourner le prix de revient du produit
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rset != null) rset.close();  // Fermer le ResultSet
                if (stmt != null) stmt.close();  // Fermer le PreparedStatement
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return -1; // Retourner -1 si le prix de revient n'a pas été trouvé
    }

    /**
     * Cette méthode permet de finaliser un achat dans une vente de type "Descendant". 
     * Elle enregistre l'offre de l'acheteur avec le prix final, la quantité achetée 
     * et la date/heure actuelle dans la base de données.
     */
    private void completeDescendantAuction(int venteId, String email, double finalPrice, int quantity) {
        // Obtenir la date et l'heure actuelles
        java.sql.Timestamp currentTimestamp = new java.sql.Timestamp(System.currentTimeMillis());

        // Requête SQL pour insérer une nouvelle offre
        String query = "INSERT INTO Offre (date_offre, email, prix_offre, quantite, id_vente) " +
                    "VALUES (?, ?, ?, ?, ?)";

        PreparedStatement stmt = null;

        try {
            stmt = this.con.prepareStatement(query);
            stmt.setTimestamp(1, currentTimestamp);  
            stmt.setString(2, email);                 
            stmt.setDouble(3, finalPrice);            
            stmt.setInt(4, quantity);                 
            stmt.setInt(5, venteId);                  

            // Exécution de la requête d'insertion
            int rowsAffected = stmt.executeUpdate();
            
            // Afficher un message en fonction du succès de l'insertion
            if (rowsAffected > 0) {
                System.out.println("Votre achat a été effectué avec succès au prix de : " + finalPrice + " €");
            } else {
                System.out.println("Erreur : Impossible de finaliser l'achat.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null) stmt.close();  // Fermeture du PreparedStatement
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Cette méthode gère la fin d'une enchère pour la vente spécifiée par `venteId`.
     * Elle met à jour le stock du produit, déclare l'enchère comme terminée, et affiche le gagnant.
     */
    private boolean handleAuctionEnd(int venteId) {
        try {
            // Récupérer l'offre la plus élevée pour la vente spécifiée
            String offerQuery = "SELECT email, prix_offre, quantite FROM Offre WHERE id_vente = ? ORDER BY prix_offre DESC FETCH FIRST 1 ROWS ONLY";
            PreparedStatement offerStmt = this.con.prepareStatement(offerQuery);
            offerStmt.setInt(1, venteId);  // Paramétrer l'ID de la vente
            ResultSet offerRset = offerStmt.executeQuery();

            // Vérifier si une offre a été trouvée
            if (offerRset.next()) {
                // Récupérer l'email du gagnant, le prix de l'offre et la quantité achetée
                String winningBidderEmail = offerRset.getString("email");
                double highestPrice = offerRset.getDouble("prix_offre");
                int quantity = offerRset.getInt("quantite");

                // Mettre à jour le stock du produit en fonction de la quantité achetée
                String updateProductQuery = "UPDATE Produit SET stock_vente = stock_vente - ? WHERE id_produit = (SELECT id_produit FROM Vente WHERE id_vente = ?)";
                PreparedStatement updateProductStmt = this.con.prepareStatement(updateProductQuery);
                updateProductStmt.setInt(1, quantity);
                updateProductStmt.setInt(2, venteId);  // Paramétrer l'ID de la vente
                int rowsUpdated = updateProductStmt.executeUpdate();

                // Si la mise à jour du stock a été effectuée correctement
                if (rowsUpdated > 0) {
                    // Mettre à jour le statut de la vente pour la marquer comme terminée
                    String updateStatusQuery = "UPDATE Vente SET encours = 0 WHERE id_vente = ?";
                    PreparedStatement updateStatusStmt = this.con.prepareStatement(updateStatusQuery);
                    updateStatusStmt.setInt(1, venteId);  // Paramétrer l'ID de la vente
                    updateStatusStmt.executeUpdate();
                    
                    // Retourner true pour indiquer que l'enchère a été correctement terminée
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Retourner false en cas d'erreur
        return false;
    }

    /**
     * Cette méthode vérifie toutes les ventes en cours (encours = 1) et clôture celles dont l'enchère est terminée.
     * Elle traite chaque vente active et effectue la gestion nécessaire à la fin de l'enchère.
     */
    public void checkAndEndAuctions() {
        try {
            this.con.setAutoCommit(false);
        } catch (SQLException e) {
            System.err.println("Erreur lors de la désactivation de l'auto-commit : " + e.getMessage());
        }
        
        // Requête SQL pour obtenir les identifiants des ventes en cours (encours = 1)
        String query = "SELECT id_vente FROM Vente WHERE encours = 1";
        PreparedStatement stmt = null;
        ResultSet rset = null;
        
        try {
            stmt = this.con.prepareStatement(query);
            rset = stmt.executeQuery();
            
            while (rset.next()) {
                int venteId = rset.getInt("id_vente");
                
                // Vérifier si l'enchère est terminée
                if (isAuctionEnded(venteId)) {
                    // Gérer la fin de l'enchère (par exemple, enregistrer le résultat final)
                    handleAuctionEnd(venteId);
                }
            }
            // Valider la transaction si toutes les ventes ont été traitées avec succès
            this.con.commit();
            
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rset != null) rset.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Cette méthode permet d'afficher toutes les ventes associées à un utilisateur via son email.
     * Elle récupère les informations de la base de données et les affiche à l'utilisateur.
     */
    public boolean afficherVentesParEmail(String email) {
        // Requête SQL pour récupérer les ventes d'un utilisateur spécifié par son email
        String query = "SELECT id_vente, prix_depart, offre_multiple, revocable, prix_baisse, email, id_produit, id_salle " +
                    "FROM Vente WHERE email = ?";
        
        PreparedStatement stmt = null;
        ResultSet rset = null;
        
        try {
            // Préparer la requête avec l'email de l'utilisateur
            stmt = this.con.prepareStatement(query);
            stmt.setString(1, email);  // Définir l'email dans la requête
        
            rset = stmt.executeQuery();
        
            // Vérifier si des résultats ont été retournés
            if (!rset.isBeforeFirst()) {
                System.out.println("Aucune vente trouvée.");
                return false; // Aucune vente n'a été trouvée
            } else {
                System.out.println("\n--- Mes ventes ---");
                // Parcourir les résultats pour afficher les détails de chaque vente
                while (rset.next()) {
                    // Récupérer les informations de la vente
                    int idVente = rset.getInt("id_vente");
                    double prixDepart = rset.getDouble("prix_depart");
                    int offreMultiple = rset.getInt("offre_multiple");
                    int revocable = rset.getInt("revocable");
                    double prixBaisse = rset.getDouble("prix_baisse");
                    int idProduit = rset.getInt("id_produit");
                    int idSalle = rset.getInt("id_salle");
        
                    // Afficher les détails de la vente
                    System.out.println("ID de la vente: " + idVente);
                    System.out.println("Prix de départ: " + prixDepart + " €");
                    System.out.println("Offres multiples autorisées: " + (offreMultiple == 1 ? "Oui" : "Non"));
                    System.out.println("Révocable: " + (revocable == 1 ? "Oui" : "Non"));
                    System.out.println("Type: " + (prixBaisse == 0 ? "Montant" : "Descendant (" + prixBaisse + "€)"));
                    System.out.println("ID du produit: " + idProduit);
                    System.out.println("ID de la salle: " + idSalle);
                    System.out.println("------------------------------");
                }
            }
        } catch (SQLException e) {
            // Si une erreur se produit, afficher un message d'erreur
            System.out.println("Erreur lors de la récupération des ventes: " + e.getMessage());
        } finally {
            try {
                if (rset != null) rset.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return true; // Les ventes ont été affichées avec succès
    }

    /**
     * Cette méthode permet à un utilisateur de révoquer une vente en cours.
     * 
     * Le processus commence par demander à l'utilisateur s'il souhaite révoquer une vente.
     * Si l'utilisateur répond "oui", le programme récupère toutes les ventes révoquables et actives (revocable = 1, encours = 1).
     * Ensuite, il affiche la liste des ventes révoquables et permet à l'utilisateur de choisir celle qu'il souhaite révoquer.
     * Avant de révoquer la vente, le programme vérifie si la vente appartient bien à l'utilisateur en utilisant son email.
     * Si la vente appartient à l'utilisateur, la vente est révoquée et les données associées sont supprimées.
     * Si la vente ne correspond pas à l'utilisateur ou si une erreur se produit, un message d'erreur est affiché.
     */
    public void revokeSale(Scanner scanner, String email) {
        // Demande à l'utilisateur s'il souhaite révoquer une vente
        System.out.print("Voulez-vous révoquer une vente ? (1 pour oui, 0 pour non): ");
        int decision = scanner.nextInt();
        
        // Si la décision est 'non', on quitte la méthode sans rien faire
        if (decision == 0) {
            return;
        }
        
        // Récupère la liste des ventes révoquables et actives (revocable = 1 ET encours = 1)
        List<Map<String, Object>> revocableSales = getRevocableSales();
        
        // Si aucune vente révoquable n'est trouvée
        if (revocableSales.isEmpty()) {
            System.out.println("Aucune vente révoquable n'a été trouvée.");
            return;
        }
        
        // Affiche la liste des ventes révoquables avec des informations supplémentaires
        System.out.println("Voici les ventes révoquables :");
        for (int i = 0; i < revocableSales.size(); i++) {
            Map<String, Object> sale = revocableSales.get(i);
            System.out.println((i + 1) + ". Vente ID: " + sale.get("id_vente") + 
                            ", Produit: " + sale.get("nom_produit") + 
                            ", Quantité: " + sale.get("stock_vente") + 
                            ", Prix de départ: " + sale.get("prix_depart") + 
                            ", Offre la plus élevée: " + sale.get("highest_bid"));
        }
        
        // Demande à l'utilisateur de sélectionner le numéro de la vente à révoquer
        System.out.print("Entrez le numéro de la vente à révoquer: ");
        int saleNumber = scanner.nextInt();
        
        // Vérifie que le numéro de vente sélectionné est valide
        if (saleNumber < 1 || saleNumber > revocableSales.size()) {
            System.out.println("Numéro de vente invalide.");
            return;
        }
        
        // Récupère l'ID de la vente sélectionnée
        int idVente = (int) revocableSales.get(saleNumber - 1).get("id_vente");
        
        // Vérifie si la vente appartient à l'utilisateur (en fonction de l'email)
        if (isSaleAssociatedWithUser(idVente, email)) {
            // Tente de révoquer la vente
            if (revokeSaleById(idVente)) {
                System.out.println("La vente " + idVente + " a été révoquée avec succès.");
            } else {
                System.out.println("Échec de la révocation. Vérifiez l'ID de la vente.");
            }
        } else {
            System.out.println("La vente " + idVente + " n'est pas associée à votre compte.");
        }
    }
 
    /**
     * Cette méthode permet de révoquer une vente en supprimant la vente et toutes les données associées.
     * Elle vérifie d'abord si la vente est en cours (active), puis supprime la vente et les enregistrements associés
     * dans les tables VenteLibre, VenteLimite, et Offre.
     */
    private boolean revokeSaleById(int idVente) {
        String checkEncoursQuery = "SELECT encours FROM Vente WHERE id_vente = ?";
        String deleteSaleQuery = "DELETE FROM Vente WHERE id_vente = ? AND revocable = 1 AND encours = 1";
        String deleteVenteLibreQuery = "DELETE FROM VenteLibre WHERE id_vente = ?";
        String deleteVenteLimiteQuery = "DELETE FROM VenteLimite WHERE id_vente = ?";
        String deleteOffreQuery = "DELETE FROM Offre WHERE id_vente = ?";
        
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            // Vérifie si la vente est en cours (active)
            stmt = this.con.prepareStatement(checkEncoursQuery);
            stmt.setInt(1, idVente);
            rs = stmt.executeQuery();

            if (rs.next()) {
                int encours = rs.getInt("encours");

                if (encours != 1) {
                    System.out.println("La vente n'est pas en cours. Impossible de la révoquer.");
                    return false;  // La vente n'est pas active, donc on ne peut pas la révoquer
                }
            }

            // Démarre une transaction pour garantir que toutes les suppressions se fassent ou aucune
            this.con.setAutoCommit(false);

            // Supprime les enregistrements associés dans la table VenteLibre
            stmt = this.con.prepareStatement(deleteVenteLibreQuery);
            stmt.setInt(1, idVente);
            stmt.executeUpdate();

            // Supprime les enregistrements associés dans la table VenteLimite
            stmt = this.con.prepareStatement(deleteVenteLimiteQuery);
            stmt.setInt(1, idVente);
            stmt.executeUpdate();

            // Supprime les enregistrements associés dans la table Offre
            stmt = this.con.prepareStatement(deleteOffreQuery);
            stmt.setInt(1, idVente);
            stmt.executeUpdate();

            // Enfin, supprime la vente elle-même
            stmt = this.con.prepareStatement(deleteSaleQuery);
            stmt.setInt(1, idVente);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                this.con.commit();  // Commit de la transaction si toutes les suppressions sont réussies
                System.out.println("La vente et toutes les données associées ont été révoquées.");
                return true;
            } else {
                this.con.rollback();  // Rollback si la suppression de la vente échoue
                System.out.println("Échec de la révocation de la vente. Aucune donnée n'a été supprimée.");
                return false;
            }

        } catch (SQLException e) {
            try {
                this.con.rollback();  // Rollback en cas d'exception
            } catch (SQLException rollbackException) {
                rollbackException.printStackTrace();
            }
            e.printStackTrace();
            return false;
        } finally {
            try {
                this.con.setAutoCommit(true);  // Restaure le mode auto-commit
                if (stmt != null) stmt.close();
                if (rs != null) rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Cette méthode récupère une liste des ventes qui sont révoquables et actuellement en cours.
     * Elle exécute une requête SQL pour obtenir les informations de la vente, y compris le prix de départ,
     * l'offre la plus élevée et le stock disponible du produit associé.
     * 
     */
    private List<Map<String, Object>> getRevocableSales() {
        List<Map<String, Object>> revocableSales = new ArrayList<>();
        String query = 
            "SELECT v.id_vente, p.nom_produit, v.prix_depart, " +
            "       COALESCE(MAX(o.prix_offre), 0) AS highest_bid, " +
            "       p.stock_vente " +
            "FROM Vente v " +
            "JOIN Produit p ON v.id_produit = p.id_produit " +
            "LEFT JOIN Offre o ON v.id_vente = o.id_vente " +
            "WHERE v.revocable = 1 AND v.encours = 1 " +
            "GROUP BY v.id_vente, p.nom_produit, v.prix_depart, p.stock_vente";
        
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            stmt = this.con.prepareStatement(query);  // Prépare la requête SQL
            rs = stmt.executeQuery();  // Exécute la requête et récupère les résultats

            // Parcourt les résultats et ajoute chaque vente à la liste
            while (rs.next()) {
                Map<String, Object> sale = new HashMap<>();
                sale.put("id_vente", rs.getInt("id_vente"));
                sale.put("nom_produit", rs.getString("nom_produit"));
                sale.put("prix_depart", rs.getDouble("prix_depart"));
                sale.put("highest_bid", rs.getDouble("highest_bid"));
                sale.put("stock_vente", rs.getInt("stock_vente"));

                revocableSales.add(sale);  // Ajoute la vente à la liste
            }
        } catch (SQLException e) {
            e.printStackTrace();  // Affiche l'exception en cas d'erreur
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (rs != null) rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return revocableSales;  // Retourne la liste des ventes révoquables
    }

    /**
     * Cette méthode vérifie si une vente est associée à un utilisateur donné par son email.
     * Elle exécute une requête SQL pour vérifier s'il existe une vente avec l'ID de vente 
     * spécifié et l'email de l'utilisateur.
     */
    private boolean isSaleAssociatedWithUser(int idVente, String email) {
        String query = "SELECT 1 FROM Vente WHERE id_vente = ? AND email = ?";
        PreparedStatement stmt = null;
        ResultSet rset = null;

        try {
            stmt = this.con.prepareStatement(query);
            stmt.setInt(1, idVente);
            stmt.setString(2, email);

            rset = stmt.executeQuery();
            return rset.next(); // Retourne true si une vente avec cet ID et cet email existe
        } catch (SQLException e) {
            e.printStackTrace();
            return false; // En cas d'erreur ou si la vente n'est pas associée à l'utilisateur, retourne false
        } finally {
            try {
                if (rset != null) rset.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Cette méthode liste toutes les ventes terminées avec les détails de la meilleure offre pour chaque vente.
     * Elle affiche les informations suivantes : prix de départ, type de vente, dates de début et de fin,
     * ainsi que la meilleure offre (prix, quantité, email de l'offreur).
     * Elle filtre les ventes terminées dont l'utilisateur (avec l'email donné) a participé.
     */
    public void listerVentesTermineesAvecDetailsOffre(String email) {
        String query = "SELECT v.id_vente, v.prix_depart, v.prix_baisse, v.date_debut, v.encours, "
                    + "       vl.date_fin, o.email AS offer_email, o.prix_offre, o.quantite, o.date_offre "
                    + "FROM Vente v "
                    + "LEFT JOIN VenteLimite vl ON v.id_vente = vl.id_vente "
                    + "LEFT JOIN Offre o ON v.id_vente = o.id_vente "
                    + "WHERE v.encours = 0 AND (vl.date_fin < CURRENT_TIMESTAMP OR v.date_debut < CURRENT_TIMESTAMP) "
                    + "AND v.email = ? "
                    + "ORDER BY v.id_vente, o.prix_offre DESC"; // Trier par prix d'offre décroissant

        PreparedStatement stmt = null;
        ResultSet rset = null;
        Map<Integer, String> bestOfferEmailMap = new HashMap<>();
        Map<Integer, Double> bestOfferPriceMap = new HashMap<>();
        Map<Integer, Integer> bestOfferQuantityMap = new HashMap<>();
        Map<Integer, Timestamp> bestOfferDateMap = new HashMap<>(); // Pour stocker la date de l'offre

        // Définir le format de la date
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        try {
            stmt = this.con.prepareStatement(query);
            stmt.setString(1, email);  // Définir le paramètre email
            rset = stmt.executeQuery();

            // Parcourir les résultats
            while (rset.next()) {
                int venteId = rset.getInt("id_vente");
                double prixDepart = rset.getDouble("prix_depart");
                double prixBaisse = rset.getDouble("prix_baisse");
                Timestamp dateDebut = rset.getTimestamp("date_debut");
                Timestamp dateFin = rset.getTimestamp("date_fin");

                // Récupérer les détails de l'offre
                String offerEmail = rset.getString("offer_email");
                double prixOffre = rset.getDouble("prix_offre");
                int quantite = rset.getInt("quantite");
                Timestamp dateOffre = rset.getTimestamp("date_offre"); // Date de l'offre

                // On garde la meilleure offre pour chaque vente
                if (offerEmail != null) {
                    if (!bestOfferEmailMap.containsKey(venteId) || prixOffre > bestOfferPriceMap.get(venteId)) {
                        bestOfferEmailMap.put(venteId, offerEmail);
                        bestOfferPriceMap.put(venteId, prixOffre);
                        bestOfferQuantityMap.put(venteId, quantite);
                        bestOfferDateMap.put(venteId, dateOffre); // Stocker la date de l'offre
                    }
                }
            }

            // Afficher les résultats
            for (Map.Entry<Integer, String> entry : bestOfferEmailMap.entrySet()) {
                int venteId = entry.getKey();
                String bestOfferEmail = entry.getValue();
                double bestOfferPrice = bestOfferPriceMap.get(venteId);
                int bestOfferQuantity = bestOfferQuantityMap.get(venteId);
                Timestamp offerDate = bestOfferDateMap.get(venteId); // Récupérer la date de l'offre

                // Récupérer les informations de vente depuis la base de données
                stmt = this.con.prepareStatement("SELECT v.prix_depart, v.prix_baisse, v.date_debut, vl.date_fin "
                                                + "FROM Vente v "
                                                + "LEFT JOIN VenteLimite vl ON v.id_vente = vl.id_vente "
                                                + "WHERE v.id_vente = ?");
                stmt.setInt(1, venteId);
                rset = stmt.executeQuery();
                if (rset.next()) {
                    double prixDepart = rset.getDouble("prix_depart");
                    double prixBaisse = rset.getDouble("prix_baisse");
                    Timestamp dateDebut = rset.getTimestamp("date_debut");
                    Timestamp dateFin = rset.getTimestamp("date_fin");

                    // Afficher les détails de la vente sur une seule ligne
                    System.out.print("ID de la vente: " + venteId + ", ");
                    System.out.print("Prix de départ: " + prixDepart + "€, ");
                    System.out.println("Type : " + (prixBaisse == 0.0 ? "Montant" : "Descendant(" + prixBaisse + "€)" ));

                    // Si `date_fin` est null, on remplace par la date de l'offre
                    Timestamp finalDate = (dateFin != null) ? dateFin : offerDate;
                    System.out.print(dateFormat.format(dateDebut) + " - ");
                    System.out.println((finalDate != null ? dateFormat.format(finalDate) : "N/A"));

                    // Afficher les détails de la meilleure offre sur une seule ligne
                    System.out.println("Offre la plus élevée - " + bestOfferEmail 
                                    + " (" + bestOfferPrice 
                                    + "€, " + bestOfferQuantity + " unité(s))");
                    System.out.println("------------");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rset != null) rset.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Cette méthode vérifie les enchères de type 'VenteLimite' qui ont dépassé leur date de fin.
     * Si une enchère a dépassé sa date de fin, elle met à jour la colonne 'encours' à 0,
     * ce qui marque l'enchère comme terminée.
     */
    public void updateVenteLimiteStatus() {
        try {
            // Restaurer l'auto-commit à false
            this.con.setAutoCommit(false);
        } catch (SQLException e) {
            System.err.println("Erreur lors de la désactivation de l'auto-commit : " + e.getMessage());
        }

        // Requête pour trouver les enchères de type VenteLimite dont la date_fin est dépassée
        String query = "SELECT v.id_vente, vl.date_fin "
                    + "FROM Vente v "
                    + "INNER JOIN VenteLimite vl ON v.id_vente = vl.id_vente "
                    + "WHERE v.encours = 1 AND vl.date_fin < CURRENT_TIMESTAMP"; // Vérifie uniquement les enchères actives dont la date_fin est dépassée
        
        PreparedStatement stmt = null;
        ResultSet rset = null;

        try {
            stmt = this.con.prepareStatement(query);
            rset = stmt.executeQuery();

            // Parcourt chaque VenteLimite dont la date de fin est dépassée
            while (rset.next()) {
                int venteId = rset.getInt("id_vente");
                Timestamp dateFin = rset.getTimestamp("date_fin");

                if (dateFin != null && dateFin.before(new java.sql.Timestamp(System.currentTimeMillis()))) {
                    // Si l'enchère a dépassé sa date de fin, mettre à jour 'encours' à 0
                    updateEncoursToZero(venteId);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rset != null) rset.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Cette méthode met à jour la colonne 'encours' dans la table 'Vente',
     * en la définissant à 0 pour une vente spécifique identifiée par 'venteId'.
     * Cela marque la vente comme terminée. Si la mise à jour réussit, la transaction
     * est validée.
     */
    private void updateEncoursToZero(int venteId) {
        String updateQuery = "UPDATE Vente SET encours = 0 WHERE id_vente = ?";
        PreparedStatement stmt = null;
        try {
            stmt = this.con.prepareStatement(updateQuery);
            stmt.setInt(1, venteId);
            int rowsUpdated = stmt.executeUpdate();
            
            if (rowsUpdated > 0) {

                System.out.println("L'enchère avec l'ID " + venteId + " a été marquée comme terminée (encours = 0).");
                this.con.commit();  // Validation de la transaction
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Affichage de l'erreur en cas de problème
        } finally {
            try {
                if (stmt != null) stmt.close(); 
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Cette méthode récupère et affiche toutes les catégories distinctes
     * présentes dans la table 'Categorie'. Elle exécute une requête SQL pour
     * obtenir les noms des catégories et les affiche ensuite à l'utilisateur.
     */
    public void afficherCats(){
        String query = "SELECT DISTINCT nom_categorie FROM Categorie";

        System.out.println("Catégories disponibles : ");
        try (PreparedStatement pstmt = con.prepareStatement(query)) {
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String categorieName = rs.getString("nom_categorie");
                    System.out.println("- " + categorieName);
                }
                System.out.println("-------------------");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
