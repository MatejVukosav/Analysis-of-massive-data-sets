import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.StringTokenizer;


/**
 * Ostvariti algoritam procjene broja jedinica u zadanom toku bitova.
 **/
public class DGIM {

    //zadani prozor maksimalne velicine od N bitova, nece biti veci od 10 na 6.
    private int N;
    private int currentTimeStamp = 0;

    /**
     * A linear collection that supports element insertion and removal at both ends.
     * The name 'deque' is short for 'double ended queue' and is usually pronounced 'deck'
     */
    private Deque<Bucket> bucketDeque;
    //num of max same buckets size
    private int bucketsNum;

    public DGIM(int bucketsNum, int N) {
        this.bucketDeque = new LinkedList<>();
        this.bucketsNum = bucketsNum;
        this.N = N;
    }

    public static void main(String[] args) {


        InputStream path = DGIM.class.getResourceAsStream("input2");
        // try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(path))) {
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in))) {


            //velicina samog ulaznog toka nula i jedinica moze biti manja ili veca od N
            int N = Integer.parseInt(bufferedReader.readLine());
            DGIM dgim = new DGIM(2, N);

            //bitovi ili upit
            //upit oblika q k gdje je k velicina prozora za upit ( 1 <= k <= N )
            String line;
            StringTokenizer stringTokenizer;
            while ((line = bufferedReader.readLine()) != null) {
                stringTokenizer = new StringTokenizer(line, " ");

                if (stringTokenizer.countTokens() == 2) {
                    //first is char q
                    stringTokenizer.nextToken();
                    //first one is char q
                    String k = stringTokenizer.nextToken();
                    System.out.println(String.valueOf(dgim.query(Integer.parseInt(k))));

                } else {
                    String[] stream = line.split("");
                    //stream
                    int length = stream.length;
                    for (int i = 0; i < length; i++) {
                        String bit = stream[i];
                        dgim.algorithm(Integer.parseInt(bit));
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void dropLastBucket() {
        bucketDeque.removeLast();
    }

    //algoritam procjenjuje broj jedinica u zadnjih k elemenata toka
    // na nacin da u memoriji ne sprema niti jedan jedini bit ulaznog toka.
    private void algorithm(@DataType int bit) {
        /**
         * 1. pronaci najstariji pretinac z cija vremenska oznaka jos uvijek pripada prozoru od k
         * 2. sumirati velicine svih pretinaca s recentnijim vremenskim oznakama od one pretinca z
         * 3. dodati sumi iz 2. pola velicine pretinca z (zaokruzeno na manji broj)
         */

        currentTimeStamp++;
        if (DataType.zero == bit) {
            return;
        }
        add(currentTimeStamp);
    }

    private void add(int timeStamp) {
        Bucket newBucket = new Bucket(timeStamp, 1);
        bucketDeque.addFirst(newBucket);

        if (bucketDeque.getLast().creationTime < currentTimeStamp - N) {
            dropLastBucket();
        }

        Iterator<Bucket> bucketIterator = bucketDeque.iterator();
        if (checkIfNeedMerge(bucketIterator)) {
            Iterator<Bucket> iter = bucketDeque.iterator();
            merge(iter);
        }
    }

    private void merge(Iterator<Bucket> bucketIterator) {
        bucketIterator.next();
        Bucket next = bucketIterator.next();
        int nextSize = next.oneSize;

        Bucket afterNext = bucketIterator.next();
        next.oneSize += afterNext.oneSize;

        bucketIterator.remove();

        Iterator<Bucket> iter = getIterator(nextSize);
        if (checkIfNeedMerge(iter)) {
            Iterator<Bucket> iter2 = getIterator(nextSize);
            merge(iter2);
        }
    }

    private Iterator<Bucket> getIterator(int firstValue) {
        Iterator<Bucket> iter = bucketDeque.iterator();
        while (iter.hasNext()) {
            Bucket bucket = iter.next();
            if (bucket.oneSize == firstValue)
                return iter;
        }
        return null;
    }

    private boolean checkIfNeedMerge(Iterator<Bucket> iter) {
        Bucket current;
        Bucket next;
        Bucket afterNext;


        if (iter.hasNext()) {
            current = iter.next();
            if (iter.hasNext()) {
                next = iter.next();

                if (next.oneSize != current.oneSize) {
                    return false;
                } else {
                    if (iter.hasNext()) {
                        afterNext = iter.next();
                        return next.oneSize == afterNext.oneSize;
                    } else {
                        return false;
                    }
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private int query(int k) {
        final int[] size = {0};
        final int[] lastBucketSize = {0};
        final int[] sum = {0};
        final int[] elements = {0};

        for (Bucket bucket : bucketDeque) {
            if (bucket.creationTime <= currentTimeStamp - k) {
                break;
            }
            elements[0]++;
            sum[0] += bucket.oneSize;
            size[0] += bucket.oneSize;
            lastBucketSize[0] = bucket.oneSize;
        }


        return size[0] - (int) Math.ceil((float) lastBucketSize[0] / 2);
    }

    /**
     * The right side of the bucket should always start with 1
     * Every bucket should have at least one 1, else no bucket can be formed
     * All bucket sizes should be a power of 2
     * The buckets cannot decrease in oneSize as we move to the left
     * When a new bit comes in, drop the last bucket if its end-time is prior to N time units before the current time.
     * if ( current time - N > end time )
     * <p>
     * If 1001011 bucket oneSize is 4
     *
     * @param
     */
    static class Bucket {
        int creationTime;
        int oneSize;

        public Bucket(int creationTime, int oneSize) {
            this.creationTime = creationTime;
            this.oneSize = oneSize;
        }

        @Override
        public String toString() {
            return "Bucket size: " + oneSize;
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    @interface DataType {
        int zero = 0;
        int one = 1;
    }

    @Retention(RetentionPolicy.SOURCE)
    @interface QueryType {
        char query = 'q';
    }

}
