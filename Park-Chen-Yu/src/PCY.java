import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * U drugoj laboratorijskoj vjezbi zadatak je programski ostvariti algoritam za pronalazenje cestih
 * skupova predmeta PCY (Park Chen Yu).
 * U sklopu ovog problema, razmatramo skup podataka
 * koji se sastoji od odjeljaka (kosara).
 * Svaka kosara sastoji se od predmeta koji se mogu pojaviti
 * u vise kosara, ali se u pojedinoj kosari pojavljuju najvise jednom.
 * Cilj algoritma je pronaci podskupove predmeta koji se pojavljuju u najvecem broju kosara.
 */
public class PCY {
    //broj kosara
    private static int brKosara;
    //s ima vrijednos 0-1
    private static float s;
    //broj pretinaca
    private static int brPretinaca;
    //podaci o pragu
    private static int prag;
    //brojac predmeta
    private static int[] brPredmeta;
    private static int brojPredmeta = 100;

    private static Kosara[] kosare;


    public static void main(String[] args) {

        InputStream path = PCY.class.getResourceAsStream("input1.txt");
        try {
            //try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(path))) {
                try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in))) {

                brKosara = Integer.parseInt(bufferedReader.readLine());
                kosare = new Kosara[brKosara];
                s = Float.parseFloat(bufferedReader.readLine());
                brPretinaca = Integer.parseInt(bufferedReader.readLine());
                prag = (int) Math.floor(s * brKosara);

                brPredmeta = new int[brojPredmeta + 1];


                float start = System.nanoTime();

                // region prvi prolaz - prebroji ukupno pojavljivanje pojedinog elemenata
                for (int i = 0; i < brKosara; i++) {
                    Kosara k = new Kosara();

                    k.predmeti = Arrays
                            .stream(bufferedReader.readLine().split(" "))
                            .mapToInt(Integer::parseInt)
                            .toArray();
                    kosare[i] = k;

                    for (int predmet : k.predmeti) {
                        brPredmeta[predmet]++;
                    }
                }


                for (int i = 0; i < brPredmeta.length; i++) {
                    //   System.out.println(i + " " + brPredmeta[i]);
                }


                //endregion
                int dsad = 0;

                printTime("Prvi pass", start);
                start = System.nanoTime();

                //pretinci za funkciju sazimanja - polje velicine brPretinaca
                int[] pretinci = new int[brPretinaca];

                //drugi prolaz - sazimanje
                for (int i = 0; i < brKosara; i++) {
                    Kosara kosara = kosare[i];
                    int length = kosara.predmeti.length;
                    if (length >= 2) {

                        //za svaki par predmeta {i,j}
                        for (int j = 0; j < length; j++) {
                            for (int z = j + 1; z < length; z++) {
                                int predmetI = kosara.predmeti[j];
                                int predmetJ = kosara.predmeti[z];

                                int brPredmetaI = brPredmeta[predmetI];
                                int brPredmetaJ = brPredmeta[predmetJ];

                                if (brPredmetaI >= prag && brPredmetaJ >= prag) {
                                    //ako je predmet cest
                                    int hash = getHash(predmetI, predmetJ);
                                    //  System.err.println(hash);
                                    pretinci[hash]++;
                                }
                            }
                        }
                    }
                }

                printTime("Drugi pass", start);
                start = System.nanoTime();


                //treci prolaz - brojanje parova
                List<Par> parovi = new ArrayList<>();

                for (int i = 0; i < brKosara; i++) {
                    Kosara k = kosare[i];
                    int length = k.predmeti.length;
                    if (length >= 2) {

                        Par newP = new Par(-1, -1);
                        for (int j = 0; j < length; j++) {
                            for (int z = j + 1; z < length; z++) {
                                int predmetI = k.predmeti[j];
                                int predmetJ = k.predmeti[z];

                                int brPredmetaI = brPredmeta[predmetI];
                                int brPredmetaJ = brPredmeta[predmetJ];

                                //cesti parovi
                                if (brPredmetaI >= prag && brPredmetaJ >= prag) {

                                    int hash = getHash(predmetI, predmetJ);
                                    if (pretinci[hash] >= prag) {
                                        newP.i = predmetI;
                                        newP.j = predmetJ;

                                        int index = parovi.indexOf(newP);
                                        if (index == -1) {
                                            parovi.add(new Par(predmetI, predmetJ));
                                        } else {
                                            parovi.get(index).zbroj++;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                printTime("Treci pass", start);
                printOutput(parovi);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void printTime(String pass, float start) {
        // System.out.println(String.format(pass + " %.2f s", getTime(start, System.nanoTime())));
    }

    private static float getTime(float start, float end) {
        return (end - start) / 1000000000;
    }


    /**
     * A je ukupan broj kandidata cestih parova koje bi brojao algoritam A-priori.
     * Ako je algoritam u prvom prolazu odredio da je m predmeta cesto,
     * onda taj broj iznosi: m * (m-1 )/2
     * P je ukupan broj parova koje prebrojava algoritam PCY.
     * Radi se samo o parovima koji se sazimaju u cesti pretinac.
     * X1
     * X2
     * ..
     * Xn su silazno sortirani brojevi ponavljanja cestih parova.
     * Navodi se samo ukupan broj ponavljanja za svaki par.
     */
    private static void printOutput(List<Par> parovi) {

        List<Par> cestiParovi = parovi.stream().filter(x -> x.zbroj >= prag).collect(Collectors.toList());
        //broj cestih predmeta
        int m = (int) Arrays.stream(brPredmeta).filter(p -> p >= prag).count();
        int A = (m * (m - 1)) / 2;
        System.out.println("" + A);
        int size = parovi.size();
        System.out.println("" + size);

        cestiParovi.sort((o1, o2) -> {
            if (o1.zbroj == o2.zbroj) {
                return 0;
            }

            return o1.zbroj < o2.zbroj ? 1 : -1;
        });

        for (Par par : cestiParovi) {
            System.out.println(String.valueOf(par.zbroj));
        }
    }

    //par predmeta i, j
    private static int getHash(int i, int j) {
        return ((i * brojPredmeta) + j) % brPretinaca;
    }

    static class Kosara {
        int[] predmeti;
    }

    static class Par {
        int i;
        int j;
        int zbroj;

        public Par(int i, int j) {
            this.i = i;
            this.j = j;
            this.zbroj = 1;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Par) {
                Par p = (Par) obj;
                return i == p.i && j == p.j;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return i * 17 + j * 31 + i;
        }
    }

}
