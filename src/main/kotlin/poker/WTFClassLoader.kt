package poker
import org.jetbrains.kotlin.cli.common.arguments.K2JVMCompilerArguments
import org.jetbrains.kotlin.cli.common.messages.MessageRenderer
import org.jetbrains.kotlin.cli.common.messages.PrintingMessageCollector
import org.jetbrains.kotlin.cli.jvm.K2JVMCompiler
import org.jetbrains.kotlin.config.Services
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.PrintWriter
import java.net.URLClassLoader
import java.nio.file.Files
import java.util.concurrent.ThreadLocalRandom
import kotlin.reflect.full.createInstance


class WTFClassLoader {
    companion object {
        inline fun <reified T> createAndLoad(className: String, classString: String): T {
            val random = ThreadLocalRandom.current()
            val dir: File = Files.createTempDirectory(System.currentTimeMillis().toString() + "_" + random.nextInt()).toFile()
            try {
                val codegenOutputFile = File(dir.absolutePath + "/source.kt")
                val writer = PrintWriter(codegenOutputFile, "UTF-8")
                writer.println(classString)
                writer.close()
                val compileOutputDir = dir
                println(JvmCompile.exe(codegenOutputFile, compileOutputDir)) //should be true

                return Initializer(compileOutputDir)
                        .createInstance<T>(className)!!
//                ?.accept("Hello, world!")
            } finally {
                delete(dir)
            }
        }

        @Throws(IOException::class)
        fun delete(f: File) {
            if (f.isDirectory()) {
                for (c in f.listFiles())
                    delete(c)
            }
            if (!f.delete())
                throw FileNotFoundException("Failed to delete file: $f")
        }

        object JvmCompile {

            fun exe(input: File, output: File): Boolean = K2JVMCompiler().run {
                val args = K2JVMCompilerArguments().apply {
                    freeArgs = listOf(input.absolutePath)
                    destination = output.absolutePath
                    classpath =
                            System.getProperty("java.class.path")
                                    .split(System.getProperty("path.separator"))
                                    .filter {
                                        File(it).exists() && File(it).canRead()
                                    }.joinToString(":")
                    noStdlib = true
                    noReflect = true
                    skipRuntimeVersionCheck = true
                    reportPerf = true
                }
                output.deleteOnExit()
                execImpl(
                        PrintingMessageCollector(
                                System.out,
                                MessageRenderer.WITHOUT_PATHS, true),
                        Services.EMPTY,
                        args)
            }.code == 0

        }


        class Initializer(private val root: File) {

            val loader = URLClassLoader(
                    listOf(root.toURI().toURL()).toTypedArray(),
                    this::class.java.classLoader)

            @Suppress("UNCHECKED_CAST")
            inline fun <reified T> loadCompiledObject(clazzName: String): T? = loader.loadClass(clazzName).kotlin.objectInstance as T

            @Suppress("UNCHECKED_CAST")
            inline fun <reified T> createInstance(clazzName: String): T? = loader.loadClass(clazzName).kotlin.createInstance() as T

        }

        @JvmStatic
        fun main(args: Array<String>) {
            val lol = createAndLoad<Object>("Test", "class Test : ai.framework.client.players.PokerPlayer() { fun lol() = false }")
        }
    }
}
