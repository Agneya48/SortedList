import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * A utility class for generating a random list of words from a file.
 * Uses file streaming with reservoir sampling to ensure low memory overhead.
 */
public class WordSampler {
    private final String resourceFilePath;
    private final Random random = new Random();

    public WordSampler(String resourceFilePath) {
        this.resourceFilePath = resourceFilePath;
    }

    /**
     * Returns a list of random words from the previously set word list file.
     * Uses reservoir sampling to ensure uniform selection with minimal memory usage.
     *
     * @param count number of words to select
     * @return list of random words
     * @throws IOException if the file cannot be read
     */
    public List<String> getRandomWords(int count) throws Exception {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourceFilePath);
        if (inputStream == null) {
            throw new IllegalArgumentException("Resource not found: " + resourceFilePath);
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            List<String> reservoir = new ArrayList<>(count);
            int index = 0;
            String line;

            while ((line = reader.readLine()) != null) {
                //clean up each line of surrounding whitespace and set to lowercase. Every line should be one word
                line = line.trim().toLowerCase();

                if(line.isEmpty()) continue; //skip blank lines, so they don't affect the reservoir logic

                if (index < count) {
                    reservoir.add(line); // first, add the requested count of words to the reservoir
                } else {
                    int replaceindex = random.nextInt(index + 1);
                    if (replaceindex < count) {
                        reservoir.set(replaceindex, line);
                        //replace a random element
                        //every item in the stream has an equal probability of being
                        //included in the final resevoir.
                    }
                }
                index++;
            }

            if (reservoir.isEmpty()) {
                throw new IllegalArgumentException("Word list is empty or invalid.");
            }

            return reservoir;
        }
    }
}
