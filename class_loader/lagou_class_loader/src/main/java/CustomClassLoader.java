import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CustomClassLoader extends ClassLoader {
    private String filePath;

    CustomClassLoader(String filePath) {
        this.filePath = filePath;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        String newPath = filePath + name + ".class";
        byte[] classBytes = null;
        Path path = null;
        try {
            path = Paths.get(new URI(newPath));
            classBytes = Files.readAllBytes(path);
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
        }
        // 创建Class返回
        return defineClass(name, classBytes, 0, classBytes.length);
    }
}
