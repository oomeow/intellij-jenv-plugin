package com.github.oomeow.jenv.model;

import com.intellij.openapi.projectRoots.Sdk;

public class JenvRenameModel {
    private Sdk ideaSdk;
    private String changeName;
    private boolean belongJenv;

    public Sdk getIdeaSdk() {
        return ideaSdk;
    }

    public void setIdeaSdk(Sdk ideaSdk) {
        this.ideaSdk = ideaSdk;
    }

    public String getChangeName() {
        return changeName;
    }

    public void setChangeName(String changeName) {
        this.changeName = changeName;
    }

    public boolean isBelongJenv() {
        return belongJenv;
    }

    public void setBelongJenv(boolean belongJenv) {
        this.belongJenv = belongJenv;
    }

}
