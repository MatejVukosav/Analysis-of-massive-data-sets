import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Potrebno je za svaki cvor u neusmjerenom grafu izracunati odredeno svojstvo
 * Created by Vuki on 24.5.2017..
 */
public class ClosestBlackNode {


    boolean[] visited;
    Node[] nodes;
    int numOfNodes;
    int numOfEdges;

    public static void main(String[] args) {
        long start = System.nanoTime();
        ClosestBlackNode closestBlackNode = new ClosestBlackNode();
        closestBlackNode.start();

        long end = System.nanoTime();
        //System.out.println("Time: " + (end - start) / 1000000000f);
    }

    /**
     * Zadan je neusmjereni graf G koji se sastoji od crnih i bijelih cvorova.
     * Potrebno je za svaki cvor u grafu izracunati udaljenost do najblizeg crnog cvora te specificirati o kojem crnom
     * cvoru se radi ( ako za neki cvor postoji vise crnih cvorova koji su mu najblizi, tada je potrebno ispisati crni
     * cvor koji ima najmanji index )
     * Za svaki crni cvor udaljenost je po definiciji 0.
     * Moguce da u ulaznom grafu postoje bijeli cvorovi iz kojih nije moguce doci do crnog cvora, sto program treba
     * ispisati u skladu s opisom koji je dan u specifikaciji izlazne datoteke.
     * Za svaki cvor iz kojeg je moguce doci do nekog crnog cvora vrijedi da maksimalna udaljenost od tog cvora do nekog
     * crnog cvora iznosi 10.
     * Drugim rijecima, ili je najblizi cvor udaljen za dist<=10 ili je crni cvor nedostupan
     */
    public void start() {
        InputStream path = ClosestBlackNode.class.getResourceAsStream("closest_black_node_input_s");
        //try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(path))) {
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in))) {

            String[] conf = bufferedReader.readLine().split(" ");
            // 1 - 10 na 5
            numOfNodes = Integer.parseInt(conf[0]);
            // e < 2.5 * n
            numOfEdges = Integer.parseInt(conf[1]);
            nodes = new Node[numOfNodes];
            visited = new boolean[numOfNodes];

            List<Node> blackNodes = new ArrayList<>();

            for (int i = 0; i < numOfNodes; i++) {
                //linije opisuju tipove cvorova u grafu gdje ako pise 1 cvor je crn, inace bijel
                String type = bufferedReader.readLine();
                nodes[i] = new Node();
                nodes[i].value = i;
                if ("1".equals(type)) {
                    nodes[i].isBlack = true;
                    nodes[i].distanceToBlack = 0;
                    nodes[i].closestBlackNode = nodes[i];
                    blackNodes.add(nodes[i]);
                } else {
                    nodes[i].isBlack = false;
                    nodes[i].distanceToBlack = Integer.MAX_VALUE;
                }
            }

            for (int i = 0; i < numOfEdges; i++) {
                //linije opisuju bridove neusmjerenog grafa
                //indeksi cvorova izmedu kojih postoji brid
                String[] edges = bufferedReader.readLine().split(" ");
                int node1 = Integer.parseInt(edges[0]);
                int node2 = Integer.parseInt(edges[1]);

                nodes[node1].neighbours.add(nodes[node2]);
                nodes[node2].neighbours.add(nodes[node1]);
            }

            calculateClosestBlackNodes(blackNodes);
            print();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void calculateClosestBlackNodes(List<Node> nodes) {
        //kreni od crnih i siri se kuda god mozes
        Node blackNode;

        while (!nodes.isEmpty()) {
            blackNode = nodes.get(0);
            blackNode.visited = true;
            for (Node neighbour : blackNode.neighbours) {
                //za svakog susjeda pogledaj udaljenost
                if (!neighbour.isBlack && !neighbour.visited) {
                    //ako nije crn i ako vec nije obraden
                    nodes.add(neighbour);
                    neighbour.visited = true;
                    if (blackNode.distanceToBlack + 1 < neighbour.distanceToBlack) {
                        neighbour.distanceToBlack = blackNode.distanceToBlack + 1;
                        neighbour.closestBlackNode = blackNode.closestBlackNode;
                    }
                }
            }
            nodes.remove(blackNode);
        }
    }

    //program treba za svaki cvor u grafu ispisati indeks najblizeg crnog cvora i udaljenost do istog cvora
    private void print() {
        for (Node node : nodes) {
            if (node.distanceToBlack == Integer.MAX_VALUE) {
                System.out.println("-1 -1");
            } else {
                System.out.println(node.closestBlackNode.value + " " + node.distanceToBlack);
            }
        }
    }

    static class Node {
        int value;
        boolean isBlack;
        boolean visited;
        int distanceToBlack = 0;
        Node closestBlackNode;
        List<Node> neighbours = new ArrayList<>();
    }
}