// Arquivo de build no nível do projeto

// Bloco de buildscript para adicionar dependências de build
buildscript {
    dependencies {
        classpath("com.google.gms:google-services:4.3.14") // Plugin do Google Services para Firebase
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
}


