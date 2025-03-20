package Matrices;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Classe utilitaire pour implémenter tous les algorithmes de gestion de matrices.
 *
 * Terminologie utilisée dans cette documentation :
 * - Méthode mutable : Modifie l'objet existant sur lequel elle est appelée
 * - Méthode immutable : Ne modifie pas l'objet existant, mais retourne un nouvel objet
 * - Opération intermédiaire : Retourne un objet permettant de continuer les opérations (method chaining)
 * - Opération terminale : Retourne une valeur finale et termine la chaîne d'opérations
 */
public abstract class Matrix<T extends Matrix<T>> {

    double[][] data;

    /**
     * Constructeur créant une matrice de taille rows x cols remplie de zéros.
     *
     * @param rows Nombre de lignes
     * @param cols Nombre de colonnes
     * @throws AssertionError si rows ou cols sont négatifs ou nuls
     */
    public Matrix(int rows, int cols) {
        assert(rows > 0) : "Le nombre de lignes doit être supérieur à 0 (" + rows + ").";
        assert(cols > 0) : "Le nombre de colonnes doit être supérieur à 0 (" + cols + ").";
        this.data = new double[rows][cols];
    }

    /**
     * Constructeur à partir d'un tableau bidimensionnel.
     * Effectue une copie du tableau pour créer la nouvelle matrice.
     * @param data Tableau 2D de valeurs double
     */
    public Matrix(double[][] data) {
        this.data = data.clone();
    }

    /**
     * Constructeur de copie à partir d'une autre matrice.
     * Effectue une copie profonde de la matrice source.
     * @param source La matrice à copier
     */
    public Matrix(Matrix<?> source){
        this(source.getNumberOfRows(), source.getNumberOfColumns());
        this.data = source.data.clone();
    }

    /**
     * Méthode abstraite pour créer une nouvelle instance du type concret.
     * Utilisée pour le pattern CRTP dans les méthodes retournant des matrices.
     *
     * @param rows Nombre de lignes de la nouvelle instance
     * @param cols Nombre de colonnes de la nouvelle instance
     * @return Une nouvelle instance du type concret
     */
    protected abstract T createInstance(int rows, int cols);

    /**
     * Permet de retourner la matrice de même type que celle sur laquelle on applique la fonction
     * (pour préserver les types lors du chaining)
     *
     * @return Cette instance, typée correctement
     */
    @SuppressWarnings("unchecked")
    protected T self() {
        return (T) this;
    }

    /**
     * Récupère les données brutes de la matrice.
     *
     * @return Le tableau bidimensionnel contenant les données
     */
    public double[][] getData() {
        return data;
    }

    /**
     * Récupère le nombre de colonnes de la matrice.
     *
     * @return Le nombre de colonnes ou 0 si la matrice est vide
     */
    public int getNumberOfColumns(){
        if (data == null || data.length == 0) {
            return 0;
        }
        return this.getData()[0].length;
    }

    /**
     * Récupère le nombre de lignes de la matrice.
     *
     * @return Le nombre de lignes ou 0 si la matrice est null
     */
    public int getNumberOfRows(){
        if (data == null) {
            return 0;
        }
        return this.data.length;
    }

    /**
     * Convertit cette matrice en GradientMatrix.
     *
     * @return Une nouvelle GradientMatrix avec les mêmes données
     */
    public GradientMatrix toGradientMatrix() {
        return new GradientMatrix(this.data);
    }

    /**
     * Interface fonctionnelle définissant une opération sur les indices d'une matrice.
     */
    @FunctionalInterface
    public interface ElementOperation {
        void apply(int i, int j);
    }

    /**
     * Applique une opération à chaque position (i,j) de la matrice.
     *
     * @param operation L'opération à appliquer
     * @mutable Cette méthode modifie la matrice actuelle
     * @intermédiaire Ne renvoie pas d'objet, mais permet d'enchaîner des opérations
     */
    public void applyToElements(ElementOperation operation){
        for(int i = 0; i < this.getNumberOfRows(); i++){
            for(int j = 0; j < this.getNumberOfColumns(); j++){
                operation.apply(i, j);
            }
        }
    }

    /**
     * Applique une opération à chaque valeur de la matrice.
     *
     * @param action L'action à effectuer sur chaque valeur
     * @return La matrice modifiée
     * @mutable Cette méthode modifie la matrice actuelle
     * @intermédiaire Renvoie this pour permettre le chaînage
     */
    public T forEach(Consumer<? super Double> action){
        applyToElements((i,j) -> action.accept(data[i][j]));
        return self();
    }

    /**
     * Crée une copie profonde de la matrice.
     *
     * @return Une nouvelle matrice avec les mêmes valeurs
     * @immutable Ne modifie pas la matrice actuelle
     * @intermédiaire Renvoie une nouvelle matrice pour permettre le chaînage
     */
    public T clone(){
        T res = this.createInstance(this.getNumberOfRows(), this.getNumberOfColumns());
        applyToElements((i,j) -> res.getData()[i][j] = this.getData()[i][j]);
        return res;
    }

    /**
     * Convertit cette matrice en une ActivationMatrix.
     *
     * @return Une nouvelle ActivationMatrix avec les mêmes valeurs
     * @immutable Ne modifie pas la matrice actuelle
     * @intermédiaire Renvoie une nouvelle matrice pour permettre le chaînage
     */
    public ActivationMatrix cloneActivation(){
        return (ActivationMatrix) this.clone();
    }

    /**
     * Convertit cette matrice en une WeightMatrix.
     *
     * @return Une nouvelle WeightMatrix avec les mêmes valeurs
     * @immutable Ne modifie pas la matrice actuelle
     * @intermédiaire Renvoie une nouvelle matrice pour permettre le chaînage
     */
    public WeightMatrix cloneWeight(){
        return (WeightMatrix) this.clone();
    }

    /**
     * Applique une opération élément par élément entre cette matrice et une autre.
     *
     * @param function La fonction binaire à appliquer (élément actuel, élément de l'autre matrice)
     * @param matrix La deuxième matrice
     * @return La matrice modifiée
     * @throws AssertionError si les dimensions ne correspondent pas
     * @mutable Cette méthode modifie la matrice actuelle
     * @intermédiaire Renvoie this pour permettre le chaînage
     */
    public T elementWiseOperation(BiFunction<Double,Double,Double> function, Matrix<?> matrix){
        verifyDimensions(matrix);
        applyToElements((i,j) -> this.data[i][j] = function.apply(this.getData()[i][j], matrix.getData()[i][j]));
        return self();
    }

    /**
     * Applique une fonction à chaque élément de la matrice.
     *
     * @param function La fonction à appliquer à chaque élément
     * @return La matrice modifiée
     * @mutable Cette méthode modifie la matrice actuelle
     * @intermédiaire Renvoie this pour permettre le chaînage
     */
    public T applyFunction(Function<Double,Double> function) {
        this.applyToElements((i,j) -> this.data[i][j] = function.apply(this.data[i][j]));
        return self();
    }

    /**
     * Calcule le produit matriciel AxB où A est cette matrice et B est la matrice en paramètre.
     *
     * @param matrix La matrice B à multiplier à droite
     * @return Une nouvelle matrice résultant du produit
     * @throws AssertionError si les dimensions sont incompatibles
     * @immutable Ne modifie pas la matrice actuelle
     * @intermédiaire Renvoie une nouvelle matrice pour permettre le chaînage
     */
    public T multiply(Matrix<?> matrix){
        assert(this.getNumberOfColumns() == matrix.getNumberOfRows()) : "Matrices incompatibles pour un produit AxB :"
                + " Nombre de colonnes de A : " + this.getNumberOfColumns()
                + " Nombre de lignes de B : " + matrix.getNumberOfRows();
        int newNumberOfRows = this.getNumberOfRows();
        int newNumberOfColumns = matrix.getNumberOfColumns();

        T newMatrix = createInstance(newNumberOfRows, newNumberOfColumns);
        newMatrix.applyToElements((i, j) -> {
            double sum = 0;
            for(int k = 0; k < this.getNumberOfColumns(); k++){
                sum += this.getData()[i][k] * matrix.getData()[k][j];
            }
            newMatrix.data[i][j] = sum;
        });

        return newMatrix;
    }

    /**
     * Calcule le produit matriciel BxA où A est cette matrice et B est la matrice en paramètre.
     *
     * @param matrix La matrice B à multiplier à gauche
     * @return Une nouvelle matrice résultant du produit
     * @throws AssertionError si les dimensions sont incompatibles
     * @immutable Ne modifie pas la matrice actuelle
     * @intermédiaire Renvoie une nouvelle matrice pour permettre le chaînage
     */
    public T multiplyAtRight(Matrix<?> matrix) {
        assert(matrix.getNumberOfColumns() == this.getNumberOfRows()) : "Matrices incompatibles pour un produit BxA :"
                + " Nombre de colonnes de B : " + matrix.getNumberOfColumns()
                + " Nombre de lignes de A : " + this.getNumberOfRows();

        int newNumberOfRows = matrix.getNumberOfRows();
        int newNumberOfColumns = this.getNumberOfColumns();
        T newMatrix = createInstance(newNumberOfRows, newNumberOfColumns);
        newMatrix.applyToElements((i, j) -> {
            double sum = 0;
            for(int k = 0; k < matrix.getNumberOfColumns(); k++){
                sum += matrix.getData()[i][k] * this.getData()[k][j];
            }
            newMatrix.data[i][j] = sum;
        });

        return newMatrix;
    }

    /**
     * Calcule la transposée de cette matrice.
     *
     * @return Une nouvelle matrice transposée
     * @immutable Ne modifie pas la matrice actuelle
     * @intermédiaire Renvoie une nouvelle matrice pour permettre le chaînage
     */
    public T transpose(){
        T newMatrix = this.createInstance(getNumberOfColumns(), getNumberOfRows());
        this.applyToElements((i,j) -> newMatrix.data[j][i] = this.data[i][j]);
        return newMatrix;
    }

    /**
     * Calcule la somme de tous les éléments de la matrice.
     *
     * @return La somme des éléments
     * @terminale Renvoie une valeur finale et termine la chaîne d'opérations
     */
    public double sum(){
        // Nécessaire de passer par un tableau pour pouvoir
        // Utiliser la variable dans le lambda #forEach
        double[] res = {0.0};
        this.forEach(d -> res[0] += d);
        return res[0];
    }

    /**
     * Calcule la somme des éléments de chaque ligne.
     * Si la matrice est de taille n x p, renvoie un vecteur n x 1.
     *
     * @return Un tableau 2D représentant les sommes par ligne
     * @immutable Ne modifie pas la matrice actuelle
     * @terminale Renvoie une valeur finale et termine la chaîne d'opérations
     */
    public double[][] sumOverRows(){
        double[][] res = new double[this.getNumberOfRows()][1];
        applyToElements((i, j) -> {
            res[i][0] += this.getData()[i][j];
        });

        return res;
    }


    /**
     * Calcule la somme des éléments de chaque colonne.
     * Si la matrice est de taille n x p, renvoie un vecteur p x 1.
     *
     * @return Un tableau 2D représentant les sommes par ligne
     * @immutable Ne modifie pas la matrice actuelle
     * @terminale Renvoie une valeur finale et termine la chaîne d'opérations
     */
    public double[][] sumOverColumns(){
        double[][] res = new double[this.getNumberOfColumns()][1];
        applyToElements((i, j) -> {
            res[i][0] += this.getData()[i][j];
        });

        return res;
    }

    /**
     * 
     *  1 2 3 4
     *  5 6 7 8
     *  9 0 0 0
     * Soustrait une autre matrice à celle-ci élément par élément.
     *
     * @param matrix La matrice à soustraire
     * @return La matrice modifiée
     * @throws AssertionError si les dimensions ne correspondent pas
     * @mutable Cette méthode modifie la matrice actuelle
     * @intermédiaire Renvoie this pour permettre le chaînage
     */
    public T substract(Matrix<?> matrix){
        verifyDimensions(matrix);
        return elementWiseOperation((d1,d2) -> d1 - d2, matrix);
    }

    /**
     * Additionne une autre matrice à celle-ci élément par élément.
     *
     * @param matrix La matrice à additionner
     * @return La matrice modifiée
     * @throws AssertionError si les dimensions ne correspondent pas
     * @mutable Cette méthode modifie la matrice actuelle
     * @intermédiaire Renvoie this pour permettre le chaînage
     */
    public T add(Matrix<?> matrix){
        verifyDimensions(matrix);
        return elementWiseOperation(Double::sum, matrix);
    }

    /**
     * Calcule le produit de Hadamard (multiplication élément par élément) avec une autre matrice.
     *
     * @param matrix La matrice à multiplier élément par élément
     * @return La matrice modifiée
     * @throws AssertionError si les dimensions ne correspondent pas
     * @mutable Cette méthode modifie la matrice actuelle
     * @intermédiaire Renvoie this pour permettre le chaînage
     */
    public T hadamardProduct(Matrix<?> matrix){
        verifyDimensions(matrix);
        return elementWiseOperation((d1, d2) -> d1 * d2, matrix);
    }

    /**
     * Vérifie que la matrice passée en argument a les mêmes dimensions que la matrice actuelle.
     *
     * @param matrix La matrice à vérifier
     * @throws AssertionError si les dimensions ne correspondent pas
     * @terminale Ne renvoie rien et termine la chaîne d'opérations
     */
    public void verifyDimensions(Matrix<?> matrix) {
        assert(this.hasSameDimensions(matrix)) : "Les matrices ne sont pas de même dimensions !"
                + " Matrice A : " + this.getNumberOfRows()+ " x " + this.getNumberOfColumns()
                + " Matrice B : " + matrix.getNumberOfRows()+ " x " + matrix.getNumberOfColumns();
    }

    /**
     * Vérifie si deux matrices ont les mêmes dimensions.
     *
     * @param matrix La matrice à comparer
     * @return true si les dimensions sont identiques, false sinon
     * @terminale Renvoie une valeur finale et termine la chaîne d'opérations
     */
    public boolean hasSameDimensions(Matrix<?> matrix) {
        return this.getNumberOfColumns() == matrix.getNumberOfColumns()
                && this.getNumberOfRows() == matrix.getNumberOfRows();
    }

    /**
     * Applique la fonction signum à chaque élément de la matrice.
     * Les éléments négatifs deviennent -1, les positifs 1, et 0 reste 0.
     *
     * @return La matrice modifiée
     * @mutable Cette méthode modifie la matrice actuelle
     * @intermédiaire Renvoie this pour permettre le chaînage
     */
    public T sign() {
        return this.applyFunction(d -> (double) Integer.signum(d.compareTo(0.0)));
    }

    /**
     * Applique le logarithme népérien à chaque élément de la matrice.
     *
     * @return La matrice modifiée
     * @mutable Cette méthode modifie la matrice actuelle
     * @intermédiaire Renvoie this pour permettre le chaînage
     */
    public T log(){
        return this.applyFunction(Math::log);
    }

    /**
     * Applique le cosinus hyperbolique à chaque élément de la matrice.
     *
     * @return La matrice modifiée
     * @mutable Cette méthode modifie la matrice actuelle
     * @intermédiaire Renvoie this pour permettre le chaînage
     */
    public T cosh(){
        return this.applyFunction(Math::cosh);
    }

    /**
     * Applique la tangente hyperbolique à chaque élément de la matrice.
     *
     * @return La matrice modifiée
     * @mutable Cette méthode modifie la matrice actuelle
     * @intermédiaire Renvoie this pour permettre le chaînage
     */
    public T tanh(){
        return this.applyFunction(Math::tanh);
    }

    /**
     * Élève au carré chaque élément de la matrice.
     *
     * @return La matrice modifiée
     * @mutable Cette méthode modifie la matrice actuelle
     * @intermédiaire Renvoie this pour permettre le chaînage
     */
    public T square(){
        return this.applyFunction(d -> Math.pow(d,2));
    }

    /**
     * Multiplie chaque élément de la matrice par un scalaire.
     *
     * @param scalar Le scalaire à multiplier
     * @return La matrice modifiée
     * @mutable Cette méthode modifie la matrice actuelle
     * @intermédiaire Renvoie this pour permettre le chaînage
     */
    public T multiply(double scalar){
        return this.applyFunction(d -> d*scalar);
    }

    /**
     * Divise chaque élément de la matrice par un scalaire.
     *
     * @param scalar Le diviseur
     * @return La matrice modifiée
     * @mutable Cette méthode modifie la matrice actuelle
     * @intermédiaire Renvoie this pour permettre le chaînage
     */
    public T divide(double scalar){
        return this.applyFunction(d -> d / scalar);
    }

    /**
     * Affiche la matrice dans la console.
     *
     * @terminale Ne renvoie rien et termine la chaîne d'opérations
     */
    public void print(){
        this.applyToElements((i,j) -> {
            if(j == 0) System.out.println();
            System.out.print(this.getData()[i][j] + ",  ");
        });
        System.out.println();
    }

    /**
     * Calcule le nombre total d'éléments dans la matrice (rows * columns).
     *
     * @return Le nombre d'éléments
     * @terminale Renvoie une valeur finale et termine la chaîne d'opérations
     */
    public int size(){
        return this.getNumberOfColumns()*this.getNumberOfRows();
    }

    /**
     * Crée une matrice identité de taille n x n.
     *
     * @param n La taille de la matrice identité
     * @return Une nouvelle matrice identité
     * @immutable Ne modifie pas la matrice actuelle
     * @intermédiaire Renvoie une nouvelle matrice pour permettre le chaînage
     */
    @SuppressWarnings("unchecked")
    public <T extends Matrix<?>> T createIdentity(int n){
        T identity = (T) createInstance(n,n);
        identity.applyToElements((i,j) -> {
            identity.getData()[i][j] = (i == j ? 1.0 : 0.0);
        });

        return identity;
    }

    /**
     * Affiche les dimensions de la matrice dans la console, pour le débogage.
     *
     * @param type Le type de matrice (Weight, Activation, etc.)
     * @param name Le nom à afficher pour identifier la matrice
     * @terminale Ne renvoie rien et termine la chaîne d'opérations
     */
    public void printDimensions(String type, String name){
        System.out.println(type + "Matrix " + name + " has dimensions " + this.getNumberOfRows()+","+this.getNumberOfColumns() + ".");
    }

    /**
     * Calcule la norme euclidienne (ou norme L2) de la matrice.
     *
     * @return La norme de la matrice
     * @terminale Renvoie une valeur finale et termine la chaîne d'opérations
     */
    public double norm(){
        return Math.sqrt(this.square().sum());
    }

    public boolean equals(Matrix<?> matrix){
        if(this.getNumberOfRows() != matrix.getNumberOfRows()
                || this.getNumberOfColumns() != matrix.getNumberOfColumns()) {
            return false;
        }

        AtomicBoolean res = new AtomicBoolean(true);
        this.applyToElements((i,j) -> {
            if (this.getData()[i][j] != matrix.getData()[i][j]) res.set(false);
        });

        return res.get();
    }

    /**
     * Affiche la norme de la matrice.
     * @mutable Modifie la matrice actuelle
     * @terminale Renvoie une valeur finale et termine la chaîne d'opérations
     */
    public void printNorm() {
        System.out.println("Norme : " + this.clone().norm());
    }
}