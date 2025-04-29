import java.util.ArrayList;
import java.util.List;
import java.text.Collator;

/**
 * A list that automatically sorts itself into ascending lexographic order as entries are added.
 * Uses a collator instance to determine lexographic order, so it still works with minimal
 * input validation. For maximum international compatibility, ICU4J can be used instead
 * with minimal code change, but that is omitted from this version so a 3-5 MB dependency isn't needed.
 * Currently fed a Japanese Locale collator for testing since I have a Japanese IME installed.
 * Still functionally identical to English locale when used pure for English script sorting.
 */
public class SortedList {
    private List<String> list;
    private final Collator collator;

    public SortedList(Collator collator) {
        this.list = new ArrayList<>();
        this.collator = collator;
    }

    public void add(String item) {
        int index = findInsertPosition(item);
        list.add(index, item);
    }

    /**
     * Uses binary search to efficiently find the pre-sorted insert position for a given word
     * @param word String to be inserted and sorted
     * @return int index position where word will be sorted
     */
    public int findInsertPosition(String word) {
        int low = 0, high = list.size();

        while (low < high) {
            int mid = (low + high) / 2;
            if (collator.compare(list.get(mid), word) < 0) {
                low = mid + 1;
            } else {
                high = mid;
            }
        }

        return low;
    }

    /**
     * Simple binary search algorithm that attempts to find the given word in the SortedList.
     * Searches for exact matches, and will return -1 if no exact match is found.
     * This is so the closest match method can be then called as a fallback.
     * @param word search String to find in SortedList
     * @return index of exact match if found, otherwise -1
     */
    public int binarySearch(String word) {
        int low = 0;
        int high = list.size() - 1;

        while (low <= high) {
            int mid = (low + high) / 2;
            int cmp = collator.compare(list.get(mid), word);

            if (cmp < 0) {
                low = mid + 1;
            } else if (cmp > 0) {
                high = mid - 1;
            } else {
                // Potential match found
                // Confirm it is an exact match (collator ignores case etc.)
                if (list.get(mid).equals(word)) {
                    return mid; // Exact match confirmed
                } else {
                    break;
                    // Similar but not identical,
                    // treat as no match so closestMatch method can be called
                }
            }
        }

        return -1; // No exact match found
    }

    /**
     * Finds the closest match to the given query word by using a binary search with an Upper Bound bias.
     * This way, partial prefix matches will default to the higher bound, not the lower, which will
     * usually be much closer in auto-complete scenarios.
     * @param word String to find closest match in List
     * @return String that is the closest match, biased to upper bound
     */
    public String closestMatch(String word) {
        if (list.isEmpty()) return null;

        int low = 0;
        int high = list.size() - 1;
        int bestLower = -1; // closest smaller
        int bestUpper = -1; // closest larger

        while (low <= high) {
            int mid = (low + high) / 2;
            int cmp = collator.compare(list.get(mid), word);

            if (cmp < 0) {
                bestLower = mid;
                low = mid + 1;
            } else if (cmp > 0) {
                bestUpper = mid;
                high = mid - 1;
            } else {
                // Exact match
                return list.get(mid);
            }
        }

        // Post-processing: prefer bestUpper first (first word >= query)
        if (bestUpper != -1) {
            return list.get(bestUpper);
        }
        if (bestLower != -1) {
            return list.get(bestLower);
        }

        return null; // Should not happen unless list is empty
    }

    /**
     * Used for live autocomplete suggestions. Returns entries in the list that
     * match a given prefix.
     * @param prefix A partially typed word, that will be compared to words in SortedList
     * @return list of entries that match prefix
     */
    public List<String> prefixMatches(String prefix) {
        List<String> matches = new ArrayList<>();
        if (prefix == null || prefix.isEmpty()) return matches;

        for (String word : list) {
            if (word.startsWith(prefix)) {
                matches.add(word);
            }
        }

        // Sort matches: shorter words first, then alphabetically
        matches.sort((a, b) -> {
            if (a.length() != b.length()) {
                return Integer.compare(a.length(), b.length());
            } else {
                return collator.compare(a, b);
            }
        });

        return matches;
    }

    /**
     * Uses the pre-sorted nature of the list and the existing binary search method to quickly
     * check if a given item is already in the List. Much faster than standard
     * ArrayList.contains(), with O(log n) complexity vs O(n)
     * @param word
     * @return
     */
    public boolean contains(String word) {
        return binarySearch(word) >= 0;
    }

    public String get(int index) {
        return list.get(index);
    }

    public List<String> getList() {
        return new ArrayList<>(list);
    }

    public int size() {
        return list.size();
    }

    public void clear() {
        list.clear();
    }

    public Collator getCollator() {
        return this.collator;
    }

    @Override
    public String toString() {
        return list.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        SortedList other = (SortedList) obj;
        return this.list.equals(other.list); //use List interface .equals to compare elements
    }

    @Override
    public int hashCode() {
        return list.hashCode();
    }
}
