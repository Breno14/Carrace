plugins {
    id("java")
    id("application")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

repositories {
    mavenCentral()
}

dependencies {
    // Importa o JAR exportado do módulo app
    implementation(files("../app/build/libs/app-export.jar"))
    // Exemplo de bibliotecas Java puras adicionais
    implementation("com.google.guava:guava:31.0.1-jre") // Biblioteca de utilitários
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<Jar> {
    manifest {
        attributes(
            "Main-Class" to "com.example.simulation.AmdahlSimulation"
        )
    }
}

tasks.register<JavaExec>("runSimulation") {
    group = "application"
    description = "Executa a simulação Amdahl"
    mainClass.set("com.example.simulation.AmdahlSimulation")
    classpath = sourceSets.main.get().runtimeClasspath
}
