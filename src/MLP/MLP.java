package MLP;

import Function.LossFunction;
import Matrices.ActivationMatrix;
import Matrices.BiasVector;
import Matrices.GradientMatrix;
import Matrices.WeightMatrix;

import java.util.ArrayList;
import java.util.List;

public class MLP {

    private final int dimInput;
    private final List<Layer> layers;

    public MLP(List<Layer> layers, int dimInput){
        this.layers = layers;
        this.dimInput = dimInput;
    }

    /**
     * Envoie au réseau de neurones une batch ({@link ActivationMatrix}.
     * et renvoie les matrices d'activations des couches du réseau après le calcul.
     * La fonction renvoie une paire ou le premier élément de la paire est la matrice des activations
     * après application de la fonction d'activation, le second avant. (utile pour {@link #backpropagate}.
     * @param input Le vecteur d'activation initial
     * @return Une liste de paires matrices d'activation qui correspond aux activations de chaque couche
     * dans l'ordre.
     */

    public List<Pair<ActivationMatrix,ActivationMatrix>> feedForward(ActivationMatrix input){
        assert(input.getNumberOfColumns() == dimInput) : "Erreur : dim d'entrée attendue = " + dimInput + " , obtenue : " + input.getNumberOfColumns() + ".";

        List<Pair<ActivationMatrix,ActivationMatrix>> result = new ArrayList<>();
        ActivationMatrix activationMatrixOfLayer = input;


        for(Layer layer : layers){
            // Calcul de    AxW + B
            ActivationMatrix activationsBeforeAF = layer.multiplyByWeightsAndAddBias(activationMatrixOfLayer);
            // Calcul de  f(AxW + B)
            activationMatrixOfLayer = activationsBeforeAF
                    .clone() // Nécessaire pour éviter de modifier activationsBeforeAF
                    .applyFunction(layer.getActivationFunction());

            result.add(new Pair<>(activationMatrixOfLayer, activationsBeforeAF));
        }


        return result;
    }


    /**
     * Evalue la prédiction du réseau de neurones sur une certaine entrée {@link ActivationMatrix}.
     * @param input Le {@link ActivationMatrix} dont on calcule le coût
     * @return le coût associé
     */
    public double computeLoss(ActivationMatrix input, ActivationMatrix expectedOutput, LossFunction lossFunction){
        ActivationMatrix networkOutput = feedForward(input).getLast().getA();
        return lossFunction.apply(networkOutput, expectedOutput);
    }

    public void updateParameters(ActivationMatrix input, ActivationMatrix expectedOutput, LossFunction lossFunction){
        List<Pair<GradientMatrix,BiasVector>> gradients = backpropagate(input, expectedOutput, lossFunction);

     //   gradients.forEach(p -> System.out.println(p.getA().norm()));
        // TODO IMPLEMENT OPTIMIZERS
        double learningRate = 0.01;

        for(int l = 0; l < this.layers.size(); l++){
            GradientMatrix weightCorrection = gradients.get(l).getA().clone().multiply(learningRate);
            BiasVector biasGradient = gradients.get(l).getB().clone().multiply(learningRate);

            Layer currentLayer = this.getLayer(l);

            // TODO use setter with assert to verify dimensions
            currentLayer.getWeightMatrix().substract(weightCorrection);
            currentLayer.getBiasVector().substract(biasGradient);
        }


    }

    /**
     *
     * @param input
     * @param expectedOutput
     * @param lossFunction
     * @return
     */
    public List<Pair<GradientMatrix,BiasVector>> backpropagate(ActivationMatrix input, ActivationMatrix expectedOutput, LossFunction lossFunction) {
        List<Pair<GradientMatrix,BiasVector>> gradients = new ArrayList<>();
        List<Pair<ActivationMatrix, ActivationMatrix>> activations = this.feedForward(input);
        int L = layers.size();
        ActivationMatrix a_L = activations.getLast().getA(); // Activations de la dernière couche APRES fonction d'activation
        ActivationMatrix z_L = activations.getLast().getB(); // Activations de la dernière couche AVANT fonction d'activation

        // Vérification des dimensions de sortie
        assert(expectedOutput.hasSameDimensions(a_L)) : String.format(
                "Dimensions de sortie incorrectes: attendu %dx%d, obtenu %dx%d",
                a_L.getNumberOfRows(), a_L.getNumberOfColumns(),
                expectedOutput.getNumberOfRows(), expectedOutput.getNumberOfColumns());

        // Calcul de dL/da_L [sortie attendue]
        GradientMatrix dL_da_L = lossFunction.applyDerivative(a_L, expectedOutput);
        // Calcul de σ'(z_L)
        ActivationMatrix sigma_prime_z_L = z_L.applyFunction(getLastLayer().getDerivativeOfAF());
        //  dL/da_L ⊙ σ'(z_L)
        GradientMatrix delta_L = dL_da_L.hadamardProduct(sigma_prime_z_L);

        // Calcul des gradients pour la couche de sortie
        ActivationMatrix a_L_minus_1 = (L-1 > 0) ? activations.get(L-2).getA() : input;
        GradientMatrix dL_dW_L = delta_L.multiplyAtRight(a_L_minus_1.transpose());

        // TODO VERIFY SI CESET LA BONNE CHOSE !
        BiasVector dL_db_L = delta_L.sumErrorTerm();
        gradients.addFirst(new Pair<>(dL_dW_L, dL_db_L));

        // Rétropropagation à travers les couches cachées
        GradientMatrix delta_l_plus_1 = delta_L;

        // Rétropropagation à travers les couches cachées

        for (int l = L-2; l >= 0; l--) {

            ActivationMatrix z_l = activations.get(l).getB(); // Activations Pre-AF
            ActivationMatrix a_l_minus_1 = (l > 0) ? activations.get(l-1).getA() : input; // Activations Post-AF

            Layer layer_l_plus_1 = this.layers.get(l+1);
            Layer layer_l = this.layers.get(l);

            WeightMatrix W_l_plus_1_T = layer_l_plus_1.getWeightMatrix().transpose();
            ActivationMatrix sigma_prime_z_l = z_l.applyFunction(layer_l.getDerivativeOfAF());
            GradientMatrix delta_l = delta_l_plus_1
                    .multiply(W_l_plus_1_T)
                    .hadamardProduct(sigma_prime_z_l);

            // Finalement : calcul du gradient des poids et du biais
            GradientMatrix dl_dW_l = delta_l.multiplyAtRight(a_l_minus_1.transpose());
            BiasVector dL_db_l = delta_l.sumErrorTerm();

            gradients.addFirst(new Pair<>(dl_dW_l, dL_db_l));
            delta_l_plus_1 = delta_l;

        }

        return gradients;

    }

    public List<Pair<GradientMatrix,BiasVector>> backpropagateDebugSave(ActivationMatrix input, ActivationMatrix expectedOutput, LossFunction lossFunction) {
        List<WeightMatrix> weightsClone = new ArrayList<>();
        List<BiasVector> biasesClone = new ArrayList<>();

        for(Layer layer : this.getLayers()) {
            weightsClone.add(layer.getWeightMatrix().clone());
            biasesClone.add(layer.getBiasVector().clone());
        }

        ///
        List<Pair<GradientMatrix,BiasVector>> gradients = new ArrayList<>();
        List<Pair<ActivationMatrix, ActivationMatrix>> activations = this.feedForward(input);
        int L = layers.size();
        ActivationMatrix a_L = activations.getLast().getA(); // Activations de la dernière couche APRES fonction d'activation
        ActivationMatrix z_L = activations.getLast().getB(); // Activations de la dernière couche AVANT fonction d'activation

        // Vérification des dimensions de sortie
        assert(expectedOutput.hasSameDimensions(a_L)) : String.format(
                "Dimensions de sortie incorrectes: attendu %dx%d, obtenu %dx%d",
                a_L.getNumberOfRows(), a_L.getNumberOfColumns(),
                expectedOutput.getNumberOfRows(), expectedOutput.getNumberOfColumns());

        // Calcul de dL/da_L [sortie attendue]
        GradientMatrix dL_da_L = lossFunction.applyDerivative(a_L, expectedOutput);
        // Calcul de σ'(z_L)
        ActivationMatrix sigma_prime_z_L = z_L.applyFunction(getLastLayer().getDerivativeOfAF());
        //  dL/da_L ⊙ σ'(z_L)
        GradientMatrix delta_L = dL_da_L.hadamardProduct(sigma_prime_z_L);

        // Calcul des gradients pour la couche de sortie
        ActivationMatrix a_L_minus_1 = (L-1 > 0) ? activations.get(L-2).getA() : input;
        GradientMatrix dL_dW_L = delta_L.multiply(a_L_minus_1.transpose());

        // TODO VERIFY SI CESET LA BONNE CHOSE !
        List<Pair<GradientMatrix,BiasVector>> gradientsClone = new ArrayList<>();

        BiasVector dL_db_L = delta_L.sumErrorTerm();
        gradients.addFirst(new Pair<>(dL_dW_L, dL_db_L));
        gradientsClone.addFirst(new Pair<>(dL_dW_L, dL_db_L));


        // Rétropropagation à travers les couches cachées
        GradientMatrix delta_l_plus_1 = delta_L;

        // Rétropropagation à travers les couches cachées

        for (int l = L-2; l >= 0; l--) {

            ActivationMatrix z_l = activations.get(l).getB(); // Activations Pre-AF
            ActivationMatrix a_l_minus_1 = (l > 0) ? activations.get(l-1).getA() : input;

            Layer layer_l_plus_1 = this.layers.get(l+1);
            Layer layer_l = this.layers.get(l);

            WeightMatrix W_l_plus_1_T = layer_l_plus_1.getWeightMatrix().clone().transpose();
            ActivationMatrix sigma_prime_z_l = z_l.applyFunction(layer_l.getDerivativeOfAF());
            GradientMatrix delta_l = delta_l_plus_1
                    .multiplyAtRight(W_l_plus_1_T)
                    .hadamardProduct(sigma_prime_z_l);

            // Finalement : calcul du gradient des poids et du biais
            GradientMatrix dl_dW_l = delta_l.multiply(a_l_minus_1.transpose());
            BiasVector dL_db_l = delta_l.sumErrorTerm();

            gradients.addFirst(new Pair<>(dl_dW_l, dL_db_l));
            gradientsClone.addFirst(new Pair<>(dl_dW_l.clone(), dL_db_l.clone()));

            delta_l_plus_1 = delta_l;

            for(int z = 0; z < l-1; z++){
                assert(gradients.get(z).getA().equals(gradientsClone.get(z).getA())) : "la loop dans backpro a modifié un des gradients précédents";
                assert(gradients.get(z).getB().equals(gradientsClone.get(z).getB())) : "la loop dans backpro a modifié un des gradients B précédents";
            }

        }


        //

        for(int l = 0; l < this.getLayers().size(); l++){
            WeightMatrix cloneWeight = weightsClone.get(l);
            WeightMatrix weightOfLayer = this.getLayer(l).getWeightMatrix();

            BiasVector cloneBias = biasesClone.get(l);
            BiasVector biasOfLayer = this.getLayer(l).getBiasVector();

            assert(cloneWeight.equals(weightOfLayer)) : "La backpro a modifié les poids du réseau. A la couche : " + l;
            assert(cloneBias.equals(biasOfLayer)) : "La backpro a modifié les biais du réseau.";
            assert(activations.get(l).getA().getNumberOfRows() == weightOfLayer.getNumberOfRows()) : "Le nombre de neurones dans le résultat du feedforward n'est pas égal au nombre de neuronnes de la couche.";
        }

        //

        return gradients;
    }



    public static MLPBuilder builder(int dimInput){
        return new MLPBuilder(dimInput);
    }

    public void print(){

        int n = layers.size();
        System.out.println();
        System.out.println("Dimension d'entrée : " + this.dimInput);
        System.out.println();
        for(int i = 0; i < n; i ++){
            System.out.println("Layer n°" + i + " : ");
            this.layers.get(i).print();;
            System.out.println();
        }
    }

    public void printDimensions(){
        for(int i = 0; i < layers.size(); i++){
            System.out.println("Layer " + i + " of size : " + this.getLayer(i).size());
            this.getLayer(i).getWeightMatrix().printDimensions(String.valueOf(i));
            this.getLayer(i).getBiasVector().printDimensions(String.valueOf(i));
            System.out.println();
            System.out.println();
        }
    }

    public Layer getLastLayer(){
        return this.layers.getLast();
    }

    public Layer getLayer(int i){
        return this.layers.get(i);
    }


    public void printNorms() {
        for(int i = 0; i < layers.size(); i++){
            System.out.println("Layer " + i + " of size : " + this.getLayer(i).size());
            System.out.println(this.getLayer(i).getWeightMatrix().norm());
            System.out.println(this.getLayer(i).getBiasVector().norm());
            System.out.println();
            System.out.println();
        }
    }

    public List<Layer> getLayers(){
        return this.layers;
    }

    public int getDimInput() {
        return this.dimInput;
    }
}
