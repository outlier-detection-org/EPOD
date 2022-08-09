package RPC;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.net.Socket;

public interface Client {

    public default Object invoke(String host, int port, Method method, Object[] arguments) throws Throwable {
        try (Socket socket = new Socket(host, port)) {
            try (ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream())) {
                output.writeUTF(method.getName());
                output.writeObject(method.getParameterTypes());
                output.writeObject(arguments);
                try (ObjectInputStream input = new ObjectInputStream(socket.getInputStream())) {
                    Object result = input.readObject();
                    if (result instanceof Throwable) {
                        throw (Throwable) result;
                    }
                    return result;
                }
            }
        }
    }
}
