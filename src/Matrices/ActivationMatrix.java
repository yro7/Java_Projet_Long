package Matrices;

/**
 * Représente une matrice d'activation d'une couche (Layer) d'un réseau de neurones (MLP).
 * Plutôt qu'un simple vecteur d'activation, cette classe permet de traiter plusieurs entrées
 * simultanément. Voir <a href="https://en.wikipedia.org/wiki/Online_machine_learning#Batch_learning">Batch Learning</a>.
 *
 * Une ActivationMatrix contient typiquement les valeurs d'activation pour tous les neurones
 * d'une couche donnée, pour chaque exemple dans un lot (batch) d'entrées.
 */
public class ActivationMatrix extends Matrix<ActivationMatrix> {

    /**
     * Constructeur créant une matrice d'activation de taille rows x cols remplie de zéros.
     *
     * @param rows Nombre de lignes (correspond au nombre d'items dans le batch)
     * @param cols Nombre de colonnes (correspond au nombre de neurones dans la couche)
     */
    public ActivationMatrix(int rows, int cols) {
        super(rows, cols);
    }

    /**
     * Constructeur à partir d'un tableau bidimensionnel.
     *
     * @param data Tableau 2D de double représentant les activations
     */
    public ActivationMatrix(double[][] data) {
        super(data);
    }

    /**
     * Méthode factory pour créer une nouvelle instance d'ActivationMatrix.
     * Utilisée en interne par les méthodes héritées de Matrix.
     *
     * @param rows Nombre de lignes de la nouvelle instance
     * @param cols Nombre de colonnes de la nouvelle instance
     * @return Une nouvelle instance d'ActivationMatrix
     */
    @Override
    protected ActivationMatrix createInstance(int rows, int cols) {
        return new ActivationMatrix(rows, cols);
    }

    /**
     * Ajoute un vecteur de biais à chaque ligne de cette matrice d'activation.
     * Cette opération est typiquement effectuée dans le calcul forward du réseau de neurones.
     *
     * @param biasVector Le vecteur de biais à ajouter
     * @return Cette matrice modifiée
     * @throws AssertionError si les dimensions ne correspondent pas
     * @mutable Cette méthode modifie la matrice actuelle
     * @intermédiaire Renvoie this pour permettre le chaînage
     */
    public ActivationMatrix addBiasVector(BiasVector biasVector) {
        assert(this.getNumberOfColumns() == biasVector.getLength()) : "Vecteur d'entrée incorrect : "
                + "Longueur du vecteur bias : " + biasVector.getLength()
                + " Nombre de colonnes de la matrice : " + this.getNumberOfRows();

        double[][] data = this.getData();
        for(int i = 0; i < this.getNumberOfRows(); i++){
            for(int j = 0; j < this.getNumberOfColumns(); j++){
                double[] dataRow = data[i];
                dataRow[j] += biasVector.getData()[0][j];
            }
        }
        return this;
    }

    /**
     * Affiche les dimensions de la matrice d'activation dans la console, pour le débogage.
     *
     * @param name Le nom à afficher pour identifier la matrice
     * @terminale Ne renvoie rien et termine la chaîne d'opérations
     */
    public void printDimensions(String name) {
        super.printDimensions("Activation", name);
    }

    /**
     * Renvoie la taille du batch de la matrice d'activation,
     * càd le nombre de colonnes de la matrice.
     * @return
     */
    public int getBatchSize(){
        return this.getNumberOfColumns();
    }

    /**
     * Calcule le produit matriciel entre cette matrice d'activation et une matrice de poids.
     * Cette opération est typiquement effectuée dans le calcul forward d'un réseau de neurones.
     *
     * Si A est cette matrice d'activation et W est la matrice de poids,
     * calcule le produit A x W.
     *
     * @param weightMatrix La matrice de poids à multiplier à droite
     * @return Une nouvelle matrice d'activation résultant du produit
     * @throws AssertionError si les dimensions sont incompatibles
     * @immutable Ne modifie pas la matrice d'activation actuelle
     * @intermédiaire Renvoie une nouvelle matrice pour permettre le chaînage
     */
    public ActivationMatrix multiplyByWeightMatrix(WeightMatrix weightMatrix) {
        return this.multiply(weightMatrix);
    }
}