import java.util.ArrayList;
import java.util.List;
import java.text.Collator;

/**
 * A sorted list implementation with binary search-based insertion, prefix matching,
 * and locale-aware comparison via Collator.
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
     * Uses binary search to find the pre-sorted insert position for a given word
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
     * Uses the pre-sorted nature of the list and the existing binary search method to quickly
     * check if a given item is already in the List. O(log n) complexity
     */
    public boolean contains(String word) {
        return binarySearch(word) >= 0;
    }

    /**
     * Simple binary search algorithm that attempts to find the given word in the SortedList.
     * Searches for exact matches, and will return -1 if no exact match is found.
     * This is so the closestMatch method can be then called as a fallback.
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
