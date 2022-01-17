package dev.thatsmybaby;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class VersionInfo {

    private final String buildVersion = "#build";
    private final String branchName;
    private final String commitId;
    private final boolean development;
    private final String author;

    public static VersionInfo unknown() {
        return new VersionInfo("Unknown", "Unknown", true,"Unknown");
    }

    public String buildVersion() {
        return this.buildVersion;
    }

    public String branchName() {
        return this.branchName;
    }

    public String commitId() {
        return this.commitId;
    }

    public boolean development() {
        return this.development;
    }

    public String author() {
        return this.author;
    }
}