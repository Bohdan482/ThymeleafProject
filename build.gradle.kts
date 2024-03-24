plugins {
    id("java")
    id("war")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    compileOnly("javax.servlet:javax.servlet-api:4.0.0")
    implementation("org.thymeleaf:thymeleaf:3.1.2.RELEASE")
}

tasks.test {
    useJUnitPlatform()
}