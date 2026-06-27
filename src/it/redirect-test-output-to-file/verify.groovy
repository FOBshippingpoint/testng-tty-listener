import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

// basedir = java.io.File 	The absolute path to the base directory of the test project.
// https://maven.apache.org/plugins/maven-invoker-plugin/examples/pre-post-build-script.html
def baseDirPath = (basedir as File).toPath()

def reportsDirectory = baseDirPath.resolve("target").resolve("surefire-reports")
assert Files.isDirectory(reportsDirectory)

def outputFiles = Files.list(reportsDirectory).withCloseable { paths ->
    paths.filter { Files.isRegularFile(it) && it.fileName.toString().endsWith('-output.txt') }
            .toList()
}

assert outputFiles

def outputs = outputFiles.collectEntries { [(it): Files.readString(it, StandardCharsets.UTF_8)] }

def outputFile = outputs.find { path, output -> output.contains('HELLO') }?.key
assert outputFile != null

String combinedOutput = outputs.values().join(System.lineSeparator())
assert combinedOutput.contains('HELLO')

def xmlReports = Files.list(reportsDirectory).withCloseable { paths ->
    paths.filter {
        Files.isRegularFile(it)
                && it.fileName.toString().startsWith('TEST-')
                && it.fileName.toString().endsWith('.xml')
    }.toList()
}

assert xmlReports

assert !combinedOutput.contains('\u001B[')

if (ttyWritable()) {
    assert !combinedOutput.contains('Test started:')
    assert !combinedOutput.contains('Test finished:')
    assert !combinedOutput.contains('[TTY] ')
} else {
    assert combinedOutput.contains('[TTY] Test started: Redirected output test')
    assert combinedOutput.contains('[TTY] Test finished: Redirected output test')
    assert combinedOutput.contains('[TTY] .')
}

static boolean ttyWritable() {
    try {
        Files.newOutputStream(Path.of('/dev/tty')).withCloseable {}
        return true
    } catch (IOException ignored) {
        return false
    }
}
