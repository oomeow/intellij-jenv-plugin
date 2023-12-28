<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# intellij-jenv-plugin Changelog

## [Unreleased]
### Fixes
- type jenv command to change is not correctly change project JDK

## [0.1.0] - 2023-12-23

- change jEnv status bar position (before `Go to Line` status bar)
- remove duplicate jEnv JDK. (canonical path exists, delete all. Multiple same home path, delete them until only one exists)
- add `Invalid Jenv` separator in jEnv status bar popup menu, click item to ask for remove this JDK
- add completions for the `.java-version` file which under the project
- change JDK rename dialog validator
- click the jEnv JDK which only name not match, show the right jEnv name and ask for rename
- add invalid jEnv JDK banner when the project JDK belongs to jEnv has an invalid home path

### Fixes

- jEnv status bar icon no change when the project not set up JDK
- in add all jEnv JDK step, find JDK home file function some time (jenv remove and re-add) return null

## [0.0.4] - 2023-12-02

### Fixes

- JDK with the same name was added too much in the rename dialog
- click cancel action in the rename dialog, the jenv JDKs that only need to be updated will be added again.

### Other

- rename dialog skip validation of the jenv JDK
- remove all checkBox in rename dialog, as the dialog is only displayed when you click on the "Add All" action, which means that all the jenv JDKs must be added.
- add tool tip text in the rename dialog

## [0.0.3] - 2023-11-29

- Add JDK rename dialog when Add jEnv JDK and IDEA has same name JDK.
- Add create jEnv version file dialog when click action in status bar and this project isn't existing jEnv version file.

## [0.0.2]

- Listening the change of project jEnv version file (.java-version) and change the project JDK.
- Use StatusBar to change project sdk, add some options, like refresh 、 add all jEnv JDK option

## [0.0.1]

- AutoSwitch IDEA JDK when this project is open.
- Add jEnv Select Dialog in the Tool menu.

[Unreleased]: https://github.com/JokingAboutLife/intellij-jenv-plugin/compare/v0.1.0...HEAD
[0.1.0]: https://github.com/JokingAboutLife/intellij-jenv-plugin/compare/v0.0.4...v0.1.0
[0.0.4]: https://github.com/JokingAboutLife/intellij-jenv-plugin/compare/v0.0.3...v0.0.4
[0.0.3]: https://github.com/JokingAboutLife/intellij-jenv-plugin/compare/v0.0.2...v0.0.3
[0.0.2]: https://github.com/JokingAboutLife/intellij-jenv-plugin/compare/v0.0.1...v0.0.2
[0.0.1]: https://github.com/JokingAboutLife/intellij-jenv-plugin/commits/v0.0.1
