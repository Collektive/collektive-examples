import org.gradle.api.provider.Provider
import org.gradle.plugin.use.PluginDependency

val Provider<PluginDependency>.id: String get() = get().pluginId
