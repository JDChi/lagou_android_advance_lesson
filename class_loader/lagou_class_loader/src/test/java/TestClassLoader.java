import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class TestClassLoader {

    @Test
    public void testClassLoader() {
        CustomClassLoader customClassLoader = new CustomClassLoader("/Users/wecut1260/develop_workspace/java/");
        try {
            Class c = customClassLoader.loadClass("Secret");
            if (c != null) {
                Object obj = c.newInstance();
                // 通过反射调用Secret里的printSecret方法
                Method method = c.getDeclaredMethod("printSecret", null);
                method.invoke(obj, null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
