import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;
import java.nio.file.StandardOpenOption;

public class LibMinerInstrumentationHelper {

    public final static UUID id = UUID.randomUUID();

    public static void writeInvokeInfoObj(String invokeData, Object object) {
        try {
            String text = String.format(invokeData, System.identityHashCode(object), System.nanoTime(), id.toString());
            Files.write(Paths.get(Thread.currentThread().getId() + "_libminer.log"), text.getBytes(), StandardOpenOption.APPEND, StandardOpenOption.CREATE);
        } catch (IOException ignored) {
        }
    }
}
