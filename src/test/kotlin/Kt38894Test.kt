import kotlinx.metadata.KmFunction
import kotlinx.metadata.jvm.KotlinClassHeader
import kotlinx.metadata.jvm.KotlinClassMetadata
import kotlinx.metadata.jvm.signature
import org.junit.Test
import kotlin.test.assertEquals

class Kt38894Test {
    @Test
    fun test() {
        val klass =
            Class.forName("kotlin.coroutines.experimental.intrinsics.IntrinsicsKt__IntrinsicsJvmKt\$buildContinuationByInvokeCall\$continuation$1")
        val header = with(klass.getAnnotation(Metadata::class.java)) {
            KotlinClassHeader(kind, metadataVersion, bytecodeVersion, data1, data2, extraString, packageName, extraInt)
        }
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
}
