package com.cyber;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Mojo(name = "generate",
        defaultPhase = LifecyclePhase.GENERATE_SOURCES,
        requiresDependencyResolution = ResolutionScope.COMPILE)
public class ClassgenPluginMojo extends AbstractMojo {
    private static final String GENERATED_CLASS_SRC = """
            package #PACKAGE_NAME#;
                        
            public class #CLASS_NAME# {
                public static void main(String[] args) {
                    System.out.println("Hello from generated world!");
                }
            }           
            """;

    @Parameter(property = "project", required = true, readonly = true)
    private MavenProject project;

    @Parameter(defaultValue = "${project.build.directory}/generated-sources/classgen")
    private File outputDirectory;

    @Parameter(property = "packageName")
    private String packageName = "com.cyber";


    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        Log log = getLog();

        log.info("packageName = " + packageName);
        log.info(project.getProperties().toString());

        if (!outputDirectory.exists()) {
            outputDirectory.mkdirs();
        }

        try {
            writeFile(packageName, "Generated", GENERATED_CLASS_SRC);
        } catch (IOException ex) {
            throw new MojoExecutionException(ex);
        }

        if (project != null) {
            addSourceRoot(outputDirectory);
        }
    }

    void writeFile(String packageName, String className, String content) throws IOException {
        Path outputPackagePath = Path.of(outputDirectory.toString(), packageName.split("\\."));
        Files.createDirectories(outputPackagePath);

        String fileName = className + ".java";
        Path outputFilePath = outputPackagePath.resolve(fileName);

        content = content.replaceAll("#PACKAGE_NAME#", packageName);
        content = content.replaceAll("#CLASS_NAME#", className);
        Files.writeString(outputFilePath, content);
    }

    void addSourceRoot(File outputDir) {
        project.addCompileSourceRoot(outputDir.getPath());
    }
}
