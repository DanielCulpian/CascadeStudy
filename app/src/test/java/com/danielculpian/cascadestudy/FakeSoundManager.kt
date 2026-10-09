package com.danielculpian.cascadestudy

import com.danielculpian.cascadestudy.sound.SoundManager

// Fake SoundManager implementation for verifying sound triggers in unit tests
class FakeSoundManager : SoundManager(TestDummyContext()) {
    var startStudySoundCount = 0
        private set
    var finishStudySoundCount = 0
        private set
    var finishSessionSoundCount = 0
        private set

    override fun playStartStudySound() {
        startStudySoundCount++
    }

    override fun playFinishStudySound() {
        finishStudySoundCount++
    }

    override fun playFinishSessionSound() {
        finishSessionSoundCount++
    }

    override fun release() {
        // No-op for unit tests
    }
}
