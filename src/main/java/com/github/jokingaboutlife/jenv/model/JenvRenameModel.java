package com.github.jokingaboutlife.jenv.model;

import com.intellij.openapi.projectRoots.Sdk;

public class JenvRenameModel {
    private JenvJdkModel jenvJdk;
    private Sdk ideaSdk;
    private String changeName;
    private boolean belongJenv;
    private boolean selected;

    public JenvRenameModel() {
        this.selected = true;
    }

    public JenvJdkModel getJenvJdk() {
        return jenvJdk;
    }

    public void setJenvJdk(JenvJdkModel jenvJdk) {
        this.jenvJdk = jenvJdk;
    }

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

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

}
