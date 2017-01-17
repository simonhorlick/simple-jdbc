workspace(name = "com_github_simonhorlick_simple_jdbc")

git_repository(
    name = "com_github_simonhorlick_base",
    commit = "696ef833804e9aea54b4284d57c27236e367453c",
    remote = "https://github.com/simonhorlick/base.git",
)

# This needs to come before rpc repositories.
git_repository(
    name = "io_bazel_rules_go",
    remote = "https://github.com/bazelbuild/rules_go.git",
    tag = "0.2.0",
)

load("@io_bazel_rules_go//go:def.bzl", "go_repositories", "new_go_repository")

go_repositories()

load("@com_github_simonhorlick_base//:java_base_repositories.bzl", "java_base_repositories")
load("@com_github_simonhorlick_base//:java_test_repositories.bzl", "java_test_repositories")
load("@com_github_simonhorlick_base//:rpc_repositories.bzl", "rpc_repositories")

java_base_repositories()

java_test_repositories()

rpc_repositories()

android_sdk_repository(
    name = "androidsdk",
    api_level = 23,
    build_tools_version = "23.0.3",
    path = "/Users/simon/Library/Android/sdk",
)

maven_jar(
    name = "ch_qos_logback_logback_classic",
    artifact = "ch.qos.logback:logback-classic:jar:1.1.8",
    sha1 = "e54f49a227611976642f81cf2106b81c1ece8bf5",
)

maven_jar(
    name = "ch_qos_logback_logback_core",
    artifact = "ch.qos.logback:logback-core:jar:1.1.8",
    sha1 = "0b4d1c3dea91727faf8b3d19cf4b9b972d6c0b89",
)

maven_jar(
    name = "org_postgresql_postgresql",
    artifact = "org.postgresql:postgresql:jar:9.4.1212",
    sha1 = "38931d70811d9bfcecf9c06f7222973c038a12de",
)

maven_jar(
    name = "com_google_errorprone_error_prone_annotations",
    artifact = "com.google.errorprone:error_prone_annotations:jar:2.0.15",
    sha1 = "822652ed7196d119b35d2e22eb9cd4ffda11e640",
)
