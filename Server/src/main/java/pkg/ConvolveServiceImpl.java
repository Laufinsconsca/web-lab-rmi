package pkg;

import java.awt.image.BufferedImage;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class ConvolveServiceImpl extends UnicastRemoteObject implements ConvolveService {
    public static final double ZERO_SCALE = 0.5;

    public ConvolveServiceImpl() throws RemoteException {
        super();
    }

    @Override
    public ByteArrayOfTheImage convolveAnImage(ByteArrayOfTheImage image, Kernel kernel) throws RemoteException {
        System.out.println("A client has connected");
        ByteArrayOfTheImage byteArrayOfTheImage =  new ByteArrayOfTheImage(applyFilter(image.getBufferedImage(), kernel.getKernelData()));
        System.out.println("The result was returned to the client");
        return byteArrayOfTheImage;
    }

    private static BufferedImage applyFilter(BufferedImage input, double[][] kernel) {
        BufferedImage output = new BufferedImage(input.getWidth(), input.getHeight(),
                BufferedImage.TYPE_INT_ARGB);
        int indent = (kernel.length - 1) / 2;
        for (int i = indent; i < input.getWidth() - indent; i++) {
            for (int j = indent; j < input.getHeight() - indent; j++) {
                applyToAPixel(input, output, kernel, i, j);
            }
        }
        return output;
    }

    private static void applyToAPixel(BufferedImage input, BufferedImage output, double[][] kernel, int x, int y) {
        if (x == 0 || x == input.getWidth() - 1) {
            throw new RuntimeException("A x-axis boundary pixel");
        }
        if (y == 0 || y == input.getHeight() - 1) {
            throw new RuntimeException("A y-axis boundary pixel");
        }
        if (x < 0 || y < 0 || x > input.getWidth() - 1 || y > input.getHeight() - 1) {
            throw new RuntimeException("Wrong coordinates");
        }
        int[][] pixels = new int[kernel.length][kernel[0].length];
        for (int i = x - 1; i <= x + 1; i++) {
            for (int j = y - 1; j <= y + 1; j++) {
                pixels[i - x + 1][j - y + 1] = input.getRGB(i, j);
            }
        }
        output.setRGB(x, y, convolve(pixels, kernel));
    }

    private static double[] unpackAPixel(int intPixel) {
        double[] doublePixel = new double[4];
        for (int i = 0; i < 4; i++) {
            doublePixel[i] = (intPixel & (255 << (8 * (3 - i)))) >> (8 * (3 - i));
            doublePixel[i] = doublePixel[i] < 0 ? doublePixel[i] + 256 : doublePixel[i];
        }
        return doublePixel;
    }

    private static int packAPixel(double[] doublePixel) {
        return -16777216 | (((int) doublePixel[1]) << 16)
                | (((int) doublePixel[2]) << 8) | ((int) doublePixel[3]);
    }

    private static double[][][] unpackAMatrixOfPixels(int[][] intPixels) {
        double[][][] doublePixels = new double[intPixels.length][intPixels[0].length][4];
        for (int i = 0; i < intPixels.length; i++) {
            for (int j = 0; j < intPixels[0].length; j++) {
                doublePixels[i][j] = unpackAPixel(intPixels[i][j]);
            }
        }
        return doublePixels;
    }

    public static double[] multiply(double[][][] pixels, double[][] kernel) {
        if ((pixels.length != pixels[0].length) || (pixels.length != kernel.length) || (pixels[0].length != kernel[0].length)) {
            throw new RuntimeException("Incorrect pixels dimensions");
        }
        double[] resultPixel = new double[4];
        double[] coefficients = new double[4];
        double[] min = new double[4];
        double[] max = new double[4];
        double div = getDiv(kernel);
        for (int k = 0; k < 4; k++) {
            coefficients[k] = 0;
            min[k] = 0;
            max[k] = 0;
        }
        for (int i = 0; i < pixels.length; i++) {
            for (int j = 0; j < pixels.length; j++) {
                for (int k = 0; k < 4; k++) {
                    coefficients[k] += pixels[i][j][k] * kernel[i][j];
                }
            }
        }
        for (int k = 0; k < 4; k++) {
            resultPixel[k] = coefficients[k] / div;
        }
        for (int k = 0; k < 4; k++) {
            if (resultPixel[k] > max[k]) {
                max[k] = resultPixel[k];
            }
            if (resultPixel[k] < min[k]) {
                min[k] = resultPixel[k];
            }
        }
        for (int k = 0; k < 4; k++) {
            if (resultPixel[k] > 255) {
                resultPixel[k] *= 255 / max[k];
            }
            if (resultPixel[k] < 0) {
                resultPixel[k] -= min[k];
                resultPixel[k] *= 255 / (255 - min[k]);
            }
        }
        return resultPixel;
    }

    private static double getDiv(double[][] kernel) {
        double result = 0;
        for (double[] doubles : kernel) {
            for (int j = 0; j < kernel[0].length; j++) {
                result += doubles[j];
            }
        }
        return result == 0 ? ZERO_SCALE : result;
    }

    private static int convolve(int[][] pixels, double[][] kernel) {
        return packAPixel(multiply(unpackAMatrixOfPixels(pixels), kernel));
    }

    public static String toBinaryString(int value) {
        StringBuilder result = new StringBuilder(32);
        for (int i = 0; i < 32; ++i) {
            result.append(value & 1);
            value >>>= 1;
        }
        return result.reverse().toString();
    }

    private static byte[] getRGBMax(BufferedImage image) {
        byte[] max = {0, 0, 0};
        int pixel;
        for (int i = 0; i < image.getWidth(); i++) {
            for (int j = 0; j < image.getHeight(); j++) {
                pixel = image.getRGB(i, j);
                for (int k = 0; k < 3; k++) {
                    byte b = (byte) ((pixel & (255 << (8 * (2 - i)))) >> (8 * (2 - i)));
                    if (Byte.compareUnsigned(b, max[k]) > 0) {
                        max[k] = b;
                    }
                }
            }
        }
        return max;
    }

    public static BufferedImage norm(BufferedImage image) {
        byte[] max = getRGBMax(image);
        int pixel;
        for (int i = 0; i < image.getWidth(); i++) {
            for (int j = 0; j < image.getHeight(); j++) {
                pixel = image.getRGB(i, j);
                image.setRGB(i, j, -16777216 | (scaleUnsignedByte((pixel & 0xff0000) >> 16, max[0] < 0 ? 255 / (double) (max[0] + 256) : 255 / (double) max[0])) << 16
                        | (scaleUnsignedByte((pixel & 0xff00) >> 8, max[1] < 0 ? 255 / (double) (max[1] + 256) : 255 / (double) max[1])) << 8
                        | scaleUnsignedByte(pixel & 0xff, max[2] < 0 ? 255 / (double) (max[2] + 256) : 255 / (double) max[2]));
            }
        }
        return image;
    }

    private static int scaleUnsignedByte(int a, double scaleFactor) {
        return (int) (a * scaleFactor);
    }
}
