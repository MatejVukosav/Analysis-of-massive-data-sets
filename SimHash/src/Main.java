import org.apache.commons.codec.digest.DigestUtils;

/**
 * U prvoj laboratorijskoj vjezbi zadatak je ostvariti sazimanje tekstova koristenjem algoritma
 * Simhash. Za razliku od kriptografskih algoritama sazimanja (koji su izuzetno osjetljivi na
 * minimalne promjene ulaznog teksta), algoritam Simhash cuva slicnost ulaznih tekstova; ako su
 * ulazni tekstovi vrlo slicni (npr. nekoliko razicitih rijeci) onda se i sazeci generirani algoritmom
 * Simhash razlikuju u malom broju bitova (po Hammingovoj udaljenosti). Generirani Simhash
 * sazeci ce se koristiti za identifikaciju slicnih tekstova
 * U prvom zadatku(zadatak A) identifikaciju slicnih tekstova treba provesti slijednim pretrazivanjem sazetaka
 * svih tekstova
 */
public class Main {
    private static String testString = "fakultet elektrotehnike i racunarstva";

    public static void main(String[] args) {
        System.out.println(simHash(testString));
    }

    /*
      Ulaz u algoritam simHash je niz znakova (tekst, dokument), a izlaz je niz znakova koji
    predstavlja heksadecimalni zapis 128-bitnog Simhash sazetka.
    Algoritam Simhash interno koristi jedan od tradicionalnih algoritama sazimanja. U ovoj
   laboratorijskoj vjezbi ce se koristiti 128-bitni algoritam kriptografskog sazimanja md5.
    */
    public static String simHash(String text) {
        return simHash(text, 128);
    }


    public static String simHash(String text, int hashSize) {

        String[] words = text.split(" ");
        int[] sh = new int[hashSize];

        for (String word : words) {
            byte[] hash = DigestUtils.md5(word);

            for (int j = 0; j < hash.length; j++) {
                //za svaki bajt hasha prodi po svakom bitu
                byte bajt = hash[j];

                for (int i = 0; i < 8; i++) {
                    int positionInSh = i + j * 8;

                    if (getBit(bajt, i) == 1) {
                        sh[positionInSh] += 1;
                    } else {
                        sh[positionInSh] -= 1;
                    }
                }
            }
        }

        for (int i = 0; i < sh.length; i++) {
            if (sh[i] >= 0) {
                sh[i] = 1;
            } else {
                sh[i] = 0;
            }
        }
        return concatenateDigits(sh);
    }

    private static String concatenateDigits(int[] digits) {
        StringBuilder sb = new StringBuilder(digits.length);
        for (int digit : digits) {
            sb.append(digit);
        }
        return sb.toString();
    }

    private static int getBit(byte bajt, int position) {
        //return (bajt >> position) & 1;
        return bajt >> (8 - (position + 1)) & 0x0001;
    }


}
