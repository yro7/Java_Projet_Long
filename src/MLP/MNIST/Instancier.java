package MLP.MNIST;


import MLP.Data.LabeledDataset;
import MLP.MLP;
import Matrices.ActivationMatrix;
import MLP.Trainer;
import MLP.Optimizer;

import java.io.IOException;

import static Function.ActivationFunction.*;
import static Function.LossFunction.*;
import static MLP.Trainer.Evaluation;
import static MLP.Data.LabeledDataset.LabeledBatch;
import static MLP.Data.LabeledDataset.LabeledDataSample;

public class Instancier {

    public static void main(String[] args) throws IOException {
        // Chemins vers les fichiers MNIST
        // Génère un ensemble de données d'entraînement Mnist
        MnistTrainingDataset2 trainData = new MnistTrainingDataset2(10);

        MnistTestData testData = new MnistTestData();


        LabeledBatch batch = trainData.getBatch(4);


        // Construction du trainer
        Trainer mnistTrainer = Trainer.builder()
                .setLossFunction(CE)
                .setOptimizer(new Optimizer(1))
                .setTrainingData(trainData)
                .setTestData(testData)
                .setEpoch(10)
                .setBatchSize(2000)
                .build();

        MLP mnistMLP = MLP.builder(784)
                .setRandomSeed(2)
                .addLayer(256, ReLU)
                .addLayer(128, ReLU)
                .addLayer(10, SoftMax)
                .build()
                .train(mnistTrainer);

        Evaluation eval = mnistTrainer.evaluate(mnistMLP);

    }


    public static void printLoss(MLP mlp, ActivationMatrix batchInput, ActivationMatrix batchTheorique){
        System.out.println("Loss : " + mlp.computeLoss(batchInput, batchTheorique, CE));
    }

    public static int maxIndiceOfArray(double[] array){
        int res = 0;
        double max = array[0];
        for(int i = 0; i < array.length; i ++){
            if(max < array[i]) {
                res = i;
                max = array[i];
            }
        }

        return res;
    }
/**
    public static void save() throws IOException {
        String imagesPath = "src/MLP/MNIST/data/t10k-images.idx3-ubyte";
        String labelsPath = "src/MLP/MNIST/data/t10k-labels.idx1-ubyte";

        // Contient 10k MnistVector
        MnistVector[] mnistVectors = MnistDataReader.readData(imagesPath, labelsPath);

        int batchSize = 2000;

        MnistTrainDatasetLabeled data = new MnistTrainDatasetLabeled(mnistVectors, batchSize);


        // Construction du MLP
        MLP mnistMLP = MLP.builder(784)
                .setRandomSeed(2)
                .addLayer(256, ReLU)
                .addLayer(128, ReLU)
                .addLayer(10, SoftMax)
                .build();

        ActivationMatrix batchTheorique = new ActivationMatrix(batchSize, 10);
        List<Pair<ActivationMatrix, ActivationMatrix>> res;
        // Construction de la matrice des sorties attendues
        for(int i = 0; i < batchSize; i++){
            MnistVector vectorI = mnistVectors[i];
            int label = vectorI.getLabel();

            for(int k = 0; k < 10; k++){
                batchTheorique.getData()[i][k] = (k == label) ? 1 : 0;
            }
        }

        // Calcul de la loss initiale
        printLoss(mnistMLP, data, batchTheorique);

        for(int i = 0; i < 20; i++){
            mnistMLP.updateParameters(data, batchTheorique, CE);
            System.out.println("Etape d'entraînement " + i + " finie!");
            printLoss(mnistMLP, data, batchTheorique);
        }


        for(int i = 0; i < 50; i++){
            MnistVector vector = mnistVectors[i];
            int pred = maxIndiceOfArray(res.getLast().getA().getData()[0]);
            System.out.println("Prédiction : " + pred + "   |   Valeur réelle : " + vector.getLabel());

        }

        mnistMLP.serialize("mnist_resolver");
    }**/
}