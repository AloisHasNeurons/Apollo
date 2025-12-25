package com.alois.apollo

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.alois.apollo.data.model.ExerciseConfig
import com.alois.apollo.data.model.ExerciseUnit
import com.alois.apollo.data.model.WorkoutConfig
import com.alois.apollo.ui.WorkoutViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class WorkoutViewModelTest {

    private lateinit var viewModel: WorkoutViewModel
    private val testWorkout =
        WorkoutConfig(
            id = "test_workout",
            name = "Test Workout",
            focus = "Testing",
            exercises =
                listOf(
                    ExerciseConfig(
                        id = "ex1",
                        nameEn = "Ex 1",
                        nameFr = "Ex 1 FR",
                        descriptionEn = "Desc",
                        descriptionFr = "Desc FR",
                        repRange = "5-10",
                        targetReps = 8,
                        unit = ExerciseUnit.REPS,
                        restSeconds = 60
                    ),
                    ExerciseConfig(
                        id = "ex2",
                        nameEn = "Ex 2",
                        nameFr = "Ex 2 FR",
                        descriptionEn = "Desc",
                        descriptionFr = "Desc FR",
                        repRange = "30s",
                        targetReps = 30,
                        unit = ExerciseUnit.TIME,
                        restSeconds = 60
                    )
                )
        )

    @Before
    fun setup() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        viewModel = WorkoutViewModel(app)
    }

    @Test
    fun `starting workout initializes state correctly`() {
        viewModel.startWorkout(testWorkout)

        assertEquals(testWorkout, viewModel.activeWorkout.value)
        assertEquals(0, viewModel.currentExerciseIndex.value)
        assertEquals(0, viewModel.currentSetIndex.value)
        assertFalse(viewModel.showRecap.value)
    }

    @Test
    fun `nextStep increments exercise index then set index`() {
        viewModel.startWorkout(testWorkout)

        // Exercise 1, Set 1 -> Next -> Exercise 2, Set 1
        viewModel.nextStep()
        assertEquals(1, viewModel.currentExerciseIndex.value)
        assertEquals(0, viewModel.currentSetIndex.value)

        // Exercise 2, Set 1 -> Next -> Exercise 1, Set 2
        viewModel.nextStep()
        assertEquals(0, viewModel.currentExerciseIndex.value)
        assertEquals(1, viewModel.currentSetIndex.value)
    }

    @Test
    fun `nextStep shows recap after all sets and exercises`() {
        viewModel.startWorkout(testWorkout)

        // 2 exercises * 3 sets = 6 steps to finish
        repeat(5) { viewModel.nextStep() }
        assertFalse(viewModel.showRecap.value)

        viewModel.nextStep()
        assertTrue(viewModel.showRecap.value)
    }

    @Test
    fun `updateResult modifies workout results correctly`() {
        viewModel.startWorkout(testWorkout)

        viewModel.updateResult("ex1", 0, 15)
        val results = viewModel.workoutResults.value
        assertEquals(15, results["ex1"]?.get(0))
    }

    @Test
    fun `cancelWorkout clears active workout`() {
        viewModel.startWorkout(testWorkout)
        viewModel.cancelWorkout()

        assertNull(viewModel.activeWorkout.value)
        assertFalse(viewModel.showRecap.value)
    }
}
