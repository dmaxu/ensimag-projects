import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.sql.Timestamp;
import java.time.format.DateTimeParseException;
import java.time.Instant;

public class TerminalApp {
    private BaieElec app;
    private String sessionEmail;

    public TerminalApp() {
        this.app = new BaieElec(); 
        this.sessionEmail = null;
    }

    public String getSessionEmail(){
        return this.sessionEmail;
    }

    public void setSessionEmail(String sessionEmail){
        this.sessionEmail = sessionEmail;
    }

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            TerminalApp ta = new TerminalApp();
            ta.auth(scanner); // Authentification de l'utilisateur

            boolean running = true;
            while (running) {
                // Affichage du menu principal
                System.out.println("\n--- Bienvenue dans l'application ---");
                ta.app.checkAndEndAuctions(); // Vérifie et termine les enchères en cours
                ta.app.updateVenteLimiteStatus(); // Met à jour l'état des ventes limitées
                System.out.println("Connecté en tant que : " + ta.getSessionEmail());
                System.out.println("1. Créer vente");
                System.out.println("2. Faire une offre");
                System.out.println("3. Ajouter produit");
                System.out.println("4. Visualiser ma liste d'offres");
                System.out.println("5. Visualiser ma liste de ventes");
                System.out.println("6. Visualiser mes produits");
                System.out.println("7. Visualiser mes ventes finies");
                System.out.println("8. Quitter");
                System.out.print("Sélectionnez une option : ");

                int choice = -1;
                while (true) {
                    if (scanner.hasNextInt()) {
                        choice = scanner.nextInt();
                        scanner.nextLine(); // Consomme la nouvelle ligne
                        break;
                    } else {
                        System.out.println("Entrée invalide. Veuillez entrer un numéro.");
                        scanner.nextLine(); // Consomme l'entrée invalide
                    }
                }
                switch (choice) {
                    case 1:
                        if(ta.app.afficherProduitsVendreParEmail(ta.getSessionEmail())){
                            ta.createVendre(scanner);
                        }
                        break;
                    case 2:
                        ta.app.affichageCategorie(scanner, ta.getSessionEmail());
                        break;
                    case 3:
                        ta.createProduct(scanner);
                        break;
                    case 4:
                        ta.app.afficherMaListeOffre(ta.app.getCon(), ta.getSessionEmail());
                        break;
                    case 5:
                        if(ta.app.afficherVentesParEmail(ta.getSessionEmail())){
                            ta.app.revokeSale(scanner, ta.getSessionEmail());
                        }
                        break;
                    case 6:
                        ta.app.afficherProduitsParEmail(ta.getSessionEmail());
                        break;
                    case 7:
                        ta.app.listerVentesTermineesAvecDetailsOffre(ta.getSessionEmail());
                        break;
                    case 8:
                        ta.app.close_connection();
                        running = false;
                        System.out.println("Fermeture de l'application. Au revoir!");
                        break;
                    default:
                        System.out.println("Option invalide. Veuillez réessayer.");
                }
            }
        }
    }

    // Méthode d'authentification
    public void auth(Scanner scanner) {
        boolean authenticated = false;

        while (!authenticated) {
            System.out.println("--- Accès Utilisateur ---");
            System.out.println("1. Se connecter");
            System.out.println("2. Créer un utilisateur");
            System.out.print("Sélectionnez une option : ");

            int choice = -1;
            while (true) {
                if (scanner.hasNextInt()) {
                    choice = scanner.nextInt();
                    scanner.nextLine(); // Consomme la nouvelle ligne
                    break;
                } else {
                    System.out.println("Entrée invalide. Veuillez entrer un numéro.");
                    scanner.nextLine(); // Consomme l'entrée invalide
                }
            }

            switch (choice) {
                case 1:
                    authenticated = login(scanner); // Tente de connecter l'utilisateur
                    break;
                case 2:
                    createUser(scanner); // Crée un nouvel utilisateur
                    break;
                default:
                    System.out.println("Option invalide. Veuillez réessayer.");
            }
        }
    }

    // Méthode de connexion
    public boolean login(Scanner scanner) {
        System.out.println("\n--- Connexion ---");
        System.out.print("Entrez votre email : ");
        String email = scanner.nextLine();

        // Logique de vérification (s'assurer que app.getCon() n'est pas null)
        if (app != null && app.getCon() != null && app.verifyEmailExists(app.getCon(), email)) {
            System.out.println("Connexion réussie!");
            this.setSessionEmail(email);
            return true;
        } else {
            System.out.println("Email invalide. Veuillez réessayer.");
            return false;
        }
    }

    // Méthode de création d'un utilisateur
    public void createUser(Scanner scanner) {
        System.out.println("\n--- Créer un utilisateur ---");
        System.out.print("Votre email : ");
        String email = scanner.nextLine();
        System.out.print("Votre nom : ");
        String nom = scanner.nextLine();
        System.out.print("Votre prénom : ");
        String prenom = scanner.nextLine();
        System.out.print("Votre adresse : ");
        String adresse = scanner.nextLine();

        this.app.createUser(this.app.getCon(), email, nom, prenom, adresse);
    }

    // Méthode de création d'un produit
    public void createProduct(Scanner scanner) {
        System.out.println("\n--- Créer un produit ---");
    
        // Collecte des détails du produit
        System.out.print("Nom du produit : ");
        String productName = scanner.nextLine();
    
        System.out.print("Prix : ");
        double price = Double.parseDouble(scanner.nextLine());
    
        System.out.print("Quantité en stock : ");
        int stock = Integer.parseInt(scanner.nextLine());
    
        // Collecte des caractéristiques du produit
        List<String[]> characteristics = new ArrayList<>();
        System.out.println("Entrez les caractéristiques (format 'nom:valeur', ou '#' pour arrêter) :");
        
        while (true) {
            System.out.print("Caractéristique : ");
            String input = scanner.nextLine();
    
            if (input.equals("#")) {
                break; // Quitte la boucle
            }
    
            String[] parts = input.split(":", 2); // Divise en nom et valeur
            if (parts.length == 2) {
                characteristics.add(parts);
            } else {
                System.out.println("Format invalide. Veuillez utiliser 'nom:valeur'.");
            }
        }
    
        app.createProduit(productName, price, stock, this.getSessionEmail(), characteristics);
    
        System.out.println("Produit créé avec succès!");
    }

    // Méthode de création d'une vente
    public void createVendre(Scanner scanner) {
        System.out.println("\n--- Création de Vente ---");
        System.out.print("ID du produit : ");
        int produitId = scanner.nextInt(); 
        
        System.out.print("Prix de départ : ");
        double prixDepart = scanner.nextDouble(); 

        System.out.print("Montant ou Descendant (1 = Montant, 0 = Descendant) : ");
        int montante = scanner.nextInt();
        
        int offreMultiple = 0;

        if(montante == 1){
            System.out.print("Les offres multiples sont-elles autorisées ? (1 = Oui, 0 = Non) : ");
            offreMultiple = scanner.nextInt();
        }
        
        System.out.print("La vente est-elle révocable ? (1 = Oui, 0 = Non) : ");
        int revocable = scanner.nextInt();

        double prixBaisse;

        if (montante == 1) {
            prixBaisse = 0.0; 
        } else {
            System.out.print("Réduction de prix (baisse) : ");
            prixBaisse = scanner.nextDouble();
        }

        System.out.print("Vente libre ou limitée ? (1 = Libre, 0 = Limite) : ");
        int isVenteLibre = scanner.nextInt();

        Timestamp start = Timestamp.from(Instant.now());
        Timestamp end = null;

        if (isVenteLibre == 0) {
            System.out.print("Date de fin (YYYY-MM-DD) : ");
            String endDateInput = scanner.next();
            System.out.print("Heure de fin (HH:MM, ex. 15:59) : ");
            String endTimeInput = scanner.next();

            try {
                    String endTimestampInput = endDateInput + "T" + endTimeInput;
                    LocalDateTime endTimestamp = LocalDateTime.parse(endTimestampInput);
            
                    // Vérifie que la date de fin est après la date de début
                    if (endTimestamp.isBefore(start.toLocalDateTime())) {
                        System.out.println("La date et l'heure de fin doivent être après la date et l'heure de début.");
                        return;  // Sort si invalide
                    }

                    end = Timestamp.valueOf(endTimestamp);
                    
                } catch (DateTimeParseException e) {
                    System.out.println("Format de date ou d'heure invalide. Veuillez utiliser le format YYYY-MM-DD pour la date et HH:MM pour l'heure.");
                    return;  // Sort si parsing échoue
                }
        }

        this.app.afficherCats(); // Affiche les catégories
        System.out.println("Sélectionner ou créer une catégorie.");
        System.out.print("Nom de la catégorie : ");
        String nomCategorie = scanner.next(); 
        
        scanner.nextLine(); 
        System.out.print("Description de la catégorie (facultatif) : ");
        String descCategorie = scanner.nextLine();

        this.app.createVente(prixDepart, offreMultiple, revocable, prixBaisse, montante, this.getSessionEmail(), produitId, nomCategorie, descCategorie, isVenteLibre, start, end);
    }
}
