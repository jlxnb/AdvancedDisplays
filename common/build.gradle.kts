repositories {
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
}

dependencies {
    compileOnly("me.clip:placeholderapi:2.11.6")
    implementation("commons-io:commons-io:2.16.1")
    implementation(project(":api"))
}