package example.lifecycle.plugin

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import example.asm.LifecycleClassVisitor
import groovy.io.FileType
import org.apache.commons.io.FileUtils
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter

// Transform 可以被看作是 Gradle 在编译项目时的一个 task，
// 在 .class 文件转换成 .dex 的流程中会执行这些 task，
// 对所有的 .class 文件（可包括第三方库的 .class）进行转换，转换的逻辑定义在 Transform 的 transform 方法中。
// 实际上平时我们在 build.gradle 中常用的功能都是通过 Transform 实现的，
// 比如混淆（proguard）、分包（multi-dex）、jar 包合并（jarMerge）。
class LifeCycleTransform extends Transform {

    // 设置自定义的Transform对应的Task名称
    @Override
    String getName() {
        return "LifeCycleTransform"
    }

    // 设置接收的文件类型
    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    // 设置检索的范围
    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.PROJECT_ONLY
    }

    // 是否支持增量编译
    @Override
    boolean isIncremental() {
        return false
    }

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        // transformInvocation可以获取两个数据流向
        // inputs:输入流，有jar和directory两种格式
        // outputProvider: 获取到输出目录，最后将修改的文件复制到输出目录

        Collection<TransformInput> transformInputs = transformInvocation.inputs
        TransformOutputProvider outputProvider = transformInvocation.outputProvider
        if (outputProvider != null) {
            outputProvider.deleteAll()
        }

        transformInput.jarInputs.each { JarInput jarInput ->
            File file = jarInput.file
            System.out.println("find jar input: " + file.name)
            def dest = outputProvider.getContentLocation(jarInput.name,
                    jarInput.contentTypes,
                    jarInput.scopes, Format.JAR)
            FileUtils.copyFile(file, dest)
        }

        transformInputs.each { TransformInput transformInput ->
            // 遍历directoryInputs(文件夹中的class文件) directoryInputs代表着以源码方式参与项目编译的所有目录结构及其目录下的源码文件
            // 比如我们手写的类以及R.class、BuildConfig.class以及MainActivity.class等
            transformInput.directoryInputs.each { DirectoryInput directoryInput ->
                File dir = directoryInput.file
                if (dir) {
                    dir.traverse(type: FileType.FILES, nameFilter: ~/.*\.class/) { File file ->
                        System.out.println("find class: " + file.name)

                        // 对class文件进行读取与解析
                        ClassReader classReader = new ClassReader(file.bytes)
                        // 对class文件的写入
                        ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_MAXS)
                        // 访问class文件相应的内容，解析到某一个结构就会通知到ClassVisitor的相应方法
                        ClassVisitor classVisitor = new LifecycleClassVisitor(classWriter)
                        // 依次调用 ClassVisitor 接口的各个方法
                        classReader.accept(classVisitor, ClassReader.EXPAND_FRAMES)
                        // toByteArray方法会将最终修改的字节码以 byte 数组形式返回
                        byte[] bytes = classWriter.toByteArray()


                        FileOutputStream outputStream = new FileOutputStream(file.path)
                        outputStream.write(bytes)
                        outputStream.close()


                    }
                }


                // 处理完输入文件后把输出传给下一个文件
                def dest = outputProvider.getContentLocation(directoryInput.name,
                        directoryInput.contentTypes, directoryInput.scopes, Format.DIRECTORY)
                FileUtils.copyDirectory(directoryInput.file, dest)
            }


        }
    }


}