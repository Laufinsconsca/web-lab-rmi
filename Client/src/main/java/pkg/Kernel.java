package pkg;

import java.io.Serializable;

public class Kernel implements Serializable {

    public static final long serialVersionUID = 5073088137981309407L;

    double[][] kernelData;

    public Kernel(double[][] kernelArray) {
        if (kernelArray.length == kernelArray[0].length && kernelArray.length % 2 == 1 && kernelArray.length > 1) {
            this.kernelData = kernelArray;
        } else {
            throw new InvalidKernelException();
        }
    }

    public double[][] getKernelData() {
        return kernelData;
    }

}
