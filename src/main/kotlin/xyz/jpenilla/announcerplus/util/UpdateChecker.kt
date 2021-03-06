package xyz.jpenilla.announcerplus.util

import com.google.common.base.Charsets
import com.google.gson.JsonParser
import xyz.jpenilla.announcerplus.AnnouncerPlus
import java.io.IOException
import java.io.InputStreamReader
import java.net.URL
import java.util.LinkedList

class UpdateChecker(private val plugin: AnnouncerPlus, private val githubRepo: String) {
    private val parser = JsonParser()

    fun updateCheck() {
        plugin.runAsync {
            val result = try {
                parser.parse(InputStreamReader(URL("https://api.github.com/repos/$githubRepo/releases").openStream(), Charsets.UTF_8)).asJsonArray
            } catch (exception: IOException) {
                plugin.logger.info("Cannot look for updates: " + exception.message)
                return@runAsync
            }
            val versionMap = LinkedHashMap<String, String>()
            result.forEach { versionMap[it.asJsonObject["tag_name"].asString] = it.asJsonObject["html_url"].asString }
            val versionList = LinkedList(versionMap.keys)
            val currentVersion = "v" + plugin.description.version
            if (versionList[0] == currentVersion) {
                plugin.logger.info("You are running the latest version of ${plugin.name}! :)")
                return@runAsync
            }
            if (currentVersion.contains("SNAPSHOT")) {
                plugin.logger.info("You are running a development build of ${plugin.name}! ($currentVersion)")
                plugin.logger.info("The latest official release is " + versionList[0])
                return@runAsync
            }
            val versionsBehind = versionList.indexOf(currentVersion)
            plugin.logger.info("There is an update available for ${plugin.name}!")
            plugin.logger.info("You are running version $currentVersion, which is ${if (versionsBehind == -1) "many" else versionsBehind} versions outdated.")
            plugin.logger.info("Download the latest version, ${versionList[0]} from GitHub at the link below:")
            plugin.logger.info(versionMap[versionList[0]])
        }
    }
}
