package pkg;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ConvolveService extends Remote {
    ByteArrayOfTheImage convolveAnImage(ByteArrayOfTheImage image, Kernel kernel) throws RemoteException;
}
