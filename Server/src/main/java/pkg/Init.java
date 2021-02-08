package pkg;

import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Init {

    public static void main(String[] args)
    {
        System.out.println("Server has started");
        try {
            System.out.println("Waiting for users to connect");
            System.setProperty("java.rmi.server.hostname", "127.0.0.1");
            ConvolveService service = new ConvolveServiceImpl();
            Registry registry = LocateRegistry.createRegistry(1099);
            registry.bind("Convolve", service);
        }
        catch (RemoteException e){
            System.out.println("Remote exception: " + e.getMessage());
        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }

    }
}