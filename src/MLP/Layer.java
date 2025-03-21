package MLP;

import Function.ActivationFunction;
import Matrices.ActivationMatrix;
import Matrices.BiasVector;
import Matrices.WeightMatrix;

import java.util.function.Function;

public class Layer {


    public WeightMatrix weightMatrix;

    /**
     * Le biais de chaque neurone. Si la couche possède n neurones, la {@link Matrices.Matrix} {@link BiasVector} sera de dimension 1 x n.
     */
    public BiasVector biasVector;
    /** La <a href="https://en.wikipedia.org/wiki/Activation_function">Fonction d'Activation</a> à utiliser dans cette couche du réseau de neurones.
     * Cette architecture ne permet donc pas d'avoir une {@link ActivationFunction} différente par neurone de la couche.
     * (la couche est neuron-agnostique, elle ne possède pas d'objet "neurone" à proprement parler), mais à part dans certaines architectures
     * précises (voir <a href="https://www.ibm.com/think/topics/mixture-of-experts">Mixture of Experts</a>) cela n'est généralement pas quelque chose de nécessaire à avoir.
     */
    private ActivationFunction activationFunction;

    public Layer(WeightMatrix weightMatrix, BiasVector biasVector, ActivationFunction activationFunction) {
        this.weightMatrix = weightMatrix;
        this.biasVector = biasVector;
        this.activationFunction = activationFunction;
    }

    public WeightMatrix getWeightMatrix() {
        return weightMatrix;
    }

    /**
     * @param numberOfNeuronsOfPreviousLayer le nombre de neurones de la couche précédente.
     * @param numberOfNeuronsOfNewLayer Le nombre de neurones de la nouvelle couche.
     * @param activationFunction la fonction d'activation à appliquer à la fin du calcul
     */
    public Layer(int numberOfNeuronsOfPreviousLayer, int numberOfNeuronsOfNewLayer, ActivationFunction activationFunction){
        this.weightMatrix = new WeightMatrix(numberOfNeuronsOfPreviousLayer, numberOfNeuronsOfNewLayer, activationFunction);
        this.activationFunction = activationFunction;
        this.biasVector = new BiasVector(numberOfNeuronsOfNewLayer, numberOfNeuronsOfPreviousLayer, activationFunction);
    }

    public void print() {
        System.out.println("Activation function : " + this.getActivationFunction());
        System.out.println("Taille : " + this.getWeightMatrix().getNumberOfColumns());
        System.out.println("Taille de la couche précédente : " + this.getWeightMatrix().getNumberOfRows());
        System.out.println("Weights of the layer : ");
        this.getWeightMatrix().print();
        System.out.println("Biais: ");
        this.biasVector.print();
    }

    /**
     * Renvoie AxW + B où W est la matrice de poids, A la matrice d'activation actuelle,
     * B le vecteur biais, f la fonction d'activation de la couche.
     *
     * Calcule donc les activations des neuronnes de cette couche.
     * @param activationsOfPreviousLayer Le vecteur d'activation de la couche précédente
     * @return La nouvelle matrice d'activation de cette couche.
     * @immutable ne modifie pas la matrice passée en argument
     */
    public ActivationMatrix multiplyByWeightsAndAddBias(ActivationMatrix activationsOfPreviousLayer) {
        return activationsOfPreviousLayer
                .multiplyByWeightMatrix(this.weightMatrix)  // Performe A' = A*W
                .addBiasVector(this.biasVector); // A' = A + B
    }

    public ActivationFunction getActivationFunction(){
        return this.activationFunction;
    }

    /**
     * Renvoie le nombre de lignes dans la matrice de poids de la couche, i.e le nombre de neurones de la couche.
     * @return le nombre de neurones dans la couche.
     */
    public int size(){
        return this.getWeightMatrix().getNumberOfRows();
    }

    /**
     * Renvoie la dérivée de la fonction d'activation de la couche.
     * @return
     */
    public Function<Double,Double> getDerivativeOfAF(){
        return this.getActivationFunction().getDerivative();
    }

    public BiasVector getBiasVector(){
        return this.biasVector;
    }
}
