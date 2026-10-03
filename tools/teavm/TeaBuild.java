import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import org.teavm.diagnostics.Problem;
import org.teavm.tooling.ConsoleTeaVMToolLog;
import org.teavm.tooling.TeaVMTargetType;
import org.teavm.tooling.TeaVMTool;
import org.teavm.vm.TeaVMOptimizationLevel;

/**
 * Minimal TeaVM driver (no Maven/Gradle needed).
 * usage: TeaBuild <classesDir> <outDir> <mainClass>
 * Writes <outDir>/game.js.
 */
public final class TeaBuild {
    public static void main(String[] args) throws Exception {
        File classes = new File(args[0]);
        File out = new File(args[1]);
        out.mkdirs();
        URLClassLoader loader = new URLClassLoader(new URL[] { classes.toURI().toURL() }, TeaBuild.class.getClassLoader()) {
            @Override
            public URL getResource(String name) {
                URL u = super.getResource(name);
                if (u != null && "jrt".equals(u.getProtocol()) && System.getenv("TEABUILD_TRACE") != null) {
                    System.err.println("TeaVM read JDK class: " + name);
                }
                return u;
            }
        };
        TeaVMTool tool = new TeaVMTool();
        tool.setClassLoader(loader);
        tool.setTargetType(TeaVMTargetType.JAVASCRIPT);
        tool.setMainClass(args[2]);
        tool.setTargetDirectory(out);
        tool.setTargetFileName("game.js");
        tool.setObfuscated(true);
        tool.setOptimizationLevel(TeaVMOptimizationLevel.ADVANCED);
        tool.setLog(new ConsoleTeaVMToolLog(false));
        tool.generate();
        boolean failed = false;
        for (Problem p : tool.getProblemProvider().getSevereProblems()) {
            System.err.println("TeaVM: " + p.getText() + " " + java.util.Arrays.toString(p.getParams()) + " at " + p.getLocation());
            failed = true;
        }
        if (failed || !new File(out, "game.js").isFile()) {
            System.err.println("TeaVM build failed");
            System.exit(1);
        }
    }
}
