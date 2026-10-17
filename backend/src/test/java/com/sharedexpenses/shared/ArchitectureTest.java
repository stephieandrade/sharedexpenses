package com.sharedexpenses.shared;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class ArchitectureTest {

    private static final Path SOURCES = Path.of("src/main/java/com/sharedexpenses");

    @Test
    void domainAndApplicationDoNotDependOnFrameworks() throws IOException {
        assertThat(filesWithImport("/domain/", "/application/", "import org.springframework", "import jakarta"))
                .isEmpty();
    }

    @Test
    void domainAndApplicationDoNotDependOnInfrastructure() throws IOException {
        assertThat(filesWithImport("/domain/", "/application/", ".infrastructure.")).isEmpty();
    }

    @Test
    void domainDoesNotDependOnApplication() throws IOException {
        assertThat(filesWithImport("/domain/", "/domain/", ".application.")).isEmpty();
    }

    private static List<Path> filesWithImport(String layer, String otherLayer, String... forbidden) throws IOException {
        try (Stream<Path> files = Files.walk(SOURCES)) {
            return files
                    .filter(p -> p.toString().endsWith(".java"))
                    .filter(p -> {
                        String path = p.toString().replace('\\', '/');
                        return path.contains(layer) || path.contains(otherLayer);
                    })
                    .filter(p -> importsAny(p, forbidden))
                    .toList();
        }
    }

    private static boolean importsAny(Path file, String... forbidden) {
        try {
            return Files.readAllLines(file).stream()
                    .filter(line -> line.startsWith("import "))
                    .anyMatch(line -> Stream.of(forbidden).anyMatch(line::contains));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
