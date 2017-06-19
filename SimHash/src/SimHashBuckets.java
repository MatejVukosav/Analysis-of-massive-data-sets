import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Created by Vuki on 5.4.2017..
 */
@SuppressWarnings("Duplicates")
public class SimHashBuckets {

    public static void main(String[] args) {

        InputStream paths = SimHash.class.getResourceAsStream("sim_hash_buckets_input2");

        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in))) {
            //   try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(paths))) {

            int N = Integer.parseInt(bufferedReader.readLine());
            String[] hashes = new String[N];
            //text
            for (int i = 0; i < N; i++) {
                // Svaki tekst je zapisan u zasebnoj liniji te sadrzi male engleske znakove
                String line = bufferedReader.readLine();
                hashes[i] = Main.simHash(line);
                //System.out.println(hashes[i]);
            }

            Map<Integer, Set<Integer>> kandidati = lsh(hashes);

            int Q = Integer.parseInt(bufferedReader.readLine());
            //quests
            for (int i = 0; i < Q; i++) {
                String[] line = bufferedReader.readLine().split(" ");
                int I = Integer.parseInt(line[0]);
                int K = Integer.parseInt(line[1]);

                int numOfTexts = diffWithDistanceFromHash(hashes, kandidati, I, K);
                System.out.println(numOfTexts);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static Map<Integer, Set<Integer>> lsh(String[] hashes) {
        //Nakon sto se zavrsi algoritam LSH, u mapi kandidati za svaki ulazni tekst nalazi se skup
        //tekstova kandidata za slicnost (preciznije, radi se o rednim brojevima tih tekstova s obzirom na
        //poredak u ulaznoj datoteci).

        Map<Integer, Set<Integer>> kandidati = new HashMap<>();
        int bandwithMax = 8;
        for (int pojas = 1; pojas <= bandwithMax; pojas++) {

            Map<Integer, Set<Integer>> pretinci = new HashMap<>();

            for (int trenutniId = 0; trenutniId < hashes.length; trenutniId++) {
                String hash = hashes[trenutniId];
                // hash sadrzi 128 bita
                // Uzmi r = 16 bita u trenutnom pojasu
                // pocevsi od manje bitnih bitova
                // npr. za pojas = 1, uzimaju se bitovi od 0 do 15
                // za pojas=2, uzimaju se bitovi od 16 do 31, itd.
                // koristenjem fje hash2int pretvori tih 16 bita u integer

                int val = hash2int(pojas, hash);
                Set<Integer> tekstoviUPretincu = new HashSet<>();
                if (pretinci.get(val) != null) {
                    tekstoviUPretincu = pretinci.get(val);


                    for (Integer textId : tekstoviUPretincu) {
                        //ako postoji dodaj ga, inace mu stvori kljuc pa ga dodaj
                        kandidati.computeIfAbsent(trenutniId, k -> new HashSet<>());
                        kandidati.get(trenutniId).add(textId);

                        kandidati.computeIfAbsent(textId, k -> new HashSet<>());
                        kandidati.get(textId).add(trenutniId);
                    }
                } else {
                    tekstoviUPretincu = new HashSet<>();
                }
                tekstoviUPretincu.add(trenutniId);
                pretinci.put(val, tekstoviUPretincu);
            }
        }
        return kandidati;
    }

    private static String getBit(String hash, int from, int to) {
        return hash.substring(from, to);
    }

    private static int hash2int(int pojas, String hash) {
        return Integer.parseInt(getBit(hash, pojas * 16 - 16, pojas * 16), 2);
    }

    private static int diffWithDistanceFromHash(String[] hashes, Map<Integer, Set<Integer>> kandidati, int hashId, int distance) {
        int diff = 0;
        String currentHash = hashes[hashId];
        char[] currentHashChars = currentHash.toCharArray();
        Set<Integer> candidatesForI = kandidati.get(hashId);

        for (Integer id : candidatesForI) {
            char[] otherHashChars = hashes[id].toCharArray();
            int diffTemp = 0;
            for (int i = 0; i < otherHashChars.length; i++) {
                char c = otherHashChars[i];
                if (c != currentHashChars[i]) {
                    diffTemp++;
                }
                if (diffTemp > distance) {
                    break;
                }
            }
            if (diffTemp <= distance) {
                diff++;
            }
        }
        return diff;
    }

}
