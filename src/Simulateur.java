import gui.GUISimulator;
import gui.Simulable;
import gui.Rectangle;
import gui.ImageElement;
import java.util.PriorityQueue;
import java.awt.Color;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.zip.DataFormatException;

/**
 * Cette classe s'occupe de l'affichage graphique des données de simulation.
 */
public class Simulateur implements Simulable {
    private GUISimulator gui;
    private DonneesSimulation donnees;
    private Carte carte;
    private long dateSimulation;
    private PriorityQueue<Evenement> evenements;
    private PriorityQueue<Evenement> bis_evenements;
    private int NbrEvenement;

    /**
     * Constructeur de la classe AffichageGraphique.
     * 
     * @param donnees      Les données de simulation contenant les robots et les
     *                     incendies.
     * @param NbrEvenement Le nombre d'evenements a réaliser
     */
    public Simulateur(DonneesSimulation donnees, int NbrEvenement) {
        this.donnees=donnees;
        this.carte = donnees.getCarte();
        this.NbrEvenement=NbrEvenement;
        // Création de la fenêtre graphique
        this.gui = new GUISimulator(800, 600, java.awt.Color.WHITE, this);
        afficheTout();
        // Debut des events
        this.dateSimulation = 0;
        initEvenement(NbrEvenement);
    }

    /**
     * Affiche le quadrillage de la carte.
     */
    public void afficheCases() {
        int tailleX = 800 / carte.getNbColonnes();
        int tailleY = 600 / carte.getNbLignes();

        for (int i = 0; i < carte.getNbLignes(); i++) {
            for (int j = 0; j < carte.getNbColonnes(); j++) {
                int x = j * (800 / carte.getNbColonnes()) + tailleX / 2;
                int y = i * (600 / carte.getNbLignes()) + tailleY / 2;
                Rectangle r = new Rectangle(x, y, java.awt.Color.black, null, tailleX, tailleY);
                this.gui.addGraphicalElement(r);
            }
        }
    }

    /**
     * Affiche les images des cases sur la carte en fonction de leur nature.
     */
    public void imageCases() {
        int tailleX = 800 / carte.getNbColonnes();
        int tailleY = 600 / carte.getNbLignes();

        for (int i = 0; i < carte.getNbLignes(); i++) {
            for (int j = 0; j < carte.getNbColonnes(); j++) {
                int x = j * tailleX;
                int y = i * tailleY;

                NatureTerrain nature = carte.getCase(i, j).getNature();
                String image = "";
                switch (nature) {
                    case EAU:
                        image = "images/water.png";
                        break;
                    case FORET:
                        image = "images/forest.png";
                        break;
                    case ROCHE:
                        image = "images/rock.png";
                        break;
                    case TERRAIN_LIBRE:
                        image = "images/grass.png";
                        break;
                    case HABITAT:
                        image = "images/house.png";
                        break;
                    default:
                        break;
                }

                ImageElement r = new ImageElement(x, y, image, tailleX, tailleY, null);
                this.gui.addGraphicalElement(r);
            }
        }
    }

    /**
     * Affiche les images des incendies sur la carte.
     */
    public void imageIncendie() {
        int tailleX = 800 / carte.getNbColonnes();
        int tailleY = 600 / carte.getNbLignes();
        for (Incendie incendie : donnees.getIncendies()) {
            int x = incendie.getPosition().getColonne() * tailleX;
            int y = incendie.getPosition().getLigne() * tailleY;
            String image = "images/fire.png";
            ImageElement r = new ImageElement(x, y, image, tailleX, tailleY, null);
            this.gui.addGraphicalElement(r);
            afficheBarreIntensite(incendie, x, y, tailleX, tailleY);
        }
    }

    /**
     * Affiche les images des robots sur la carte.
     */
    public void imageRobots() {
        int tailleX = 800 / carte.getNbColonnes();
        int tailleY = 600 / carte.getNbLignes();
        for (Robot robot : donnees.getRobots()) {
            int x = robot.getPosition().getColonne() * tailleX;
            int y = robot.getPosition().getLigne() * tailleY;
            String image = "";
            if (robot instanceof RobotDrone) {
                image = "images/drone.png";
            } else if (robot instanceof RobotARoues) {
                image = "images/roues.png";
            } else if (robot instanceof RobotChenille) {
                image = "images/chenilles.png";
            } else if (robot instanceof RobotAPattes) {
                image = "images/pattes.png";
            }
            ImageElement r = new ImageElement(x, y, image, tailleX, tailleY, null);
            this.gui.addGraphicalElement(r);
            afficheBarreReservoir(robot, x, y, tailleX, tailleY);
        }
    }

    /**
     * Reset et affiche toutes les images sur la carte.
     */
    public void afficheTout() {
        this.gui.reset();
        this.imageCases();
        this.imageIncendie();
        this.imageRobots();
        this.afficheCases();
    }

    /**
     * Initialise une série d'évènement à executer au cours du jeu.
     * 
     * @param Donnees Les données issues de la carte.
     */
    private void initEvenement(int NbrEvenement) {
        // int nbr=4;//Nbr d'evenements à executer; On ne peut ne pas utiliser NbrEvenement donc a revoir
        this.evenements = new PriorityQueue<Evenement>(NbrEvenement);
        this.bis_evenements=new PriorityQueue<Evenement>(NbrEvenement);

    }

    /**
     * Définition: 
     * On appellera évènement primaire un évènement appellé dans les fichiers Test et secondaire ceux appellés dans tout autre fichier. 
     * 
     * Ajoute un évènement primaire qui doit être réaliser au simulateur.
     * 
     * @param e Evènement à ajouter
     */
    public void ajouteEvenement(Evenement e) {
        this.evenements.add(e);
        ajouteEvenementbis(e);
    }
        
    /**
     * Ajoute un évènement primaire à la copie de la liste des évènements
     * @param e Evènement a ajouter
     */
    private void ajouteEvenementbis(Evenement e){
        //Copie de l'evenement
        if (e.getClass()== EvenementChef.class){
            EvenementChef chef= (EvenementChef) e;
            //EvenementChef copie= new EvenementChef(chef.getDate(),chef.getChef() , chef.getInterval() , this );
            this.bis_evenements.add( new EvenementChef(chef.getDate(),copyChef(chef.getChef()) , chef.getInterval() , this ) );

        } else if (e.getClass()==EvenementDeplacement.class){
            EvenementDeplacement depl= (EvenementDeplacement) e;
            this.bis_evenements.add(new EvenementDeplacement(depl.getDate(), depl.getDirection(), copyRobot(depl.getRobot()), carte,this));
        
        } else if (e.getClass()==EvenementExtinction.class){
            EvenementExtinction ext= (EvenementExtinction) e;
            this.bis_evenements.add(new EvenementExtinction(this,copyIncendie(ext.getIncendie()) , copyRobot(ext.getRobot()), ext.getRemplissageAuto()));
        
        } else if (e.getClass()==EvenementRemplissage.class){
            EvenementRemplissage remp= (EvenementRemplissage) e;
            this.bis_evenements.add(new EvenementRemplissage(carte, copyRobot(remp.getRobot()), this));
        }
        
    }

    /**
     * Copie un robot en creant une nouvelle instance
     * @param R robot à copier
     * @return Copie du robot entré en paramètre
     */
    private Robot copyRobot(Robot R){
        Robot copy;
        if (R.getClass()==RobotAPattes.class ){
            copy = new RobotAPattes(R.getPosition().copy(), R.getVitesse(NatureTerrain.TERRAIN_LIBRE));
        } else if (R.getClass()==RobotARoues.class ){
            copy = new RobotARoues(R.getPosition().copy(), R.getVitesse(NatureTerrain.TERRAIN_LIBRE));
        } else if (R.getClass()==RobotChenille.class ){
            copy = new RobotChenille(R.getPosition().copy(), R.getVitesse(NatureTerrain.TERRAIN_LIBRE));
        } else {
            copy = new RobotDrone(R.getPosition().copy(), R.getVitesse(NatureTerrain.TERRAIN_LIBRE));
        }
        return copy;
    }

    /**
     * Copie un Chef  en créant un nouvelle instance
     * @param chef Chef à copier
     * @return Copie du Chef passé en paramètre
     */
    private Chef copyChef(Chef chef){
        //Creation d'un nouveau tableau de robots
        Robot[] robots= new Robot[chef.getRobots().length];
        for (int i=0; i<chef.getRobots().length; i++){
            robots[i]=copyRobot(chef.getRobots()[i]);
        }

        //Creation du Hashet des incendies
        HashSet<Incendie> incendies= new HashSet<Incendie>(chef.getIncendies().size());
        for (Incendie I: chef.getIncendies()){
            Incendie ic=copyIncendie(I);
            incendies.add(ic);
            //System.out.println("INcendie copie: "+ic.getPosition().getLigne()+" "+ic.getPosition().getColonne());
        }

        //Construction de la classe et retour
        Chef copy;
        if (chef.getClass()==ChefCollaboratif.class){copy=new ChefCollaboratif(carte, robots, incendies, this);}
        else if (chef.getClass()==ChefEvolue.class){copy= new ChefEvolue(carte, robots, incendies, this);}
        else {copy= new ChefSimple(carte, robots, incendies, this);}
        
        return copy;
    }
    
    /**
     * Copie de l'incendie passé en paramètre
     * @param incendie Incendie à copier
     * @return Copie de l'incendie
     */
    private Incendie copyIncendie(Incendie incendie){
        return new Incendie(incendie.getPosition().copy(), incendie.getIntensite());
    }

    /**
     * Ajoute un évènement secondaire à la liste d'évènement principale
     * NB: A ne pas utiliser dans les fichier tests mais seulement dans les autres fichier.
     * @param e evenement à ajouter
     */
    public void remplaceEvenement(Evenement e){
        this.evenements.add(e);
    }

    /**
     * Re-affecte tous les évènements copiés à la liste d'evenements principaux 
     */
    private void restartEvent(){
        this.evenements= new PriorityQueue<Evenement>(this.NbrEvenement);

        for (Evenement E : this.bis_evenements){
            this.evenements.add(E);
            
            //Affectation des robots dans les donnees a ceux copier qui se retrouve dans les nouveaux evenements.
            if (E.getClass()==EvenementDeplacement.class){
                EvenementDeplacement event=(EvenementDeplacement)E;
                for (int i=0; i<donnees.getRobots().length; i++){
                    if (samePosition(donnees.getRobots()[i].getPosition(), event.getRobot().getPosition())){
                        //donnees.getRobots()[i].setRobot(event.getRobot());
                        event.setRobot(donnees.getRobots()[i]);
                        //System.out.println("Deplacement robot:"+ donnees.getRobots()[i]+" et:"+event.getRobot());
                        //System.out.println("position:"+donnees.getRobots()[i].getPosition()+"et:"+event.getRobot().getPosition());
                        break;
                    }
                }
            
            //Affactation de la carte
                event.setCarte(this.carte);

            
            //Affectation des robots dans les donnees a ceux copier qui se retrouve dans les nouveaux evenements
            } else if (E.getClass()==EvenementExtinction.class){
                EvenementExtinction event=(EvenementExtinction)E;
                for (int i=0; i<donnees.getRobots().length; i++){
                    if (samePosition(donnees.getRobots()[i].getPosition(), event.getRobot().getPosition())){
                        //donnees.getRobots()[i].setRobot(event.getRobot());
                        event.setRobot(donnees.getRobots()[i]);
                        break;
                    }
                }

                //Affecte les incendies des évènements à ceux des données
                for ( Incendie i: donnees.getIncendies()){
                    if (samePosition(event.getRobot().getPosition(), i.getPosition())){
                        event.setIncendie(i);
                        break;
                    }
                }

            //Affectation des robots dans les donnees a ceux copier qui se retrouve dans les nouveaux evenements
            } else if (E.getClass()==EvenementRemplissage.class){
                EvenementRemplissage event= (EvenementRemplissage)E;
                for (int i=0; i<donnees.getRobots().length; i++){
                    if (samePosition(donnees.getRobots()[i].getPosition(), event.getRobot().getPosition())){
                        //donnees.getRobots()[i].setRobot(event.getRobot());
                        event.setRobot(donnees.getRobots()[i]);
                        break;
                    }
                }

            //Affectation de la carte
                event.setCarte(this.carte);
            
            //Affectation des robots dans les donnees a ceux copier qui se retrouve dans les nouveaux evenements
            } else {
                EvenementChef event=(EvenementChef)E;
                for (int j=0; j<event.getChef().getRobots().length; j++){

                    for (int i=0; i<donnees.getRobots().length; i++){
                        if (samePosition(donnees.getRobots()[i].getPosition(), event.getChef().getRobots()[j].getPosition())){
                            //donnees.getRobots()[i].setRobot(event.getChef().getRobots()[j]);
                            //event.getChef().getRobots()[j].setRobot(donnees.getRobots()[i]);
                            event.getChef().setRobot(j, donnees.getRobots()[i]);
                            //System.out.println("je suis dans simu, robot chef"+event.getChef().getRobots()[j]+"donnes:"+donnees.getRobots()[i]);
                            break;
                        }
                        
                    }
                }
                //System.out.println("dans simu incendies:"+event.getChef().getIncendies());
                //System.out.println("dans simu incendies:"+donnees.getIncendies());

                //Affecte les incendies de Chef à ceux des données
                event.getChef().setIncendies(donnees.getIncendies());
                //System.out.println("dans simu: incendies apres:"+event.getChef().getIncendies());
                //Affecte la carte
                event.getChef().setCarte(this.carte);
            }
            
        }

        this.bis_evenements=new PriorityQueue<Evenement>(this.NbrEvenement);
        //Recreation de evenements_bis
        for (Evenement E : this.evenements){
            ajouteEvenementbis(E);
        }
    }

    /**
     *  Compare A revoir si je dois utiliser Equals ou pas 
     * @param A
     * @param B
     * @return
     */
    private boolean samePosition(Case A, Case B){
        return A.getColonne()==B.getColonne() && A.getLigne()==B.getLigne();
    }

    /**
     * Incremente la 'date courante' de 1
     */
    private void incrementeDate() {
        this.dateSimulation++;
    }

    /**
     * Retourne la date courante
     * 
     * @return long
     */
    public long getDate() {
        return this.dateSimulation;
    }

    @Override
    public void next() {
        this.incrementeDate();
        System.out.println("date de simu:"+this.dateSimulation);
        while (!this.evenements.isEmpty() && this.evenements.peek().getDate() <= this.dateSimulation) {
            this.evenements.poll().execute();
        }
        System.out.println("");
    }

    @Override
    public void restart() {
        //System.out.println("Avant donnee: "+this.donnees.getRobots()[2].getPosition() + " Lec"+LecteurDonnees.getDonnees().getRobots()[2].getPosition());
        //System.out.println("robots:"+ Arrays.toString(LecteurDonnees.getDonnees().getRobots() ) );

        this.gui.reset();
        String fichier=LecteurDonnees.getFichier();

        try {
            LecteurDonnees.lire(fichier);
        } catch (FileNotFoundException e) {
            System.out.println("fichier " + fichier + " inconnu ou illisible");
        } catch (DataFormatException e) {
            System.out.println("\n\t**format du fichier " + fichier + " invalide: " + e.getMessage());
        }

        //this.NbrEvenement=NbrEvenement;
        this.donnees=LecteurDonnees.getDonnees();
        this.carte = this.donnees.getCarte();

        // Création de la fenêtre graphique
        this.gui.setSimulable(this);
        afficheTout();
        
        // Debut des events
        this.dateSimulation = 0;
        this.restartEvent();
        System.out.println("cartes sim"+carte);
    }

    /**
     * Retourne le GUISimulator
     * 
     * @return GUISimulator
     */
    public GUISimulator getGUI() {
        return this.gui;
    }

    /**
     * Retourne les Données de simulations
     * 
     * @return DonneesSimulation
     */
    public DonneesSimulation getDonnees() {
        return this.donnees;
    }

    /**
     * Affiche la barre de réservoir d'un robot.
     * 
     * @param robot
     * @param x
     * @param y
     * @param tailleX
     * @param tailleY
     */
    private void afficheBarreReservoir(Robot robot, int x, int y, int tailleX, int tailleY) {
        double ratio = (double) robot.getVolumeActuelle() / (double) robot.getVolumeMax();
        int hauteurBarre = (int) (tailleY * ratio);
        int largeurBarre = 10;
        int xBarre = x + tailleX + 5;
        int yBarre = y + tailleY - hauteurBarre;

        // Fond de la barre
        gui.addGraphicalElement(new Rectangle(xBarre + largeurBarre / 2, y + tailleY / 2, Color.LIGHT_GRAY,
                Color.LIGHT_GRAY, largeurBarre, tailleY));
        // Barre de réservoir
        gui.addGraphicalElement(new Rectangle(xBarre + largeurBarre / 2, yBarre + hauteurBarre / 2, Color.BLUE,
                Color.BLUE, largeurBarre, hauteurBarre));
        // Bordure de la barre
        gui.addGraphicalElement(
                new Rectangle(xBarre + largeurBarre / 2, y + tailleY / 2, Color.BLACK, null, largeurBarre, tailleY));
    }

    /**
     * Affiche la barre d'intensité d'un incendie.
     * 
     * @param incendie
     * @param x
     * @param y
     * @param tailleX
     * @param tailleY
     */
    private void afficheBarreIntensite(Incendie incendie, int x, int y, int tailleX, int tailleY) {
        double ratio = (double) incendie.getIntensite() / (double) incendie.getIntensiteMax();
        int hauteurBarre = (int) (tailleY * ratio);
        int largeurBarre = 10;
        int xBarre = x + tailleX - 10;
        int yBarre = y + tailleY - hauteurBarre;

        // Fond de la barre
        gui.addGraphicalElement(new Rectangle(xBarre + largeurBarre / 2, y + tailleY / 2, Color.LIGHT_GRAY,
                Color.LIGHT_GRAY, largeurBarre, tailleY));
        // Barre d'intensité
        gui.addGraphicalElement(new Rectangle(xBarre + largeurBarre / 2, yBarre + hauteurBarre / 2, Color.RED,
                Color.RED, largeurBarre, hauteurBarre));
        // Bordure de la barre
        gui.addGraphicalElement(
                new Rectangle(xBarre + largeurBarre / 2, y + tailleY / 2, Color.BLACK, null, largeurBarre, tailleY));
    }
}
