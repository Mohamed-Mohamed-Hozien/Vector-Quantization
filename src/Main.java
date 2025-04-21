import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        try {
            int blockSize = 32;
            int k = 4;

            BufferedImage rgbImage = ImageIO.read(new File("src/input_image.jpg"));
            System.out.println("Input Image Loaded: " + rgbImage.getWidth() + "x" + rgbImage.getHeight());

            BufferedImage grayImage = new BufferedImage(rgbImage.getWidth(), rgbImage.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
            Graphics g = grayImage.getGraphics();
            g.drawImage(rgbImage, 0, 0, null);
            g.dispose();
            System.out.println("Converted to Grayscale.");

            List<int[][]> blocks = divideIntoBlocks(grayImage, blockSize);
            System.out.println("Divided into Blocks: " + blocks.size());

            System.out.println("Generating Codebook...");
            List<int[][]> codeBook_K_means = generateCodebook(blocks, k);
            System.out.println("Codebook Generated. Size: " + codeBook_K_means.size());

            System.out.println("Reconstructing Image...");
            BufferedImage reconstructedImage = reconstructImage(grayImage, codeBook_K_means, blockSize);
            System.out.println("Image Reconstruction Completed.");

            ImageIO.write(reconstructedImage, "jpg", new File("src/reconstructed_image.jpg"));
            System.out.println("Reconstructed Image Saved.");

            double mse = calculateMeanSquareError(grayImage, reconstructedImage);
            System.out.println("Mean Square Error (MSE): " + mse);

            double compressionRatio = calculateCompressionRatio(grayImage, codeBook_K_means.size(), blockSize);
            System.out.println("Compression Ratio: " + compressionRatio);

        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static List<int[][]> divideIntoBlocks(BufferedImage image, int blockSize) {
        int width = image.getWidth();
        int height = image.getHeight();
        List<int[][]> blocks = new ArrayList<>();
        System.out.println("Dividing Image into Blocks...");

        for (int y = 0; y < height; y += blockSize) {
            for (int x = 0; x < width; x += blockSize) {
                int[][] block = new int[blockSize][blockSize];
                for (int i = 0; i < blockSize; i++) {
                    for (int j = 0; j < blockSize; j++) {
                        if (x + j < width && y + i < height) {
                            block[i][j] = new Color(image.getRGB(x + j, y + i)).getRed();
                        } else {
                            block[i][j] = 0;
                        }
                    }
                }
                blocks.add(block);
            }
        }
        System.out.println("Finished Dividing.");
        return blocks;
    }

    private static List<int[][]> generateCodebook(List<int[][]> blocks, int k) {
        List<int[][]> codebook = new ArrayList<>();
        int blockSize = blocks.get(0).length;

        System.out.println("Calculating Initial Average Block...");
        int[][] averageBlock = calculateAverageBlock(blocks);
        codebook.add(averageBlock);

        while (codebook.size() < k) {
            List<int[][]> newCodebook = new ArrayList<>();
            for (int[][] codeWord : codebook) {
                System.out.println("Splitting Code Word...");
                int[][] perturbation = createPerturbation(blockSize);
                newCodebook.add(addBlocks(codeWord, perturbation));
                newCodebook.add(subtractBlocks(codeWord, perturbation));
            }

            System.out.println("Refining Codebook...");
            codebook = refineCodebook(blocks, newCodebook);
            System.out.println("Codebook size after refinement: " + codebook.size());
        }

        return codebook;
    }

    private static BufferedImage reconstructImage(BufferedImage original, List<int[][]> codebook, int blockSize) {
        int width = original.getWidth();
        int height = original.getHeight();
        BufferedImage reconstructed = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);

        for (int y = 0; y < height; y += blockSize) {
            for (int x = 0; x < width; x += blockSize) {
                int[][] block = extractBlock(original, x, y, blockSize);
                int nearestIndex = findNearestBlockInCodebook(block, codebook);
                int[][] nearestBlock = codebook.get(nearestIndex);

                for (int i = 0; i < blockSize; i++) {
                    for (int j = 0; j < blockSize; j++) {
                        if (y + i < height && x + j < width) {
                            int grayValue = nearestBlock[i][j];
                            reconstructed.setRGB(x + j, y + i, new Color(grayValue, grayValue, grayValue).getRGB());
                        }
                    }
                }
            }
        }
        return reconstructed;
    }

    private static double calculateMeanSquareError(BufferedImage original, BufferedImage reconstructed) {
        int width = original.getWidth();
        int height = original.getHeight();
        long sumSquaredError = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int originalGray = new Color(original.getRGB(x, y)).getRed();
                int reconstructedGray = new Color(reconstructed.getRGB(x, y)).getRed();
                int error = originalGray - reconstructedGray;
                sumSquaredError += error * error;
            }
        }

        return (double) sumSquaredError / (width * height);
    }

    private static double calculateCompressionRatio(BufferedImage image, int codebookSize, int blockSize) {
        int imagePixels = image.getWidth() * image.getHeight();
        int bitsPerBlock = (int) Math.ceil(Math.log(codebookSize) / Math.log(2));
        double originalSize = imagePixels * 8;
        double compressedSize = (imagePixels / (blockSize * blockSize)) * bitsPerBlock;
        return originalSize / compressedSize;
    }

    // helper methods

    private static int[][] calculateAverageBlock(List<int[][]> blocks) {
        int size = blocks.get(0).length;
        int[][] avg = new int[size][size];
        for (int[][] block : blocks) {
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    avg[i][j] += block[i][j];
                }
            }
        }
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                avg[i][j] /= blocks.size();
            }
        }
        return avg;
    }

    private static int[][] createPerturbation(int size) {
        int[][] perturb = new int[size][size];
        for (int i = 0; i < size; i++)
            for (int j = 0; j < size; j++)
                perturb[i][j] = 1;
        return perturb;
    }

    private static int[][] addBlocks(int[][] a, int[][] b) {
        int size = a.length;
        int[][] result = new int[size][size];
        for (int i = 0; i < size; i++)
            for (int j = 0; j < size; j++)
                result[i][j] = Math.min(255, a[i][j] + b[i][j]);
        return result;
    }

    private static int[][] subtractBlocks(int[][] a, int[][] b) {
        int size = a.length;
        int[][] result = new int[size][size];
        for (int i = 0; i < size; i++)
            for (int j = 0; j < size; j++)
                result[i][j] = Math.max(0, a[i][j] - b[i][j]);
        return result;
    }

    private static List<int[][]> refineCodebook(List<int[][]> blocks, List<int[][]> codebook) {
        int blockSize = blocks.get(0).length;
        List<List<int[][]>> assignments = new ArrayList<>(Collections.nCopies(codebook.size(), null));
        for (int i = 0; i < codebook.size(); i++)
            assignments.set(i, new ArrayList<>());

        for (int[][] block : blocks) {
            int index = findNearestBlockInCodebook(block, codebook);
            assignments.get(index).add(block);
        }

        List<int[][]> newCodebook = new ArrayList<>();
        for (List<int[][]> group : assignments) {
            if (!group.isEmpty()) {
                newCodebook.add(calculateAverageBlock(group));
            }
        }

        return newCodebook;
    }

    private static int findNearestBlockInCodebook(int[][] block, List<int[][]> codebook) {
        int minDist = Integer.MAX_VALUE;
        int bestIndex = 0;
        for (int i = 0; i < codebook.size(); i++) {
            int dist = calculateBlockDistance(block, codebook.get(i));
            if (dist < minDist) {
                minDist = dist;
                bestIndex = i;
            }
        }
        return bestIndex;
    }

    private static int calculateBlockDistance(int[][] a, int[][] b) {
        int size = a.length;
        int sum = 0;
        for (int i = 0; i < size; i++)
            for (int j = 0; j < size; j++)
                sum += (a[i][j] - b[i][j]) * (a[i][j] - b[i][j]);
        return sum;
    }

    private static int[][] extractBlock(BufferedImage image, int x, int y, int blockSize) {
        int[][] block = new int[blockSize][blockSize];
        for (int i = 0; i < blockSize; i++) {
            for (int j = 0; j < blockSize; j++) {
                if (x + j < image.getWidth() && y + i < image.getHeight()) {
                    block[i][j] = new Color(image.getRGB(x + j, y + i)).getRed();
                } else {
                    block[i][j] = 0;
                }
            }
        }
        return block;
    }
}
