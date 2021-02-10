import static org.junit.Assert.assertEquals;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import kotlinx.metadata.KmClass;
import kotlinx.metadata.jvm.JvmExtensionsKt;
import kotlinx.metadata.jvm.KotlinClassHeader;
import kotlinx.metadata.jvm.KotlinClassMetadata;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;

public class Kt38894TestJava {

  @Test
  public void testSpecificInJarFile() throws Exception {
    Path path = Paths.get("src/test/resources/kotlin-stdlib.jar");
    JarFile jarFile = new JarFile(path.toFile());
    ZipEntry entry =
        jarFile.getEntry(
            "kotlin/coroutines/experimental/intrinsics/IntrinsicsKt__IntrinsicsJvmKt$createCoroutineUnchecked$$inlined$buildContinuationByInvokeCall$IntrinsicsKt__IntrinsicsJvmKt$2.class");
    ClassNode classNode = new ClassNode();
    new ClassReader(jarFile.getInputStream(entry)).accept(classNode, 0);
    AnnotationNode annotationNode = classNode.visibleAnnotations.get(0);
    Map<String, Object> values = new HashMap<>();
    for (int i = 0; i < annotationNode.values.size(); i += 2) {
      values.put(annotationNode.values.get(i).toString(), annotationNode.values.get(i + 1));
    }
    Integer k = (Integer) values.get("k");
    int[] mv = toIntArray(values.get("mv"));
    int[] bv = toIntArray(values.get("bv"));
    String[] d1 = toStringArray(values.get("d1"));
    String[] d2 = toStringArray(values.get("d2"));
    KotlinClassHeader header = new KotlinClassHeader(k, mv, bv, d1, d2, null, null, null);
    KmClass kmClass = ((KotlinClassMetadata.Class) KotlinClassMetadata.read(header)).toKmClass();
    List<String> signatures =
        kmClass.getFunctions().stream()
            .map(x -> JvmExtensionsKt.getSignature(x).asString())
            .collect(Collectors.toList());
    assertEquals(Arrays.asList("resume(Lkotlin/Unit;)V", "resumeWithException(L;)V"), signatures);
  }

  private int[] toIntArray(Object obj) {
    ArrayList<Integer> integers = (ArrayList<Integer>) obj;
    int[] value = new int[integers.size()];
    for (int i = 0; i < integers.size(); i++) {
      value[i] = integers.get(i);
    }
    return value;
  }

  private String[] toStringArray(Object obj) {
    return ((ArrayList<Integer>) obj).toArray(new String[0]);
  }
}
