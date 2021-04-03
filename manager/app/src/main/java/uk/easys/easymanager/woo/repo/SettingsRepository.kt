package uk.easys.easymanager.woo.repo

import uk.easys.easymanager.woo.data.api.SettingsAPI
import uk.easys.easymanager.woo.models.SettingOption

class SettingsRepository(baseUrl: String, consumerKey: String, consumerSecret: String) :
    WooRepository(baseUrl, consumerKey, consumerSecret) {

    private val apiService: SettingsAPI

    init {
        apiService = retrofit.create(SettingsAPI::class.java)
    }

    fun settings() = apiService.settings()

    fun option(group_id: String, option_id: String) = apiService.option(group_id, option_id)

    fun options(group_id: String) = apiService.options(group_id)

    fun updateOption(group_id: String, option_id: String, option: SettingOption) =
        apiService.update(group_id, option_id, option)
}
