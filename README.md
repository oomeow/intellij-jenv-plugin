<img src="src/main/resources/META-INF/pluginIcon.svg" width="80" height="80" alt="icon" align="left"/>

jEnv Helper
===

![Build](https://github.com/JokingAboutLife/intellij-jenv-plugin/workflows/Build/badge.svg)
[![Version](https://img.shields.io/jetbrains/plugin/v/PLUGIN_ID.svg)](https://plugins.jetbrains.com/plugin/PLUGIN_ID)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/PLUGIN_ID.svg)](https://plugins.jetbrains.com/plugin/PLUGIN_ID)

**This is a project I just started to learn Intellij plugin development, some code may be written with some problems.**

<!-- Plugin description -->

## This is jEnv plugin for Intellij IDEAs
> [jEnv](https://www.jenv.be) is a command line tool to help you forget how to set the JAVA_HOME environment variable.<br/>
> jEnv source code: https://github.com/jenv/jenv

- Auto switch IDEA JDK.
- Add All jEnv JDK option, rename IDEA SDK when some names are same with jEnv JDK.
- Listening the change of project jEnv version file `.java-version` and change this project JDK.
- Change project JDK by status bar, when jEnv version file does not exist in this project, show message dialog to create jEnv version file in this project.

<!-- Plugin description end -->

## License
This project is licensed under the [MIT license](https://github.com/JokingAboutLife/intellij-jenv-plugin/blob/8969efeb61b4cc2aaea465fb07ccac5bbca04272/LICENSE).
