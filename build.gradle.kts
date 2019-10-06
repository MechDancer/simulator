import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.50"
    `build-scan`
}

buildScan {
    termsOfServiceUrl = "https://gradle.com/terms-of-service"
    termsOfServiceAgree = "yes"
//  publishAlways()
}

version = "0.0.1"

// 包括主项目的构建脚本
allprojects {
    apply(plugin = "kotlin")
    group = "org.mechdancer"
    repositories {
        mavenCentral()
        jcenter()
    }
    dependencies {
        // 自动依赖 kotlin 标准库
        implementation(kotlin("stdlib-jdk8"))
        // 线性代数
        implementation("org.mechdancer", "linearalgebra", "0.2.5-dev-3")
        // 使用示例中采用协程
        implementation("org.jetbrains.kotlinx", "kotlinx-coroutines-core", "+")
        // 支持网络工具
        testImplementation("org.mechdancer", "dependency", "0.1.0-rc-3")
        testImplementation("org.mechdancer", "remote", "0.2.1-dev-13")
        testImplementation("org.slf4j", "slf4j-api", "+")
        testImplementation(kotlin("reflect"))
        // 单元测试
        testImplementation("junit", "junit", "+")
        testImplementation(kotlin("test-junit"))
    }
    tasks.withType<KotlinCompile> {
        kotlinOptions { jvmTarget = "1.8" }
    }
    tasks.withType<JavaCompile> {
        sourceCompatibility = "1.8"
        targetCompatibility = "1.8"
    }
    // 源码导出任务
    val sourceTaskName = "sourcesJar"
    task<Jar>(sourceTaskName) {
        archiveClassifier.set("sources")
        group = "build"

        from(sourceSets["main"].allSource)
    }
    tasks["jar"].dependsOn(sourceTaskName)
}
