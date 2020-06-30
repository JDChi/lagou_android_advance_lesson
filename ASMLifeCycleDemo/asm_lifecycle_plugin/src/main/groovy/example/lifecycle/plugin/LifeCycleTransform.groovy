package example.lifecycle.plugin

import com.android.build.api.transform.*

import com.android.build.gradle.internal.pipeline.TransformManager

import groovy.io.FileType

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
        transformInputs.each { TransformInput transformInput ->
            // 遍历directoryInputs(文件夹中的class文件) directoryInputs代表着以源码方式参与项目编译的所有目录结构及其目录下的源码文件
            // 比如我们手写的类以及R.class、BuildConfig.class以及MainActivity.class等
            transformInput.directoryInputs.each { DirectoryInput directoryInput ->
                File dir = directoryInput.file
                if (dir) {
                    dir.traverse(type: FileType.FILES, nameFilter: ~/.*\.class/) { File file ->
                        System.out.println("find class: " + file.name)

                    }
                }
            }
        }
    }


}