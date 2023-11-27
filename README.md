# intellij-jenv-plugin

![Build](https://github.com/JokingAboutLife/intellij-jenv-plugin/workflows/Build/badge.svg)
[![Version](https://img.shields.io/jetbrains/plugin/v/PLUGIN_ID.svg)](https://plugins.jetbrains.com/plugin/PLUGIN_ID)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/PLUGIN_ID.svg)](https://plugins.jetbrains.com/plugin/PLUGIN_ID)

<!-- Plugin description -->

## This is a Jenv Plugin
This is IntelliJ plugin for Jenv (Website: https://www.jenv.be).

- Auto switch IDEA JDK.
- Add All Jenv JDK option, rename IDEA SDK when some names are same with Jenv JDK.
- Listening the change of project Jenv version file (`.java-version`) and change this project JDK.
- Change project JDK by status bar, when Jenv version file does not exist in this project, show message dialog to create Jenv version file in this project.

<!-- Plugin description end -->

---

Plugin based on the [IntelliJ Platform Plugin Template][template].

---

## License
This project is licensed under the [MIT license].

[template]: https://github.com/JetBrains/intellij-platform-plugin-template
[docs:plugin-description]: https://plugins.jetbrains.com/docs/intellij/plugin-user-experience.html#plugin-description-and-presentation
[Jenv]: https://www.jenv.be
[MIT license]: https://github.com/JokingAboutLife/intellij-jenv-plugin/blob/8969efeb61b4cc2aaea465fb07ccac5bbca04272/LICENSE
