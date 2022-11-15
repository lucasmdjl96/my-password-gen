job("Build and deploy") {
    startOn {
        gitPush {
            branchFilter {
                +"refs/heads/release"
            }
        }
    }
    container("azul/zulu-openjdk:11-latest") {
        env["GPG_PRIVATE_KEY"] = Secrets("gpg_private_key")
        env["GPG_PASSPHRASE"] = Secrets("gpg_passphrase")

        shellScript {
            interpreter = "/bin/bash"
            location = "./scripts/space-deploy-script.sh"
        }
    }
}
