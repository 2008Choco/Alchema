<a href="https://github.com/2008Choco/Alchema/releases/latest" alt="Latest release">
    <img src="https://img.shields.io/github/v/release/2008Choco/Alchema?include_prereleases" alt="Latest release">
</a>
<a href="https://www.spigotmc.org/resources/alchema.87078/">
    <img src="https://img.shields.io/spiget/downloads/87078?color=yellow&label=Spigot%20downloads" alt="Spigot Downloads">
</a>
<a href="http://choco.wtf/javadocs/alchema" alt="Javadocs">
    <img src="https://img.shields.io/badge/Javadocs-Regularly_updated-brightgreen" alt="Javadocs"/>
</a>
<a href="https://twitter.com/intent/follow?screen_name=2008Choco_" alt="Follow on Twitter">
    <img src="https://img.shields.io/twitter/follow/2008Choco_?style=social&logo=twitter" alt="Follow on Twitter">
</a>

# Alchema

This Minecraft (Bukkit) plugin aims to introduce in-world cauldron crafting, witchcraft and sorcery for CraftBukkit and Spigot servers. Licensed under GPLv3, releases are made on GitHub to comply with this license. You are welcome to fork this project and create a pull request or request features/report bugs through the issue tracker. Please see the Contribution Guidelines below.

For information about the plugin and how to use it, please see the plugin's [resource page on SpigotMC](https://www.spigotmc.org/resources/87078/).

![Alchema cauldron recipes](https://user-images.githubusercontent.com/10508906/103101156-48bae500-45e4-11eb-96cd-7ab3bb1d025e.gif)

# Contribution Guidelines
If you plan on contributing to Alchema, thank you! Contributions are much appreciated. Please follow the following guidelines if you plan on contributing either as a developer or a user of this plugin.

## Suggesting a Feature
You do not have to be a developer to contribute to this project. Requesting features helps me out just as much! In order to suggest a feature, please visit the [issues tab](https://github.com/2008Choco/Alchema/issues) and select the most appropriate issue template. The templates should guide you on how to make a ticket but please be as thorough as possible as to not require any additional inquiries for information not provided in the original ticket.

If you would like to suggest a recipe to be added to the default set of recipes, please create a ticket using the "New cauldron recipe" template. Include the JSON file and a reason as to why you believe this recipe should be added. Additionally, please include the name that you would like to use as it will be included in the recipe file itself for author attribution.

## Creating a Pull Request
When creating a pull request, it is expected that your changes have been tested and will compile successfully. Alchema makes use of a few different compile-time checks including Checkstyle and JUnit tests to ensure that code quality meets basic standards.

PLEASE be sure that your changes compile! This makes the review process easier and ensures that a follow-up commit need not be created to patch any compile errors. This means that tests should be enabled and passed (run `mvn test` in the command line or through your IDE) and no Checkstyle violations should be present.
