import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Arrays;
import java.util.Locale;
import java.util.PrimitiveIterator;

/**
 * Collaborative Filtering task
 * Created by Vuki on 7.6.2017..
 */
@SuppressWarnings("Duplicates")
public class CF {

    //item /user
    private float userItemMatrix[][];
    //user/item
    private float userItemMatrixTransponed[][];
    private int numOfItems;
    private int numOfUsers;
    private float[] usersRatingAverage;
    private float[] itemsRatingAverage;

    public static void main(String[] args) {
        CF cf = new CF();
        cf.readInput();
    }

    private void readInput() {
        InputStream inputStream = CF.class.getResourceAsStream("zi2016");
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream))) {
            //try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in))) {
            //prva linija sadrzi broj stavki i broj korisnika
            String[] conf = bufferedReader.readLine().split(" ");
            numOfItems = Integer.parseInt(conf[0]); //N
            numOfUsers = Integer.parseInt(conf[1]); //M

            userItemMatrix = new float[numOfItems][numOfUsers];
            userItemMatrixTransponed = new float[numOfUsers][numOfItems];

            usersRatingAverage = new float[numOfUsers];
            itemsRatingAverage = new float[numOfItems];

            /*Zatim slijedi zapis user-item matrice u kojoj su vrijednosti koje nedostaju prikazane znakom X.
              Zapis matrice cini N linija od kojih svaka linija sadrzi M vrijednosti odijeljenih praznim znakom.
              Vrijednost u matrici mogu biti cijeli brojevi u rasponu od 1 do 5.
              Ukoliko vrijednost matrice ne postoji, tada su elementi oznaceni s X.
             */

            parseInput(bufferedReader);
            calculateAverages();

            //konstanta Q predstavlja broj upita 1<=Q<=100
            int Q = Integer.parseInt(bufferedReader.readLine());
            /* Slijedi Q linija upita: 4 broja, I J T K :
              I , J = su koordinate elementa matrice oznacenog znakom X - element za koji je potrebno
              izracunati vrijedost preporuke
              T = tip algoritma koji je potrebno koristiti
                   Ako je T=0 potrebno je koristiti item-item pristup suradnickog filtriranja,
                   a za vrijednost T=1 user-user pristup suradnickog filtriranja.
              K = predstavlja maksimalni kardinalni broj skupa slicnih stavki/korisnika koje sustav preporuke razmatra
                   prilikom racunanja vrijednosti preporuka.
             */
            for (int i = 0; i < Q; i++) {
                PrimitiveIterator.OfInt iterator = Arrays.stream(bufferedReader.readLine().split(" ")).mapToInt(Integer::parseInt).iterator();
                int I = iterator.next();
                int J = iterator.next();
                int T = iterator.next();
                int K = iterator.next();
                //za svaki upit program treba ispisati vrijednost preporuke u zasebnoj liniji
                switch (T) {
                    case AlgorithmType.ITEM_ITEM:
                        userItemAlgorithm(I - 1, J - 1, K, userItemMatrix.clone(), itemsRatingAverage);
                        break;
                    case AlgorithmType.USER_USER:
                        //matrica je transponirana
                        userItemAlgorithm(J - 1, I - 1, K, userItemMatrixTransponed.clone(), usersRatingAverage);
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown algorithm code");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void parseInput(BufferedReader bufferedReader) throws IOException {
        for (int i = 0; i < numOfItems; i++) {
            String line = bufferedReader.readLine();
            String[] ratings = line.split(" ");
            for (int j = 0; j < numOfUsers; j++) {
                String rating = ratings[j];
                if ("X".equals(rating)) {
                    userItemMatrix[i][j] = Rating.NO_RATING;
                    userItemMatrixTransponed[j][i] = Rating.NO_RATING;
                } else {
                    userItemMatrix[i][j] = Float.parseFloat(rating);
                    userItemMatrixTransponed[j][i] = Float.parseFloat(rating);
                }
            }
        }
    }

    private void calculateAverages() {
        usersRatingAverage = calculateAverage(numOfUsers, numOfItems, userItemMatrixTransponed);
        itemsRatingAverage = calculateAverage(numOfItems, numOfUsers, userItemMatrix);
    }

    private float[] calculateAverage(int items, int users, float[][] data) {
        float[] result = new float[items];
        for (int i = 0; i < items; i++) {
            float sumOfItemRatings = 0;
            int ratingsCount = 0;
            for (int j = 0; j < users; j++) {
                float ratingInIthRow = data[i][j];

                if (ratingInIthRow != Rating.NO_RATING) {
                    sumOfItemRatings += ratingInIthRow;
                    ratingsCount++;
                }
            }
            result[i] = sumOfItemRatings / ratingsCount;
        }
        return result;
    }

    /**
     * PCC (Pearson Correlation Coefficient) = potrebno je od pojedinih ocjena oduzeti prosjek predmeta (item-item)
     * odnosno oduzeti prosjek korisnika (user-user) te nad normaliziranim ocjenama izracunati cosine mjeru slicnosti.
     *
     * @param item    koordinata predmeta
     * @param user    koordinata korisnika
     * @param k       maksimalni kardinalni broj skupa koje sustav preporuke razmatra prilikom racunanja vrijednosti
     *                item, user matrix
     * @param data    matrica s kojom racunam
     * @param average vektor prosjeka elemenata po retcima
     */
    private void userItemAlgorithm(int item, int user, int k, float[][] data, float[] average) {
        int items = data.length;
        int users = data[0].length;

        if (data.length == 0) {
            return;
        }

        SimilarityItem[] similarities = new SimilarityItem[items];
        float[][] matrix = makeCopy(data);

        normalize(items, users, matrix, average);

        //checkPrint(items,matrix);

        for (int i = 0; i < items; i++) {

            float up = 0;
            float sumOfOwnerRatings = 0;
            float sumOfOtherRatings = 0;
            float result;

            if (i != item) {
                //gore je suma umnozaka elementa od x i y po pozicijama
                for (int j = 0; j < users; j++) {
                    float other = matrix[i][j];
                    float owner = matrix[item][j];
                    up += owner * other;

                    sumOfOwnerRatings += Math.pow(owner, 2);
                    sumOfOtherRatings += Math.pow(other, 2);
                }
                result = (float) (up / Math.sqrt(sumOfOtherRatings * sumOfOwnerRatings));
            } else {
                //item je sam sebi slican
                result = 1;
            }
            similarities[i] = new SimilarityItem(i, result);
        }

        checkPrint(items, matrix, similarities);

        Arrays.sort(similarities);

        int taken = 0;
        float resultSimilarities = 0;
        float gradeMultipleSimilarities = 0;
        for (int i = similarities.length - 1; i >= 0; i--) {
            //dohvati k elemenata s najvecom vrijednoscu slicnosti
            if (taken == k) {
                break;
            }
            float similarity = similarities[i].value;

            if (similarity > 0) {
                //ako je ocjena veca od nule i ako nisam na promatranom predmetu
                int position = similarities[i].position;
                //pozicija elementa, tako da mogu iz originalne tablice za usera uzet ocjenu tog predmeta
                int grade = (int) data[position][user];
                if (grade > 0 && position != item) {
                    taken++;
                    resultSimilarities += similarity;
                    //ako ocjena postoji onda je uzmi u obzir
                    gradeMultipleSimilarities += grade * similarity;
                }
            }
        }
        float recommendation = gradeMultipleSimilarities / resultSimilarities;
        printResult(recommendation);
    }

    private void printResult(float result) {
        Locale.setDefault(new Locale("en", "US"));
        DecimalFormat df = new DecimalFormat("#.000", DecimalFormatSymbols.getInstance());
        BigDecimal bd = new BigDecimal(result);
        BigDecimal res = bd.setScale(3, RoundingMode.HALF_UP);
        System.out.println(df.format(res));
    }

    private void normalize(int items, int users, float[][] matrix, float[] average) {
        //Normalizacija ocjena predmeta oduzimanjem prosjeka predmeta od svake ocjene
        for (int i = 0; i < items; i++) {
            for (int j = 0; j < users; j++) {
                float rating = matrix[i][j];
                if (rating != Rating.NO_RATING) {
                    //za svakog korisnika x dohvati ocjenu predmeta j
                    matrix[i][j] = rating - average[i];
                }
            }
        }
    }

    private float[][] makeCopy(float[][] data) {
        float[][] matrix = new float[data.length][data[0].length];
        for (int i = 0; i < matrix.length; i++) {
            matrix[i] = data[i].clone();
        }
        return matrix;
    }

    @SuppressWarnings("unused")
    private void checkPrint(int items, float[][] matrix, SimilarityItem[] similarities) {
        System.out.println("CHECK PRINT");
        System.out.println("Normalized matrix");
        for (int i = 0; i < items; i++) {
            System.out.println(Arrays.toString(matrix[i]));
            //System.out.println();
        }
        System.out.println("Item rating average");
        System.out.println(Arrays.toString(itemsRatingAverage));
        System.out.println("User rating average");
        System.out.println(Arrays.toString(usersRatingAverage));
        System.out.println();
        System.out.println("Similarities");
        System.out.println(Arrays.toString(similarities));
        System.out.println();
    }

    @Retention(RetentionPolicy.SOURCE)
    private @interface AlgorithmType {
        int ITEM_ITEM = 0;
        int USER_USER = 1;
    }

    @SuppressWarnings("unused")
    @Retention(RetentionPolicy.SOURCE)
    private @interface Rating {
        int NO_RATING = 0;
        int ONE = 1;
        int TWO = 2;
        int TREE = 3;
        int FOUR = 4;
        int FIVE = 5;
    }

    static class SimilarityItem implements Comparable<SimilarityItem> {
        int position;
        float value;

        SimilarityItem(int position, float value) {
            this.position = position;
            this.value = value;
        }

        @Override
        public int compareTo(SimilarityItem o) {
            if (this.value > o.value) {
                return 1;
            } else if (this.value < o.value) {
                return -1;
            } else {
                return 0;
            }
        }

        @Override
        public String toString() {
            return "" + value;
        }
    }

}
