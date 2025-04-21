# README: Vector Quantization for Image Compression

## Overview
This project implements **Vector Quantization (VQ)** for image compression, a technique leveraged in reducing the size of images while maintaining an acceptable quality level. The process divides images into smaller building blocks (vectors) and compresses them using a fixed-size **codebook**.

The project includes two main Java classes:
1. **VectorQuantization.java**: Focuses on key vector quantization operations like vector extraction, clustering, and codebook generation.
2. **Main.java**: Provides helper functionalities like dividing images into blocks, handling reconstruction, and calculating metrics such as Mean Square Error (MSE) and Compression Ratio.

## Core Features
1. **Image Compression**:
    - Divides an image into smaller blocks of pixels.
    - Generates a fixed-size codebook to represent image blocks efficiently.
    - Encodes the image using the codebook for compression.

2. **Image Reconstruction**:
    - Reconstructs the compressed image from the encoded data and the codebook.
    - Metrics such as Mean Square Error (MSE) and Compression Ratio are calculated to evaluate performance.

3. **Customizable Parameters**:
    - Block size (e.g., 2x2 pixel blocks can be used).
    - Codebook size (defines how many unique patterns the compressed image can utilize).
    - Iterations for optimization of the codebook during vector quantization.

4. **Visualization**:
    - Displays the codebook vectors and optionally visualizes reconstructed images for clarity.

---

## Installation and Usage

### Prerequisites
- Java Development Kit (JDK) 8 or later.
- A compatible IDE (e.g., IntelliJ IDEA) or a build tool setup like Maven/Gradle.

### Steps to Run
1. **Clone the Repository**:
```shell script
git clone <repository-url>
   cd vector-quantization
```

2. **Prepare Input**:
    - Place the image(s) you want to compress in the `resources/` folder (or modify the file paths accordingly in the code).

3. **Compile and Execute**:
    - Compile the project using your IDE or directly via the terminal:
```shell script
javac -d bin src/*.java
```
- Execute the entry point class:
```shell script
java -cp bin Main
```

4. **Configuration**:
    - Update constants such as:
        - `BLOCK_SIZE` (size of blocks for vector quantization).
        - `CODEBOOK_SIZE` (number of representative patterns).
        - `MAX_ITERATIONS` for codebook clustering.

### Example Output
1. Compression ratio and visualizations of original and reconstructed images.
2. Quality metrics, including **Mean Square Error (MSE)** and **Compression Ratio**.

---

## Code Structure

### 1. **VectorQuantization.java**
This file implements the core **Vector Quantization** algorithm. Key methods include:
- **`extractVectors`**: Extracts vector blocks from an image.
- **`initializeCodebook`**: Randomly generates initial codebook vectors.
- **`assignToClusters`**: Assigns vectors to their closest codebook entries.
- **`updateCodebook`**: Refines the codebook iteratively based on cluster centroids.
- **`euclideanDistance`**: Calculates the distance between points in the vector space.
- **`visualizeCodebook`**: Saves the codebook vectors as an image for visualization.

### 2. **Main.java**
Handles image preprocessing, compression, and postprocessing:
- **Image Processing**:
    - `divideIntoBlocks`: Divides the input image into smaller pixel blocks.
    - `reconstructImage`: Builds the compressed image using the codebook.
- **Codebook Optimization**:
    - `generateCodebook`: Generates a codebook by clustering image vectors.
    - `refineCodebook`: Dynamically adjusts the codebook for better lossy compression.
- **Evaluation Metrics**:
    - `calculateMeanSquareError`: Measures the difference between the original and reconstructed images.
    - `calculateCompressionRatio`: Evaluates the amount of achieved compression.

---

## Results and Evaluation

### Metrics
1. **Compression Ratio**:
    - Measures how much the original image size has been reduced.
    - Formula:
      \[
      \text{Compression Ratio} = \frac{\text{Original Size}}{\text{Compressed Size}}
      \]

2. **Mean Square Error (MSE)**:
    - Quantifies the loss in quality from compression.
    - Formula:
      \[
      MSE = \frac{1}{N} \sum_{i=0}^{N} (I_{original}[i] - I_{reconstructed}[i])^2
      \]

### Visualization
The **codebook representation** and the **reconstructed image** are available as output to compare the effects of compression.

---

## Potential Improvements
- Support for other block sizes (e.g., 4x4, 8x8) and image dimensions.
- Dynamic adjustment of the codebook size based on image content.
- Implement alternative distance metrics (e.g., Manhattan Distance).
- Parallel processing for faster executions on large datasets.

---

## Contributing
1. Fork the repository and create a new branch.
2. Commit code with clear descriptions.
3. Submit a pull request, and adhere to the project coding and formatting conventions.

---

## License
This project is licensed under the **MIT License**. Feel free to use and modify it as per your requirements.

---

## Acknowledgements
Special thanks to the research and practical applications of **Vector Quantization** in image and data compression, which have inspired this project.