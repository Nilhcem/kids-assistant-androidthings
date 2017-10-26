package com.nilhcem.kidsroom.data

import com.nilhcem.kidsroom.R

enum class RfidDevice(val id: String, val actionStringResId: Int) {
    TOTAKEKE("04FACDAA564980", R.string.action_nursery_rhymes),
    CRUZ("20625D7E", R.string.action_control_fan),
    FLASH("700EF673", R.string.action_learn_colors),
    SPLATOON("0475D1B2E34C80", R.string.action_lights_color),
    BLAISE("042FC6D2B24981", R.string.action_google_assistant),
    KEYRING("208D8015", R.string.action_parents_mode);

    companion object {
        fun getFromRfidId(id: String): RfidDevice? {
            return RfidDevice.values().firstOrNull { it.id == id }
        }
    }
}
