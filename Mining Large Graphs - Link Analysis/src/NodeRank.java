import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Potrebno je za ulazni usmjereni graf za svaki cvor u grafu izracunati rang (engl. Node rank) .
 * Created by Vuki on 24.5.2017..
 */
public class NodeRank {

    //broj cvorova u usmjerenom grafu
    private int numOfNodes;
    //vjerojatnost da slucajni setac prilikom setnje grafom slijedi bridove u grafu. (1-b je vjerojatnost teleportiranja)
    private double walkerPropability;
    private double teleport = 1 - walkerPropability;
    private Node[] data;

    private DecimalFormat decimalFormat;

    public NodeRank() {
        decimalFormat = new DecimalFormat("0.0000000000");
    }

    public static void main(String[] args) {
        NodeRank nodeRank = new NodeRank();
        nodeRank.start();
    }

    public void start() {
        InputStream path = NodeRank.class.getResourceAsStream("node_rank_input_m");
        //try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(path))) {
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in))) {

            String[] conf = bufferedReader.readLine().split(" ");
            numOfNodes = Integer.parseInt(conf[0]);
            data = new Node[numOfNodes];
            walkerPropability = Double.parseDouble(conf[1]);

            for (int i = 0; i < numOfNodes; i++) {
                data[i] = new Node();
            }

            for (int i = 0; i < numOfNodes; i++) {
                //linije opisuju brifove u grafu
                String[] edges = bufferedReader.readLine().split(" ");
                Node node = data[i];
                node.name = i;
                node.rank = 1 / numOfNodes;
                int[] neighbours = Arrays.stream(edges).mapToInt(Integer::valueOf).toArray();
                node.outGoingSize = neighbours.length;
                //reci susjedima da on ide u njih
                for (int neighbour : neighbours) {
                    data[neighbour].ingoing.add(node.name);
                }

                data[i] = node;
            }

            pageRank(bufferedReader);
            //calculateNew(bufferedReader);


        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void pageRank(BufferedReader bufferedReader) throws IOException {
        double[][] rank = new double[101][numOfNodes];
        for (int k = 0; k < numOfNodes; k++) {
            rank[0][k] = (double) 1 / numOfNodes;
        }

        for (int k = 0; k < 101 - 1; k++) {
            double S = 0;

            for (int i = 0; i < numOfNodes; i++) {
                for (Integer pointer : data[i].ingoing) {
                    rank[k + 1][i] += walkerPropability * (rank[k][pointer]) / data[pointer].outGoingSize;
                }
                S += rank[k + 1][i];
            }

            for (int j = 0; j < numOfNodes; j++) {
                rank[k + 1][j] += (1 - S) / numOfNodes;
            }
        }

        //broj upita na koje program treba odgovorit
        int numOfQuestions = Integer.parseInt(bufferedReader.readLine());
        for (int i = 0; i < numOfQuestions; i++) {
            //upit: prvi broj predstavlja index cvora, a drugi broj predstavlja redni broj iteracije algoritma NodeRank
            String[] questData = bufferedReader.readLine().split(" ");
            questData(rank[Integer.parseInt(questData[1])][Integer.parseInt(questData[0])]);
        }
    }

    private void questData(double data) {
        System.out.println(decimalFormat.format(data).replace(",", "."));
    }

    static class Node {
        private double rank;
        private int name;
        private int outGoingSize = 0;
        //lista onih koji izlaze
        //  List<Integer> outgoing = new ArrayList<>();
        //lista onih koji ulaze
        List<Integer> ingoing = new ArrayList<>();
    }
}
