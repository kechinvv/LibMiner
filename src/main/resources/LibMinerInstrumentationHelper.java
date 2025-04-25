import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class LibMinerInstrumentationHelper {
    public static void writeInvokeInfoObj(String invokeData, Object object) {
        try {
            var text = String.format(invokeData, System.identityHashCode(object), System.nanoTime());
            Files.write(Paths.get(Thread.currentThread().getId() + "_libminer.log"), text.getBytes(), StandardOpenOption.APPEND, StandardOpenOption.CREATE);
        } catch (IOException ignored) {
        }
    }

    public static void writeInvokeInfoObj(String invokeData) {
        try {
            var text = String.format(invokeData, 0, System.nanoTime());
            Files.write(Paths.get(Thread.currentThread().getId() + "_libminer.log"), text.getBytes(), StandardOpenOption.APPEND, StandardOpenOption.CREATE);
        } catch (IOException ignored) {
        }
    }
}
