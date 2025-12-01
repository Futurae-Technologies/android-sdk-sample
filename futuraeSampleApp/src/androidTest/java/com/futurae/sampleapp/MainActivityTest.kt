package com.futurae.sampleapp

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityNavigationTest {

    @get:Rule
    val rule = createAndroidComposeRule<MainActivity>()

    @get:Rule
    val permissionRule: GrantPermissionRule =
        GrantPermissionRule.grant(android.Manifest.permission.POST_NOTIFICATIONS)

    @Test
    fun App_launches_configs_and_sends_enroll_with_expired_shortcode() {

        val lockType = rule.activity.getString(R.string.lock_mechanism)
        val submit = rule.activity.getString(R.string.submit)
        val more = rule.activity.getString(R.string.bottom_navigation_more_item)
        val settings = rule.activity.getString(R.string.settings)
        val intCheck = rule.activity.getString(R.string.integrity_check)

        waitForText(lockType)
        rule.onNodeWithText(lockType, substring = false).performClick()
        rule.onNodeWithText("NONE", substring = false).performClick()
        rule.onNodeWithText(submit, substring = false).performClick()


        waitForText(more)
        rule.onNodeWithText(more, substring = false).performClick()
        rule.onNodeWithText(settings, substring = false).performClick()
        rule.onNodeWithText(intCheck, substring = false).performClick()

    }

    private fun waitForText(text: String) {
        rule.waitUntil(
            timeoutMillis = 5_000
        ) {
            rule.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
        }
    }
}
