package com.example.jenv.constant;

public enum JenvJdkExistsType {
    Jenv,
    Idea,
    Both,
    OnlyMajorVersionMatch,
    OnlyHomePathMatch,
    OnlyNameNotMatch,
    /*
     * [遍历 ideaJdks]
     *
     * version 存在(修改.java-version)
     *      - path 存在  ==> ExistsInBoth
     *      - path 不存在  ==> VersionInIdea --- 提示路径不对
     *
     * version 不存在 --- 提示不修改 .java-version，使用idea的jdk
     *      - path 存在  ==>
     *      - path 不存在
     *          - 判断使用的 java 版本 getVersion
     *              - 版本存在 -- 修改 .java-version
     **/

}
