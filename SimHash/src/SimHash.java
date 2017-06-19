import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by Vuki on 4.4.2017..
 */
public class SimHash {

    private static String[] hashes;

    public static void main(String[] args) {

        InputStream paths = SimHash.class.getResourceAsStream("sim_hash_input");

        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in))) {
            //try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(paths))) {

            int N = Integer.parseInt(bufferedReader.readLine());
            hashes = new String[N];
            //text
            for (int i = 0; i < N; i++) {
                // Svaki tekst je zapisan u zasebnoj liniji te sadrzi male engleske znakove
                String line = bufferedReader.readLine();
                hashes[i] = Main.simHash(line);
                //System.out.println(hashes[i]);

            }

            int Q = Integer.parseInt(bufferedReader.readLine());
            //quests
            for (int i = 0; i < Q; i++) {
                //Q linija predstavlja upite za izracun slicnosti.
                //Jedan upit sadrzi dva cijela broja odvojena prazninom I, K; 0 <= I <= N-1 i 0 <= K <= 31
                String[] line = bufferedReader.readLine().split(" ");
                int I = Integer.parseInt(line[0]);
                int K = Integer.parseInt(line[1]);

                /** Za svaki upit program mora generirati cijeli broj koji oznacava ukupan broj tekstova ciji se sazeci
                 razlikuju do na K bitova (ukljucujuci K) od sazetka I-tog teksta (po Hammingovoj udaljenosti).
                 Npr. ako je I=16 i K=3 onda izlaz za navedeni upit sadrzi broj tekstova ciji se sazeci razlikuju od
                 sazetka 17-tog (pretpostavlja se 0-based indexing dokumenata) teksta do ukljucivo 3 bita.
                 **/
                int numOfTexts = diffWithDistanceFromHash(I, K);
                System.out.println(numOfTexts);
            }


        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //i je od kojeg teksta
    //k je kolika razlika
    private static int diffWithDistanceFromHash(int hashPosition, int distance) {
        int numOfTextsInKDistance = 0;
        String hash = hashes[hashPosition];

        for (String other : hashes) {
            if (isDiffBetweenHashesInsideDistance(hash, other, distance)) {
                numOfTextsInKDistance++;
            }
        }
        //Minus one because need to remove itself from sum
        return numOfTextsInKDistance - 1;
    }

    private static boolean isDiffBetweenHashesInsideDistance(String sourceHash, String targetHash, int distance) {
        int diff = 0;
        char[] chars = sourceHash.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = targetHash.charAt(i);
            if (c != chars[i]) {
                diff++;
            }
            if (diff > distance) {
                break;
            }
        }
        return diff <= distance;
    }

}
