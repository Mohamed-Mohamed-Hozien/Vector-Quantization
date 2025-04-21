import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class VectorQuantization {

    static final int BLOCK_SIZE = 2; // 2x2 block
    static final int CODEBOOK_SIZE = 16;
    static final int MAX_ITERATIONS = 10;

    public static void main(String[] args) throws IOException {
        BufferedImage image = ImageIO.read(new File("src/input_image.jpg"));
        int width = image.getWidth();
        int height = image.getHeight();

        List<double[]> vectors = extractVectors(image);
        System.out.println("Extracted " + vectors.size() + " vectors:");
        for (int i = 0; i < vectors.size(); i++) {
            System.out.println("Vector " + i + ": " + Arrays.toString(vectors.get(i)));
        }

        List<double[]> codebook = initializeCodebook(vectors);
        System.out.println("\nInitial Codebook:");
        printCodebook(codebook);

        for (int iteration = 1; iteration <= MAX_ITERATIONS; iteration++) {
            System.out.println("\n--- Iteration " + iteration + " ---");

            Map<double[], List<double[]>> clusters = assignToClusters(vectors, codebook);
            codebook = updateCodebook(clusters);

            System.out.println("Updated Codebook:");
            printCodebook(codebook);
        }

        visualizeCodebook(codebook, BLOCK_SIZE);
    }

    private static List<double[]> extractVectors(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        List<double[]> vectors = new ArrayList<>();

        for (int y = 0; y <= height - BLOCK_SIZE; y += BLOCK_SIZE) {
            for (int x = 0; x <= width - BLOCK_SIZE; x += BLOCK_SIZE) {
                double[] vector = new double[BLOCK_SIZE * BLOCK_SIZE];
                int k = 0;
                for (int j = 0; j < BLOCK_SIZE; j++) {
                    for (int i = 0; i < BLOCK_SIZE; i++) {
                        int rgb = image.getRGB(x + i, y + j);
                        int gray = (rgb >> 16) & 0xff; // assume grayscale
                        vector[k++] = gray;
                    }
                }
                vectors.add(vector);
            }
        }
        return vectors;
    }

    private static List<double[]> initializeCodebook(List<double[]> vectors) {
        Collections.shuffle(vectors, new Random());
        return new ArrayList<>(vectors.subList(0, CODEBOOK_SIZE));
    }

    private static Map<double[], List<double[]>> assignToClusters(List<double[]> vectors, List<double[]> codebook) {
        Map<double[], List<double[]>> clusters = new HashMap<>();
        for (double[] code : codebook) {
            clusters.put(code, new ArrayList<>());
        }

        for (double[] vector : vectors) {
            double[] bestCode = null;
            double minDist = Double.MAX_VALUE;

            for (double[] code : codebook) {
                double dist = euclideanDistance(code, vector);
                if (dist < minDist) {
                    minDist = dist;
                    bestCode = code;
                }
            }
            clusters.get(bestCode).add(vector);
        }
        return clusters;
    }

    private static List<double[]> updateCodebook(Map<double[], List<double[]>> clusters) {
        List<double[]> newCodebook = new ArrayList<>();
        for (Map.Entry<double[], List<double[]>> entry : clusters.entrySet()) {
            List<double[]> group = entry.getValue();
            double[] mean = new double[BLOCK_SIZE * BLOCK_SIZE];
            if (group.isEmpty()) {
                newCodebook.add(entry.getKey());
                continue;
            }

            for (double[] vector : group) {
                for (int i = 0; i < mean.length; i++) {
                    mean[i] += vector[i];
                }
            }

            for (int i = 0; i < mean.length; i++) {
                mean[i] /= group.size();
            }
            newCodebook.add(mean);
        }
        return newCodebook;
    }

    private static double euclideanDistance(double[] a, double[] b) {
        double sum = 0;
        for (int i = 0; i < a.length; i++) {
            sum += Math.pow(a[i] - b[i], 2);
        }
        return Math.sqrt(sum);
    }

    private static void printCodebook(List<double[]> codebook) {
        for (int i = 0; i < codebook.size(); i++) {
            System.out.println("Code " + i + ": " + Arrays.toString(codebook.get(i)));
        }
    }

    private static void visualizeCodebook(List<double[]> codebook, int blockSize) throws IOException {
        int gridSize = (int) Math.ceil(Math.sqrt(codebook.size()));
        int imgSize = gridSize * blockSize;

        BufferedImage codebookImg = new BufferedImage(imgSize, imgSize, BufferedImage.TYPE_BYTE_GRAY);

        for (int idx = 0; idx < codebook.size(); idx++) {
            int startX = (idx % gridSize) * blockSize;
            int startY = (idx / gridSize) * blockSize;
            double[] vector = codebook.get(idx);

            int k = 0;
            for (int j = 0; j < blockSize; j++) {
                for (int i = 0; i < blockSize; i++) {
                    int gray = (int) vector[k++];
                    int rgb = (gray << 16) | (gray << 8) | gray;
                    codebookImg.setRGB(startX + i, startY + j, rgb);
                }
            }
        }

        File output = new File("codebook_visualization.png");
        ImageIO.write(codebookImg, "png", output);
        System.out.println("\nCodebook visualized in codebook_visualization.png");
    }
}
