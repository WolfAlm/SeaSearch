plugins {
    id ("com.github.node-gradle.node") version "3.2.1"
}

node {
    download.set(true)
    version.set("16.14.0")
}