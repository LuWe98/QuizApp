package com.example.quizapp.view.fragments.settingsscreen

import com.example.quizapp.R
import com.example.quizapp.model.mongodb.documents.user.Role

object SettingsModel {
    const val HEADER_PREFERENCE_ID = 0
    const val HEADER_USER_ID = 1
    const val HEADER_STATISTICS_ID = 2
    const val HEADER_ADMIN_FUNCTIONALITY_ID = 3

    const val ITEM_DARK_MODE_ID = 4
    const val ITEM_LANGUAGE_ID = 5
    const val ITEM_USER_NAME_ID = 6
    const val ITEM_USER_ROLE_ID = 7
    const val ITEM_USER_PASSWORD_ID = 8
    const val ITEM_USER_LOGOUT_ID = 9
    const val ITEM_COMPLETED_QUESTIONNAIRES_ID = 10
    const val ITEM_GIVEN_ANSWERS_ID = 11
    const val ITEM_CREATED_QUESTIONNAIRES_ID = 12
    const val ITEM_ADMIN_PAGE_ID = 13


    private val preferencesList = mutableListOf(
        SettingsMenuItem.HeaderItem(
            HEADER_PREFERENCE_ID,
            R.string.preference
        ),
        SettingsMenuItem.DropDownItem(
            ITEM_DARK_MODE_ID,
            R.drawable.ic_dark_mode,
            R.string.darkMode
        ),
        SettingsMenuItem.DropDownItem(
            ITEM_LANGUAGE_ID,
            R.drawable.ic_language,
            R.string.language
        ),
    )

    private val userList = mutableListOf(
        SettingsMenuItem.HeaderItem(
            HEADER_USER_ID,
            R.string.user
        ),
        SettingsMenuItem.TextItem(
            ITEM_USER_NAME_ID,
            R.drawable.ic_person,
            R.string.userName
        ),
        SettingsMenuItem.TextItem(
            ITEM_USER_ROLE_ID,
            R.drawable.ic_role_badge,
            R.string.role
        ),
        SettingsMenuItem.ClickableItem(
            ITEM_USER_PASSWORD_ID,
            R.drawable.ic_password,
            R.string.changePassword
        ),
        SettingsMenuItem.ClickableItem(
            ITEM_USER_LOGOUT_ID,
            R.drawable.ic_logout,
            R.string.logout
        )
    )

    private val statisticList = mutableListOf(
        SettingsMenuItem.HeaderItem(
            HEADER_STATISTICS_ID,
            R.string.statistics
        ),
        SettingsMenuItem.TextItem(
            ITEM_COMPLETED_QUESTIONNAIRES_ID,
            R.drawable.ic_questions,
            R.string.completedQuestionnaires
        ),
        SettingsMenuItem.TextItem(
            ITEM_GIVEN_ANSWERS_ID,
            R.drawable.ic_question,
            R.string.givenQuestionsAmount
        ),
        SettingsMenuItem.TextItem(
            ITEM_CREATED_QUESTIONNAIRES_ID,
            R.drawable.ic_edit_list,
            R.string.createdQuestionnaires
        )
    )

    private val adminList = mutableListOf(
        SettingsMenuItem.HeaderItem(
            HEADER_ADMIN_FUNCTIONALITY_ID,
            R.string.adminFunctionality
        ),
        SettingsMenuItem.ClickableItem(
            ITEM_ADMIN_PAGE_ID,
            R.drawable.ic_admin_panel,
            R.string.adminSettings
        )
    )


    private val settingsListMutableLiveData =mutableListOf<SettingsMenuItem>().apply {
        addAll(preferencesList)
        addAll(userList)
    }

    private val settingsAdminListMutableLiveData = mutableListOf<SettingsMenuItem>().apply {
        addAll(settingsListMutableLiveData)
        addAll(adminList)
    }

    fun getSettingsItemList(userRole: Role) = if(userRole == Role.ADMIN) settingsAdminListMutableLiveData else settingsListMutableLiveData
}