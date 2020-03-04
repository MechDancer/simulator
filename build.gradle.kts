import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.70"
}

version = "0.0.3"

apply(plugin = "kotlin")
group = "org.mechdancer"
repositories {
    mavenCentral()
    jcenter()
    maven("https://maven.aliyun.com/repository/central")
    maven("https://maven.aliyun.com/repository/google")
    maven("https://maven.aliyun.com/repository/gradle-plugin")
    maven("https://maven.aliyun.com/repository/jcenter")
}
dependencies {
    // 自动依赖 kotlin 标准库
    implementation(kotlin("stdlib-jdk8"))
    // 线性代数
    implementation("org.mechdancer", "linearalgebra", "+")
    // 使用示例中采用协程
    implementation("org.jetbrains.kotlinx", "kotlinx-coroutines-core", "1.3.3")
    // 支持网络工具
    testImplementation("org.mechdancer", "dependency", "+")
    testImplementation("org.mechdancer", "remote", "+")
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
