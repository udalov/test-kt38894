import kotlinx.metadata.KmFunction
import kotlinx.metadata.jvm.KotlinClassHeader
import kotlinx.metadata.jvm.KotlinClassMetadata
import kotlinx.metadata.jvm.signature
import org.junit.Test
import org.objectweb.asm.ClassReader
import org.objectweb.asm.tree.ClassNode
import kotlin.test.assertEquals

class Kt38894Test {
    private fun loadAndCheck(header: KotlinClassHeader) {
        val metadata = KotlinClassMetadata.read(header) as KotlinClassMetadata.Class
        val kmClass = metadata.toKmClass()

        val functionSignatures = kmClass.functions.map(KmFunction::signature).map { it?.toString() }
        assertEquals(
            listOf(
                "resume(Lkotlin/Unit;)V",
                "resumeWithException(Ljava/lang/Throwable;)V"
            ),
            functionSignatures
        )
    }

    @Test
    fun testReflection() {
        val klass = Class.forName(
            "kotlin.coroutines.experimental.intrinsics.IntrinsicsKt__IntrinsicsJvmKt\$buildContinuationByInvokeCall\$continuation$1"
        )
        val header = with(klass.getAnnotation(Metadata::class.java)) {
            KotlinClassHeader(kind, metadataVersion, bytecodeVersion, data1, data2, extraString, packageName, extraInt)
        }
        loadAndCheck(header)
    }

    @Test
    @Suppress("UNCHECKED_CAST")
    fun testFile() {
        val bytes = this::class.java.getResourceAsStream(
            "kotlin/coroutines/experimental/intrinsics/IntrinsicsKt__IntrinsicsJvmKt\$buildContinuationByInvokeCall\$continuation$1.class"
        ).readBytes()
        val classNode = ClassNode().also { ClassReader(bytes).accept(it, 0) }
        val metadataAnnotation = classNode.visibleAnnotations.single()
        val values: Map<String, Any> = metadataAnnotation.values.chunked(2).map { (x, y) -> x.toString() to y }.toMap()
        val header = KotlinClassHeader(
            values["k"] as? Int,
            (values["mv"] as? List<Int>)?.toTypedArray()?.toIntArray(),
            null,
            (values["d1"] as? List<String>)?.toTypedArray(),
            (values["d2"] as? List<String>)?.toTypedArray(),
            null, null, null
        )
        loadAndCheck(header)
    }
}
