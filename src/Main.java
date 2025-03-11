import Function.ActivationFunction;
import Matrices.*;

import javax.swing.text.Position;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

import static Function.ActivationFunction.ReLU;
import static Function.LossFunction.MSE;

public class Main {
    public static void main(String[] args) {


        MLP mlp = MLP.builder(43)
                .setRandomSeed(2)
                .addLayer(7, ReLU)
                .addLayer(3, ActivationFunction.ReLU)
                .addLayer(12, ReLU)
                .addLayer(17, ActivationFunction.ReLU)
                .addLayer(24, ActivationFunction.Sigmoid)
                .addLayer(36, ActivationFunction.Sigmoid)
                .build();

        ActivationMatrix batchInput = new ActivationMatrix(creerTableau(43,10));
        ActivationMatrix batchTheorique = new ActivationMatrix(creerTableau(36,10));


        mlp.printNorms();
        mlp.feedForward(batchInput);
        mlp.feedForward(batchInput);
        mlp.printNorms();


        //mlp.backpropagate(batchInput, batchTheorique, MSE);
        /**
        IntStream.range(1,15).forEach(i -> {
            mlp.backpropagate(batchInput, batchTheorique, MSE);
            loss[0] = mlp.computeLoss(batchInput, batchTheorique, MSE);
            System.out.println("loss at iteration : " + i + " : "  + loss[0]);
**/

    }

    public static double[][] creerTableau(int n, int p){
        double[][] res = new double[n][p];
        int compteur = 1;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < p; j++) {
                res[i][j] = compteur++;
            }
        }
        return res;
    }

    public static void loopBackpro(MLP mlp, ActivationMatrix batchInput, ActivationMatrix batchTheorique, int n){
        IntStream.range(1,n).forEach(i -> {
            mlp.backpropagate(batchInput, batchTheorique, MSE);
    });

    }


}